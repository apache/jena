/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.update;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils ;
import com.hp.hpl.jena.sparql.modify.UpdateEngineFactory ;
import com.hp.hpl.jena.sparql.modify.UpdateEngineRegistry ;
import com.hp.hpl.jena.sparql.modify.UpdateProcessorBase ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Create UpdateProcessors (one-time executions of a SPARQL Update request) */
public class UpdateExecutionFactory
{

    /** Create a UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param update
     * @param graphStore
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(Update update, GraphStore graphStore)
    {
        return create(update, graphStore, (Binding)null) ;
    }

    /** Create a UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param update
     * @param graphStore
     * @param initialSolution
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(Update update, GraphStore graphStore, QuerySolution initialSolution)
    {        
        return create(new UpdateRequest(update), graphStore, initialSolution) ;
    }

    /** Create a UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param update
     * @param graphStore
     * @param initialBinding
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(Update update, GraphStore graphStore, Binding initialBinding)
    {        
        return create(new UpdateRequest(update), graphStore, initialBinding) ;
    }

    /** Create a UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param graphStore
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, GraphStore graphStore)
    {
        return create(updateRequest, graphStore, (Binding)null) ;
    }

    /** Create a UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param graphStore
     * @param initialSolution
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, GraphStore graphStore, QuerySolution initialSolution)
    {
        return create(updateRequest, graphStore, BindingUtils.asBinding(initialSolution)) ;
    }
    
    /** Create a UpdateProcessor appropriate to the GraphStore, or null if no available factory to make an UpdateProcessor 
     * @param updateRequest
     * @param graphStore
     * @param initialBinding
     * @return UpdateProcessor or null
     */
    public static UpdateProcessor create(UpdateRequest updateRequest, GraphStore graphStore, Binding initialBinding)
    {        
        return make(updateRequest, graphStore, initialBinding, null) ;
    }

    // Everything comes through here
    private static UpdateProcessor make(UpdateRequest updateRequest, GraphStore graphStore, Binding initialBinding, Context context)
    {
        if ( context == null )
            context = ARQ.getContext().copy();
        
        UpdateEngineFactory f = UpdateEngineRegistry.get().find(updateRequest, graphStore, ARQ.getContext()) ;
        if ( f == null )
            return null ;
        
        UpdateProcessorBase uProc = new UpdateProcessorBase(updateRequest, graphStore, ARQ.getContext(), f) ;
        if ( initialBinding != null )
            uProc.setInitialBinding(initialBinding) ;
        return uProc ;
    }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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