package com.example.search.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ESConfig {

  @Bean(destroyMethod = "close")
  public RestHighLevelClient esClient(@Value("${elasticsearch.host}") String host,
                                      @Value("${elasticsearch.port}") int port,
                                      @Value("${elasticsearch.username}") String user,
                                      @Value("${elasticsearch.password}") String pass) {
    BasicCredentialsProvider creds = new BasicCredentialsProvider();
    creds.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, pass));
    return new RestHighLevelClient(
        RestClient.builder(new HttpHost(host, port))
            .setHttpClientConfigCallback(cb -> cb.setDefaultCredentialsProvider(creds))
    );
  }
}
