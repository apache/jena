/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.engine1.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.engine.engine1.PlanElement;
import com.hp.hpl.jena.sparql.engine.engine1.plan.*;
import com.hp.hpl.jena.sparql.syntax.*;
import com.hp.hpl.jena.sparql.util.Context;

public class QueryCompilerVisitor implements ElementVisitor
{
    protected Context context ;
    // The return stack
    private Stack retStack = new Stack() ;
    
    public QueryCompilerVisitor(Context context) {this.context  = context ;}
    
    public PlanElement compile(Element el)
    {
        ElementWalker.walk(el, this) ; 
        if ( retStack.size() != 1 )
            throw new ARQInternalErrorException("Plan compilation stack is not the expected size") ;
        PlanElement cElt = pop() ;
        return cElt ;
    }
    
    // ---- Element visitor

    public void visit(ElementTriplesBlock el)
    {
        // This compiles the triple patterns
        PlanElement cElt = PlanTriplesBlock.make(context, el) ;
        push(cElt) ;
    }
    
    public void visit(ElementFilter el)
    {
        PlanElement cElt = PlanFilter.make(context, el) ;
        push(cElt) ;
    }
    
    public void visit(ElementUnion el)
    {
        List acc = new ArrayList() ;
        int x = el.getElements().size() ;
        int z1 = retStack.size() ;
        
        // FIFO stack!
        for ( int i = 0 ; i < x ; i++ )
        {
            PlanElement ex2 = pop() ;
            // Always add at the low end to reverse the FIFO stack.
            acc.add(0, ex2) ;
        }
        int z2 = retStack.size() ;
        //assert z1-z2 == el.getElements().size() ;
        // NB: May not be a PlanUnion if it was just one element.
        PlanElement ex = PlanUnion.make(context, acc) ;
        push(ex) ;
    }
    
    public void visit(ElementGroup el)
    {
        List acc = new ArrayList() ;
        int x = el.getElements().size() ;
        
        // Bottom up: the compiled elements are on the stack
        // Stacks are FIFO so comes off in reverse order. 
        // A PlanBasicGraphpattern may contain several PlanElements
        // but it is a single plan unit in a group so this
        // code knows how many elements to pop.
        
        for ( int i = 0 ; i < x ; i++ )
        {
            PlanElement ex2 = pop() ;
            acc.add(0, ex2) ;
        }
        // NB: May not be a PlanGroup if it was just one element.
        // Could do one skipping here if we need PlanGroup.make to return a PlanGroup
        PlanElement ex = PlanGroup.make(context, acc) ;
        push(ex) ;
    }
    
    public void visit(ElementOptional el)
    {
        // Note : in reverse order (it's a stack!).
        // Note there may be only one!
        PlanElement pOpt = pop() ;
        PlanElement cElt = PlanOptional.make(context, el, pOpt) ;
        push(cElt) ;
    }
    
    public void visit(ElementDataset el)
    {
        PlanElement cSubElt = null ;
        
        // May have an empty block (e.g. DESCRIBE <uri>)
        if ( el.getPatternElement() != null )
            // Get the block compiled element from the stack
            cSubElt = pop() ;
        PlanElement cElt = PlanDataset.make(context, el, cSubElt) ;
        push(cElt) ;
    }

    public void visit(ElementNamedGraph el)
    {
        PlanElement cSubElt = pop() ;
        PlanElement cElt = PlanNamedGraph.make(context, el, cSubElt) ;
        push(cElt) ;
    }
    
    public void visit(ElementUnsaid el)
    {
        PlanElement cSubElt = pop() ;
        PlanElement cElt = PlanUnsaid.make(context, el, cSubElt) ;
        push(cElt) ;
    }
    
    public void visit(ElementExtension el)
    {
        PlanElement cElt = PlanExtension.make(context, el) ;
        push(cElt) ;
    }
    
    private void push(PlanElement cElt)  { retStack.push(cElt) ; }
    private PlanElement pop()            { return (PlanElement)retStack.pop() ; }
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
