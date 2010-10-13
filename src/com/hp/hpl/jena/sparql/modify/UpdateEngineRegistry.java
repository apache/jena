/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify;

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class UpdateEngineRegistry
{
    List<UpdateEngineFactory> factories = new ArrayList<UpdateEngineFactory>() ;
    static { init() ; }
    
    // Singleton
    static UpdateEngineRegistry registry = null ;
    static public UpdateEngineRegistry get()
    { 
        if ( registry == null )
            init() ;
        return registry;
    }
    
    private UpdateEngineRegistry() { }
    
    private static synchronized void init()
    {
        registry = new UpdateEngineRegistry() ;
        registry.add(UpdateEngineMain.getFactory()) ;
    }
    
    /** Locate a suitable factory for this query and dataset from the default registry
     * 
     * @param request   UpdateRequest 
     * @param graphStore The graph store
     * @return A QueryExecutionFactory or null if none accept the request
     */
    
    public static UpdateEngineFactory findFactory(UpdateRequest request, GraphStore graphStore, Context context)
    { return get().find(request, graphStore, context) ; }
    
    /** Locate a suitable factory for this query and dataset
     * 
     * @param request       UpdateRequest 
     * @param graphStore    A GraphStore
     * @return A UpdateProcessorFactroy or null if none accept the request
     */
    
    public UpdateEngineFactory find(UpdateRequest request, GraphStore graphStore, Context context)
    {
        for ( UpdateEngineFactory f : factories )
        {
            if ( f.accept(request, graphStore, context) )
                return f ;
        }
        return null ;
    }
    
    /** Add a QueryExecutionFactory to the default registry */
    public static void addFactory(UpdateEngineFactory f) { get().add(f) ; }
    
    /** Add a QueryExecutionFactory */
    public void add(UpdateEngineFactory f)
    {
        // Add to low end so that newer factories are tried first
        factories.add(0, f) ; 
    }
    
    /** Remove a QueryExecutionFactory */
    public static void removeFactory(UpdateEngineFactory f)  { get().remove(f) ; }
    
    /** Remove a QueryExecutionFactory */
    public void remove(UpdateEngineFactory f)  { factories.remove(f) ; }
    
    /** Allow <b>careful</b> manipulation of the factories list */
    public List<UpdateEngineFactory> factories() { return factories ; }

    /** Check whether a query engine factory is already registered in teh default registry*/
    public static boolean containsFactory(UpdateEngineFactory f) { return get().contains(f) ; }

    /** Check whether a query engine factory is already registered */
    public boolean contains(UpdateEngineFactory f) { return factories.contains(f) ; }

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