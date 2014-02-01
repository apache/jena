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

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;

/**
 * A query iterator which allows remapping variables to different names
 * 
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
        BindingMap binding = BindingFactory.create();
        Iterator<Var> vs = b.vars();
        while (vs.hasNext()) {
            Var v = vs.next();
            Node value = b.get(v);

            // Only remap non-null variables for which there is a mapping
            if (value == null)
                continue;
            if (this.varMapping.containsKey(v)) {
                binding.add(this.varMapping.get(v), value);
            }
        }
        return binding;
    }

}
