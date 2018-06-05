package com.coremedia.blueprint.connectors.canto.rest.entities;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public abstract class AbstractCantoEntity implements Serializable {

  static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

  /**
   * Stores all unmapped custom attributes.
   */
  private Map<String, Object> customAttributes = new HashMap<>();

  @JsonAnyGetter
  public Map<String, Object> customAttributes() {
    return customAttributes;
  }

  @JsonAnySetter
  public void set(String name, Object value) {
    customAttributes.put(name, value);
  }

}
