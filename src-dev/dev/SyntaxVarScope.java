/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Iterator ;
import java.util.Set ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.syntax.PatternVars ;

/** Calculate in-scope variables from the AST */ 
public class SyntaxVarScope
{
    
    static void check(Query query)
    {
        VarExprList exprList = query.getProject() ;
        Set<Var> vars = PatternVars.vars(query.getQueryPattern()) ;
        // + Group by.
        for ( Iterator<Var> iter = query.getGroupBy().getVars().iterator() ;
            iter.hasNext(); )
        {
            Var v = iter.next();
            if ( Var.isNamedVar(v) ) 
            vars.add(v) ;
        }
        
        for ( Iterator<Var> iter = exprList.getVars().iterator() ; iter.hasNext() ; )
        {
            // In scope?
            Var v = iter.next();
            Expr e = exprList.getExpr(v) ;
            if ( vars.contains(v) ) 
            {
                System.out.println("Var already in-scope: "+v) ;
                continue ;
            }
            vars.add(v) ;
        }
    }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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