package org.abdrafikov.groupbuy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.abdrafikov.groupbuy.model.base.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "users")
public class User extends BaseEntity {


    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "owner")
    private Set<Workspace> ownedWorkspaces = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<WorkspaceMember> workspaceMemberships = new HashSet<>();

//    @OneToMany(mappedBy = "author")
//    private Set<PurchaseItem> purchaseItems = new HashSet<>();
//
//    @OneToMany(mappedBy = "author")
//    private Set<Comment> comments = new HashSet<>();
//
//    @OneToMany(mappedBy = "createdBy")
//    private Set<Order> orders = new HashSet<>();

}