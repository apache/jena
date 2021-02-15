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

package org.apache.jena.sparql.pfunction.library;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.pfunction.PropFuncArg ;
import org.apache.jena.sparql.pfunction.PropFuncArgType ;
import org.apache.jena.sparql.pfunction.PropertyFunctionEval ;
import org.apache.jena.sparql.util.IterLib ;

public class versionARQ extends PropertyFunctionEval
{
    static String versionStr = ARQ.VERSION ;    // X.Y.Z

    static Node version = NodeValue.makeString(versionStr).asNode() ;

    static Node arq = NodeFactory.createURI(ARQ.arqIRI) ;

    public versionARQ()
    { super(PropFuncArgType.PF_ARG_SINGLE, PropFuncArgType.PF_ARG_SINGLE) ; }

    @Override
    public QueryIterator execEvaluated(Binding binding, PropFuncArg subject, Node predicate, PropFuncArg object, ExecutionContext execCxt)
    {
        BindingBuilder builder = Binding.builder(binding) ;

        Node subj = subject.getArg() ;
        if ( ! isSameOrVar(subj, arq) )
            IterLib.noResults(execCxt) ;
        if ( subj.isVariable() )
            builder.add(Var.alloc(subj), arq) ;

        Node obj = object.getArg() ;
        if ( ! isSameOrVar(obj, version) )
            IterLib.noResults(execCxt) ;
        if ( obj.isVariable() )
            builder.add(Var.alloc(obj), version) ;

        return IterLib.result(builder.build(), execCxt) ;
    }

    private boolean isSameOrVar(Node var, Node value)
    {
        return var.isVariable() || var.equals(value) ;
    }
}
