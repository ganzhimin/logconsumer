package com.zju.logservice.writer;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sf.json.JSONObject;
import oi.thekraken.grok.api.exception.GrokException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import com.zju.logservice.parser.LogParserUtil;
import com.zju.logservice.parser.MatchWithCount;
import com.zju.logservice.util.AppUtil;
import com.zju.logservice.util.CacheUtil;
import com.zju.logservice.util.DateUtils;
import com.zju.logservice.util.ElasticsearchClient;
import com.zju.logservice.util.HttpTookitEnhance;

public class LogAnalyser {

	private static final Logger logger = Logger.getLogger(LogAnalyser.class);

	public static final String ZONE1_DATE_FORMAT = "dd/MM/yyyy:HH:mm:ss +0000";
	public static final String ZONE1_CH_DATE_FORMAT = "dd/MM/yyyy:HH:mm:ss +0800";
	public static final String ZONE2_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss+00:00";
	public static final String ZONE2_CH_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss+08:00";
	private static final String PATTERN_LOCATION="pattern_config.properties";

	private int timeLength = 60 * 60;

	// 计时器
	private Countdown counter;

	// log位置
	/*private ConcurrentMap<String, Integer> fileLineMap = new ConcurrentHashMap<String, Integer>();*/
	
	//private Map<String, Integer> fileLineMap = new HashMap<String, Integer>();

	// log文件名
	private HashMap<String, String> fileNameMap = new HashMap<String, String>();

	private Map<String, String> appNameMaping = new HashMap<String, String>();

	private AppUtil appUtil = new AppUtil();

	private LogParserUtil lp = new LogParserUtil();

	private Date currentDate;

	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH");
	
	private CacheUtil patternCache;
	
	private String patternUrl;

	public LogAnalyser(CacheUtil patternCache) {
		this.patternCache=patternCache;
		init();
	}

	public void init() {
		InputStream pattern_in = this.getClass().getClassLoader()
				.getResourceAsStream(PATTERN_LOCATION);
		Properties pattern_props = new Properties();
		try {
			pattern_props.load(pattern_in);
			patternUrl = pattern_props.getProperty("pattern_url", 
					"http://10.10.102.101:1234/cfWeb");
			logger.info("Read configuration file successfully!");
		} catch (IOException e) {
			logger.error("load consumer.properties file fail!");
		}
	}

	public void processMsg(String msg,int line){
		if (counter == null) {
			counter = new Countdown(timeLength);
		}
		JSONObject jsonLog;
		try {
			jsonLog = JSONObject
					.fromObject(lp.grokParse(msg, "MSG"));
			if (!jsonLog.isEmpty() && jsonLog.getString("Source").equals("GP")) {
				persistPatterns(jsonLog.getString("appid"));
			} else if (!jsonLog.isEmpty()
					&& jsonLog.getString("Source").equals("RP")) {
				removePatterns(jsonLog.getString("appid"));
			} else {
				analyseLog(msg,line);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	private Object persistPatterns(String appid) throws Exception {
		String queryString = "appId=" + appid;
		String patterns = HttpTookitEnhance.doGet(
				patternUrl+"/rest/app/patterns",
				queryString, true);
		if (patterns.equals("") || patterns == null) {
			logger.info(appid + " patters is empty");
			return null;
		}
		JSONArray ja = new JSONArray(patterns);
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < ja.length(); i++) {
			org.codehaus.jettison.json.JSONObject json = ja.getJSONObject(i);
			String pattern = json.get("pattern").toString();
			String patternName = json.get("patternName").toString();
			if(!StringUtils.isEmpty(pattern)){
				String s = patternName + " " + pattern;
				lp.addPattern(s,appid);
			}
			logger.info(patternName+" stored");
			list.add(patternName);
			
		}
		patternCache.cacheData(appid, list);
		return list;
	}

	private void removePatterns(String appid) throws JSONException {
		patternCache.removeData(appid);
		lp.removeAppPatterns(appid);	
	}

	public void analyseLog(String log,int line) {
		logger.info(this);
		JSONObject jsonLog = null;

		try {
			jsonLog = JSONObject.fromObject(lp.grokParse(log, "NGINX"));
			System.out.println(jsonLog);
		} catch (GrokException e) {
			logger.error("grok parse log error",e);
		}

		String source = jsonLog.getString("Source");
		String appid = jsonLog.getString("appid");
		String msg = jsonLog.getString("message");
		
		if(StringUtils.isEmpty(source) | StringUtils.isEmpty(appid) | source.equals(" ") | appid.equals(" ")
				| StringUtils.isEmpty(msg) | msg.equals(" ")){
			//no source or no appid or no message
			return ;
		}

		String appInstanceName = null;
		if (appNameMaping.containsKey(appid)) {
			appInstanceName = appNameMaping.get(appid);
			logger.info(appInstanceName+"from map"+appid); 
		} else {
			try {
				appInstanceName = appUtil.getAppNameById(appid);
				logger.info(appInstanceName+"from database"+appid); 
			} catch (Exception e) {
				logger.info("get app name from database error",e);
				return;
			}
			appNameMaping.put(appid, appInstanceName);
		}
		//time in msg isn't same as the system.currentTime();
		currentDate = new Date(counter.getStartPoint());
		
		if (counter.count()) {
			if (!fileNameMap.isEmpty()) {
				fileNameMap.clear();
			}
			counter.resumeStartPoint();
		}
		
		if (!fileNameMap.containsKey(source)) {
			fileNameMap.put(source,
					buildFileName(appInstanceName, source, currentDate));
		}

		/* send filecontent to elasticsearch */
		if (source.indexOf("App") > -1) {
			HashMap<String, Object> filecontent = new HashMap<String, Object>();
			filecontent.put("fileName", fileNameMap.get(source));
			filecontent.put("message", msg);
			filecontent.put("app", appInstanceName);
			filecontent.put("line", line);
			filecontent.put("timestamp", DateUtils.format(new Date()));
			ElasticsearchClient.getInstance().uploadLogFile(filecontent,appInstanceName);
		}

		/* send appdata or routerdata to bamos and elasticsearch */
		if (source.indexOf("App") > -1) {
			LogParserUtil lp = new LogParserUtil();
			//List<String> list = appPatterns.get(appid);
			logger.info("get patterns from cache --------------"+appid);
			Object object=patternCache.getData(appid);
			if(object==null){
				try {
				object=persistPatterns(appid);
				} catch (Exception e) {
				logger.error("get patterns error");
				e.printStackTrace();
				}
			}
			List<String> list = object==null?null:(List<String>)object ;
			object=null;
			Map<String, Object> map = null;
			MatchWithCount match=null;
			if (list != null) {
				for (String patternName : list) {
					if(StringUtils.isEmpty(patternName) | patternName.equals(" ")){
						logger.info("not correct patternName in list");
						continue;
					}

					try {
						match = (MatchWithCount) lp.grokParseToMapForApp(msg, patternName+":source",appid);
						map=match.toMap();
						if (map.isEmpty()) {
							logger.info("empty map");
							continue;
						}
						logger.info(patternName+" mathed");
						map.remove("source");
						map.put("eventType", patternName);
						map.put("serviceName", appInstanceName);
						map.put("unread", 1);
						if(jsonLog.getString("time").endsWith("+00:00")){
							map.put("timestamp",DateUtils.format(DateUtils.parse(jsonLog.getString("time"), ZONE2_DATE_FORMAT)));
						}else{
							map.put("timestamp",DateUtils.format(DateUtils.parse(jsonLog.getString("time"), ZONE2_CH_DATE_FORMAT)));
						}
						map.put("fileName", fileNameMap.get(source));
						map.put("line", line);
						for(int i=0;i<match.getCount();i++){
							ElasticsearchClient.getInstance().uploadParsedLog(map,appInstanceName);
						}
					} catch (GrokException e) {
						logger.error("grok exception", e);
					} catch (ParseException e) {
						logger.error("app event time  parse error");
					}
				}
			}
		} else if (source.equals("RTR")) {
			JSONObject routerLog;
			try {
				routerLog = JSONObject.fromObject(lp.grokParse(msg,
						"NGINXACCESSV2"));
				System.out.println(routerLog);
			} catch (GrokException e) {
				logger.error(msg + "parser wrong");
				return;
			}
			
			if(filterRequest(routerLog.getString("request"))){
				return;
			}
			
			//HashMap<String, Object> routerdata = new HashMap<String, Object>();
			HashMap<String, Object> esRouterData = new HashMap<String, Object>();
			//routerdata.put("serviceName", routerLog.getString("clientip")
			//		+ routerLog.getString("request"));
			esRouterData.put("serviceName", appInstanceName);
			if(routerLog.getString("time").endsWith("+0000")){
				try {
					/*routerdata.put(
							"timestamp",
							DateUtils.parse(routerLog.getString("time"),
									ZONE1_DATE_FORMAT).getTime());*/
					esRouterData.put("timestamp", DateUtils.format(DateUtils.parse(
							routerLog.getString("time"), ZONE1_DATE_FORMAT)));
					// esRouterData.put("timestamp",routerLog.getString("time"));
				} catch (ParseException e) {
					logger.error("parse route time error");
				}
			}else{
				try {
					/*routerdata.put(
							"timestamp",
							DateUtils.parse(routerLog.getString("time"),
									ZONE1_CH_DATE_FORMAT).getTime());*/
					esRouterData.put("timestamp", DateUtils.format(DateUtils.parse(
							routerLog.getString("time"), ZONE1_CH_DATE_FORMAT)));
				} catch (ParseException e1) {
					logger.error("parse route time error");
				}
			}
			
			//routerdata.put("responseTime",
			//		routerLog.getString("request_duration"));
			esRouterData.put("responseTime",
					routerLog.getDouble("request_duration")*1000);

			esRouterData.put("eventType", "access");
			logger.info(line);
			ElasticsearchClient.getInstance().uploadParsedLog(esRouterData,appInstanceName);
		}
	}
	
	private boolean filterRequest(String request) {
		if(request==null||request.isEmpty()){
			return true;
		}
		if(request.endsWith(".css")){
			return true;
		}
		if(request.endsWith(".js")){
			return true;
		}
		if(request.endsWith(".jpg")){
			return true;
		}
		if(request.endsWith(".png")){
			return true;
		}
		if(request.endsWith(".gif")){
			return true;
		}
		if(request.endsWith(".jpeg")){
			return true;
		}
		if(request.startsWith("/fonts")){
			return true;
		}
		if(request.endsWith(".ico")){
			return true;
		}
		return false;
	}

	private String buildFileName(String appInstanceName, String source,
			Date time) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(appInstanceName);
		stringBuffer.append("-");
		//num of instances can't lg than 9.
		stringBuffer.append(source.substring(source.indexOf("/") + 1));
		stringBuffer.append("-");
		stringBuffer.append((df.format(time)));
		return stringBuffer.toString();
	}

}
