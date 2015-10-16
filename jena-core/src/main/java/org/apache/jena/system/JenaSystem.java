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

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Jena "system" - simple controls for ensuring components are loaded and initialized.
 * <p>
 * All initialization should be concurrent and thread-safe.  In particular,
 * some subsystems need initialization some sort of order (e.g. ARQ before TDB).
 * <p>
 * This is achieved by "levels": levels less than 100 are considered "jena system levels" 
 * and are reserved. 
 * <ul>
 * <li>0 - reserved
 * <li>10 - jena-core
 * <li>20 - RIOT
 * <li>30 - ARQ
 * <li>40 - TDB
 * <li>9999 - other
 * </ul>
 * See also the <a href="http://jena.apache.org/documentation/notes/system-initialization.html">notes on Jena initialization</a>.
 */
public class JenaSystem {

    /** Development support - flag to enable output during
     * initialization. Output to {@code System.err}, not a logger
     * to avoid the risk of recursive initialization.   
     */
    public static boolean DEBUG_INIT = false ;
    
    // A correct way to manage without synchonized using the double checked locking pattern.
    //   http://en.wikipedia.org/wiki/Double-checked_locking
    //   http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html 
    private static volatile boolean initialized = false ;
    private static Object initLock = new Object() ;
    
    /** Initialize Jena.
     * <p>
     * This function is cheap to call when already initialized so can be called to be sure.
     * A commonly used idom in jena is in static initailizers in key classes.
     * <p> 
     * By default, initialization happens by using {@code ServiceLoader.load} to find
     * {@link JenaSubsystemLifecycle} objects.
     * See {@link #setSubsystemRegistry} to intercept that choice.
     */
    public static void init() {
        // Any other thread attempting to initialize as well will
        // first test the volatile outside the lock; if it's 
        // not INITIALIZED, the thread will attempt to grab the lock
        // and hence wait, then see initialized as true.

        // But we need to cope with recursive calls of JenaSystem.init() as well.
        // The same thread will not stop at the lock.
        // Set initialized to true before a recursive call is possible
        // handles this.  The recursive call will see initialized true and
        // and returnn on the first test.

        // Net effect:
        // After a top level call of JenaSystem.init() returns, tjena has
        // finishes initialization.
        // Recursive calls do not have this property.

        if ( initialized )
            return ;
        synchronized(initLock) {
            if ( initialized )  {
                if ( DEBUG_INIT )
                    System.err.println("JenaSystem.init - return");
                return ;
            } 
            // Catches recursive calls, same thread.
            initialized = true ;
            if ( DEBUG_INIT )
                System.err.println("JenaSystem.init - start");
            
            if ( get() == null )
                setSubsystemRegistry(new JenaSubsystemRegistryBasic()) ;
            
            get().load() ;
            
            // Debug : what did we find?
            if ( JenaSystem.DEBUG_INIT ) {
                get().snapshot().forEach(mod->
                    System.err.println("  "+mod.getClass().getSimpleName())) ;
            }
            get().add(new JenaInitLevel0()) ;

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
    
    private static JenaSubsystemRegistry singleton = null;

    /**
     * Set the {@link JenaSubsystemRegistry}.
     * To have any effect, this function
     * must be called before any other Jena code,
     * and especially before calling {@code JenaSystem.init()}.
     */
    public static void setSubsystemRegistry(JenaSubsystemRegistry thing) {
        singleton = thing;
    }

    /** The current JenaSubsystemRegistry */
    public static JenaSubsystemRegistry get() {
        return singleton;
    }

    /**
     * Call an action on each item in the registry. Calls are made sequentially
     * and in increasing level order. The exact order within a level is not
     * specified; it is not registration order.
     * 
     * @param action
     */
    public static void forEach(Consumer<JenaSubsystemLifecycle> action) {
        forEach(action, comparator);
    }

    /**
     * Call an action on each item in the registry but in the reverse
     * enumeration order. Calls are made sequentially and in decreasing level
     * order. The "reverse" is opposite order to {@link #forEach}, which may not
     * be stable within a level. It is not related to registration order.
     * 
     * @param action
     */
    public static void forEachReverse(Consumer<JenaSubsystemLifecycle> action) {
        forEach(action, reverseComparator);
    }

    // Order by level (increasing)
    private static Comparator<JenaSubsystemLifecycle> comparator = (obj1, obj2) -> Integer.compare(obj1.level(), obj2.level()) ;
    // Order by level (decreasing)
    private static Comparator<JenaSubsystemLifecycle> reverseComparator = (obj1, obj2) -> -1 * Integer.compare(obj1.level(), obj2.level()) ;

    private synchronized static void forEach(Consumer<JenaSubsystemLifecycle> action, Comparator<JenaSubsystemLifecycle> ordering) {
        List<JenaSubsystemLifecycle> x = get().snapshot() ;
        Collections.sort(x, ordering);
        x.forEach(action);
    }

    /** The level 0 subsystem - inserted without using the Registry load function. 
     *  There should be only one such level 0 handler. 
     */
    private static class JenaInitLevel0 implements JenaSubsystemLifecycle {
        private static Logger log = LoggerFactory.getLogger("Jena") ; 
        @Override
        public void start() {
            log.debug("Jena initialization");
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
}
