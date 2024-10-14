package com.chen.common.utils.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author chenyunzhi
 * @date 2022/3/22
 * @description fastjson工具类
 */
public class FastJsonUtils {
    private static SerializeConfig config;
    private static SerializerFeature[] features = {
        //输出空值字段
        SerializerFeature.WriteMapNullValue,
        //如果数组结果为null,则输出为[],而不是null
        SerializerFeature.WriteNullListAsEmpty,
        //数值字段为null,则输出为0,而不是null
        SerializerFeature.WriteNullNumberAsZero,
        //Boolean字段为null,则输出为false,而不是null
        SerializerFeature.WriteNullBooleanAsFalse,
        //字符类型如果为null,则输出为" ",而不是null
        SerializerFeature.WriteNullStringAsEmpty
    };

    static {
        config = new SerializeConfig();
        // 日期格式化
        config.put(java.util.Date.class, new SimpleDateFormatSerializer(JSONObject.DEFFAULT_DATE_FORMAT));
        config.put(java.sql.Date.class, new SimpleDateFormatSerializer(JSONObject.DEFFAULT_DATE_FORMAT));
    }

    /**
     * 将一个对象装换为Json字符串
     */
    public static String toJson(Object object) {
        return JSONObject.toJSONString(object, config, features);
    }

    /**
     * 将Json字符串转换为Object类型的
     */
    public static Object toObject(String str) {
        return JSON.parse(str);
    }

    /**
     * 将Object类型的转换为List
     */
    public static List toList(Object obj) {
        List<Object> list = new ArrayList<Object>();
        if (obj instanceof ArrayList<?>) {
            for (Object o : (List<?>) obj) {
                list.add(o);
            }
            return list;
        }
        return null;
    }

    /**
     * 将Json字符串转换为JsonObject类型的
     */
    public static JSONObject toJsonObject(String str) {
        return JSON.parseObject(str);
    }

    /**
     * 将Json字符串转换为实例
     */
    public static <T> T toObject(String str, Class<T> t) {
        return JSON.parseObject(str, t);
    }

    /**
     * 将Json转换为指定类型的List
     */
    public static <T> List<T> toList(String str, Class<T> t) {
        return JSON.parseArray(str, t);
    }

    /**
     * 将Json转换为Map
     */
    public static <T> Map<String, T> toMap(String str) {
        return (Map<String, T>) JSONObject.parseObject(str);
    }

    /**
     * 转成json原生方法
     * 忽略配置
     *
     * @param object
     * @return
     */
    public static String toJSONString(Object object) {
        return JSONObject.toJSONString(object);
    }


}

