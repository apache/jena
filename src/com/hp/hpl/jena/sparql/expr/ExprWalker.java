/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

// Walk the expression tree

public class ExprWalker //implements ExprVisitor 
{
    ExprVisitor visitor ;
    
    public ExprWalker(ExprVisitor visitor)
    {
        this.visitor = visitor ;
    }
    
    public void walk(Expr expr) { expr.visit(visitor) ; }

    static public void walk(ExprVisitor visitor, Expr expr)
    { expr.visit(new WalkerTopDown(visitor)) ; }
    
    
    
    static class Walker implements ExprVisitor
    {
        ExprVisitor visitor ;
        boolean topDown = true ;
        
        private Walker(ExprVisitor visitor, boolean topDown)
        { 
            this.visitor = visitor ;
            this.topDown = topDown ;
        }
        
        public void startVisit() {}
        
        public void visit(ExprFunction func)
        {
            if ( topDown )
                func.visit(visitor) ;    
            for ( int i = 1 ; i <= func.numArgs() ; i++ )
            {
                Expr expr = func.getArg(i) ;
                if ( expr == null )
                    break ; 
                expr.visit(this) ;
            }
            if ( !topDown )
                func.visit(visitor) ;
        }
        
        public void visit(ExprFunctionOp funcOp)
        { funcOp.visit(visitor) ; }
        
        public void visit(NodeValue nv)   { nv.visit(visitor) ; }
        public void visit(ExprVar nv)     { nv.visit(visitor) ; }
        
        public void finishVisit() { }
    }
    
    // Visit current element then visit subelements
    public static class WalkerTopDown extends Walker
    {
        private WalkerTopDown(ExprVisitor visitor)
        { super(visitor, true) ; }
    }

    // Visit current element then visit subelements
    public static class WalkerBottomUp extends Walker
    {
        private WalkerBottomUp(ExprVisitor visitor)
        { super(visitor, false) ; }
    }

}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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