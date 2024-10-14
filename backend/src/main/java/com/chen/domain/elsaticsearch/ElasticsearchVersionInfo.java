package com.chen.domain.elsaticsearch;

import co.elastic.clients.util.DateTime;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author chenyunzhi
 * @DATE 2024/7/8 18:04
 * @Description:
 */

@Data
public class ElasticsearchVersionInfo  implements Serializable {

    private DateTime buildDate;
    private String buildFlavor;
    private String buildHash;
    private boolean buildSnapshot;
    private String buildType;
    private String luceneVersion;
    private String minimumIndexCompatibilityVersion;
    private String minimumWireCompatibilityVersion;
    private String number;

}
