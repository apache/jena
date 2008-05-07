/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.store.StoreHolder;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.modify.UpdateProcessorFactory;
import com.hp.hpl.jena.sparql.modify.UpdateProcessorRegistry;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

public class UpdateProcessorSDB  extends StoreHolder implements UpdateProcessor
{
    private UpdateRequest request ;
    private Binding inputBinding ;

    public UpdateProcessorSDB(Store store, UpdateRequest request, Binding inputBinding)
    { 
        super(store) ;
        this.request = request ;
        this.inputBinding = inputBinding ;
        if ( inputBinding != null )
            ALog.warn(this, "Initial binding (not implemented)") ;
    }
    
    public void execute()
    {
        
    }

    // ---- Factory
    public static UpdateProcessorFactory getFactory() { 
        return new UpdateProcessorFactory()
        {
            public boolean accept(UpdateRequest request, GraphStore graphStore)
            {
                ALog.warn(this, "Not accepting a GraphStore") ;
                return (graphStore instanceof Store) ;
            }
        
            public UpdateProcessor create(UpdateRequest request, GraphStore graphStore, Binding inputBinding)
            {
                //return new UpdateProcessorSDB(graphStore, request, inputBinding) ;
                ALog.fatal(this, "No Store implements GraphStore") ;
                return null ;
            }
        } ;
    }

    public static void register() { UpdateProcessorRegistry.get().add(getFactory()) ; }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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