package com.chen.domain.elsaticsearch;

import lombok.Data;

import javax.annotation.Nullable;

/**
 * @Author chenyunzhi
 * @DATE 2024/7/17 14:01
 * @Description:
 */

@Data
public class CatTemplatesRecord {

    public String name;

    public String indexPatterns;

    public String order;

    public String version;

    public String composedOf;
}
