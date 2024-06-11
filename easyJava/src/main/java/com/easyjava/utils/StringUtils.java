package com.easyjava.utils;

public class StringUtils {
    public static String upperFirstLetter(String field) {
        if(org.apache.commons.lang3.StringUtils.isEmpty(field)){
            return field;
        }
        return field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    public static String lowerFirstLetter(String field) {
        if(org.apache.commons.lang3.StringUtils.isEmpty(field)){
            return field;
        }
        return field.substring(0, 1).toLowerCase() + field.substring(1);
    }

    public static void main(String[] args) {
        System.out.println(lowerFirstLetter("Company"));
    }
}
