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

package com.hp.hpl.jena.sparql.engine.binding;

import java.util.Iterator ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;

/** A binding that wraps another. */

public class BindingWrapped implements Binding
{
    protected Binding binding ;
    
    public BindingWrapped(Binding other) { binding = other; } 
    
    public Binding getWrapped() { return binding ; }

    @Override
    public boolean contains(Var var)
    {
        return binding.contains(var) ;
    }

    @Override
    public Node get(Var var)
    {
        return binding.get(var) ;
    }

    @Override
    public Iterator<Var> vars()
    {
        return binding.vars() ;
    }
    
    @Override
    public String toString() { return binding.toString(); }

    @Override
    public int size()           { return binding.size() ; }

    @Override
    public boolean isEmpty()    { return binding.isEmpty() ; }
    
    @Override
    public int hashCode() { return BindingBase.hashCode(this) ; } 
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( ! ( other instanceof Binding) ) return false ;
        Binding binding = (Binding)other ;
        return BindingBase.equals(this, binding) ; 
    }   
    
}
