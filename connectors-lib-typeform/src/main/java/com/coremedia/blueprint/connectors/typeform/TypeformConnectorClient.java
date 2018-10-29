package com.coremedia.blueprint.connectors.typeform;

import com.coremedia.blueprint.connectors.typeform.data.Form;
import com.coremedia.blueprint.connectors.typeform.data.Forms;
import com.coremedia.blueprint.connectors.typeform.data.Response;
import com.coremedia.blueprint.connectors.typeform.data.Responses;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class TypeformConnectorClient {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private String accessToken;

    private RestTemplate restTemplate = new RestTemplate();

    public TypeformConnectorClient(String accessToken) {
        this.accessToken = accessToken;
    }

    private HttpEntity<Object> createAuthEntity() {
        HttpHeaders headers = new HttpHeaders();

        headers.add(AUTHORIZATION_HEADER, "Bearer " + accessToken);
        return new HttpEntity<>(null, headers);
    }

    public List<Form> getForms() {

        ResponseEntity<Forms> formsResponseEntity = restTemplate.exchange("https://api.typeform.com/forms", HttpMethod.GET, createAuthEntity(), Forms.class);

        if (!formsResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return null;
        }
        Forms forms = formsResponseEntity.getBody();
        if (forms != null) {
            return forms.getItems();
        }

        return new ArrayList<>();
    }

    public Form getForm(String id) {

        ResponseEntity<Form> formResponseEntity = restTemplate.exchange("https://api.typeform.com/forms/" + id, HttpMethod.GET, createAuthEntity(), Form.class);

        if (!formResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return null;
        }
        return formResponseEntity.getBody();
    }

    public List<Response> getResponses(String formId) {

        ResponseEntity<Responses> responsesResponseEntity = restTemplate.exchange("https://api.typeform.com/forms/" + formId + "/responses", HttpMethod.GET, createAuthEntity(), Responses.class);

        if (!responsesResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return null;
        }
        return responsesResponseEntity.getBody().getItems();

    }
}
