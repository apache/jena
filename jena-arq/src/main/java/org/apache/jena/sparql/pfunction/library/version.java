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

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.sparql.SystemARQ ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import org.apache.jena.sparql.mgt.SystemInfo ;
import org.apache.jena.sparql.pfunction.PropFuncArg ;
import org.apache.jena.sparql.pfunction.PropFuncArgType ;
import org.apache.jena.sparql.pfunction.PropertyFunctionEval ;

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

            BindingBuilder builder = Binding.builder(binding) ;
            if ( subj.isVariable() )
                builder.add(Var.alloc(subj), info.getIRI()) ;
            if ( subj.isVariable() )
                builder.add(Var.alloc(obj), version) ;
            results.add(builder.build()) ;
        }
        return QueryIterPlainWrapper.create(results.iterator(), execCxt) ;
    }

    private boolean isSameOrVar(Node var, Node value)
    {
        return var.isVariable() || var.equals(value) ;
    }
}
