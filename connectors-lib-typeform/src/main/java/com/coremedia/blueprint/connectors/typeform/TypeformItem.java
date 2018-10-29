package com.coremedia.blueprint.connectors.typeform;

import com.coremedia.blueprint.connectors.api.*;
import com.coremedia.blueprint.connectors.typeform.data.Answer;
import com.coremedia.blueprint.connectors.typeform.data.Form;
import com.coremedia.blueprint.connectors.typeform.data.Response;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

public class TypeformItem implements ConnectorItem {

    private ConnectorId connectorId;
    private TypeformConnectorService service;
    private ConnectorContext context;

    private Form form;

    public TypeformItem(TypeformConnectorService service, ConnectorContext context, ConnectorId connectorId) {
        this.service = service;
        this.context = context;
        this.connectorId = connectorId;
    }

    public TypeformItem(TypeformConnectorService service, ConnectorContext context, String id) {
        this.service = service;
        this.context = context;
        this.connectorId = ConnectorId.createItemId(ConnectorId.createRootId(context.getConnectionId()), id);
    }

    @Override
    public long getSize() {
        List<Response> responses = service.client.getResponses(connectorId.getExternalId());
        if (responses != null) {
            return responses.size();
        }
        return 0;
    }

    @Override
    public String getDescription() {
        return getForm().getId();
    }

    @Override
    public InputStream stream() {
        return createResponseStream();
    }

    @Override
    public boolean isDownloadable() {
        return true;
    }

    @Override
    public ConnectorId getConnectorId() {
        return connectorId;
    }

    @Override
    public String getName() {
        return getForm().getTitle();
    }

    @Override
    public ConnectorContext getContext() {
        return context;
    }

    @Override
    public ConnectorCategory getParent() {
        return service.getRootCategory(context);
    }

    @Override
    public String getDisplayName() {
        return getForm().getTitle();
    }

    @Override
    public Date getLastModified() {
        return null;
    }

    @Override
    public String getManagementUrl() {
        // @ToDo
        return null;
    }

    @Override
    public boolean isDeleteable() {
        return false;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public ConnectorMetaData getMetaData() {
        Map<String, Object> data = new HashMap<>();

        Form form = getForm();
        if (form != null) {
            data.put("id", form.getId());
            data.put("language", form.getLanguage());
        }

        return () -> data;
    }

    @Override
    public String getItemType() {
        return "form";
    }

    @Override
    public String getMimeType() {
        return "text/csv";
    }

    protected Form getForm() {
        if (form == null) {
            form = service.client.getForm(connectorId.getExternalId());
        }
        return form;
    }

    private InputStream createResponseStream() {
        List<Response> responses = service.client.getResponses(connectorId.getExternalId());
        if ((responses != null) && responses.size() > 0) {
            StringWriter writer = new StringWriter();
            try {
                Response first = responses.get(0);
                ArrayList<String> headers = new ArrayList<>();
                headers.add("Token");
                for (Answer answer : first.getAnswers()) {
                    headers.add(answer.getFieldId());
                }

                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])));
                for (Response response : responses) {
                    ArrayList<String> record = new ArrayList<>();
                    record.add(response.getToken());
                    List<Answer> answers = response.getAnswers();
                    for (Answer answer : answers) {
                        record.add(answer.getValue().toString());
                    }
                    if (answers.size() > 0) {
                        csvPrinter.printRecord(record);
                    }
                }
                csvPrinter.flush();

                return new ByteArrayInputStream(writer.getBuffer().toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
