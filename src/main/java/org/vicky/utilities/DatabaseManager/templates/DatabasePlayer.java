package org.vicky.utilities.DatabaseManager.templates;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.*;

/**
 * <strong>If you would like to extend this class for a more versatile database entity:</strong>
 * <p>
 * <@>Entity<br>
 * public class ExtendedGlobalPlayer extends GlobalPlayer {<br>
 *     <@>JoinTable(<br>
 *         name = "player_advancements",<br>
 *         joinColumns = @JoinColumn(name = "player_id"),<br>
 *         inverseJoinColumns = @JoinColumn(name = "advancement_id")<br>
 *     )<br>
 *     <@>OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)<br>
 *     private List<Advancement> accomplishedAdvancements = new ArrayList<>();<br>
 * }<br>
 * </p>
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(
        name = "ServerDatabasePlayers"
)
public class DatabasePlayer {
    @Id
    private String id;
    @Column
    private boolean isFirstTime;
    @Column
    @ColumnDefault(value = "light_theme")
    private String userTheme;

    public UUID getId() {
        return UUID.fromString(id);
    }

    public void setId(UUID id) {
        this.id = id.toString();
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
