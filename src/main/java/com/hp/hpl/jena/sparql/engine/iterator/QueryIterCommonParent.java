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

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.Iterator ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;

import org.apache.jena.atlas.logging.Log ;

/**
 * Yield new bindings, with a fixed parent, with values from an iterator. 
 * Parent must not have variables in common with the iterator stream.
 */
public class QueryIterCommonParent extends QueryIterConvert
{
    public QueryIterCommonParent(QueryIterator input, Binding binding, ExecutionContext execCxt)
    {
        super(input, new ConverterExtend(binding) , execCxt) ;
    }

    // Extend (with checking) an iterator stream of binding to have a common parent. 
    static class ConverterExtend implements QueryIterConvert.Converter
    {
        private Binding parentBinding ;
        
        ConverterExtend(Binding parent) { parentBinding = parent ; }
        
        @Override
        public Binding convert(Binding b)
        {
            if ( parentBinding == null || parentBinding.isEmpty() )
                return b ;
        
            // This is the result.  Could have BindingBase.setParent etc.  
            BindingMap b2 = BindingFactory.create(parentBinding) ;

            // Copy the resultSet bindings to the combined result binding with checking. 
            for ( Iterator<Var> iter = b.vars() ; iter.hasNext(); )
            {
                Var v = iter.next();
                Node n = b.get(v) ;
                if ( b2.contains(v) )
                {
                    Node n2 = b2.get(v) ;
                    if ( n2.equals(n) )
                        Log.warn(this, "Binding already for "+v+" (same value)" ) ;
                    else
                    {
                        Log.fatal(this, "Binding already for "+v+" (different values)" ) ;
                        throw new ARQInternalErrorException("Incompatible bindings for "+v) ;
                    }
                }
                b2.add(v, n) ;
            }
            return b2 ;
        }
    }
}
