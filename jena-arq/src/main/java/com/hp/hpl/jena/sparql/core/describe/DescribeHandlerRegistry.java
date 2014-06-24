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

package com.hp.hpl.jena.sparql.core.describe;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Registry of DescribeHandlers. */

public class DescribeHandlerRegistry
{
    static private DescribeHandlerRegistry globalRegistry = null ;
    
    private List<DescribeHandlerFactory> registry = new ArrayList<>() ;

    private DescribeHandlerRegistry() { }
    
    private static synchronized DescribeHandlerRegistry standardRegistry()
    {
        DescribeHandlerRegistry reg = new DescribeHandlerRegistry() ;
        reg.add(new DescribeBNodeClosureFactory()) ;
        return reg ;
    }
    
    public static DescribeHandlerRegistry get(Context context)
    {
        if ( context == null )
            return null ;
        return (DescribeHandlerRegistry)ARQ.getContext().get(ARQConstants.registryDescribeHandlers) ;
    }
    
    public static void set(Context context, DescribeHandlerRegistry reg)
    {
        context.set(ARQConstants.registryDescribeHandlers, reg) ;
    }
    
    public static DescribeHandlerRegistry get()
    {
        // Intialize if there is no registry already set 
        DescribeHandlerRegistry reg = get(ARQ.getContext()) ;
        if ( reg == null )
        {
            reg = standardRegistry() ;
            set(ARQ.getContext(), reg) ;
        }
        return reg ;
    }
    
    public void add(DescribeHandlerFactory handlerFactory )
    {
        registry.add(0, handlerFactory) ; 
    }
    
    public void remove(DescribeHandlerFactory handlerFactory)
    {
        registry.remove(handlerFactory) ; 
    }
    
    public void clear()
    {
        registry.clear() ;
    }
    
    public List<DescribeHandler> newHandlerList()
    {
        List<DescribeHandler> a = new ArrayList<>(registry.size()) ;
        for ( Iterator<DescribeHandlerFactory> iter = handlers() ; iter.hasNext() ; )
        {
            DescribeHandlerFactory f = iter.next();
            a.add(f.create()) ;
        }
        return a ;
    }
    
    
    public Iterator<DescribeHandlerFactory> handlers()
    {
        return registry.iterator() ;
    }
}
