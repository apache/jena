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

/** A binding that is fixed - used in calculating DISTINCT result sets.
 *  .hashCode and .equals are overridden for content equality semantics (where
 *  "equality" means Node.equals, not Node.sameValueAs). */


public class BindingFixed extends BindingWrapped
{
    int varSize = 0 ;
    int calcHashCode = 0 ;
    private boolean haveDoneHashCode = false ; 
    
    public BindingFixed(Binding binding)
    { super(binding) ; }
    
    private int calcHashCode()
    {
        int _hashCode = 0 ;
        for ( Iterator<Var> iter = vars() ; iter.hasNext() ; )
        {
            Var var = iter.next() ;
            Node n = get(var) ;
            if ( n == null )
                continue ;
            // Independent of variable order.
            _hashCode = _hashCode^n.hashCode()^var.hashCode() ; 
            varSize ++ ;
        }
        return _hashCode ;
    }
        
    @Override
    public boolean equals(Object obj)
    {
        if ( this == obj ) return true ;
        
        if ( ! ( obj instanceof BindingFixed) )
            return false ;
        
        BindingFixed b = (BindingFixed)obj ;
        return BindingBase.equals(this, b) ; 
    }
    
    @Override
    public int hashCode()
    {
        if ( ! haveDoneHashCode )
        {
            calcHashCode = calcHashCode() ;
            haveDoneHashCode = true ;
        }
        return calcHashCode ;
    }
    
    protected void checkAdd1(Var v, Node node) { }
}
