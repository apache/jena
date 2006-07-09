/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.engine;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine1.PlanElement;
import com.hp.hpl.jena.query.engine1.plan.Transformer;
import com.hp.hpl.jena.query.util.Context;
import com.hp.hpl.jena.sdb.store.PlanTranslator;
import com.hp.hpl.jena.sdb.store.Store;

/** Turns plans, that is a tree of PlanElements, from engine1 into a form
 * that SDB can process.  Involves checking and extracting those parts that
 * can be done in SQL.  This results in a new PlanElement tree with specific
 * elements for calls on the SDB subsystem.  This may be the entire PlanElement
 * tree (the whole query was translated) or it may rely on engine1 to put
 * execute some parts and only execute SQL statements directly for specific
 * parts of the plan in order to keep exact SPARQL semantics.
 * 
 * As development proceeeds, more and more of the engine1 tree will
 * be subsuimed by the SDB susbsystem.     
 * 
 * @author Andy Seaborne
 * @version $Id$
 */ 
public class PlanTranslatorGeneral implements PlanTranslator
{
    private boolean translateOptionals ;
    private boolean translateConstraints ;

    public PlanTranslatorGeneral(boolean translateOptionals, boolean translateConstraints)
    {
        this.translateOptionals = translateOptionals ;
        this.translateConstraints = translateConstraints ;
    }
    
    public PlanElement queryPlanTranslate(Context context, Query query, Store store, PlanElement planElement)
    {
        // Expand SELECT * etc.  Shouldn't this happen in QueryEngine.getPlan()?
        query.setResultVars() ;
        
        PlanTranslator t = store.getPlanTranslator() ; 
        
        PlanToSDB xform = new PlanToSDB(context, query, store, translateOptionals,translateConstraints) ;
        planElement = Transformer.transform(xform, planElement) ;
        planElement = modify(planElement) ;
        return planElement ;
    }

    private PlanElement modify(PlanElement planElt)
    {
        PlanSDB planSDB = PlanSDB.getPlanSDB(planElt) ;
        if ( planSDB == null )
        {
            // Can't improve - not a full rewritten query (PlanSDB not at the top) - return original
            return planElt ;
        }

        // It's a single SDB plan - set the projection on the top Block/PlanSDB.  
        List x = planSDB.getQuery().getResultVars() ;   // Names
        
        if ( x.size() > 0 )
        {
            for ( Iterator iter = x.iterator() ; iter.hasNext() ; )
            {
                String vn = (String)iter.next() ;
                planSDB.getBlock().addProjectVar(new Var(vn)) ;
            }
        }
        return planSDB ;
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