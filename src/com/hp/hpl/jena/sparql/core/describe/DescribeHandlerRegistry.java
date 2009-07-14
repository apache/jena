/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core.describe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.util.Context;

/** Registry of DescribeHandlers.     
 * 
 * @author Andy Seaborne
 */

public class DescribeHandlerRegistry
{
    static private DescribeHandlerRegistry globalRegistry = null ;
    
    private List<DescribeHandlerFactory> registry = new ArrayList<DescribeHandlerFactory>() ; 

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
        List<DescribeHandler> a = new ArrayList<DescribeHandler>(registry.size()) ;
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

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */