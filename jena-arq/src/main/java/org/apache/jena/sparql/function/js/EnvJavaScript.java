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

package org.apache.jena.sparql.function.js;

import javax.script.ScriptException;

import org.apache.jena.atlas.lib.Pool;
import org.apache.jena.atlas.lib.PoolBase;
import org.apache.jena.atlas.lib.PoolSync;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/** Environment for executing a JavaScript function.
 * <p>
 * Functions are loaded from the file named in context setting
 * {@link EnvJavaScript#symJavaScriptLibFile}.
 * <p>
 * Function are loaded from a string value in context setting
 * {@link EnvJavaScript#symJavaScriptLib}.
 * <p>
 * If both are present, the file named by {@code EnvJavaScript.symJavaScriptLibFile} is loaded
 * then the string from {@code EnvJavaScript.symJavaScriptLib}.
 */
public class EnvJavaScript {
    /** JavaScript functions as a string value which is evaluated. */ 
    public static Symbol symJavaScriptLib = SystemARQ.allocSymbol("js-functions");
    /** JavaScript library of functions in a file. */ 
    public static Symbol symJavaScriptLibFile = SystemARQ.allocSymbol("js-library");
    
    private final String scriptLib;
    private final String scriptLibFile;
    
    public static EnvJavaScript create(Context context) {
        return new EnvJavaScript(context);
    }
    
    private static EnvJavaScript global = null;
    
    /**
     * Return the global {@code EnvJavaScript}. 
     * Returns null if no JavaScript has been provided.
     */
    public static EnvJavaScript get() {
        if ( global == null ) {
            synchronized(EnvJavaScript.class) {
                Context context = ARQ.getContext();
                if ( context.isDefined(symJavaScriptLib) || context.isDefined(symJavaScriptLibFile) )
                    global = create(ARQ.getContext());
            }
        }
        return global ;
    }

    /** Reset the global {@code EnvJavaScript} based on the system-wide context */
    public static void reset() {
        reset(ARQ.getContext());
    }

    /** Reset the global {@code EnvJavaScript} */
    public static void reset(Context context) {
        global = create(context);
    }
    
    // ---- EnvJavaScript Object

    // One script engine per thread, here done by one per usage.
    // Nashorn script engines are thread safe but the script Bindings} must not be shared.
    // Direct use of Nashorn, via the protected APIs of jdk.nashorn.api.scripting
    // are needed to utilize this and having one compiled form and many execution units.
    // But in Java8 is saving up problems for Java9 and the 
    // Nashorn subsystem is imporved at Java9 for this use case.
    // For now, in combination with the implementation of JSEngine,
    // we keep separate Nashorn script engines. 
    
    private Pool<JSEngine> pool = PoolSync.create(new PoolBase<JSEngine>());
    
    private EnvJavaScript(Context context) {
        this.scriptLib = context.getAsString(symJavaScriptLib);
        this.scriptLibFile = context.getAsString(symJavaScriptLibFile);
        // Put one in the pool.
        pool.put(build());
    }
    
    private JSEngine build() {
        return new JSEngine(scriptLib, scriptLibFile);
    }
    
    private JSEngine getEngine() {
        JSEngine engine = pool.get();
        if ( engine == null )
            // Which will go into the pool when finished with. 
            engine = new JSEngine(scriptLib, scriptLibFile);
        return engine;
    }

    private EnvJavaScript(String functions, String functionLibFile) { 
        this.scriptLib = functions;
        this.scriptLibFile = functionLibFile;
    }

    public Object call(String functionName, Object[] args) throws NoSuchMethodException, ScriptException {
        JSEngine engine = getEngine();
        try {
            return engine.call(functionName, args);
        } finally {
            pool.put(engine);    
        }
    }

// ---- ThreadLocal version,
//    private ThreadLocal<JSEngine> invocable = ThreadLocal.withInitial(()->build());
//    
//    private JSEngine build() {
//        return new JSEngine(scriptLib, scriptLibFile);
//    } 
//    public Object call(String functionName, Object[] args) throws NoSuchMethodException, ScriptException {
//        return invocable.get().call(functionName, args);
//    }
    
}
