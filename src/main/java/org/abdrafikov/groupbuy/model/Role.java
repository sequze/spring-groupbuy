package org.abdrafikov.groupbuy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.abdrafikov.groupbuy.model.base.BaseEntity;
import org.abdrafikov.groupbuy.model.choises.GlobalRoleName;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private GlobalRoleName name;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    public Role() {
    }

}