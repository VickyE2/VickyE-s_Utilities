/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.DatabaseManager.templates;

import jakarta.persistence.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import org.vicky.utilities.DatabaseTemplate;
import org.vicky.utilities.RanksLister;

/**
 * <strong>If you would like to extend this class for a more versatile database entity:</strong>
 * <pre>{@code
 * @Entity
 * @Table(name = "extended_global_player_type_a")
 * public class ExtendedGlobalPlayer extends ExtendedPlayerBase {
 *     @JoinTable(
 *         name = "some_new_field",
 *         joinColumns = @JoinColumn(name = "some_field_parameter"),
 *         inverseJoinColumns = @JoinColumn(name = "some_other_table_id")
 *     )
 *     @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
 *     private List<SomeClass> someClassList = new ArrayList<>();
 * }
 * }</pre>
 * Some way to get the ExtendedGlobalPlayer of a DatabasePlayer
 * <pre>{@code
 *  TypedQuery<ExtendedPlayerTypeA> query = em.createQuery(
 *     "SELECT e FROM ExtendedPlayerTypeA e WHERE e.databasePlayer.id = :playerId", ExtendedPlayerTypeA.class);
 *  query.setParameter("playerId", "some-player-id");
 *  ExtendedPlayerTypeA extensions = query.getSingleResult();
 * }</pre>
 * @author VickyE2
 */
@Entity
@Table(name = "DatabasePlayers")
public class DatabasePlayer implements DatabaseTemplate {
  @Id
  @Column(name = "player_id", unique = true, nullable = false)
  private String id;

  @Column private boolean isFirstTime;

  @Column private String userTheme = "lt";

  public UUID getId() {
    return UUID.fromString(id);
  }

  public void setId(UUID id) {
    this.id = id.toString();
  }

  @Transient
  public int getRank() {
    RanksLister lister = new RanksLister();
    try {
      return lister.getHighestWeighingGroupWeight(this.getId()).get().orElse(0);
    } catch (ExecutionException | InterruptedException e) {
      return 0;
    }
  }

  @Transient
  public String getRankName() {
    RanksLister lister = new RanksLister();
    try {
      return lister.getHighestWeighingGroup(this.getId()).get();
    } catch (ExecutionException | InterruptedException e) {
      return "default";
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
