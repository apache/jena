/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.alq;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.query.engine1.plan.PlanElementExternal;
import com.hp.hpl.jena.query.engine1.plan.PlanElementExternalBase;
import com.hp.hpl.jena.query.util.Context;
import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.sdb.store.Store;

public class PlanElementSDB  
    extends PlanElementExternalBase 
    implements PlanElementExternal
{
    private Store store ;
    private Query query ;
    
    public PlanElementSDB(Context context, Query query, Store store)
    {   
        super() ;
        this.store = store ;
        this.query = query ;
        
        // Need to check how much we can process.
        // Leave the rest to reference engine?
    }

    public QueryIterator build(QueryIterator input, ExecutionContext execCxt)
    {
        return new QueryIterSDB(query, store, input, execCxt ) ;
    }
    
    @Override
    public void output(IndentedWriter out)
    {
        out.println("[PlanElementSDB") ;
        out.incIndent() ;
        //???
        out.decIndent() ;
        out.print("]") ;
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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