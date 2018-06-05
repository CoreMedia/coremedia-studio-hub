package com.coremedia.blueprint.connectors.canto.rest.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class AssetEntity extends AbstractCantoEntity {

  @JsonProperty("id")
  private int id;

  @JsonProperty("name")
  private String name;

  @JsonProperty("description")
  private String description;

  @JsonProperty("modification_date")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
  private Date modificationDate;

  @JsonProperty("size")
  private DataSizeEntity dataSize;

  @JsonProperty("file_format")
  private String fileFormat;

  @JsonProperty("rating")
  private RatingEntity rating;

  @JsonProperty("notes")
  private String notes;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getModificationDate() {
    return modificationDate;
  }

  public void setModificationDate(Date modificationDate) {
    this.modificationDate = modificationDate;
  }

  public DataSizeEntity getDataSize() {
    return dataSize;
  }

  public void setDataSize(DataSizeEntity dataSize) {
    this.dataSize = dataSize;
  }

  public String getFileFormat() {
    return fileFormat;
  }

  public void setFileFormat(String fileFormat) {
    this.fileFormat = fileFormat;
  }

  public RatingEntity getRating() {
    return rating;
  }

  public void setRating(RatingEntity rating) {
    this.rating = rating;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }



}
