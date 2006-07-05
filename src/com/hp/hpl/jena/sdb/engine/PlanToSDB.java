/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.engine;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine1.PlanElement;
import com.hp.hpl.jena.query.engine1.plan.TransformCopy;
import com.hp.hpl.jena.query.engine1.plan.PlanBasicGraphPattern;
import com.hp.hpl.jena.query.engine1.plan.PlanBlockTriples;
import com.hp.hpl.jena.query.engine1.plan.PlanFilter;
import com.hp.hpl.jena.query.engine1.plan.PlanOptional;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.query.util.CollectionUtils;
import com.hp.hpl.jena.query.util.Context;
import com.hp.hpl.jena.sdb.condition.C_Regex;
import com.hp.hpl.jena.sdb.condition.C_Var;
import com.hp.hpl.jena.sdb.condition.SDBConstraint;
import com.hp.hpl.jena.sdb.exprmatch.Action;
import com.hp.hpl.jena.sdb.exprmatch.ActionMatchString;
import com.hp.hpl.jena.sdb.exprmatch.ActionMatchVar;
import com.hp.hpl.jena.sdb.exprmatch.MapResult;
import com.hp.hpl.jena.sdb.store.Store;


public class PlanToSDB extends TransformCopy
{
    private static Log log = LogFactory.getLog(PlanToSDB.class) ;
    
    private Query query ;
    private Store store ;
    private Context context ;
    private boolean translateOptionals ;
    private boolean translateConstraints ;

    
    PlanToSDB(Context context, Query query, Store store, boolean translateOptionals, boolean translateConstraints)
    {
        super(TransformCopy.COPY_ONLY_ON_CHANGE) ;
        this.query = query ;
        this.store = store ;
        this.context = context ;
        this.translateOptionals = translateOptionals ;
        this.translateConstraints = translateConstraints ;
    }
    
    @Override
    public PlanElement transform(PlanBlockTriples planElt)
    { 
        PlanSDB x = new PlanSDB(context, query, store) ;
        @SuppressWarnings("unchecked")
        List<Triple> triples = (List<Triple>)planElt.getPattern() ;
        
        for ( Triple t : triples )  
            x.getBlock().add(t) ;
        return x ;
    }
    
   
//    @Override
//    public PlanElement transform(PlanFilter planElt)
//    {
//        PlanSDBConstraint sdb = transformFilter(planElt) ;
//        if ( sdb == null )
//            return super.transform(planElt) ;
//        return sdb ;
//    }
    
    
    @Override
    public PlanElement transform(PlanBasicGraphPattern planElt, List newElts)
    { 
        @SuppressWarnings("unchecked")
        List<PlanElement> newElements = (List<PlanElement>)newElts ;
        
        PlanSDB lastSDB = null ;
        for ( int i = 0 ; i < newElements.size() ; i++ )
        {
            PlanElement e = newElements.get(i) ;
            
            if ( e instanceof PlanSDB )
            {
                lastSDB = (PlanSDB)e ;
                continue ;
            }

            if ( e instanceof PlanFilter )
            {
                // If filters have not been transofrmed earlier.
                // Better here so can test for whether the filte ris appropriate for the BGP.
                PlanSDBConstraint c = transformFilter((PlanFilter)e) ;
                if ( c != null )
                {
                    PlanFilter filter = c.getOriginal() ;
                    // Check for complete and partial filters.
                    if ( lastSDB != null )
                    {
                        lastSDB.getBlock().add( c.get() ) ;
                        if ( c.isComplete() )
                            filter = null ;
                    }

                    // Put back in the remained external filter (may be null). 
                    newElements.set(i, filter) ;
                    continue ;
                }
            }
                
            // Or do PlanFilters in two steps.
//            if ( e instanceof PlanSDBConstraint )
//            {
//                PlanSDBConstraint c = (PlanSDBConstraint)e ;
//                PlanFilter filter = c.getOriginal() ;
//                
//                if ( lastSDB != null )
//                {
//                    lastSDB.getBlock().add( c.get() ) ;
//                    if ( c.isComplete() )
//                        filter = null ;
//                }
//
//                newElements.set(i, filter) ;
//                continue ;
//            }
            
            
            if ( e instanceof PlanSDBMarker )
                log.warn("PlanSDBMarker still present!") ;
            lastSDB = null ;
        }

        // Nulls mean no element anymore (e.g. FILTER that has been absorbed into the SDB part)  
        CollectionUtils.removeNulls(newElements) ;
        
        // Check that the FilteredBGP is wholly converted.
        // If so, remove this wrapper.
        
        if ( newElements.size() != 1 )
            return planElt.copy(newElements) ; 
        
        if ( newElements.get(0) instanceof PlanSDB  )
            // Good to remove
            return (PlanSDB)newElements.get(0) ;
        
        // No good
        return super.transform(planElt, newElements) ;
    }
    
    @Override
    public PlanElement transform(PlanOptional planElt, PlanElement fixed, PlanElement optional)
    {
        if ( fixed instanceof PlanSDB && optional instanceof PlanSDB )
        {
            PlanSDB fixedSDB = (PlanSDB)fixed ;
            PlanSDB optionalSDB = (PlanSDB)optional ;
            // Converted both sides - can convert this node.
            PlanSDB planSDB = new PlanSDB(context, query, store) ;
            fixedSDB.getBlock().addOptional(optionalSDB.getBlock()) ;
            return fixed ;
        }
        // We're not interested - do whatever the default is.
        return super.transform(planElt, fixed, optional) ;
    }
    
    // -------- Constraints
    
    // --- regex : testing a term (in a variable)
    private static ExprPattern regex1 = new ExprPattern("regex(?a1, ?a2)",
                                                        new String[]{ "a1" , "a2" },
                                                        new Action[]{ new ActionMatchVar() ,
                                                                      new ActionMatchString()}) ;
    
    private static ExprPattern regex2 = new ExprPattern("regex(?a1, ?a2, 'i')",
                                                        new String[]{ "a1" , "a2" },
                                                        new Action[]{ new ActionMatchVar() ,
                                                                      new ActionMatchString()}) ;
    // --- regex : testing the lexical form of a term (in a variable)
    private static ExprPattern regex3 = new ExprPattern("regex(str(?a1), ?a2)",
                                                        new String[]{ "a1" , "a2" },
                                                        new Action[]{ new ActionMatchVar() ,
                                                                      new ActionMatchString()}) ;
    private static ExprPattern regex4 = new ExprPattern("regex(str(?a1), ?a2, 'i')",
                                                        new String[]{ "a1" , "a2" },
                                                        new Action[]{ new ActionMatchVar() ,
                                                                      new ActionMatchString()}) ;
    
    // --- starts-with
    private static ExprPattern startsWith1 = new ExprPattern("fn:starts-with(?a1, ?a2)",
                                                             new String[]{ "a1" , "a2" },
                                                             new Action[]{ new ActionMatchVar() ,
                                                                           new ActionMatchString()}) ;

    private static ExprPattern startsWith2 = new ExprPattern("fn:starts-with(str(?a1), ?a2)",
                                                             new String[]{ "a1" , "a2" },
                                                             new Action[]{ new ActionMatchVar() ,
                                                                           new ActionMatchString()}) ;
    private PlanSDBConstraint transformFilter(PlanFilter planElt)
    {
        Expr expr = planElt.getConstraint().getExpr() ; 
        MapResult rMap = null ;
        
        if ( (rMap = regex1.match(expr)) != null )
        {
            //log.info("Matched: ?a1 = "+rMap.get("a1")+" : ?a2 = "+rMap.get("a2")) ;
            // TODO - think about need for the C_ parallel class hierarchy of constraints
            Var var = new Var(rMap.get("a1").getVar()) ;
            String pattern = rMap.get("a2").getConstant().getString() ;
            SDBConstraint c = new C_Regex(new C_Var(var), pattern, false) ;
            // IsNotNull AND ...
            // I am not perfect ...
            return new PlanSDBConstraint(c, planElt, true) ; 
        }
        
        if ( (rMap = startsWith1.match(expr)) != null )
        {
            log.info("startsWith - Matched: ?a1 = "+rMap.get("a1")+" : ?a2 = "+rMap.get("a2")) ;
            Var var = new Var(rMap.get("a1").getVar()) ;
            String pattern = rMap.get("a2").getConstant().getString() ;
            
            // becomes; isNotNull(var) AND var LIKE 'pattern%'
        }
        return null ;
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