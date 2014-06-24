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

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import com.hp.hpl.jena.sparql.mgt.SystemInfo ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArgType ;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionEval ;

/** Access the subsystem version registry and yield URI/version for each entry */ 
public class version extends PropertyFunctionEval
{
    public version()
    { super(PropFuncArgType.PF_ARG_SINGLE, PropFuncArgType.PF_ARG_SINGLE) ; }
    
    @Override
    public QueryIterator execEvaluated(Binding binding, PropFuncArg subject, Node predicate, PropFuncArg object, ExecutionContext execCxt)
    {
        
        List<Binding> results = new ArrayList<>() ;
        Node subj = subject.getArg() ;
        Node obj = object.getArg() ;
        
        Iterator<SystemInfo> iter = SystemARQ.registeredSubsystems() ;
        
        for ( ; iter.hasNext() ; )
        {
            SystemInfo info = iter.next();
            if ( ! isSameOrVar(subj, info.getIRI()) )
                continue ;
            Node version = NodeFactory.createLiteral(info.getVersion()) ;
            if ( ! isSameOrVar(obj, version) ) 
                continue ;
            
            BindingMap b = BindingFactory.create(binding) ;
            if ( subj.isVariable() )
                b.add(Var.alloc(subj), info.getIRI()) ;
            if ( subj.isVariable() )
                b.add(Var.alloc(obj), version) ;
            results.add(b) ;
        }
        return new QueryIterPlainWrapper(results.iterator(), execCxt) ;
    }

    private boolean isSameOrVar(Node var, Node value)
    {
        return var.isVariable() || var.equals(value) ;
    }
}
