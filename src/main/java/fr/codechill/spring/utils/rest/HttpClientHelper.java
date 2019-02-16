package fr.codechill.spring.utils.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public class HttpClientHelper {

  private HttpClient client;

  public HttpClientHelper() {
    RequestConfig config =
        RequestConfig.custom()
            .setConnectTimeout(-1)
            .setConnectionRequestTimeout(-1)
            .setSocketTimeout(-1)
            .build();
    this.client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
  }

  public HttpResponse get(String url, Header headers[]) throws Exception {
    HttpGet request = new HttpGet(url);
    request.setHeaders(headers);
    return client.execute(request);
  }

  public StreamingResponseBody contentToStreamingResponse(InputStream content) {
    return new StreamingResponseBody() {

      @Override
      public void writeTo(OutputStream outputStream) throws IOException {
        IOUtils.copyLarge(content, outputStream);
      }
    };
  }
}
