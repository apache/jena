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

public class BindingFactory
{
    public static final Binding noParent = null ; 
    
    /** Create a binding of no pairs */
    public static Binding binding() { return binding(noParent) ; }
    
    /** Create a binding of no pairs */
    public static Binding binding(Binding parent) { return new Binding0(parent)  ; }
    
    public static Binding binding(Var var, Node node) { return binding(noParent, var, node) ; }
    
    /** Create a binding of one (var, value) pair */
    public static Binding binding(Binding parent, Var var, Node node)
    {
        if ( Var.isAnonVar(var) )
            return new Binding0(parent) ;
        return new Binding1(parent, var, node) ;
    }
    
    public static BindingMap create() { return create(noParent) ; }
    public static BindingMap create(Binding parent) { return new BindingHashMap(parent)  ; }
    
    public static Binding root() { return BindingRoot.create() ; }

    /** Create a new Binding as a copy of an existing one.
     * Additionally, it guarantees to touch each element of the binding */ 
    public static Binding materialize(Binding b)
    {
        Iterator<Var> vIter = b.vars() ; 
        BindingMap b2 = create() ; 
        while( vIter.hasNext() )
        {
            Var v = vIter.next();
            b2.add(v, b.get(v)) ;
        }
        return b2 ;
    }
}
