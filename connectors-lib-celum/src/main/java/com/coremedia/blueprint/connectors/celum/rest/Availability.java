package com.coremedia.blueprint.connectors.celum.rest;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class Availability {

  private String availabilityType;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private Date activeFrom;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private Date activeTo;
}
