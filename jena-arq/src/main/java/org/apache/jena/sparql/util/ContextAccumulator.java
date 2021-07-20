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

package org.apache.jena.sparql.util;

import java.util.function.Supplier;

import org.apache.jena.query.ARQ;

public class ContextAccumulator {
    // Items added with "set(,)"
    private Context      addedContext       = new Context();
    // Explicitly given base context
    private Context      baseContext        = null;

    // Context when built
    private Context      builtContext        = null;

    public static ContextAccumulator newBuilder() {
        return new ContextAccumulator();
    }

    public static ContextAccumulator newBuilder(Supplier<Context> baseSupplier, Supplier<Context> extraSupplier) {
        // For use in implementation inheritance and delayed setting of base/extra.
        return new ContextAccumulator() {
            @Override
            protected Context baseContext() {
                if ( baseSupplier == null ) return null;
                return baseSupplier.get();
            }

            @Override
            protected Context extra() {
                if ( extraSupplier == null ) return null;
                return extraSupplier.get();
            }
        };
    }

    private ContextAccumulator() { }

    /** Extra setup from subclasses (e.g. dataset.getContext). Can be null. */
    protected Context extra() { return null; }

    /**
     * If no explicit base, this is the default. It will be copied to isolate it at the built point.
     * Default implement is to return ARQ.getContext().
     */
    protected Context baseContext() { return ARQ.getContext(); }

    public ContextAccumulator set(Symbol symbol, Object value) {
        update();
        addedContext.set(symbol, value);
        return this;
    }

    public ContextAccumulator set(Symbol symbol, boolean value) {
        update();
        addedContext.set(symbol, value);
        return this;
    }

    public ContextAccumulator context(Context context) {
        update();
        this.addedContext.clear();
        this.baseContext = context;
        return this;
    }

    /**
     * Build and return.
     * This will return the same object if called again with no intermediate updates.
     */
    protected Context context() {
        // Freeze and return.
        return getOrBuiltContext();
    }

    // If an update happens, ensure there is no cached build.
    private void update() {
        builtContext = null;
    }

    // Build once.
    private Context getOrBuiltContext() {
        if ( builtContext == null )
            builtContext = buildProcess();
        return builtContext;
    }

    private Context buildProcess() {
        // Build context:
        //   If a supplied context, use that.
        //   If defaulting, take from  dftContext (e.g. ARQ.getComtext) and local extras (e.g. dataset.getContext);
        // In each case, then add the local added settings.
        Context cxt;
        if ( baseContext != null ) {
            cxt = baseContext;
        } else {
            Context dftCxt = baseContext();
            cxt = ( dftCxt != null ) ? dftCxt.copy() : new Context();
            Context extra = extra();
            if ( extra != null )
                cxt.putAll(extra);
        }
        if ( addedContext != null )
            cxt.putAll(addedContext);
        return cxt;
    }
}
