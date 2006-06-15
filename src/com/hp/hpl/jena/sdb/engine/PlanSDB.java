/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.engine;

import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.*;
import com.hp.hpl.jena.query.engine1.plan.* ;
import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.sdb.core.Block;
import com.hp.hpl.jena.sdb.store.Store;

/** A block + the Plan operations for QueryIter generation */

public class PlanSDB
    // Maybe be able to extend PlanExternalBase when/if Block goes away. 
    extends Block
    implements PlanElementExternal
{
    Store store ;
    Query query ;

    public PlanSDB(Query query, Store store)
    { this.store = store ; this.query = query ; }

    public QueryIterator build(QueryIterator input, ExecutionContext execCxt)
    {
        return new QueryIterSDB(query, store, this, input, execCxt ) ;
    }

    public void visit(PlanVisitor visitor) { visitor.visit(this) ; }
    public void visit(PlanStructureVisitor visitor) { } ;
    
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
            PlanElementN g = (PlanElementN)planElt ;
            if (g.numSubElements() != 1 )
                return null ;
            PlanSDB planSDB = (PlanSDB)g.getSubElement(0) ;
            return planSDB ;
        } catch (ClassCastException ex) { return null ; }
    }

    public PlanElement getSubElement(int i) { return null ; }
    public int numSubElement()    { return 0 ; }
    public List getSubElements()  { return null ; }

    public int numSubElements()
    {
        return 0 ;
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