/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateRequest;

public class UpdateProcessorRegistry
{
    List<UpdateProcessorFactory> factories = new ArrayList<UpdateProcessorFactory>() ;
    static { init() ; }
    
    // Singleton
    static UpdateProcessorRegistry registry = null ;
    static public UpdateProcessorRegistry get()
    { 
        if ( registry == null )
            init() ;
        return registry;
    }
    
    private UpdateProcessorRegistry() { }
    
    private static synchronized void init()
    {
        registry = new UpdateProcessorRegistry() ;
        registry.add(UpdateProcessorMain.getFactory()) ;
    }
    
    /** Locate a suitable factory for this query and dataset from the default registry
     * 
     * @param request   UpdateRequest 
     * @param graphStore The graph store
     * @return A QueryExecutionFactory or null if none accept the request
     */
    
    public static UpdateProcessorFactory findFactory(UpdateRequest request, GraphStore graphStore)
    { return get().find(request, graphStore) ; }
    
    /** Locate a suitable factory for this query and dataset
     * 
     * @param request       UpdateRequest 
     * @param graphStore    A GraphStore
     * @return A UpdateProcessorFactroy or null if none accept the request
     */
    
    public UpdateProcessorFactory find(UpdateRequest request, GraphStore graphStore)
    {
        for ( Iterator<UpdateProcessorFactory> iter = factories.listIterator() ; iter.hasNext() ; )
        {
            UpdateProcessorFactory f = iter.next() ;
            if ( f.accept(request, graphStore) )
                return f ;
        }
        return null ;
    }
    
    /** Add a QueryExecutionFactory to the default registry */
    public static void addFactory(UpdateProcessorFactory f) { get().add(f) ; }
    
    /** Add a QueryExecutionFactory */
    public void add(UpdateProcessorFactory f)
    {
        // Add to low end so that newer factories are tried first
        factories.add(0, f) ; 
    }
    
    /** Remove a QueryExecutionFactory */
    public static void removeFactory(UpdateProcessorFactory f)  { get().remove(f) ; }
    
    /** Remove a QueryExecutionFactory */
    public void remove(UpdateProcessorFactory f)  { factories.remove(f) ; }
    
    /** Allow <b>careful</b> manipulation of the factories list */
    public List<UpdateProcessorFactory> factories() { return factories ; }

    /** Check whether a query engine factory is already registered in teh default registry*/
    public static boolean containsFactory(UpdateProcessorFactory f) { return get().contains(f) ; }

    /** Check whether a query engine factory is already registered */
    public boolean contains(UpdateProcessorFactory f) { return factories.contains(f) ; }

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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