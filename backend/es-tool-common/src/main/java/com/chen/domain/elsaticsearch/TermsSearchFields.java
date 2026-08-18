package com.chen.domain.elsaticsearch;

import lombok.Data;

import java.util.List;

/**
 * 多值字段查询（terms query）
 *
 * @author chenyunzhi
 */
@Data
public class TermsSearchFields {

    public String key;

    public List<String> values;

}