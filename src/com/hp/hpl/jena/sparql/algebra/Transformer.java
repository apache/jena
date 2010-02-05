/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.util.ALog;

public class Transformer
{
    private static Transformer singleton = new Transformer();
    
    /** Get the current transformer */
    public static Transformer get() { return singleton; }
    
    /** Set the current transformer - use with care */
    public static void set(Transformer value) { Transformer.singleton = value; }
    
    public static Op transform(Transform transform, Op op)
    { return get().transformation(transform, op, null, null) ; }
    
    public static Op transform(Transform transform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        return get().transformation(transform, op, beforeVisitor, afterVisitor) ;
    }
    
    public Op transformation(Transform transform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        ApplyTransformVisitor v = new ApplyTransformVisitor(transform) ;
        return transformation(v, op, beforeVisitor, afterVisitor) ;
    }
    
    public Op transformation(ApplyTransformVisitor transformApply,
                             Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        if ( op == null )
        {
            ALog.warn(this, "Attempt to transform a null Op - ignored") ;
            return op ;
        }

        OpWalker.walk(op, transformApply, beforeVisitor, afterVisitor) ;
        Op r = transformApply.result() ;
        return r ;
    }

    
    protected Transformer() { }
    
    protected static boolean noDupIfSame = true ;
    
    public static
    class ApplyTransformVisitor extends OpVisitorByType
    {
        protected final Transform transform ;
        private final Stack<Op> stack = new Stack<Op>() ;
        protected final Op pop() { return stack.pop(); }
        
        protected final void push(Op op)
        { 
            // Including nulls
            stack.push(op) ;
        }
        
        public ApplyTransformVisitor(Transform transform)
        { 
            this.transform = transform ;
        }
        
        public final Op result()
        { 
            if ( stack.size() != 1 )
                ALog.warn(this, "Stack is not aligned") ;
            return pop() ; 
        }
    
        @Override
        protected void visit0(Op0 op)
        {
            push(op.apply(transform)) ;
        }
        
        @Override
        protected void visit1(Op1 op)
        {
            Op subOp = null ;
            if ( op.getSubOp() != null )
                subOp = pop() ;
            push(op.apply(transform, subOp)) ;
        }
    
        @Override
        protected void visit2(Op2 op)
        { 
            Op left = null ;
            Op right = null ;
    
            // Must do right-left because the pushes onto the stack were left-right. 
            if ( op.getRight() != null )
                right = pop() ;
            if ( op.getLeft() != null )
                left = pop() ;
            Op opX = op.apply(transform, left, right) ; 
            push(opX) ;
        }
        
        @Override
        protected void visitN(OpN op)
        {
            List<Op> x = new ArrayList<Op>(op.size()) ;
            
            for ( Iterator<Op> iter = op.iterator() ; iter.hasNext() ; )
            {
                Op sub = iter.next() ;
                Op r = pop() ;
                // Skip nulls.
                if ( r != null )
                    // Add in reverse.
                    x.add(0, r) ;
            }
            Op opX = op.apply(transform, x) ;  
            push(opX) ;
        }
        
        @Override
        protected void visitExt(OpExt op)
        {
            push(transform.transform(op)) ;
        }
    }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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