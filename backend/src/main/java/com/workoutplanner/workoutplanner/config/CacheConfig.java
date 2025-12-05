package com.workoutplanner.workoutplanner.config;

import com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.concurrent.TimeUnit;

/**
 * JCache configuration using Caffeine provider.
 *
 * Provides caches for:
 * - rate-limit-buckets: Token buckets for Bucket4j rate limiting (1 hour TTL)
 * - auth0Users: Auth0 user principal caching (5 min TTL)
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager jCacheManager() {
        CacheManager cacheManager = Caching.getCachingProvider(CaffeineCachingProvider.class.getName())
                .getCacheManager();

        // Rate limit buckets cache (1 hour TTL)
        createCacheIfNotExists(cacheManager, "rate-limit-buckets", 1, TimeUnit.HOURS);

        // Auth0 users cache (5 min TTL for quick profile updates)
        createCacheIfNotExists(cacheManager, "auth0Users", 5, TimeUnit.MINUTES);

        return cacheManager;
    }

    private void createCacheIfNotExists(CacheManager cacheManager, String cacheName, long duration, TimeUnit unit) {
        if (cacheManager.getCache(cacheName) == null) {
            MutableConfiguration<Object, Object> config = new MutableConfiguration<>()
                    .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(unit, duration)))
                    .setStoreByValue(false)
                    .setStatisticsEnabled(true);
            cacheManager.createCache(cacheName, config);
        }
    }

    @Bean
    public org.springframework.cache.CacheManager cacheManager(CacheManager jCacheManager) {
        return new JCacheCacheManager(jCacheManager);
    }
}
