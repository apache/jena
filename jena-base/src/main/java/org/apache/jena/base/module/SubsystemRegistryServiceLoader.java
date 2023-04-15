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
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.logging.Log;

/**
 * Implementation of {@link SubsystemRegistry} that uses {@link ServiceLoader} to find sub-systems.
 * <p>
 * In unnamed modules, the {@link ServiceLoader} used to use {@code META-INF/service/interface-name.}
 * <p>
 * With modules, the application {@code module-info.java} contains:
 * <pre>
 * module com.example.app {
 *     requires java.sql;
 *      uses java.sql.Driver;
 * }
 * </pre>
 * and the provider {@code module-info.java} contains:
 * <pre>
 * module com.example.jdbc {
 *     requires java.sql;
 *     provides java.sql.Driver
 *         with com.example.jdbc.MyDriver;
 * }
 * </pre>
 * <p>
 * With classpath, not module path and automatic modules, the application (here, Jena initialization)
 * need not do anything, and the automatic modules implicitly provide an interface.
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
            ServiceLoader<T> sl;
            try {
                // Use this->classloader form : better for OSGi
                sl = ServiceLoader.load(moduleClass, this.getClass().getClassLoader());
            } catch (ServiceConfigurationError ex) {
                Log.error(this, "Problem with service loading for "+moduleClass.getName(), ex);
                throw ex;
            }

            sl.stream().forEach(provider->{
                try {
                    T module = provider.get();
                    this.add(module);
                } catch (ServiceConfigurationError ex) {
                    FmtLog.error(this.getClass(), ex,
                                 "Error instantiating class %s for %s", provider.type().getName(), moduleClass.getName());
                    throw ex;
                }
            });
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