import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.InfoResponse;
import co.elastic.clients.elasticsearch.indices.PutAliasRequest;
import co.elastic.clients.elasticsearch.indices.PutAliasResponse;
import com.chen.ChenApplication;
import com.chen.common.config.elasticsearch.ElasticsearchClientConfig;
import com.chen.domain.elsaticsearch.ElasticsearchConnectParam;
import com.chen.service.HelloWord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: 神的孩子都在歌唱
 * @date: 2023/10/20 14:58
 * @Description:
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = ChenApplication.class)
public class ElasticsearchTest {

    @Test
    public void elasticsearchConnect() throws IOException {

//
//        ElasticsearchClientConfig elasticsearchClientConfig = new ElasticsearchClientConfig(
//                new ElasticsearchConnectParam("elastic", "chenyunzhi", "192.168.1.47", 9200, "http"));

//
//        ElasticsearchClientConfig elasticsearchClientConfig = new ElasticsearchClientConfig(
//                new ElasticsearchConnectParam("cosa_manage", "Y8jpam323Ptm^QQk%QxUkZoZ", "192.168.1.83", 9200, "http"));
//        ElasticsearchClient elasticsearchClient = elasticsearchClientConfig.elasticsearchClient();
//
//        List<String> strings = new ArrayList<>();
//        strings.add("pppp_1");
//        strings.add("pppp");
//        PutAliasRequest build = new PutAliasRequest.Builder().index(strings).name("pppp_alias").isWriteIndex(true).build();
//
//        PutAliasResponse putAliasResponse = elasticsearchClient.indices().putAlias(build);
//        long count = elasticsearchClient.count().count();
//        System.out.println("资本三个月");
//        System.out.println(count);
//        InfoResponse info = elasticsearchClient.info();
//        System.out.println("Elasticsearch version: " + info.version().number());
//        elasticsearchClient._transport().close();
//

    }


}
