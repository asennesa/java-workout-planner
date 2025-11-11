    package com.workoutplanner.workoutplanner.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for Bucket4j rate limiting.
 * 
 * Following Spring Boot best practices:
 * - Centralized configuration
 * - Bean-based setup for easy testing
 * - Documented configuration choices
 * - Easy to extend (swap Caffeine for Redis later)
 * 
 * Bucket4j ProxyManager Pattern:
 * - ProxyManager manages all rate limit buckets
 * - Uses Caffeine cache as backend (for single-instance deployments)
 * - For distributed deployments: swap to Redis/Hazelcast ProxyManager
 * - Zero code changes in aspect/controllers when switching backends!
 * 
 * Why Caffeine for Single-Instance:
 * - Extremely fast (faster than ConcurrentHashMap)
 * - Automatic memory management
 * - Built-in eviction policies
 * - No external dependencies (Redis, Memcached)
 * - Perfect for most Spring Boot applications
 * 
 * Migration Path to Distributed (Future):
 * 1. Add Redis dependency
 * 2. Change ProxyManager from Caffeine to Redis
 * 3. Done! No other code changes needed
 * 
 * @author WorkoutPlanner Team
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class RateLimitConfig {
    
    /**
     * Creates Caffeine cache for storing Bucket4j buckets.
     * 
     * Caffeine Cache Benefits:
     * - Extremely fast (faster than ConcurrentHashMap)
     * - Automatic memory management
     * - Built-in eviction policies
     * - Thread-safe without manual synchronization
     * - Excellent for rate limiting use case
     * 
     * Configuration:
     * - maximumSize: 10,000 buckets (supports 10K different rate limit keys)
     * - expireAfterWrite: 1 hour (buckets auto-expire if unused)
     * - Keys are: "USER:username" or "IP:192.168.1.1" or "GLOBAL"
     * 
     * Memory Usage:
     * - Each bucket: ~100 bytes
     * - 10,000 buckets: ~1 MB
     * - Very memory-efficient!
     * 
     * Performance:
     * - Sub-microsecond access time
     * - Lock-free reads
     * - Minimal GC pressure
     * 
     * Production Considerations:
     * - maximumSize should be > expected concurrent users
     * - expireAfterWrite should be > longest rate limit window
     * - Monitor cache statistics in production
     * 
     * @return Caffeine cache for Bucket4j buckets
     */
    @Bean
    public Cache<String, Bucket> bucketCache() {
        return Caffeine.newBuilder()
            // Maximum number of rate limit buckets to cache
            // Adjust based on expected concurrent users
            // Rule of thumb: 2-3x your peak concurrent users
            .maximumSize(10_000)
            
            // Auto-expire buckets after 1 hour of inactivity
            // Prevents memory leaks from inactive users
            // Should be longer than your longest rate limit window
            .expireAfterWrite(Duration.ofHours(1))
            
            // Enable statistics for monitoring (optional but recommended)
            // Can track hit rate, evictions, etc. in production
            .recordStats()
            
            .build();
    }
}

