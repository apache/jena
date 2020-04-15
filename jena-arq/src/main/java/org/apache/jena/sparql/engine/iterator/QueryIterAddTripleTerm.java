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

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.serializer.SerializationContext;

/**
 * Add {@code (var, matchTriple)} to a {@link QueryIterator}.
 * <p>
 * The supplied triple is a triple pattern which is grounded by replacing variables
 * with terms from the current binding. It is an error not to have substitutions for
 * all variables and results in the original binding unchanged.
 */
class QueryIterAddTripleTerm extends QueryIterTriplePattern {
    private final Triple triple;
    private final Var    var;

    public QueryIterAddTripleTerm(QueryIterator chain, Var var, Triple triple, ExecutionContext execContext) {
        super(chain, triple, execContext);
        this.triple = triple;
        this.var = var;
    }

    @Override
    protected Binding moveToNextBinding() {
        Binding binding = super.moveToNextBinding();
        Triple matchedTriple  = Substitute.substitute(triple, binding);
        if ( ! matchedTriple.isConcrete() )
            // Not all concrete terms.
            return binding;
        Node nt = NodeFactory.createTripleNode(matchedTriple);
        Binding b = BindingFactory.binding(binding, var, nt);
        return b;
    }

    @Override
    protected void details(IndentedWriter out, SerializationContext sCxt) {
        out.print(this.getClass().getSimpleName()+": ["+var+"] " + triple);
    }
}
