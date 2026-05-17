package org.abdrafikov.groupbuy.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    public static final String CURRENCY_RATES_CACHE = "currencyRates";
    public static final String CURRENCY_RATES_SNAPSHOT_KEY = "snapshot";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CURRENCY_RATES_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1));
        return cacheManager;
    }
}
