package com.chen.domain.elsaticsearch;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Author chenyunzhi
 * @DATE 2024/9/30 16:27
 * @Description:
 */
@Data
@Accessors(chain = true)
public class ElasticsearchDocumentPage {

    Long count;

    List<ElasticsearchDocument> rows;
}
