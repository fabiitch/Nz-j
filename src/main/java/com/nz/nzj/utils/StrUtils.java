package com.nz.nzj.utils;

import java.text.Normalizer;

public class StrUtils {

    public static String normalizeAlphaNumeric(String str) {
        str = replaceAccent(str);
        str = replaceAllNonAlphanumeric(str);
        return str;
    }

    public static String replaceAccent(String str) {
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", ""); // Supprime les marques diacritiques
    }

    public static String replaceAllNonAlphanumeric(String str) {
        return str.replaceAll("[^A-Za-z0-9]", "");
    }

    public static String replaceAllNonAlphanumericExceptSpace(String str) {
        return str.replaceAll("[^A-Za-z0-9 ]", ""); //space after 9 !
    }

    public static String removeBegin(String str, String begin) {
        return str.substring(0, begin.length() - 1);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}
