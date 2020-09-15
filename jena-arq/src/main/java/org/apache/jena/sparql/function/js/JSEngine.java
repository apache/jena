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
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.sse.builders.ExprBuildException;

/** Abstraction of a <em>per-thread</em> JavaScript execution system */  
public class JSEngine {
    private final Invocable invoc;


    /** Create a {@code JSEngine} from a string. */ 
    public static JSEngine createFromString(String functions) {
        return new JSEngine(functions, null);
    }

    /** Create a {@code JSEngine} from the contents of a file. */ 
    public static JSEngine createFromFile(String functionLibFile) {
        return new JSEngine(null, functionLibFile);
    }
    
    /*package*/ JSEngine(String functions, String functionLibFile) {
        invoc = build(functions, functionLibFile);
    }

    private static Invocable build(String functions, String functionLibFile) {
        if ( functions == null && functionLibFile == null )
            throw new ARQException("Both script string and script filename are null");

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine scriptEngine = manager.getEngineByName("javascript");
        if (scriptEngine == null) {
            throw new ARQException("Could not load JavaScript script engine. " +
                    "Make sure that org.graalvm.js:js and org.graalvm.js:js-scriptengine are added to the class path");
        }

        if (scriptEngine.getFactory().getEngineName().equals("Graal.js")) {
            scriptEngine.getContext().setAttribute("polyglot.js.nashorn-compat", true, ScriptContext.ENGINE_SCOPE);
        } else if (scriptEngine.getFactory().getNames().contains("Nashorn")) {
            Log.warn(JSEngine.class, "Nashorn will be permanently removed in JDK 15. Consider switching to Graal VM.");
        }

        Invocable invoc = (Invocable)scriptEngine;
        if ( functionLibFile != null ) {
            try (Reader reader = Files.newBufferedReader(Paths.get(functionLibFile), StandardCharsets.UTF_8)) {
                scriptEngine.eval(reader);
            }
            catch (NoSuchFileException | FileNotFoundException ex) {
                throw new RiotNotFoundException("File: "+functionLibFile);
            }
            catch (IOException ex) { IO.exception(ex); }
            catch (ScriptException e) {
                throw new ExprBuildException("Failed to load Javascript", e);
            }
        }
        if ( functions != null ) {
            try {
                scriptEngine.eval(functions);
            }
            catch (ScriptException e) {
                throw new ExprBuildException("Failed to load Javascript", e);
            }
        }
        
        // Try to call the init function - ignore NoSuchMethodException 
        try {
            invoc.invokeFunction(ARQConstants.JavaScriptInitFunction);
        } catch (NoSuchMethodException ignore) {}
        catch (ScriptException ex) {
            throw new ARQException("Failed to call JavaScript initialization function", ex);
        }
        return invoc;
    }

    public Object call(String functionName, Object[] args) throws NoSuchMethodException, ScriptException {
        return invoc.invokeFunction(functionName, args);
    }
}
