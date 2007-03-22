/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.engine1.plan;

import java.util.List;

import com.hp.hpl.jena.sparql.engine.engine1.PlanElement;

public class TransformCopy implements Transform
{
    public static final boolean COPY_ALWAYS         = true ;
    public static final boolean COPY_ONLY_ON_CHANGE = false ;
    boolean alwaysCopy = false ;
    
    public TransformCopy() { this(COPY_ONLY_ON_CHANGE) ; }
    public TransformCopy(boolean alwaysDuplicate) { this.alwaysCopy = alwaysDuplicate ; }
    
    
    // PlanElement0 (leaves of the plan tree)
    private PlanElement xform(PlanElement0 planElt)
    {
        if ( ! alwaysCopy )
            return planElt ;
        return planElt.copy() ;
    }
    
    public PlanElement transform(PlanTriples planElt)      { return xform(planElt) ; }
    public PlanElement transform(PlanFilter planElt)            { return xform(planElt) ; }
    public PlanElement transform(PlanPropertyFunction planElt)  { return xform(planElt) ; }
    //public PlanElement transform(PlanOuterJoin planElt) { return xform(planElt) ; }

    // PlanElementN
    private PlanElement xform(PlanElementN planElt, List newElts) 
    {
        if ( ! alwaysCopy )
        {
            boolean same = true ;
            int N = planElt.numSubElements() ;
            for ( int i = 0 ; i < N ; i++)
                if ( planElt.getSubElement(i) != newElts.get(i) )
                {
                    same = false ;
                    break ;
                }
            if ( same )
                return planElt ;
        }
        
        return planElt.copy(newElts) ;
    }
    
    
    public PlanElement transform(PlanTriplesBlock planElt, List newElts)  { return xform(planElt, newElts) ; }
    public PlanElement transform(PlanGroup planElt, List newElts)              { return xform(planElt, newElts) ; }
    public PlanElement transform(PlanUnion planElt, List newElts)              { return xform(planElt, newElts) ; }
    
    // PlanElement1
    private PlanElement xform(PlanElement1 planElt, PlanElement subElt)
    {
        if ( ! alwaysCopy && planElt.getSubElement() == subElt )
            return planElt ;
        return planElt.copy(subElt) ;
    }
    
    public PlanElement transform(PlanOptional planElt, PlanElement subElt)    { return xform(planElt, subElt) ; }
    public PlanElement transform(PlanUnsaid planElt, PlanElement subElt)      { return xform(planElt, subElt) ; }
    public PlanElement transform(PlanNamedGraph planElt, PlanElement subElt)  { return xform(planElt, subElt) ; }
    public PlanElement transform(PlanDataset planElt, PlanElement subElt)     { return xform(planElt, subElt) ; }

    public PlanElement transform(PlanProject planElt, PlanElement subElt)     { return xform(planElt, subElt) ; }
    public PlanElement transform(PlanDistinct planElt, PlanElement subElt)    { return xform(planElt, subElt) ; }
    public PlanElement transform(PlanOrderBy planElt, PlanElement subElt)     { return xform(planElt, subElt) ; }
    public PlanElement transform(PlanLimitOffset planElt, PlanElement subElt) { return xform(planElt, subElt) ; }

    // Other
    //public PlanElement transform(PlanElementExternal planElt) { return planElt ; }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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