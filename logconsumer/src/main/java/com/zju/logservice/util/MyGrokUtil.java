package com.zju.logservice.util;

import com.google.code.regexp.Pattern;

/**
 * {@code GrokUtils} contain set of useful tools or methods.
 *
 * @author anthonycorbacho
 * @since 0.0.6
 */
public class MyGrokUtil {

  /**
   * Extract Grok patter like %{FOO} to FOO, Also Grok pattern with semantic.
   */
  public static final Pattern GROK_PATTERN = Pattern.compile(
      "%\\{" +
      "(?<name>" +
        "(?<pattern>[\u4e00-\u9fa5_A-z0-9]+)" +
          "(?::(?<subname>[\u4e00-\u9fa5_A-z0-9_:]+))?" +
          ")" +
          "(?:=(?<definition>" +
            "(?:" +
            "(?:[^{}]+|\\.+)+" +
            ")+" +
            ")" +
      ")?" +
      "\\}");

}
