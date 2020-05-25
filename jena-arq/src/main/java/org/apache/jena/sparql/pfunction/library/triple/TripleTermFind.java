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

package org.apache.jena.sparql.pfunction.library.triple;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.engine.iterator.RX;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.pfunction.*;

/** Property function for FIND() - allows already bound.
 * <pre>
 *    << ?s ?p ?o >> apf:find ?t .
 * <pre>
 * This binds all the variables, with <tt>?t</tt> bound to a triple term for the match of <tt>?s ?p ?o</tt>. 
 */ 
public class TripleTermFind extends PropertyFunctionEval {

    static public void init() {
        PropertyFunctionFactory factory = (uri)->new TripleTermFind();
        Node uri = NodeFactory.createURI("http://arq/find");
        PropertyFunctionRegistry.get().put(uri.getURI(), factory);
    }

    public TripleTermFind() {
        super(PropFuncArgType.PF_ARG_SINGLE, PropFuncArgType.PF_ARG_SINGLE);
    }

    @Override
    public QueryIterator execEvaluated(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {

        Node sArg = argSubject.getArg();
        if ( ! sArg.isNodeTriple() )
            throw new ExprEvalException("Not a triple term: "+sArg);
        Triple triple = Node_Triple.triple(sArg);

        Node nAssign = argObject.getArg();
        if ( ! Var.isVar(nAssign) ) {
            if ( ! nAssign.isNodeTriple() )
                return QueryIterNullIterator.create(execCxt); 
            Triple t2 = Node_Triple.triple(nAssign);
            if ( t2.equals(triple) )
                return QueryIterSingleton.create(binding, execCxt);
            return QueryIterNullIterator.create(execCxt);
        }
        
        Var var = Var.alloc(nAssign);

        QueryIterator input = QueryIterSingleton.create(binding, execCxt);

        // This matches the triple inside the Node_Triple, recursively,
        // and adds the binding for "(<< >> AS ?t)".

        QueryIterator qIter = RX.matchTripleStar(input, var, triple, execCxt);
        return qIter;
    }
}
