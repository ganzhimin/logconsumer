package com.zju.logservice.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.sf.json.JSONObject;
import oi.thekraken.grok.api.Match;
import oi.thekraken.grok.api.exception.GrokException;

import org.apache.log4j.Logger;

/**
 * 
 * @author xuanyongchen
 *
 */
public class LogParserUtil {
	private static final Logger logger = Logger.getLogger(LogParserUtil.class);
	
	private static String PATTERN_LOCATIION_CONFIG="pattern_config.properties";
	private String patterns_path;
	
	
	
	
	
	public LogParserUtil() {
		InputStream in = this.getClass().getClassLoader()
				.getResourceAsStream(PATTERN_LOCATIION_CONFIG);
		Properties props = new Properties();
		try {
			props.load(in);
			patterns_path = props.getProperty("pattern_location");	
		} catch (IOException e) {
			logger.error("init pattern path fail");
		}
	}

	/**
	 * router-acess-log patternName:NGINX
	 * app-log exception patterName:EXCEPTIONITEM
	 * app-log error patterName:ERROR
	 * 
	 * @param str
	 * @param patternName
	 * @return
	 * @throws GrokException
	 */
	public String grokParse(String str,String patternName) throws GrokException {
		MyGrok grok = new MyGrok();
		grok.addPatternFromFile(patterns_path+"grok-patterns");
		grok.addPatternFromFile(patterns_path+"local-patterns");
		grok.namedOnlyCompile("%{"+patternName+"}");
		Match match = grok.match(str);
		match.captures();
		return match.toJson();

	}
	
	public Match grokParseToMap(String str,String patternName) throws GrokException {
		MyGrok grok = new MyGrok();
		grok.addPatternFromFile(patterns_path+"grok-patterns");
		grok.addPatternFromFile(patterns_path+"local-patterns");
		grok.namedOnlyCompile("%{"+patternName+"}");
		Match match = grok.match(str);
		match.captures();
		return match;

	}
	
	public Match grokParseToMapForApp(String str,String patternName,String appGuid) throws GrokException {
		MyGrok grok = new MyGrok();
		grok.addPatternFromFile(patterns_path+"grok-patterns");
		grok.addPatternFromFile(patterns_path+"local-patterns");
		grok.addPatternFromFile(patterns_path+appGuid);
		grok.namedOnlyCompile("%{"+patternName+"}");
		Match match = grok.match(str);
		match.captures();
		return match;

	}
	
	public void addPattern(String pattern,String appGuid){
		try {
			File file=new File(patterns_path+appGuid);
			FileOutputStream fout=new FileOutputStream(file,true);
			MyGrok grok = new MyGrok();
			grok.addPatternFromFile(patterns_path+"grok-patterns");
			grok.addPatternFromFile(patterns_path+"local-patterns");
			if(file.exists()){
				grok.addPatternFromReader(new FileReader(file));
			}
			String patternName=pattern.substring(0,pattern.indexOf(" "));
			if(!grok.getPatterns().containsKey(patternName)){
				fout.write(("\r"+pattern).getBytes());
				fout.close();
				logger.info("add pattern success!");
			}else{
				logger.info("patternName:"+patternName+" already exists !");
			}
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (GrokException e) {
			logger.error(e);
		}
	}
	
	public void removeAppPatterns(String appId){
		File file=new File(patterns_path+appId);
		file.delete();
	}
	
	public static void main(String []args) throws IOException, GrokException{
		LogParserUtil lp = new LogParserUtil();	
		String msg="<14>1 2014-07-30T10:21:29+00:00 loggregator 1e3f3e80-1d5e-4c3e-a4b7-39207623182d [RTR] - - testlog.local.lai - [30/07/2014:10:21:29 +0000] \"GET /testlog HTTP/1.1\" 200 10 \"-\" \"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:30.0) Gecko/20100101 Firefox/30.0\" 10.10.101.158:47101 vcap_request_id:9ad1ab7415c12a5cff78556acbfeb65e response_time:0.011510679 app_id:1e3f3e80-1d5e-4c3e-a4b7-39207623182d";
		JSONObject jsonLog = JSONObject.fromObject(lp.grokParse(msg, "NGINX"));
		System.out.print(jsonLog.toString());
		/*JSONObject routerLog=JSONObject.fromObject(lp.grokParse("error","ERROR"));
		System.out.print(routerLog);*/
				/*File f = new File("/tmp/.ehcache-diskstore.lock");
		BufferedReader reader=new BufferedReader(new FileReader(f));
		String tmp;
		while((tmp = reader.readLine())!=null){
			System.out.println(tmp);
		}
		
		reader.close();	*/
	/*	MatchWithCount match=(MatchWithCount)lp.grokParseToMap("at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:103)", "ERROR");
		
		System.out.print(match.toJson());*/
		/*JSONObject routerLog=JSONObject.fromObject(lp.grokParse("at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:103)", "ERROR"));
		System.out.print(routerLog);*/
//		Pattern pattern = Pattern.compile("^([\u4e00-\u9fa5_a-zA-Z0-9_]+)\\s+(.*)$");
		/*Pattern pattern = Pattern.compile("ERROR|[eE]rror");
        Matcher m = pattern.matcher("at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:103)");
        int i = 0;  
        while (m.find()) {
        	 	i++;
                String s0 = m.group();
                System.out.print(s0);
         }*/
		 //测试数据  
       /*String str = "fsdfeofkldfleierueo";  
                //e表示需要匹配的数据，使用Pattern建立匹配模式  
        Pattern p = Pattern.compile(".*eo.*");  
                //使用Matcher进行各种查找替换操作  
        Matcher m = p.matcher(str);  
        int i = 0;  
        while(m.find()){  
            i++;  
        }  
       
        System.out.println(i); */
          
	}
	
	
	
}
