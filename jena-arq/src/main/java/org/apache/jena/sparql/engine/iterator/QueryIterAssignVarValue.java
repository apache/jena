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

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.QueryExecException ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingFactory ;
import org.apache.jena.sparql.serializer.SerializationContext ;
import org.apache.jena.sparql.util.FmtUtils ;

/** Extend each solution by a (var, node) 
 *  When used with mustBeNewVar=false, this is a join.
 *  If the input already has the variable assigned, then
 *  it must be the same (.equals) node and if it is not,
 *  the input row is rejected.
 *  @see QueryIterAssign
 *  @see QueryIterExtendByVar
 */ 

public class QueryIterAssignVarValue extends QueryIterProcessBinding {
    private final Var     var;
    private final Node    node;
    private final boolean mustBeNewVar;

    public QueryIterAssignVarValue(QueryIterator input, Var var, Node node, ExecutionContext qCxt) {
        this(input, var, node, qCxt, false);
    }

    public QueryIterAssignVarValue(QueryIterator input, Var var, Node node, ExecutionContext qCxt, boolean mustBeNewVar) {
        super(input, qCxt);
        this.var = var;
        this.node = node;
        this.mustBeNewVar = mustBeNewVar;
    }

    @Override
    public Binding accept(Binding binding) {
        if ( binding.contains(var) ) {
            if ( mustBeNewVar )
                throw new QueryExecException("Already set: " + var);

            Node n2 = binding.get(var) ;
            if ( ! n2.equals(node) )
                // And filter out.
                return null ;
            else
                // Already in the binding with the value -> nothing to do. 
                return binding ;
        }
        return BindingFactory.binding(binding, var, node) ;
    }
    
    @Override
    protected void details(IndentedWriter out, SerializationContext cxt) { 
        out.print(Lib.className(this)) ;
        out.print(" ?"+var.toString()+" = "+FmtUtils.stringForNode(node, cxt)) ;
    }
}
