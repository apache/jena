/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.expr;

import java.util.*;

import com.hp.hpl.jena.query.engine.binding.Binding;
import com.hp.hpl.jena.query.util.Context;

public class ExprList
{
    private List expressions = new ArrayList() ;
    
    public ExprList() {}
    public ExprList(Expr expr)
    {
        this() ;
        expressions.add(expr) ;
    }
    
    
    public boolean isSatisfied(Binding binding, Context cxt)
    {
        // Dream of generics
        for ( Iterator iter = expressions.iterator() ; iter.hasNext() ; )
        {
            Expr expr = (Expr)iter.next();
            if ( ! expr.isSatisfied(binding, cxt) )
                return false ;
        }
        return true ;
    }
    
    //public int size() { return expressions.size() ; }
    public boolean isEmpty() { return expressions.isEmpty() ; }
    
    public Set getVarsMentioned()
    {
        Set x = new HashSet() ;
        varsMentioned(x) ;
        return x ;
    }
    
    public void varsMentioned(Collection acc)
    {
        for ( Iterator iter = expressions.iterator() ; iter.hasNext() ; )
        {
            Expr expr = (Expr)iter.next();
            expr.varsMentioned(acc) ;
        }
    }
    
    public ExprList copySubstitute(Binding binding) { return copySubstitute(binding, true) ; }
    public ExprList copySubstitute(Binding binding, boolean foldConstants)
    {
        ExprList x = new ExprList() ;
        for ( Iterator iter = expressions.iterator() ; iter.hasNext() ; )
        {
            Expr expr = (Expr)iter.next();
            expr = expr.copySubstitute(binding, foldConstants) ;
            x.add(expr) ;
        }
        return x ;
    }
    public void addAll(ExprList exprs) { expressions.addAll(exprs.getList()) ; }
    public void add(Expr expr) { expressions.add(expr) ; }
    public List getList() { return expressions ; }
    public Iterator iterator() { return expressions.iterator() ; }
    
    public void prepareExprs(Context context)
    {
        // Give each expression the chance to set up (bind functions)
        for ( Iterator iter = expressions.iterator() ; iter.hasNext() ; )
        {
            Expr expr = (Expr)iter.next() ;
            ExprWalker.walk(new ExprBuild(context), expr) ;
        }
    }
    
    public String toString()
    { return expressions.toString() ; }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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