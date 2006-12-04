/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.engine;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.query.engine1.PlanElement;
import com.hp.hpl.jena.query.engine1.plan.PlanElement1;
import com.hp.hpl.jena.query.engine1.plan.PlanElementExternal;
import com.hp.hpl.jena.query.engine1.plan.PlanElementExternalBase;
import com.hp.hpl.jena.query.serializer.SerializationContext;
import com.hp.hpl.jena.query.util.Context;
import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.sdb.core.Block;
import com.hp.hpl.jena.sdb.store.Store;

/** A block + the Plan operations for QueryIter generation */

public class PlanSDB
    extends PlanElementExternalBase 
    implements PlanElementExternal
{
    Store store ;
    Query query ;
    Block block ;

    public PlanSDB(Context context, Query query, Store store, Block block)
    {   
        super() ;
        this.store = store ;
        this.query = query ;
        this.block = block ;
    }

    public QueryIterator build(QueryIterator input, ExecutionContext execCxt)
    {
        return new QueryIterSDB(query, store, getBlock(), input, execCxt ) ;
    }

    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        out.println("[PlanSDB") ;
        out.incIndent() ;
        block.output(out) ;
        out.decIndent() ;
        out.print("]") ;
    }

    public Query getQuery()   { return query ; }
    public Block getBlock()   { return block ; }

    // Get the PlanSDB element from a PlanElement tree (assuming there is
    // exactly one as the result of query translation.
    public static PlanSDB getPlanSDB(PlanElement planElt)
    {
        if ( planElt instanceof PlanSDB )
            return (PlanSDB)planElt ;
//        try {
//            PlanElementN g = (PlanElementN)planElt ;
//            if (g.numSubElements() != 1 )
//                return null ;
//            PlanSDB planSDB = (PlanSDB)g.getSubElement(0) ;
//            return planSDB ;
//        } catch (ClassCastException ex) { } 
        return null ;
    }
    
    public static PlanSDB getFirstPlanSDB(PlanElement planElt)
    {
        do {
            if ( planElt instanceof PlanSDB )
                return (PlanSDB)planElt ;
            if ( planElt instanceof PlanElement1 )
                planElt = ((PlanElement1)planElt).getSubElement() ;
            else 
                return null ;
        } while (true) ;
    }
}

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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