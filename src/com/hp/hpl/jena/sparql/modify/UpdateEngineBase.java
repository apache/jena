/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify;

import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.UpdateRequest ;

public abstract class UpdateEngineBase
{
    protected final GraphStore graphStore ;
    protected final Context context ;
    protected final Binding startBinding ;
    protected UpdateRequest request ;

    public UpdateEngineBase(GraphStore graphStore, 
                            UpdateRequest request,
                            Binding input,
                            Context context)
    {
        this.graphStore = graphStore ;
        this.request = request ;
        this.context = setupContext(context, graphStore) ;
        
        if ( input == null )
        {
            Log.warn(this, "Null initial input") ;
            input = BindingRoot.create() ;
        }
        this.startBinding = input ;
        this.context.put(ARQConstants.sysCurrentUpdateRequest, request) ;
    }
    
    public abstract void execute() ;
    
    // Put any 
    private static Context setupContext(Context context, DatasetGraph dataset)
    {
        // To many copies?
        if ( context == null )      // Copy of global context to protect against chnage.
            context = ARQ.getContext() ;
        context = context.copy() ;

        if ( dataset.getContext() != null )
            context.putAll(dataset.getContext()) ;
        
        context.set(ARQConstants.sysCurrentTime, NodeFactory.nowAsDateTime()) ;
        return context ; 
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