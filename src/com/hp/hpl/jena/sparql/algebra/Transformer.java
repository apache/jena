/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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
    static boolean noDupIfSame = true ;
    
    public static Op transform(Transform transform, Op op)
    { return transform(transform, op, null, null) ; }
    
    public static Op transform(Transform transform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        if ( op == null )
        {
            ALog.warn(Transformer.class, "Attempt to transform a null Op - ignored") ;
            return op ;
        }
        
        TransformApply v = new TransformApply(transform, beforeVisitor, afterVisitor) ;
        op.visit(v) ;
        Op r = v.result() ;
        return r ;
    }
    
    private Transformer() { }
    
    // Does not use OpWalker because we need to push/pop stack during visitX operations.
    
    private static final 
    class TransformApply extends OpVisitorByType
    {
        private OpVisitor beforeVisitor = null ;
        private OpVisitor afterVisitor = null ;
        
        private Transform transform = null ;
        private Stack stack = new Stack() ;
        private Op pop() { return (Op)stack.pop(); }
        
        private void push(Op op)
        { 
            // Including nulls
            stack.push(op) ;
        }
        
        public TransformApply(Transform transform,
                              OpVisitor beforeVisitor, 
                              OpVisitor afterVisitor)
        { 
            this.transform = transform ;
            this.beforeVisitor = beforeVisitor ;
            this.afterVisitor = afterVisitor ;
        }
        
        public Op result()
        { 
            if ( stack.size() != 1 )
                ALog.warn(this, "Stack is not aligned") ;
            return pop() ; 
        }

        private void before(Op op)
        { 
            if ( beforeVisitor != null )
                op.visit(beforeVisitor) ;
        }

        private void after(Op op)
        {
            if ( afterVisitor != null )
                op.visit(afterVisitor) ;
        }
        
        protected void visit0(Op0 op)
        {
            before(op) ;
            push(op.apply(transform)) ;
            after(op) ;
        }
        
        protected void visit1(Op1 op)
        {
            before(op) ;
            Op subOp = null ;
            if ( op.getSubOp() != null )
            {
                op.getSubOp().visit(this) ;
                subOp = pop() ;
            }
            push(op.apply(transform, subOp)) ;
            after(op) ;
        }

        protected void visit2(Op2 op)
        { 
            before(op) ;
            Op left = null ;
            Op right = null ;

            if ( op.getLeft() != null )
            {
                op.getLeft().visit(this) ;
                left = pop() ;
            }
            if ( op.getRight() != null )
            {
                op.getRight().visit(this) ;
                right = pop() ;
            }
            Op opX = op.apply(transform, left, right) ; 
            push(opX) ;
            after(op) ;

        }
        
        protected void visitN(OpN op)
        {
            before(op) ;
            List x = new ArrayList(op.size()) ;
            for ( Iterator iter = op.iterator() ; iter.hasNext() ; )
            {
                Op sub = (Op)iter.next() ;
                sub.visit(this) ;
                Op r = pop() ;
                // Skip nulls.
                if ( r != null )
                    x.add(r) ;
            }
            Op opX = op.apply(transform, x) ;  
            push(opX) ;
            after(op) ;

        }
        
        protected void visitExt(OpExt op)
        {
            before(op) ;
            push(transform.transform(op)) ;
            after(op) ;

        }
    }
}

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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