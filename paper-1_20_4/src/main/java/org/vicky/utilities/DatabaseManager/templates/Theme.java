/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.DatabaseManager.templates;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.NaturalId;
import org.vicky.utilities.DatabaseTemplate;

@Entity
@Table(name = "Themes")
public class Theme implements DatabaseTemplate {
  @Id private String id;

  @Column(name = "Name", nullable = false)
  @NaturalId
  private String name;

  @Column(name = "description")
  private String description;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return this.name;
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
}
