package com.nz.nzj.utils;

import java.util.regex.Matcher;

public class StrFormat {

    public static String format(String msg, Object... params) {
        for (Object param : params) {
            String safeParam = Matcher.quoteReplacement(param != null ? param.toString() : "null");
            msg = msg.replaceFirst("\\{}", safeParam);
        }
        return msg;
    }
}
