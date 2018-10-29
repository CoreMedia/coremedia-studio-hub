package com.coremedia.blueprint.connectors.typeform.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Form {
    private String id;
    private String title;
    private String language;
    private String theme;
    private String workspace;
    private String display;
    private boolean is_public;
    private boolean is_trial;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLanguage() {
        return language;
    }

    public String getTheme() {
        return theme;
    }

    public String getWorkspace() {
        return workspace;
    }

    public String getDisplay() {
        return display;
    }

    public boolean isIs_public() {
        return is_public;
    }

    public boolean isIs_trial() {
        return is_trial;
    }

    @JsonProperty("theme")
    private void unpackThemeFromNestedObject(Map<String, String> theme) {
        this.theme = theme.get("href");
    }

    @JsonProperty("workspace")
    private void unpackWorkspaceFromNestedObject(Map<String, String> workspace) {
        this.workspace = workspace.get("href");
    }

    @JsonProperty("_links")
    private void unpackDisplayFromNestedObject(Map<String, String> display) {
        this.display = display.get("display");
    }

    @JsonProperty("settings")
    private void unpackSettingsFromNestedObject(Map<String, Object> settings) {
        this.is_public = "true".equalsIgnoreCase(settings.get("is_public").toString());
        this.is_trial = "true".equalsIgnoreCase(settings.get("is_trial").toString());
    }

    @Override
    public String toString() {
        return "Form{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", language='" + language + '\'' +
                ", theme='" + theme + '\'' +
                ", display='" + display + '\'' +
                ", workspace='" + workspace + '\'' +
                ", is_public=" + is_public +
                ", is_trial=" + is_trial +
                '}';
    }
}
