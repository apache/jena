/**
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

package org.apache.jena.base.module;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Implementation of {@link SubsystemRegistry} for use in the simple but common case
 * of running Jena as a collection of jars on the classpath.
 * <p>
 * Uses {@link ServiceLoader} to find sub-systems.
 */
public class SubsystemRegistryServiceLoader<T extends SubsystemLifecycle> implements SubsystemRegistry<T> {

    private List<T> registry = new ArrayList<>();
    private Object registryLock = new Object();
    private Class<T> moduleClass;

    public SubsystemRegistryServiceLoader(Class<T> moduleClass) {
        this.moduleClass = moduleClass;
    }

    @Override
    public void load() {
        synchronized (registryLock) {
            // Find subsystems asking for initialization.
            ServiceLoader<T> sl =
                    // Use this->classloader form : better for OSGi
                    ServiceLoader.load(moduleClass, this.getClass().getClassLoader());
            sl.forEach(this::add);
        }
    }

    @Override
    public void add(T module) {
        synchronized (registryLock) {
            if ( !registry.contains(module) )
                registry.add(module);
        }
    }

    @Override
    public boolean isRegistered(T module) {
        synchronized (registryLock) {
            return registry.contains(module);
        }
    }

    @Override
    public void remove(T module) {
        synchronized (registryLock) {
            registry.remove(module);
        }
    }

    @Override
    public int size() {
        synchronized (registryLock) {
            return registry.size();
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (registryLock) {
            return registry.isEmpty();
        }
    }

    @Override
    public List<T> snapshot() {
        synchronized (registryLock) {
            return new ArrayList<>(registry);
        }
    }
}