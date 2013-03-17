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

package com.hp.hpl.jena.sparql.pfunction.library;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArgType ;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionEval ;
import com.hp.hpl.jena.sparql.util.IterLib ;

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
        BindingMap b = BindingFactory.create(binding) ;

        Node subj = subject.getArg() ;
        if ( ! isSameOrVar(subj, arq) ) IterLib.noResults(execCxt) ;
        if ( subj.isVariable() )
            b.add(Var.alloc(subj), arq) ;

        Node obj = object.getArg() ;
        if ( ! isSameOrVar(obj, version) ) IterLib.noResults(execCxt) ;
        if ( obj.isVariable() )
            b.add(Var.alloc(obj), version) ;
        
        return IterLib.result(b, execCxt) ;
    }

    private boolean isSameOrVar(Node var, Node value)
    {
        return var.isVariable() || var.equals(value) ;
    }
}
