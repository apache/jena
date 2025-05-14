/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.mem2.gc;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An object pool for frequently created temporary objects.
 * <p>
 * This class reduces garbage collection pressure by reusing objects
 * instead of creating new ones. It automatically adjusts the pool size
 * based on memory pressure and demand patterns.
 *
 * @param <T> The type of objects in the pool
 */
public class ObjectPool<T> {
    
    private static final Logger LOG = LoggerFactory.getLogger(ObjectPool.class);
    
    // Memory thresholds for pool size adjustment
    private static final double HIGH_MEMORY_THRESHOLD = 0.80; // 80% usage
    
    // Pool statistics
    private final AtomicLong borrowCount = new AtomicLong(0);
    private final AtomicLong returnCount = new AtomicLong(0);
    private final AtomicLong createCount = new AtomicLong(0);
    private final AtomicLong destroyCount = new AtomicLong(0);
    
    // Memory monitoring
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    
    // The actual pool implementation
    private final ObjectPool<T> pool;
    
    // Pool configuration
    private int minIdle;
    private int maxIdle;
    private int maxTotal;
    
    // State
    private final AtomicBoolean closed = new AtomicBoolean(false);
    
    /**
     * Create a new object pool with the specified factory and resetters.
     * 
     * @param factory The factory to create new objects
     * @param resetter The function to reset objects before reuse
     * @param minIdle Minimum number of idle objects in the pool
     * @param maxIdle Maximum number of idle objects in the pool
     * @param maxTotal Maximum total objects in the pool
     */
    public ObjectPool(Supplier<T> factory, Consumer<T> resetter, 
                      int minIdle, int maxIdle, int maxTotal) {
        this.minIdle = minIdle;
        this.maxIdle = maxIdle;
        this.maxTotal = maxTotal;
        
        // Configure the pool
        GenericObjectPoolConfig<T> config = new GenericObjectPoolConfig<>();
        config.setMinIdle(minIdle);
        config.setMaxIdle(maxIdle);
        config.setMaxTotal(maxTotal);
        config.setBlockWhenExhausted(false);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        config.setTimeBetweenEvictionRunsMillis(60000); // 1 minute
        
        // Create the pool with a custom factory
        this.pool = new GenericObjectPool<>(new BasePooledObjectFactory<T>() {
            @Override
            public T create() {
                createCount.incrementAndGet();
                return factory.get();
            }
            
            @Override
            public PooledObject<T> wrap(T obj) {
                return new DefaultPooledObject<>(obj);
            }
            
            @Override
            public void passivateObject(PooledObject<T> pooledObject) {
                // Reset the object when returning to the pool
                if (resetter != null) {
                    resetter.accept(pooledObject.getObject());
                }
            }
            
            @Override
            public void destroyObject(PooledObject<T> pooledObject) {
                destroyCount.incrementAndGet();
                // Additional cleanup if needed
            }
            
            @Override
            public boolean validateObject(PooledObject<T> pooledObject) {
                // Validate that the object is still usable
                return pooledObject.getObject() != null;
            }
        }, config);
        
        LOG.debug("Created ObjectPool with minIdle={}, maxIdle={}, maxTotal={}", 
            minIdle, maxIdle, maxTotal);
        
        // Pre-populate the pool
        try {
            for (int i = 0; i < minIdle; i++) {
                T obj = factory.get();
                pool.returnObject(obj);
                createCount.incrementAndGet();
            }
        } catch (Exception e) {
            LOG.error("Error pre-populating object pool", e);
        }
    }
    
    /**
     * Borrow an object from the pool.
     * 
     * @return An object from the pool, or null if the pool is exhausted
     */
    public T borrowObject() {
        checkNotClosed();
        
        try {
            T obj = pool.borrowObject();
            borrowCount.incrementAndGet();
            return obj;
        } catch (Exception e) {
            LOG.debug("Failed to borrow object from pool", e);
            return null;
        }
    }
    
    /**
     * Return an object to the pool.
     * 
     * @param obj The object to return
     */
    public void returnObject(T obj) {
        if (obj == null || closed.get()) {
            return;
        }
        
        try {
            pool.returnObject(obj);
            returnCount.incrementAndGet();
        } catch (Exception e) {
            LOG.debug("Failed to return object to pool", e);
        }
    }
    
    /**
     * Close the pool and release all resources.
     */
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                pool.close();
                LOG.debug("Closed ObjectPool");
            } catch (Exception e) {
                LOG.error("Error closing object pool", e);
            }
        }
    }
    
    /**
     * Get the number of active objects.
     */
    public int getNumActive() {
        try {
            return pool.getNumActive();
        } catch (Exception e) {
            LOG.warn("Error getting active count", e);
            return 0;
        }
    }
    
    /**
     * Get the number of idle objects.
     */
    public int getNumIdle() {
        try {
            return pool.getNumIdle();
        } catch (Exception e) {
            LOG.warn("Error getting idle count", e);
            return 0;
        }
    }
    
    /**
     * Get the borrow count.
     */
    public long getBorrowCount() {
        return borrowCount.get();
    }
    
    /**
     * Get the return count.
     */
    public long getReturnCount() {
        return returnCount.get();
    }
    
    /**
     * Get the create count.
     */
    public long getCreateCount() {
        return createCount.get();
    }
    
    /**
     * Get the destroy count.
     */
    public long getDestroyCount() {
        return destroyCount.get();
    }
    
    /**
     * Check if the pool is closed.
     */
    public boolean isClosed() {
        return closed.get();
    }
    
    /**
     * Adjust the pool size based on memory pressure.
     */
    public void adjustPoolSize() {
        if (closed.get()) {
            return;
        }
        
        try {
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            double memoryUsage = (double) heapUsage.getUsed() / heapUsage.getMax();
            
            if (memoryUsage > HIGH_MEMORY_THRESHOLD) {
                // High memory pressure, reduce pool size
                int newMaxIdle = (int) (maxIdle * 0.75);
                int newMaxTotal = (int) (maxTotal * 0.75);
                
                // Ensure we don't go below minimum
                newMaxIdle = Math.max(newMaxIdle, minIdle);
                newMaxTotal = Math.max(newMaxTotal, minIdle * 2);
                
                if (newMaxIdle != maxIdle || newMaxTotal != maxTotal) {
                    setPoolSizes(minIdle, newMaxIdle, newMaxTotal);
                    LOG.info("High memory pressure ({}%), reduced pool size: maxIdle={}, maxTotal={}", 
                        String.format("%.1f", memoryUsage * 100), newMaxIdle, newMaxTotal);
                }
            } else if (memoryUsage < HIGH_MEMORY_THRESHOLD * 0.7) {
                // Low memory pressure, can increase pool size if needed
                // and if we previously reduced it
                if (maxIdle < maxTotal) {
                    int newMaxIdle = (int) (maxIdle * 1.25);
                    int newMaxTotal = (int) (maxTotal * 1.25);
                    
                    // Don't exceed original configuration
                    newMaxIdle = Math.min(newMaxIdle, maxIdle);
                    newMaxTotal = Math.min(newMaxTotal, maxTotal);
                    
                    if (newMaxIdle != maxIdle || newMaxTotal != maxTotal) {
                        setPoolSizes(minIdle, newMaxIdle, newMaxTotal);
                        LOG.info("Low memory pressure ({}%), increased pool size: maxIdle={}, maxTotal={}", 
                            String.format("%.1f", memoryUsage * 100), newMaxIdle, newMaxTotal);
                    }
                }
            }
            
            // Log pool stats at debug level
            LOG.debug("ObjectPool stats: active={}, idle={}, borrowed={}, returned={}, created={}, destroyed={}",
                getNumActive(), getNumIdle(), getBorrowCount(), getReturnCount(), 
                getCreateCount(), getDestroyCount());
        } catch (Exception e) {
            LOG.warn("Error adjusting pool size", e);
        }
    }
    
    /**
     * Set the pool sizes.
     */
    private void setPoolSizes(int minIdle, int maxIdle, int maxTotal) {
        try {
            GenericObjectPool<T> genericPool = (GenericObjectPool<T>) pool;
            genericPool.setMinIdle(minIdle);
            genericPool.setMaxIdle(maxIdle);
            genericPool.setMaxTotal(maxTotal);
            
            this.minIdle = minIdle;
            this.maxIdle = maxIdle;
            this.maxTotal = maxTotal;
        } catch (Exception e) {
            LOG.error("Error setting pool sizes", e);
        }
    }
    
    /**
     * Throw an exception if the pool is closed.
     */
    private void checkNotClosed() {
        if (closed.get()) {
            throw new IllegalStateException("Object pool has been closed");
        }
    }
}