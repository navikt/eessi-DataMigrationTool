package eu.ec.dgempl.eessi.rina.tool.migration.application.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {
    @Value("${elasticsearch.host}")
    private String esHost;

    @Value("${elasticsearch.port}")
    private int esPort;

    @Value("${elasticsearch.connection.timeout}")
    private int connectionTimeout;

    @Value("${elasticsearch.socket.timeout}")
    private int socketTimeout;

    @Bean
    public RestClient lowLevelClient() {
        //@formatter:off
        return RestClient.builder(new HttpHost(esHost, esPort, "http"))
                .setRequestConfigCallback(builder -> builder.setConnectTimeout(connectionTimeout).setSocketTimeout(socketTimeout))
                .setFailureListener(new RestClient.FailureListener() {
                    @Override
                    public void onFailure(HttpHost host) {
                        // TODO add implementation
                    }
                })
                .build();
        //@formatter:on
    }

    @Bean
    public RestHighLevelClient highLevelClient() {
        return new RestHighLevelClient(this.lowLevelClient());
    }
}
