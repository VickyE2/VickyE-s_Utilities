package org.vicky.utilities.DatabaseManager.templates;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.NaturalId;

import java.util.UUID;

@Entity
@Table(
        name = "Themes"
)
public class Theme {
    @Id
    private String id;
    @Column(
            name = "Name",
            nullable = false
    )
    @NaturalId
    private String name;

    public UUID getId() {
        return UUID.fromString(id);
    }

    public void setId(UUID id) {
        this.id = id.toString();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}