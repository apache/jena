/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.engine;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.engine1.Plan;
import com.hp.hpl.jena.query.engine1.PlanElement;
import com.hp.hpl.jena.query.engine1.PlanWalker;
import com.hp.hpl.jena.query.engine1.compiler.PlanBlock;
import com.hp.hpl.jena.query.engine1.compiler.PlanGroup;
import com.hp.hpl.jena.sdb.store.PlanTranslator;
import com.hp.hpl.jena.sdb.store.Store;

public class PlanTranslatorGeneral implements PlanTranslator
{
    private boolean translateOptionals ;
    private boolean translateConstraints ;

    public PlanTranslatorGeneral(boolean translateOptionals, boolean translateConstraints)
    {
        this.translateOptionals = translateOptionals ;
        this.translateConstraints = translateConstraints ;
    }
    
    public PlanElement queryPlanTranslate(Query query, Store store, Plan plan, PlanElement planElement)
    {
        PlanWalker.walk(new PlanToSDB(query, store, translateOptionals,translateConstraints), planElement) ;
        // If we have optimized everything, drop the unnecessary plan elements
        // and set the project variables of the top Block.
        planElement = modify(planElement) ;
        return planElement ;
    }

    private PlanElement modify(PlanElement planElt)
    {
        // A full optimized query looks like:
        // PlanBlock
        //   PlanGroup(1)
        //     PlanSDB
        // If it is like this, we can set the projection for the PlanSDB Block. 

        PlanSDB planSDB = getPlanSDB(planElt) ;
        if ( planSDB == null )
        {
            // Can't improve - not a full rewritten query (PlanSDB not at the top) - return original
            return planElt ;
        }

        // It's a single SDB plan - set the projection on the top Block/PlanSDB.  
        List x = planSDB.getQuery().getResultVars() ;   // Names
        for ( Iterator iter = x.iterator() ; iter.hasNext() ; )
        {
            String vn = (String)iter.next() ;
            planSDB.addProjectVar(Node.createVariable(vn)) ;
        }

        // Later, maybe return the planSDB but the block is used for dataset (via it's ElementBlock)
        return planElt ;
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

// --------

//public Block toBlock()
//{
//    // try to get the block for this query, assuming that the query is completely an SQL-optimized query
//    Plan plan = new Plan() ;
//    PlanElement pElt = makePlanForQueryPattern(plan) ;
//    
//    pElt = queryPlanHook(plan, pElt) ;
//    
//    if ( pElt instanceof PlanSDB )
//        // modify() reorg'ed the tree
//        return ((PlanSDB)pElt) ;
//    
//    PlanSDB pBlock = getPlanSDB(pElt) ;
//    if ( pBlock == null )
//        System.err.println("Can't get the top block") ;
//    return pBlock ;
//}
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