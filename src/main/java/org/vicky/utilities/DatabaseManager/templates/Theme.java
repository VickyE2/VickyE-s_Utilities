/* Licensed under Apache-2.0 2025. */
package org.vicky.utilities.DatabaseManager.templates;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.NaturalId;
import org.vicky.utilities.DatabaseTemplate;

@Entity
@Table(name = "Themes")
public class Theme implements DatabaseTemplate {
  @Id private String id;

  @Column(name = "Name", nullable = false)
  @NaturalId
  private String name;

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
}
