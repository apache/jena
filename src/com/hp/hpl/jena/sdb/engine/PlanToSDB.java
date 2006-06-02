/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.engine;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.core.Constraint;
import com.hp.hpl.jena.query.engine1.PlanElement;
import com.hp.hpl.jena.query.engine1.PlanVisitorBase;
import com.hp.hpl.jena.query.engine1.compiler.*;
import com.hp.hpl.jena.sdb.core.*;
import com.hp.hpl.jena.sdb.store.QueryCompiler;
import com.hp.hpl.jena.sdb.store.Store;


public class PlanToSDB extends PlanVisitorBase
{
    private Query query ;
    private Store store ;
    private boolean translateOptionals ;
    private boolean translateConstraints ;
    
    PlanToSDB(Query query, Store store, boolean translateOptionals, boolean translateConstraints)
    {
        this.query = query ;
        this.store = store ;
        this.translateOptionals = translateOptionals ;
        this.translateConstraints = translateConstraints ;
    }

//    // Basic patterns
//    public void visit(PlanBasePattern planElt) {}
//    public void visit(PlanTriplePattern planElt) {}
    
    @Override
    @SuppressWarnings("unchecked")
    public void visit(PlanGroup planElt)
    {
        // Visitors happen bottom up - called on subgroups first.
        
        List x =  planElt.getPlanElements() ;
      elementsLoop:
        for ( int i = 0 ; i < x.size() ; i++ )
        {
            if ( x.get(i) == null )
                // Zapped entry
                continue ;
            
            PlanElement pElt = (PlanElement)x.get(i) ;
            if (! ( pElt instanceof PlanBasicPattern ) )
                // Not the start of a Block
                continue ;
                
            PlanSDB pBlock = new PlanSDB(query, store) ;
            PlanBasicPattern bgp = (PlanBasicPattern)pElt ;
            
            // Do the basic pattern
            processBasicGraphPattern(pBlock, bgp, x, i) ;  
            int j = i+1 ;
            
            if ( translateConstraints )
            {
                // Lookahead for constraints
                for( ; j < x.size() ; j++ )
                {
                    Constraint c = extractConstraint(x, j) ;
                    if ( c == null )
                        break ;
                    
                    boolean fullyHandled = processConstraint(pBlock, c, x, j) ;
                    if ( ! fullyHandled )
                        // Something tricky - just do a basic pattern and
                        // whatever constraints we could do.  The block may
                        // now have constraints that are partial (they will
                        // filter out some, but not all solutions and the
                        // original constraint must be reexecuted.
                        break elementsLoop ;
                    
                    // Contraint assumed by SQL layer.  Remove it.
                    x.set(j, null) ;
                    // Move the outer index to this point (it will move on in the loop)
                    i = j ;
                }
            }
            
            if ( translateOptionals )
            {
                // Look for optionals IFF all the filter could be handled.
                for ( ; j < x.size() ; j++ )
                {
                    // Followed by an optional?
                    Block pOpt = extractOptional(query, store.getQueryCompiler(), x, j) ;
                    if ( pOpt == null )
                        break ;
                    pBlock.addOptional(pOpt) ;
                    x.set(j, null) ;
                    i = j ;
                }
            }
        }
        // Trim nulls
        for ( Iterator iter = x.listIterator() ; iter.hasNext() ; )
        {
            if ( iter.next() == null )
                iter.remove() ;
        }
    }
    
    @SuppressWarnings("unchecked")
    private void processBasicGraphPattern(PlanSDB pBlock, PlanBasicPattern bgp, List x, int i)
    {
        pBlock.add(new BasicPattern(bgp.getPattern())) ;
        x.set(i, pBlock) ;
    }
    
    // Find an already converted basic pattern in an optional.
    private Block extractOptional(Query query, QueryCompiler compiler, List x, int i)
    {
        if ( i >= x.size() ) return null ;
        
        try {
            PlanOptional pOpt = (PlanOptional)x.get(i) ;
            
            // Get the subpattern and check it.
            PlanGroup pGrp = (PlanGroup)pOpt.getSub() ;
            if ( pGrp.getPlanElements().size() != 1 )
                return null ;
            // Is it a precompiled subelement?
            PlanSDB p = (PlanSDB)pGrp.getPlanElements().get(0) ;
            return p ;
        } catch (ClassCastException cce)
        {
            return null ;
        }
    }

    // Find a Constraint we can deal with
    private Constraint extractConstraint(List x, int i)
    {
        // TODO Condition compiler
        if ( store.getQueryCompiler().getConditionCompiler() == null )
        {
            LogFactory.getLog(this.getClass()).warn("No condition compiler - skipped") ;
            return null ;
        }
        
        if ( i >= x.size() ) return null ;
        
        try {
            PlanFilter pc = (PlanFilter)x.get(i) ;
            Constraint c = pc.getConstraint() ;
            return c ; 
        } catch (ClassCastException cce)
        { return null ; }
    }
    
    // Returns true if this constraint can be removed from the plan.
    private boolean processConstraint(PlanSDB pBlock, Constraint c, List x, int i)
    {
        if ( ConditionRecognizer.recognize(pBlock, c) )
        {
            pBlock.add(c) ;
            return true ;
        }
        return false ;
    }
    
//    public void visit(PlanUnion planElt) {}
//    public void visit(PlanOptional planElt) {}
//    public void visit(PlanUnsaid planElt) {}
//    public void visit(PlanFilter planElt) {}
//    public void visit(PlanNamedGraph planElt) {}
//    public void visit(PlanOuterJoin planElt) {}
//
//    // Other
//    public void visit(PlanExtension planElt) {}
//    public void visit(PlanBlock planElt) {}
//    
//    // Solution sequence modifiers
//    public void visit(PlanDistinct planElt) {}
//    public void visit(PlanProject planElt) {}
//    public void visit(PlanOrderBy planElt) {}
//    public void visit(PlanLimitOffset planElt) {}
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