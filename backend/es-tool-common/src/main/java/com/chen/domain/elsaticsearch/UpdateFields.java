package com.chen.domain.elsaticsearch;

import lombok.Data;

/**
 * @Author chenyunzhi
 * @DATE 2024/10/15 15:22
 * @Description: 可以更新的字段
 */
@Data
public class UpdateFields {

    public String key;

    public Object value;

}
