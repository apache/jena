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

package org.apache.jena.fuseki.main.sys;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * List of {@linkplain FusekiModule Fuseki modules}.
 * This is the immutable collection of modules for a server.
 * <p>
 * @see FusekiModulesLoaded
 */
public class FusekiModules {

    /** A Fuseki module with no members. */
    public static final FusekiModules empty = FusekiModules.create();

    /** Create a collection of Fuseki modules */
    public static FusekiModules create(FusekiModule ... modules) {
        return new FusekiModules(modules);
    }

    /** Create a collection of Fuseki modules */
    public static FusekiModules create(List<FusekiModule> modules) {
        return new FusekiModules(modules);
    }

    private final List<FusekiModule> modules;

    private FusekiModules(FusekiModule ... modules) {
        this.modules = List.of(Objects.requireNonNull(modules));
    }

    private FusekiModules(List<FusekiModule> modules) {
        this.modules = List.copyOf(Objects.requireNonNull(modules));
    }

    /**
     * Return an immutable list of modules.
     */
    public List<FusekiModule> asList() {
        return List.copyOf(modules);
    }

    /**
     * Apply an action to each module, in order, one at a time.
     */
    public void forEach(Consumer<FusekiModule> action) {
        modules.forEach(action);
    }

    /** Test whether a code module is registered. */
    public boolean contains(FusekiModule module) {
        return modules.contains(module);
    }
}
