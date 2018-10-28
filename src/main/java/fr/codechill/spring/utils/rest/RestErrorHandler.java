package fr.codechill.spring.utils.rest;

import java.io.IOException;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

@Component
public class RestErrorHandler implements ResponseErrorHandler {

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        // Do nothing, no matter the status we just want to return the ResponseEntity
    }
 
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return false;
    }
}