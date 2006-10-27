/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.engine1.*;
import com.hp.hpl.jena.query.util.Context;
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
    @Override
    protected PlanElement queryPlanPatternHook(Context context, PlanElement planElt)
    {
        PlanElement e = store.getPlanTranslator().queryPlanTranslate(context, getQuery(), store, planElt) ;
        return e ;
    }
    
    public Block toBlock()
    {
        // try to get the block for this query, 
        // assuming that the query is completely an SQL-optimized query
        PlanElement pElt = super.getPlanPattern() ;
        
        PlanSDB pBlock = PlanSDB.getPlanSDB(pElt) ;
        if ( pBlock == null )
        {
            log.warn("Can't get the top block") ;
            return null ;
        }
        return pBlock.getBlock() ;
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