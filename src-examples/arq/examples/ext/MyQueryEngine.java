/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.examples.ext;

import java.util.Iterator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.engine1.*;
import com.hp.hpl.jena.query.engine1.plan.PlanBlockTriples;
import com.hp.hpl.jena.query.engine1.plan.Transform;
import com.hp.hpl.jena.query.engine1.plan.TransformCopy;
import com.hp.hpl.jena.query.engine1.plan.Transformer;
import com.hp.hpl.jena.query.util.Context;

/** Example of a custom query engine. */

public class MyQueryEngine extends QueryEngine
{
    /* Register an engine with the QueryEngineRegistry */
    
    public MyQueryEngine(Query q)
    {
        super(q) ;
    }

//    protected PlanElement makePlanForQueryPattern(Context context, Element queryPatternElement)
//    {
//        // Intercept the process of making the plan for the pattern part    
//        return super.makePlanForQueryPattern(context, queryPatternElement) ;
//    }
    
    // A simpler way to extend the query engine is to get the standard engine
    // to generate a plan of some kind, then modify it.  There are two hook points:
    //   when the query pattern is generated
    //   when the whole has been generated (includes modifiers)
    
    protected PlanElement queryPlanPatternHook(Context context, PlanElement planElt)
    { 
        if ( planElt == null )
        {
            // No query pattern (no WHERE clause)
            return planElt ;
        }
        
        // Can hook in here and do a complete plan generation 
        // with makePlanForQueryPattern ...
        // generate a plan and replace  
        Transform f = new ExTransform() ;
        PlanElement e = Transformer.transform(f, planElt) ;
        return e ;
    }

    // Same - except it's the PlanElement for the whole query plan (modifers included).
    protected PlanElement queryPlanHook(Context context, PlanElement planElt)
    {
        return planElt ;
    }
    
}

class ExTransform extends TransformCopy
{
    public PlanElement transform(PlanBlockTriples planElt) 
    { 
        // Reverse the order of the triples in every block of triples.
        PlanBlockTriples pbt = new PlanBlockTriples(planElt.getContext()) ;
        for ( Iterator iter = planElt.triples() ; iter.hasNext(); )
        {
            pbt.getPattern().add(0, iter.next()) ;
        }
        return pbt ;
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