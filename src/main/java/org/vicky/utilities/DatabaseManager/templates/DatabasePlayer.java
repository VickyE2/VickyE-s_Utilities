/* Licensed under Apache-2.0 2025. */
package org.vicky.utilities.DatabaseManager.templates;

import jakarta.persistence.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import org.hibernate.annotations.ColumnDefault;
import org.vicky.utilities.DatabaseTemplate;
import org.vicky.utilities.RanksLister;

/**
 * <strong>If you would like to extend this class for a more versatile database entity:</strong>
 * <pre>
 * <strong>@</strong>Entity
 * public class ExtendedGlobalPlayer extends GlobalPlayer {
 *     <strong>@</strong>JoinTable(
 *         name = "player_advancements",
 *         joinColumns = @JoinColumn(name = "player_id"),
 *         inverseJoinColumns = @JoinColumn(name = "advancement_id")
 *     )
 *     <strong>@</strong>OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
 *     private List"<"Advancement">" accomplishedAdvancements = new ArrayList"<>"();
 * }
 * </pre>
 * @author VickyE2
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "ServerDatabasePlayers")
public class DatabasePlayer implements DatabaseTemplate {
  @Id
  @Column(name = "player_id", unique = true, nullable = false)
  private String id;

  @Column private boolean isFirstTime;

  @Column
  @ColumnDefault(value = "light_theme")
  private String userTheme;

  public UUID getId() {
    return UUID.fromString(id);
  }

  public void setId(UUID id) {
    this.id = id.toString();
  }

  public int getRank() {
    RanksLister lister = new RanksLister();
    try {
      return lister.getHighestWeighingGroupWeight(this.getId()).get().orElse(0);
    } catch (ExecutionException | InterruptedException e) {
      return 0;
    }
  }

  public boolean isFirstTime() {
    return this.isFirstTime;
  }

  public void setFirstTime(boolean firstTime) {
    this.isFirstTime = firstTime;
  }

  public void setUserTheme(String userTheme) {
    this.userTheme = userTheme;
  }

  public String getUserTheme() {
    return userTheme;
  }
}
