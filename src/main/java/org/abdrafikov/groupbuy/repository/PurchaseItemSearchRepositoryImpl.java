package org.abdrafikov.groupbuy.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.abdrafikov.groupbuy.model.PurchaseItem;
import org.abdrafikov.groupbuy.model.choices.PurchaseItemStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PurchaseItemSearchRepositoryImpl implements PurchaseItemSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<PurchaseItem> search(Long workspaceId, PurchaseItemStatus status, String titleQuery) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<PurchaseItem> query = cb.createQuery(PurchaseItem.class);
        Root<PurchaseItem> item = query.from(PurchaseItem.class);
        item.fetch("workspace");
        item.fetch("author");

        List<Predicate> predicates = new ArrayList<>();
        // фильтр по workspace_id
        predicates.add(cb.equal(item.get("workspace").get("id"), workspaceId));

        // необязательные фильтры
        if (status != null) {
            predicates.add(cb.equal(item.get("status"), status));
        }

        String normalizedTitleQuery = normalizeTitleQuery(titleQuery);
        if (normalizedTitleQuery != null) {
            predicates.add(cb.like(
                    cb.lower(item.get("title")),
                    "%" + escapeLike(normalizedTitleQuery) + "%",
                    '\\'
            ));
        }

        query.select(item)
                .where(predicates.toArray(Predicate[]::new))
                .orderBy(cb.desc(item.get("createdAt")));

        return entityManager.createQuery(query).getResultList();
    }

    private String normalizeTitleQuery(String titleQuery) {
        if (titleQuery == null || titleQuery.isBlank()) {
            return null;
        }
        return titleQuery.trim().toLowerCase(Locale.ROOT);
    }

    private String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
