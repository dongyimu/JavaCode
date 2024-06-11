package com.easyjava.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class JsonUtils {
    public static String toJson(Object obj) {
        if (null == obj){
            return null;
        }
        return JSON.toJSONString(obj,SerializerFeature.DisableCircularReferenceDetect);
    }
}
