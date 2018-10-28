package fr.codechill.spring.utils.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomRestTemplate extends RestTemplate {

    @Autowired
    public CustomRestTemplate(RestErrorHandler restErrorHandler) {
        super();
        this.setErrorHandler(restErrorHandler);
    }
}