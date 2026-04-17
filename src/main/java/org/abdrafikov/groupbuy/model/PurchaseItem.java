package org.abdrafikov.groupbuy.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.abdrafikov.groupbuy.model.base.BaseEntity;
import org.abdrafikov.groupbuy.model.choices.PurchaseItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Getter
@Entity
@Table(name = "purchase_items")
public class PurchaseItem extends BaseEntity {

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Setter
    @Column(nullable = false, length = 200)
    private String title;

    @Setter
    @Column(length = 2000)
    private String description;

    @Setter
    @Column(nullable = false)
    private Integer quantity;

    @Setter
    @Column(length = 50)
    private String unit;

    @Setter
    @Column(name = "price_amount", precision = 12, scale = 2)
    private BigDecimal priceAmount;

    @Setter
    @Column(name = "price_currency", length = 3)
    private String priceCurrency;

    @Setter
    @Column(name = "base_price_amount", precision = 12, scale = 2)
    private BigDecimal basePriceAmount;

    @Setter
    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PurchaseItemStatus status = PurchaseItemStatus.NEW;

    @Setter
    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Setter
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    @Setter
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by_id")
    private User rejectedBy;

    @OneToMany(mappedBy = "purchaseItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private Set<Comment> comments = new HashSet<>();

}