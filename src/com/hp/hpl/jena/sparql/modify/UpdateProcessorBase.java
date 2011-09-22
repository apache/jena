/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.UpdateProcessor ;
import com.hp.hpl.jena.update.UpdateRequest ;

/** Class to hold the general state of a update request execution.
 *  See query ExecutionContext
 */
public class UpdateProcessorBase implements UpdateProcessor
{
    protected BindingMap initialBinding = BindingFactory.create() ;
    protected final UpdateRequest request ;
    protected final GraphStore graphStore ;
    protected final UpdateEngineFactory factory ;
    protected final Context context ;

    public UpdateProcessorBase(UpdateRequest request, 
                               GraphStore graphStore, 
                               Context context, 
                               UpdateEngineFactory factory)
    {
        this.request = request ;
        this.graphStore = graphStore ;
        if ( context == null )
            context = ARQ.getContext() ;
        
        this.context = context ;
        this.factory = factory ;
    }

    //@Override
    public void execute()
    {
        UpdateEngine proc = factory.create(request, graphStore, initialBinding, context) ;
        proc.execute() ;
    }

    //@Override
    public GraphStore getGraphStore()
    {
        return graphStore ;
    }

    //@Override
    public void setInitialBinding(QuerySolution binding)
    {
        BindingUtils.addToBinding(initialBinding, binding) ;
    }

    public void setInitialBinding(Binding binding)
    {
        initialBinding.addAll(binding) ;
    }

    //@Override
    public Context getContext()
    {
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