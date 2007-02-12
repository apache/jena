/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.core.describe;

import java.util.* ;

import com.hp.hpl.jena.query.engine.main.EngineConfig;

/** Registry of DescribeHandlers.     
 * 
 * @author Andy Seaborne
 * @version $Id: DescribeHandlerRegistry.java,v 1.5 2007/01/25 16:38:53 andy_seaborne Exp $
 */

public class DescribeHandlerRegistry
{
    static private DescribeHandlerRegistry globalRegistry = null ;
    
    private List registry = new ArrayList() ; 

    private DescribeHandlerRegistry() { }
    
    private static synchronized DescribeHandlerRegistry standardRegistry()
    {
        DescribeHandlerRegistry reg = new DescribeHandlerRegistry() ;
        reg.add(new DescribeBNodeClosureFactory()) ;
        return reg ;
    }
    
    public static DescribeHandlerRegistry get()
    {
        // Intialize if there is no registry already set 
        DescribeHandlerRegistry reg = 
            (DescribeHandlerRegistry)EngineConfig.getContext().get(EngineConfig.registryDescribeHandlers) ;
        if ( reg == null )
        {
            reg = standardRegistry() ;
            EngineConfig.getContext().set(EngineConfig.registryDescribeHandlers, reg) ;
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
    
    public List newHandlerList()
    {
        List a = new ArrayList() ;
        for ( Iterator iter = handlers() ; iter.hasNext() ; )
        {
            DescribeHandlerFactory f = (DescribeHandlerFactory)iter.next();
            a.add(f.create()) ;
        }
        return a ;
    }
    
    
    public Iterator handlers()
    {
        return registry.iterator() ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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