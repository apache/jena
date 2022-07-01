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

package org.apache.jena.sparql.function.scripting;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.*;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.Pool;
import org.apache.jena.atlas.lib.PoolBase;
import org.apache.jena.atlas.lib.PoolSync;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprUndefFunction;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;
import org.apache.jena.sparql.sse.builders.SSE_ExprBuildException;

public class ScriptFunction extends FunctionBase {
	static {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
    }
	
    private static final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    private static final String ARQ_NS = "http://jena.apache.org/ARQ/";
    private static final String FUNCTION_SUFFIX = "Function";

    private static final Map<String, Pool<Invocable>> enginePools = new ConcurrentHashMap<>();

    private String lang;
    private String name;

    public static boolean isScriptFunction(String uri) {
        if (!uri.startsWith(ARQ_NS)) {
            return false;
        }
        String localPart = uri.substring(ARQ_NS.length());
        int separatorPos = localPart.indexOf('#');
        if (separatorPos < 0) {
            return false;
        }
        String langPart = localPart.substring(0, separatorPos);
        if (!langPart.endsWith(FUNCTION_SUFFIX)) {
            return false;
        }
        return true;
    }

    @Override
    public void checkBuild(String uri, ExprList args) {
        if (!isScriptFunction(uri)) {
            throw new SSE_ExprBuildException("Invalid URI: " + uri);
        }

        String localPart = uri.substring(ARQ_NS.length());
        int separatorPos = localPart.indexOf('#');
        this.lang = localPart.substring(0, separatorPos - FUNCTION_SUFFIX.length());
        this.name = localPart.substring(separatorPos + 1);
    }

    @Override
    public NodeValue exec(List<NodeValue> args) {
        Invocable engine = getEngine();

        try {
            Object[] params = args
                    .stream()
                    .map(NV::fromNodeValue)
                    .toArray();

            Object r;
            try {
                r = engine.invokeFunction(name, params);
            } catch (ScriptException e) {
                throw new ExprEvalException("Failed to evaluate " + lang + "function '" + name + "'", e);
            } catch (NoSuchMethodException e) {
                throw new ExprUndefFunction("No such " + lang + " function '" + name + "'", name);
            }

            if (r == null)
                // null is used used to signal an ExprEvalException.
                throw new ExprEvalException(name);
            return NV.toNodeValue(r);
        } finally {
            recycleEngine(engine);
        }
    }

    private Invocable getEngine() {
        Pool<Invocable> pool = enginePools.computeIfAbsent(lang, key -> PoolSync.create(new PoolBase<>()));
        Invocable engine = pool.get();
        if (engine == null) {
            engine = createEngine();
        }
        return engine;
    }

    private void recycleEngine(Invocable engine) {
        enginePools.get(lang).put(engine);
    }

    private Invocable createEngine() {
        ScriptEngine engine = scriptEngineManager.getEngineByName(lang);
        if (engine == null) {
            throw new SSE_ExprBuildException("Unknown scripting language: " + lang);
        }
        // Enforce Nashorn compatibility for Graal.js
        if (engine.getFactory().getEngineName().equals("Graal.js")) {
            engine.getContext().setAttribute("polyglot.js.nashorn-compat", true, ScriptContext.ENGINE_SCOPE);
        }

        if (!(engine instanceof Invocable)) {
            throw new SSE_ExprBuildException("Script engine  " + engine.getFactory().getEngineName() + " doesn't implement Invocable");
        }

        String functionLibFile = ARQ.getContext().getAsString(LanguageSymbols.scriptLibrary(lang));
        if (functionLibFile != null) {
            try (Reader reader = Files.newBufferedReader(Path.of(functionLibFile), StandardCharsets.UTF_8)) {
                engine.eval(reader);
            } catch (NoSuchFileException | FileNotFoundException ex) {
                throw new RiotNotFoundException("File: " + functionLibFile);
            } catch (IOException ex) {
                IO.exception(ex);
            } catch (ScriptException e) {
                throw new SSE_ExprBuildException("Failed to load " + lang + " library", e);
            }
        }

        String functions = ARQ.getContext().getAsString(LanguageSymbols.scriptFunctions(lang));
        if (functions != null) {
            try {
                engine.eval(functions);
            } catch (ScriptException e) {
                throw new SSE_ExprBuildException("Failed to load " + lang + " functions", e);
            }
        }

        Invocable invocable = (Invocable) engine;
        for (String name : engine.getFactory().getNames()) {
            try {
                invocable.invokeFunction("arq" + name + "init");
            } catch (NoSuchMethodException ignore) {
            } catch (ScriptException ex) {
                throw new SSE_ExprBuildException("Failed to call " + lang + " initialization function", ex);
            }
        }

        return invocable;
    }

    // For testing purposes only
    static void clearEngineCache() {
        enginePools.clear();
    }
}
