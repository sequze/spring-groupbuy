package org.abdrafikov.groupbuy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.abdrafikov.groupbuy.model.base.BaseEntity;
import org.abdrafikov.groupbuy.model.choices.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @Setter
    @Column(nullable = false, length = 200)
    private String title;

    @Setter
    @Column(length = 1000)
    private String description;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status = OrderStatus.DRAFT;

    @Setter
    @Column(name = "total_amount", precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Setter
    @Column(nullable = false, length = 3)
    private String currency;

    @Setter
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Setter
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> items = new HashSet<>();

}