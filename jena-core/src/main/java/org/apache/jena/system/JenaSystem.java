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

package org.apache.jena.system;

import java.util.Collections ;
import java.util.Comparator ;
import java.util.List ;
import java.util.function.Consumer ;

import org.apache.log4j.Logger ;

/** Jena "system" - simple controls for ensuring components are loaded and initialized.
 * <p>
 * All initialization should be concurrent and thread-safe.  In particular,
 * some subsystems need initialization some sort of order (e.g. ARQ before TDB).
 * <p>
 * This is achieved by "levels": levels less than 100 are considered "jena system levels" 
 * and are reserved. 
 * 
 * <li>0 - here
 * <li>10 - jena-core
 * <li>20 - RIOT
 * <li>30 - ARQ
 * <li>40 - TDB
 * <li>9999 - other
 */
public class JenaSystem {
    
    public static final boolean DEBUG_INIT = false ;
    
    // A correct way to manage without synchonized using the double checked locking pattern.
    //   http://en.wikipedia.org/wiki/Double-checked_locking
    //   http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html 
    private static volatile boolean initialized = false ;
    private static Object initLock = new Object() ;
    
    /** Initialize.
     * This function is cheap to call when already initialized so can be called to be sure.
     * By default, initialization happens by using {@code ServiceLoader.load} to find
     * {@link JenaSubsystemLifecycle} objects.
     */
    public static void init() {
        if ( initialized )
            return ;
        synchronized(initLock) {
            if ( initialized )  {
                if ( DEBUG_INIT )
                    System.err.println("JenaSystem.init - return");
                return ;
            } 
            // Catchs recursive calls, same thread.
            initialized = true ;
            if ( DEBUG_INIT )
                System.err.println("JenaSystem.init - start");
            
            if ( get() == null )
                set(new JenaSubsystemRegistryBasic()) ;
            
            get().load() ;
            
            JenaSystem.forEach( module -> {
                if ( DEBUG_INIT )
                    System.err.println("Init: "+module.getClass().getSimpleName());
                module.start() ;
            }) ;
            if ( DEBUG_INIT )
                System.err.println("JenaSystem.init - finish");
        }
    }

    /** Shutdown subsystems */
    public static void shutdown() {
        if ( ! initialized ) 
            return ;
        synchronized(initLock) {
            JenaSystem.forEachReverse(JenaSubsystemLifecycle::stop) ;
            initialized = false ;
        }
    }
    
    private static class JenaInitLogger implements JenaSubsystemLifecycle {
        private static Logger log = Logger.getLogger("Jena") ; 
        @Override
        public void start() {
            log.info/*debug*/("Jena intialization");
        }

        @Override
        public void stop() {
            log.debug("Jena shutdown");
        }

        @Override
        public int level() {
            return 0;
        }
    }
    
    private static JenaSubsystemRegistry singleton = null;

    public static void set(JenaSubsystemRegistry thing) {
        singleton = thing;
    }

    public static JenaSubsystemRegistry get() {
        return singleton;
    }
    
    private static Comparator<JenaSubsystemLifecycle> comparator = (obj1, obj2) -> Integer.compare(obj1.level(), obj2.level()) ;
    private static Comparator<JenaSubsystemLifecycle> reverseComparator = (obj1, obj2) -> -1 * Integer.compare(obj1.level(), obj2.level()) ;
    
    public static void add(JenaSubsystemLifecycle module) {
       get().add(module) ;
    }

    public static boolean isRegistered(JenaSubsystemLifecycle module) {
        return get().isRegistered(module);
    }

    public static void remove(JenaSubsystemLifecycle module) {
        get().remove(module);
    }

    public static int size() {
        return get().size();
    }

    public static boolean isEmpty() {
        return get().isEmpty();
    }

    /** Call an action on each item in the registry.
     * Calls are made sequentially and in level order.
     * The exact order within a level is not specified; it is not registration order. 
     * @param action
     */
    public static void forEach(Consumer<JenaSubsystemLifecycle> action) {
        forEach(action, comparator);
    }

    /** Call an action on each item in the registry but in the reverse enumeration order.
     * The exact order is not specified but call are made sequentially.
     * The "reverse" is opposite order to {@link #forEach}, which may not be stable.
     * It is not related to registration order.
     * @param action
     */
    public static void forEachReverse(Consumer<JenaSubsystemLifecycle> action) {
        forEach(action, reverseComparator);
    }

    private synchronized static void forEach(Consumer<JenaSubsystemLifecycle> action, Comparator<JenaSubsystemLifecycle> ordering) {
        List<JenaSubsystemLifecycle> x = get().snapshot() ;
        Collections.sort(x, ordering);
        x.forEach(action);
    }


}

