/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.engine;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.engine1.*;
import com.hp.hpl.jena.query.engine1.compiler.*;
import com.hp.hpl.jena.sdb.core.*;
import com.hp.hpl.jena.sdb.store.Store;

public class QueryEngineSDB extends QueryEngine
{
    private static Log log = LogFactory.getLog(QueryEngineSDB.class) ; 
    Store store ;
    
    public QueryEngineSDB(Store store, Query q)
    {
        super(q) ;
        this.store = store ;
    }

    
    // -------- Hooks into the usual query engine
    
    /** This operator is a hook for other query engines to reuse this framework but
     *  take responsibility for their own query pattern execution. 
     */
//    protected PlanElement makePlanForQueryPattern(Plan plan)
//    {
//        return super.makePlanForQueryPattern(plan) ;
//    }
    
    /** Inspect, and possibily modify, the query plan and execution tree.
     * Called after plan creation getPlanForQueryPattern
     * 
     * @param plan
     * @param planElt
     * @return PlanElement  New root of the planning tree (often, the one passed in)
     */
    
    @Override
    protected PlanElement queryPlanHook(Plan plan, PlanElement planElt)
    {
        return store.getPlanTranslator().queryPlanTranslate(getQuery(), store, plan, planElt) ;
    }
    
    private PlanSDB getPlanSDB(PlanElement planElt)
    {
        //pBlock = (PlanSDB)((PlanGroup)((PlanBlock)pElt).getSub()).getPlanElements().get(0) ;
        try {
            PlanBlock pb = (PlanBlock)planElt ;
            PlanGroup g = (PlanGroup)pb.getSub() ;
            List x = g.getPlanElements() ;
            if (x.size() != 1 )
                return null ;
            PlanSDB planSDB = (PlanSDB)x.get(0) ;
            return planSDB ;
        } catch (ClassCastException ex) { return null ; }
    }
//    
//    // --------
    
    public Block toBlock()
    {
        // try to get the block for this query, assuming that the query is completely an SQL-optimized query
        Plan plan = new Plan() ;
        PlanElement pElt = makePlanForQueryPattern(plan) ;
        
        pElt = queryPlanHook(plan, pElt) ;
        
        if ( pElt instanceof PlanSDB )
            // modify() reorg'ed the tree
            return ((PlanSDB)pElt) ;
        
        PlanSDB pBlock = getPlanSDB(pElt) ;
        if ( pBlock == null )
            System.err.println("Can't get the top block") ;
        return pBlock ;
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