package org.abdrafikov.groupbuy.model;

import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;
import org.abdrafikov.groupbuy.model.base.BaseEntity;

@Setter
@Getter
@Entity
@Table(name = "comments")
public class Comment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_item_id", nullable = false)
    private PurchaseItem purchaseItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "is_edited", nullable = false)
    private boolean edited = false;

}