/**
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

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.graph.query.IndexValues ;
import com.hp.hpl.jena.graph.query.VariableIndexes ;
import com.hp.hpl.jena.shared.DoesNotExistException ;
import com.hp.hpl.jena.sparql.core.Var ;

/** com.hp.hpl.jena.query.core.BindingIndex
 *  Convert between Bindings (the core main abstraction)
 *  and Jena's old style internal graph format. */

public class BindingIndex implements VariableIndexes, IndexValues
{
    List<String> indexes = new ArrayList<String>() ;
    Binding binding ;
    
    public BindingIndex(Binding b)
    {
        binding = b ; 
        for ( Iterator<Var> iter = binding.vars() ; iter.hasNext() ; )
        {
            Var var = iter.next() ;
            indexes.add(var.getVarName()) ;
        }
    }
    
    /*
     * @see com.hp.hpl.jena.graph.query.VariableIndexes#indexOf(java.lang.String)
     */
    public int indexOf(String varname)
    {
        for ( int i = 0 ; i < indexes.size() ; i++ )
        {
            if ( indexes.get(i).equals(varname) )
                return i ;
        }
        //return -1 ;
        throw new DoesNotExistException("Name not bound: "+varname) ;
    }
    
    /*
     * @see com.hp.hpl.jena.graph.query.IndexValues#get(int)
     */
    public Object get(int index)
    {
        if ( index < 0 || index > indexes.size() )
            return null ;
        String name = indexes.get(index) ;
        // The cast is a check.
        return binding.get(Var.alloc(name)) ;
    }
}
