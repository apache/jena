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

package org.apache.jena.sparql.service.enhancer.impl.util;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.io.PrintUtils;
import org.apache.jena.atlas.iterator.IteratorSlotted;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.QueryOutputUtils;

/**
 * QueryIterator implementation based on IteratorSlotted.
 * Its purpose is to ease wrapping a non-QueryIterator as one based
 * on a {@link #moveToNext()} method analogous to guava's AbstractIterator.
 */
public abstract class QueryIterSlottedBase
    extends IteratorSlotted<Binding>
    implements QueryIterator
{
    @Override
    public Binding nextBinding() {
        Binding result = isFinished()
                ? null
                : next();
        return result;
    }

    @Override
    protected boolean hasMore() {
        return !isFinished();
    }

    @Override
    public String toString(PrefixMapping pmap)
    { return QueryOutputUtils.toString(this, pmap) ; }

    // final stops it being overridden and missing the output() route.
    @Override
    public final String toString()
    { return PrintUtils.toString(this) ; }

    /** Normally overridden for better information */
    @Override
    public void output(IndentedWriter out)
    {
        out.print(Plan.startMarker) ;
        out.print(Lib.className(this)) ;
        out.print(Plan.finishMarker) ;
    }

    @Override
    public void cancel() {
        close();
    }

    @Override
    public void output(IndentedWriter out, SerializationContext sCxt) {
        output(out);
//	        out.println(Lib.className(this) + "/" + Lib.className(iterator));
//	        out.incIndent();
//	        // iterator.output(out, sCxt);
//	        out.decIndent();
//	        // out.println(Utils.className(this)+"/"+Utils.className(iterator)) ;
    }
}
