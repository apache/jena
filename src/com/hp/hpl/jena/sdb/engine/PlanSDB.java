/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.engine;

import java.util.List;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.*;
import com.hp.hpl.jena.query.engine1.compiler.PlanGroup;
import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.sdb.core.Block;
import com.hp.hpl.jena.sdb.store.Store;

/** A block + the Plan operations for QueryIter generation */

public class PlanSDB
    extends Block
    implements PlanElement
{
    Store store ;
    Query query ;

    public PlanSDB(Query query, Store store)
    { this.store = store ; this.query = query ; } 
    
    public QueryIterator build(QueryIterator input, ExecutionContext execCxt)
    {
        return new QueryIterSDB(query, store, this, input, execCxt) ;
    }
    
    public void visit(PlanVisitor visitor)
    {
        // Hmm ... not extensible
        // Could have a PlanOther in the visitor and the writer 
        // calls .output() or .toString() by reflection. 
        LogFactory.getLog(PlanSDB.class).warn(".visit called - nothing implemented") ;
    }
    
    @Override
    public void output(IndentedWriter out)
    {
        out.println("[PlanSDB") ;
        out.incIndent() ;
        super.output(out) ;
        out.decIndent() ;
        out.print("]") ;
    }

    public Query getQuery()   { return query ; }
//    public Schema getSchema() { return schema ; }

    // Get the PlanSDB element from a PlanElement tree (assuming there is
    // exactly one as the result of query translation.
    public static PlanSDB getPlanSDB(PlanElement planElt)
    {
        if ( planElt instanceof PlanSDB )
            return (PlanSDB)planElt ;
        try {
            PlanGroup g = (PlanGroup)planElt ;
            List x = g.getPlanElements() ;
            if (x.size() != 1 )
                return null ;
            PlanSDB planSDB = (PlanSDB)x.get(0) ;
            LogFactory.getLog(PlanSDB.class).info("Not top element ... found in group of one") ;
            return planSDB ;
        } catch (ClassCastException ex) { return null ; }

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