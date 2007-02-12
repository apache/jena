/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.engine1.plan;

import java.util.*;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.core.ARQInternalErrorException;
import com.hp.hpl.jena.query.core.BasicPattern;
import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.engine1.PlanElement;
import com.hp.hpl.jena.query.engine.engine1.PlanVisitor;
import com.hp.hpl.jena.query.engine.engine1.compiler.PFuncOps;
import com.hp.hpl.jena.query.pfunction.PropertyFunctionRegistry;
import com.hp.hpl.jena.query.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.query.util.Context;
import com.hp.hpl.jena.query.util.Utils;

/**  
 * @author Andy Seaborne
 * @version $Id: PlanTriplesBlock.java,v 1.5 2007/02/02 16:23:31 andy_seaborne Exp $
 */

public class PlanTriplesBlock extends PlanElementN
{
    
    
    public static PlanElement make(Context context, ElementTriplesBlock bgp)
    { 
        return new PlanTriplesBlock(context, bgp) ;
    }
    
    /** Create an empty PlanBasicGraphPattern */
    public PlanTriplesBlock(Context context)
    {
        this(context, (List)null) ;
    }
    
    private PlanTriplesBlock(Context context, ElementTriplesBlock bgp)
    {
        this(context, FP.process(context, bgp.getTriples()));
    }

    private PlanTriplesBlock(Context context, List readyMadePlanElements)
    {
        super(context, readyMadePlanElements) ;
    }
    
    // Glue the elements together.
    
    public QueryIterator build(QueryIterator input, ExecutionContext execCxt)
    { return PlanUtils.buildSerial(this, input, execCxt) ; }
    
    public void visit(PlanVisitor visitor) { visitor.visit(this) ; }
    
    public PlanElement apply(Transform transform, List newSubElements)
    { return transform.transform(this, newSubElements) ; }

    public PlanElement copy(List newSubElements)
    { return new PlanTriplesBlock(getContext(), newSubElements) ; }

}

class FP
{
    // ---------------------------------------------------------------------------------
    // Split into blocks of triples and property functions.
    
    static List process(Context context, BasicPattern pattern)
    {
        boolean doingMagicProperties = context.isTrue(ARQ.enablePropertyFunctions) ;
        PropertyFunctionRegistry registry = PFuncOps.chooseRegistry(context) ;
    
        // 1/ Find property functions.
        //   Property functions may involve other triples (for list arguments)
        // 2/ Make the property function elements, remove associated triples
        //   (but leave the proprty function triple in-place as a marker)
        // 3/ For remaining triples, put into blocks.
        
        List planBGPElements = new ArrayList() ;            // The elements of the PlanBGP
        List propertyFunctionTriples = new ArrayList() ;    // Property functions seen
        List triples = new ArrayList() ;                    // All triples
        
        findPropetryFunctions(context, pattern, 
                              doingMagicProperties, registry,
                              triples, propertyFunctionTriples) ;
        
        Map pfPlanElts = new HashMap() ;
        makePropetryFunctions(context, registry, pfPlanElts, triples, propertyFunctionTriples) ;
        
        makePlanElements(context, planBGPElements, pfPlanElts, triples, propertyFunctionTriples) ;
        return planBGPElements ;
    }

    private static void findPropetryFunctions(Context context, BasicPattern pattern,
                                              boolean doingMagicProperties,
                                              PropertyFunctionRegistry registry,
                                              List triples, List propertyFunctionTriples)
    {
        // Stage 1 : find property functions (if any); collect triples.
        for ( Iterator iter = pattern.iterator() ; iter.hasNext() ; )
        {
            Object obj = iter.next() ;
            
            if ( ! ( obj instanceof Triple ) )
            {
                LogFactory.getLog(PlanTriplesBlock.class).warn("Don't recognize: ["+Utils.className(obj)+"]") ;
                throw new ARQInternalErrorException("Not a triple pattern: "+obj.toString() ) ;
            }
                
            Triple t = (Triple)obj ;
            triples.add(t) ;
            if ( doingMagicProperties && PFuncOps.isMagicProperty(registry, t) )
                propertyFunctionTriples.add(t) ;
        }
    }

    private static void makePropetryFunctions(Context context, 
                                              PropertyFunctionRegistry registry, Map pfPlanElts, 
                                              List triples, List propertyFunctionTriples)
    {
        // Stage 2 : for each property function, make element and remove associated triples
        for ( Iterator iter = propertyFunctionTriples.iterator() ; iter.hasNext(); )
        {
            Triple pf = (Triple)iter.next();
            
            // Removes associated triples. 
            PlanElement planElt = PFuncOps.magicProperty(context, registry, pf, triples) ;
            if ( planElt == null )
            {
                LogFactory.getLog(PlanTriplesBlock.class).warn("Lost a PlanElement for a property function") ;
                continue ;
            }
            pfPlanElts.put(pf, planElt) ;
        }
    }

    private static void makePlanElements(Context context, List planBGPElements, 
                                         Map pfPlanElts, 
                                         List triples, List propertyFunctionTriples)
    {
        // Stage 3 : 
        //   For each property function, insert the implements PlanElement.
        //   For each block of non-property function triples, make a PlanBlockTriples.
        // Structure is PlanBlockTriples/PlanPropertyFunction/PlanBlockTriples...
        
        PlanTriples pBlk = null ;
        for ( Iterator iter = triples.iterator() ; iter.hasNext(); )
        {
            Triple t = (Triple)iter.next() ;
            
            if ( propertyFunctionTriples.contains(t) )
            {
                // It's a property function triple.
                PlanElement planElt = (PlanElement)pfPlanElts.get(t) ;
                planBGPElements.add(planElt) ;
                pBlk = null ;       // Unset any current PlanBlockTriples
                continue ;
            }                
                
            // Regular triples
            if ( pBlk == null )
            {
                pBlk = new PlanTriples(context) ;
                planBGPElements.add(pBlk) ;
            }
            pBlk.addTriple(t) ;         // Plain triple
        }
    }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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