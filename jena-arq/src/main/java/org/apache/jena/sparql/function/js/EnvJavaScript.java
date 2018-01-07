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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

import javax.script.*;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.sse.builders.ExprBuildException;
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
    /** JavaScript as a string value which is evaluated */ 
    public static Symbol symJavaScriptLib = SystemARQ.allocSymbol("js-functions");
    /** JavaScript library in a file as named */ 
    public static Symbol symJavaScriptLibFile = SystemARQ.allocSymbol("js-library");
    
    private final String scriptLib;
    private final String scriptLibFile;
    
    private final ScriptEngine scriptEngine;
    private CompiledScript compiledScript;
    
    private final Invocable invoc;

    public static EnvJavaScript create(Context context) {
        return new EnvJavaScript(context);
    }
    
    private static EnvJavaScript global = null;
    
    /** Return */
    public static EnvJavaScript get() { 
        if ( global == null ) {
            Context context = ARQ.getContext();
            if ( context.isDefined(symJavaScriptLib) || context.isDefined(symJavaScriptLibFile) )
                global = create(ARQ.getContext());
        }
        return global ;
    }
    
    /** Reset the global EnvJavaScript */
    public static void reset() {
        reset(ARQ.getContext());
    }

    /** Reset the global EnvJavaScript */
    public static void reset(Context context) {
        global = create(context);
    }
    
    private EnvJavaScript(Context context) {
        this.scriptLib = context.getAsString(symJavaScriptLib);
        this.scriptLibFile = context.getAsString(symJavaScriptLibFile);
        if ( this.scriptLib == null && this.scriptLibFile == null )
            throw new ARQException("Both script string and script filename are null"); 
        ScriptEngineManager manager = new ScriptEngineManager();
        scriptEngine = manager.getEngineByName("nashorn");
        // Add function to script engine.
        invoc = (Invocable)scriptEngine;
        if ( scriptLibFile != null ) {
            try {
                Reader reader = Files.newBufferedReader(Paths.get(scriptLibFile), StandardCharsets.UTF_8);
                Object x = scriptEngine.eval(reader);
            }
            catch (NoSuchFileException | FileNotFoundException ex) {
                throw new RiotNotFoundException("File: "+scriptLibFile);
            }
            catch (IOException ex) { IO.exception(ex); }
            catch (ScriptException e) {
                throw new ExprBuildException("Failed to load Javascript", e);
            }
        }
        if ( scriptLib != null ) {
            try {
                Object x = scriptEngine.eval(scriptLib);
            }
            catch (ScriptException e) {
                throw new ExprBuildException("Failed to load Javascript", e);
            }
        }
        // Try to call the init function - ignore NoSuchMethodException 
        try {
            invoc.invokeFunction(ARQConstants.JavaScriptInitFunction);
        } catch (NoSuchMethodException ex) {}
        catch (ScriptException ex) {
            throw new ARQException("Failed to call JavaScript initialization function", ex);
        }
    }

    public Invocable invoc() {
        return invoc;
    }
}
