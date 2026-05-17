package org.abdrafikov.groupbuy.repository;

import org.abdrafikov.groupbuy.model.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {

    List<CurrencyRate> findByBaseCurrency(String baseCurrency);
}
