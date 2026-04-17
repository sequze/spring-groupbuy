package org.abdrafikov.groupbuy.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.abdrafikov.groupbuy.model.base.BaseEntity;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(
        name = "order_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_order_item_order_purchase_item", columnNames = {"order_id", "purchase_item_id"})
        }
)
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_item_id", nullable = false)
    private PurchaseItem purchaseItem;

    @Column(name = "item_title_snapshot", nullable = false, length = 200)
    private String itemTitleSnapshot;

    @Column(name = "quantity_snapshot", nullable = false)
    private Integer quantitySnapshot;

    @Column(name = "price_snapshot", precision = 12, scale = 2)
    private BigDecimal priceSnapshot;

    @Column(name = "currency_snapshot", length = 3)
    private String currencySnapshot;
}
