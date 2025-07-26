/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.DatabaseManager.templates;

import jakarta.persistence.*;
import org.vicky.utilities.DatabaseTemplate;

@MappedSuperclass
public abstract class ExtendedPlayerBase implements DatabaseTemplate {
  @Id
  @Column(name = "extended_player_id", unique = true, nullable = false)
  private String id;

  @OneToOne(optional = false)
  @JoinColumn(name = "player_id", nullable = false)
  private DatabasePlayer databasePlayer;

  public DatabasePlayer getDatabasePlayer() {
    return databasePlayer;
  }

  public void setDatabasePlayer(DatabasePlayer databasePlayer) {
    this.databasePlayer = databasePlayer;
    this.setId(databasePlayer.getId().toString());
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
