/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.engine1.plan;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sparql.engine.engine1.PlanElement;
import com.hp.hpl.jena.sparql.engine.engine1.PlanStructureVisitor;

public class Transformer
{
    static boolean noDupIfSame = true ;
    
    public static PlanElement transform(Transform tranform, PlanElement elt)
    {
        if ( elt == null )
        {
            LogFactory.getLog(Transformer.class).warn("Attempt to transform a null PlanElement - ignored") ;
            return elt ;
        }
        return new Transformer().transformation(tranform, elt) ;
    }
    
    private Transformer() { }
    
    public PlanElement transformation(Transform tranform, PlanElement elt)
    {
        TransformApplyBase v = new TransformApplyBase(tranform) ;
        elt.visit(v) ;
        return v.result() ;
    }
}
    
class TransformApplyBase implements PlanStructureVisitor
{
    Stack stack = new Stack() ;
    private PlanElement pop() { return (PlanElement)stack.pop(); }
    
    private void push(PlanElement newElt)
    { 
        // Including nulls
        stack.push(newElt) ;
    }
        

    public PlanElement result()
    { 
        if ( stack.size() != 1 )
            LogFactory.getLog(TransformApplyBase.class).warn("Stack is not aligned") ;
        return pop() ; 
    }

    private Transform transform ;
    TransformApplyBase(Transform transform) { this.transform = transform ; }

    public void visit(PlanElement0 planElt)
    {
        push(planElt.apply(transform)) ;
    }

    public void visit(PlanElement1 planElt)
    {
        planElt.getSubElement().visit(this) ;
        PlanElement x = pop() ;
        push(planElt.apply(transform, x)) ;
    }

    public void visit(PlanElementN planElt)
    { 
        List x = new ArrayList() ;
        int N = planElt.numSubElements() ;
        for ( int i = 0 ; i < N ; i++ )
        {
            planElt.getSubElement(i).visit(this) ;
            x.add(pop()) ;
        }
        push(planElt.apply(transform, x)) ;
    }

    public void visit(PlanElementExternal planElt)
    {
        LogFactory.getLog(TransformApplyBase.class).warn("Visiting external plan element") ;
        push(planElt) ;
    }
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