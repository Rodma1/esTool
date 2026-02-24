package com.chen.common.utils;


import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.*;

/**
 * @author chenyunzhi
 * @date 2021/8/20
 * @description bean操作工具类
 */
public class BeanUtils extends org.springframework.beans.BeanUtils {

    public static <T> T copyObject(Object source, T target) {
        try {
            copyProperties(source, target);
        } catch (Throwable ex) {
            throw new RuntimeException("Fail to copy properties from source to target", ex);
        }
        return target;
    }

    public static <T> T copyObject(Object source, Class<T> clazz) {
        T result;
        try {
            result = clazz.newInstance();
            copyProperties(source, result);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Fail to create instance of type" + clazz.getCanonicalName(), e);
        } catch (Throwable ex) {
            throw new RuntimeException("Fail to copy properties from source to target", ex);
        }
        return result;
    }

    public static <T> List<T> copyList(List<?> list, Class<T> classz) {
        List<T> resultList = new LinkedList<>();
        if (isEmpty(list)) {
            return resultList;
        }
        for (Object obj1 : list) {
            resultList.add(copyObject(obj1, classz));
        }
        return resultList;
    }

    /**
     * 判断一个集合是否为空
     *
     * @param col
     * @param <T>
     * @return
     */
    public static <T> boolean isEmpty(Collection<T> col) {
        return col == null || col.size() == 0;
    }

    /**
     * 忽略原对象空值copy
     *
     * @param source
     * @param target
     */
    public static void copyIgnoreNull(Object source, Object target) {
        copyProperties(source, target, getNullPropertyNames(source));
    }

    /**
     * 获取需要忽略的属性
     *
     * @param source
     * @return
     */
    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            // 此处判断可根据需求修改
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
}
