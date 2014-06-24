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

package com.hp.hpl.jena.sparql.engine.binding ;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;

/** Common framework for projection; 
 * the projection policy is provided by
 * abstract method {@linkplain #accept(Var)} 
 */
public abstract class BindingProjectBase extends BindingBase {
    private List<Var>     actualVars = null ;
    private final Binding binding ;

    public BindingProjectBase(Binding bind) {
        super(null) ;
        binding = bind ;
    }

    protected abstract boolean accept(Var var) ;

    @Override
    protected boolean contains1(Var var) {
        return accept(var) && binding.contains(var) ;
    }

    @Override
    protected Node get1(Var var) {
        if ( !accept(var) )
            return null ;
        return binding.get(var) ;
    }

    @Override
    protected Iterator<Var> vars1() {
        return actualVars().iterator() ;
    }

    private List<Var> actualVars() {
        if ( actualVars == null ) {
            actualVars = new ArrayList<>() ;
            Iterator<Var> iter = binding.vars() ;
            for ( ; iter.hasNext() ; ) {
                Var v = iter.next() ;
                if ( accept(v) )
                    actualVars.add(v) ;
            }
        }
        return actualVars ;
    }

    @Override
    protected int size1() {
        return actualVars().size() ;
    }

    // NB is the projection and the binding don't overlap it is also empty.
    @Override
    protected boolean isEmpty1() {
        if ( binding.isEmpty() )
            return true ;
        if ( size1() == 0 )
            return true ;
        return false ;
    }
}
