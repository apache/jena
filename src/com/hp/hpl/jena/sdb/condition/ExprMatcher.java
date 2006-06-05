/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.condition;

import java.util.Map;

import com.hp.hpl.jena.query.expr.*;
import com.hp.hpl.jena.query.util.ExprUtils;

/**
 * Matches an expression template to a query expression.  
 * @author Andy Seaborne
 * @version $Id:$
 */

public class ExprMatcher
{
    public interface MatchAction
    {
        void invoke(Expr x) ;
    }
    
    Expr pattern ; 
    
    public ExprMatcher(String template)
    {
        this(ExprUtils.parseExpr(template)) ;
    }

    public ExprMatcher(Expr pattern)
    { 
        this.pattern = pattern ;
    }
    
    // Takes a set of restrictions on the expression (bindings for named variables)
    // Returns what variables are bound to.
    public Object matches(Map<String, MatchAction> x, Expr expression)
    {
        
        return null ;
    }

}

class MatchWalker extends ExprWalker
{
    public MatchWalker(ExprVisitor visitor)
    {
        super(visitor) ;
        
    }
}

class MatchOne implements ExprVisitor
{
    //public void  
    
    public void startVisit()
    {}

    public void visit(ExprNode1 expr)
    {}

    public void visit(ExprNode2 expr)
    {}

    public void visit(ExprNodeFunction expr)
    {
        
    }

    public void visit(NodeValue nv)
    {}

    public void visit(NodeVar nv)
    {}

    public void finishVisit()
    {}
    
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