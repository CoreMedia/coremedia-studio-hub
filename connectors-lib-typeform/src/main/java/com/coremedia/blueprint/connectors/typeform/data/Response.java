package com.coremedia.blueprint.connectors.typeform.data;

import java.util.Collections;
import java.util.List;

public class Response {

    private String landingId;
    private String token;
//    private Date landedAt;
//    private Date submittedAt;
    private List<Answer> answers;

    public String getLandingId() {
        return landingId;
    }

    public String getToken() {
        return token;
    }

    public List<Answer> getAnswers() {
        if (answers == null) {
            return Collections.emptyList();
        }
        return answers;
    }

    @Override
    public String toString() {
        return "Response{" +
                "landingId='" + landingId + '\'' +
                ", token='" + token + '\'' +
                ", answers=" + answers +
                '}';
    }
}
