package com.coremedia.labs.connectors.typeform;

import com.coremedia.blueprint.connectors.typeform.TypeformConnectorClient;
import com.coremedia.blueprint.connectors.typeform.data.Form;
import com.coremedia.blueprint.connectors.typeform.data.Response;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class TypeformConnectorClientTest {


    private static final String TYPEFORM_CLIENT_ACCESSTOKEN = "typeform.client.accesstoken";

    private static final String TYPEFORM_TEST = "typeform.test";
    private static final String TYPEFORM_TEST_FORM_ID = "typeform.test.form.id";

    private Properties properties;
    private TypeformConnectorClient client;
    private boolean testActive = false;

    public TypeformConnectorClientTest() {
        properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream("typeform-test.properties"));
            testActive = "true".equalsIgnoreCase(properties.getProperty(TYPEFORM_TEST));

            String accessToken = properties.getProperty(TYPEFORM_CLIENT_ACCESSTOKEN);

            client = new TypeformConnectorClient(accessToken);
        } catch (IOException e) {
            fail("Could not init Test");
        }
    }

    @Test
    public void getForms() {
        if (testActive) {
            List<Form> forms = client.getForms();
            assertFalse("getForms() did not return any results", forms.isEmpty());
        }
    }

    @Test
    public void getForm() {
        if (testActive) {
            String formId = properties.getProperty(TYPEFORM_TEST_FORM_ID);
            Form form = client.getForm(formId);
            assertNotNull("getForm() for " + formId + " failed", form);
        }
    }

    @Test
    public void getResponses() {
        if (testActive) {
            String formId = properties.getProperty(TYPEFORM_TEST_FORM_ID);
            List<Response> responses = client.getResponses(formId);
            assertFalse("getResonses() did not return any results", responses.isEmpty());
        }
    }
}