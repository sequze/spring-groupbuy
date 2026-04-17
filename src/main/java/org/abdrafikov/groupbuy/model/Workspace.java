package org.abdrafikov.groupbuy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.abdrafikov.groupbuy.model.base.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "workspaces")
public class Workspace extends BaseEntity {

    @Setter
    @Getter
    @Column(nullable = false, length = 150)
    private String name;

    @Setter
    @Getter
    @Column(length = 1000)
    private String description;

    @Setter
    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Setter
    @Getter
    @Column(name = "join_token", nullable = false, unique = true, length = 100)
    private String joinToken;

    @Setter
    @Getter
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Getter
    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WorkspaceMember> members = new HashSet<>();

    @OneToMany(mappedBy = "workspace")
    private Set<PurchaseItem> purchaseItems = new HashSet<>();

    @OneToMany(mappedBy = "workspace")
    private Set<Order> orders = new HashSet<>();

}