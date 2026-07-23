package com.domeni.kapita.payment.config;

import com.domeni.kapita.generated.monetbil.api.MonetbilApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class MonetbilClientConfig {

  @Bean
  public MonetbilApi monetbilApi(
      RestClient.Builder restClientBuilder, @Value("${app.monetbil.base-url}") String baseUrl) {
    RestClient restClient = restClientBuilder.baseUrl(baseUrl).build();
    HttpServiceProxyFactory factory =
        HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
    return factory.createClient(MonetbilApi.class);
  }
}
