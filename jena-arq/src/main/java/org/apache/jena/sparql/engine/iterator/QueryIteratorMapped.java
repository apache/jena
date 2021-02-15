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

package org.apache.jena.sparql.engine.iterator;

import java.util.Iterator;
import java.util.Map;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingBuilder;

/**
 * A query iterator which allows remapping variables to different names
 */
public class QueryIteratorMapped extends QueryIteratorWrapper {

    private Map<Var, Var> varMapping;

    /**
     * Creates a new iterator
     * <p>
     * If the {@code varMapping} is {@code null} then no variable re-mapping
     * will be applied and this will act as a simple wrapper over the underlying
     * wrapper
     * </p>
     *
     * @param qIter
     *            Iterator to wrap
     * @param varMapping
     *            Variable mapping
     */
    public QueryIteratorMapped(QueryIterator qIter, Map<Var, Var> varMapping) {
        super(qIter);
        this.varMapping = varMapping;
    }

    @Override
    protected Binding moveToNextBinding() {
        Binding b = super.moveToNextBinding();
        if (this.varMapping == null)
            return b;

        // Apply remapping
        BindingBuilder builder = Binding.builder();
        Iterator<Var> vs = b.vars();
        while (vs.hasNext()) {
            Var v = vs.next();
            Node value = b.get(v);

            // Only remap non-null variables for which there is a mapping
            if (value == null)
                continue;
            if (this.varMapping.containsKey(v)) {
                builder.add(this.varMapping.get(v), value);
            }
        }
        return builder.build();
    }
}
