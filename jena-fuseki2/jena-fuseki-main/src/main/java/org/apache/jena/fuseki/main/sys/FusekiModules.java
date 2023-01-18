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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.jena.base.module.Subsystem;

/** Registry of modules */
public class FusekiModules {

    private static Object lock = new Object();

    // Record of what is loaded.
    private static List<FusekiModule> registry = null;

    private static Subsystem<FusekiModule> subsystem = null;

    public static void load() {
        if ( registry == null )
            reload();
    }

    public static void reload() {
        registry = new ArrayList<>();
        subsystem = new Subsystem<FusekiModule>(FusekiModule.class);
        subsystem.initialize();
        synchronized(lock) {
            subsystem.forEach(registry::add);
        }
    }

    /** Add a code module */
    public static void add(FusekiModule module) {
        synchronized(lock) {
            load();
            module.start();
            registry.add(module);
        }
    }

    /** Remove a code module */
    public static void remove(FusekiModule module) {
        synchronized(lock) {
            registry.remove(module);
            module.stop();
        }
    }

    /** Test whether a code module is registered. */
    public static boolean contains(FusekiModule module) {
        synchronized(lock) {
            return registry.contains(module);
        }
    }

    /*package*/ static void forEachModule(Consumer<FusekiModule> action) {
        synchronized(lock) {
            if ( registry == null )
                load();
            if ( registry == null || registry.isEmpty() )
                return ;
            registry.forEach(action);
        }
    }
}
