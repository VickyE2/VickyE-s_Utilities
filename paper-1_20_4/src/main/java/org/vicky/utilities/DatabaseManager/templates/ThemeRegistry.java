/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities.DatabaseManager.templates;

import java.util.ArrayList;
import java.util.List;

import org.vicky.shaded.jakarta.persistence.*;
import org.vicky.utilities.DatabaseTemplate;

@Entity
@Table(name = "ThemeRegistry")
public class ThemeRegistry implements DatabaseTemplate {
    @Id
    private String id;

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinColumn(name = "themes")
    private List<Theme> themes = new ArrayList<>();

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Theme> getAdvancements() {
        return this.themes;
    }

    public void setAdvancements(List<Theme> themes) {
        this.themes = themes;
    }

    public void addAdvancement(Theme themes) {
        this.themes.add(themes);
    }
}
