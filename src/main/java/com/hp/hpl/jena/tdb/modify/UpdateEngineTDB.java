/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.modify;

import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.modify.UpdateEngine ;
import com.hp.hpl.jena.sparql.modify.UpdateEngineFactory ;
import com.hp.hpl.jena.sparql.modify.UpdateEngineMain ;
import com.hp.hpl.jena.sparql.modify.UpdateEngineRegistry ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class UpdateEngineTDB extends UpdateEngineMain
{
    public UpdateEngineTDB(DatasetGraphTDB graphStore, UpdateRequest request, Binding inputBinding, Context context)
    { super(graphStore, request, inputBinding, context) ; }
    
    @Override
    public void execute()
    { super.execute() ; }

    // ---- Factory
    public static UpdateEngineFactory getFactory() { 
        return new UpdateEngineFactory()
        {
            @Override
            public boolean accept(UpdateRequest request, GraphStore graphStore, Context context)
            {
                return (graphStore instanceof DatasetGraphTDB) ;
            }
        
            @Override
            public UpdateEngine create(UpdateRequest request, GraphStore graphStore, Binding inputBinding, Context context)
            {
                return new UpdateEngineTDB((DatasetGraphTDB)graphStore, request, inputBinding, context) ;
            }

        } ;
    }

    public static void register() { UpdateEngineRegistry.get().add(getFactory()) ; }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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