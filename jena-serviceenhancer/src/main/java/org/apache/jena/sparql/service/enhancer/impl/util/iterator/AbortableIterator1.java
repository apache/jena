/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.service.enhancer.impl.util.iterator;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.sparql.serializer.SerializationContext;

/** Base class for abortable iterators that are based on an input one. */
public abstract class AbortableIterator1<I, O>
    extends AbortableIteratorBase<O>
{
    private AbortableIterator<I> input;

    public AbortableIterator1(AbortableIterator<I> input) {
        super();
        this.input = input;
    }

    protected AbortableIterator<I> getInput() { return input ; }

    @Override
    protected final void closeIterator() {
        closeSubIterator();
        performClose(input);
        input = null;
    }

    @Override
    protected final void requestCancel() {
        requestSubCancel();
        performRequestCancel(input);
    }

    /** Cancellation of the query execution is happening */
    protected abstract void requestSubCancel();

    /**
     * Pass on the close method - no need to close the QueryIterator passed to the
     * QueryIter1 constructor
     */
    protected abstract void closeSubIterator();

    @Override
    public void output(IndentedWriter out) {
        output(out, null);
    }

    // Do better
    @Override
    public void output(IndentedWriter out, SerializationContext sCxt) {
        // Linear form.
        if ( getInput() != null )
            // Closed
            getInput().output(out, sCxt);
        else
            out.println("Closed");
        out.ensureStartOfLine();
        details(out, sCxt);
        out.ensureStartOfLine();

//        details(out, sCxt) ;
//        out.ensureStartOfLine() ;
//        out.incIndent() ;
//        getInput().output(out, sCxt) ;
//        out.decIndent() ;
//        out.ensureStartOfLine() ;
    }

    protected void details(IndentedWriter out, SerializationContext sCxt) {
        out.println(Lib.className(this));
    }
}
