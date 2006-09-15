/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2.expr;

import com.hp.hpl.jena.query.core.Binding;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.core.Scope;
import com.hp.hpl.jena.sdb.core.sqlexpr.*;
import com.hp.hpl.jena.sdb.engine.ExprPattern;
import com.hp.hpl.jena.sdb.engine.SDBConstraint;
import com.hp.hpl.jena.sdb.exprmatch.Action;
import com.hp.hpl.jena.sdb.exprmatch.ActionMatchString;
import com.hp.hpl.jena.sdb.exprmatch.ActionMatchVar;
import com.hp.hpl.jena.sdb.exprmatch.MapResult;
import com.hp.hpl.jena.sdb.layout2.ValueType;
import com.hp.hpl.jena.sdb.store.ConditionCompilerBase;

public class RegexCompiler extends ConditionCompilerBase
{
    // --- regex : testing a term (in a variable)
    private static ExprPattern regex1 = new ExprPattern("regex(?a1, ?a2)",
                                                        new Var[]{ new Var("a1") , new Var("a2") },
                                                        new Action[]{ new ActionMatchVar() ,
                                                                      new ActionMatchString()}) ;
    
    private static ExprPattern regex1_i = new ExprPattern("regex(?a1, ?a2, 'i')",
                                                          new Var[]{ new Var("a1") , new Var("a2") },
                                                          new Action[]{ new ActionMatchVar() ,
                                                                        new ActionMatchString()}) ;
        
    @Override
    public SDBConstraint recognize(Expr expr)
    {
        MapResult rMap = null ;
        
        if ( ( rMap = regex1.match(expr) ) != null )
        {
            Object $ = rMap.get(new Var("a1")) ;
            
            
            Var var = rMap.get("a1").getNodeVar().getAsVar() ;
            String pattern = rMap.get("a2").getConstant().getString() ;
            return new RegexSqlGen(expr, regex1, pattern, null, true) ;
        }
        if ( ( rMap = regex1_i.match(expr) ) != null )
        {
            Var var = rMap.get(new Var("a1")).getNodeVar().getAsVar() ;
            String pattern = rMap.get(new Var("a2")).getConstant().getString() ;
            return new RegexSqlGen(expr, regex1_i, pattern, "i", true) ;
        }
        return null ;
    }
}

class RegexSqlGen extends SDBConstraint
{
    ExprPattern exprPattern ;
    String patternStr ;
    String flags ;
    boolean completeConstraint ;
    
    public RegexSqlGen(Expr expr, ExprPattern exprPattern, String patternStr, String flags, boolean completeConstraint)
    {
        super(expr, completeConstraint) ;
        this.exprPattern = exprPattern ;
        this.patternStr = patternStr ;
        this.flags = flags ;
        
    }

    @Override
    public SDBConstraint substitute(Binding binding)
    {
        return new RegexSqlGen(getExpr().copySubstitute(binding),
                                    exprPattern,
                                    patternStr,
                                    flags, completeConstraint) ;
    }

    @Override
    public SqlExpr compile(Scope scope)
    {
        // TODO Convert regex to using a string value table?
        MapResult rMap = exprPattern.match(getExpr()) ;
        if ( rMap == null )
            throw new SDBException("Couldn't compile after all: "+getExpr()) ;
        
        Var var = rMap.get(new Var("a1")).getNodeVar().getAsVar() ;
        String pattern = rMap.get(new Var("a2")).getConstant().getString() ;
        
        SqlColumn vCol = scope.getColumnForVar(var) ;

        // Ensure it's the lex column
        SqlColumn lexCol = new SqlColumn(vCol.getTable(), "lex") ;
        SqlColumn vTypeCol = new SqlColumn(vCol.getTable(), "type") ;
        
        // "is a string"
        SqlExpr isStr = new S_Equal(vTypeCol, new SqlConstant(ValueType.STRING.getTypeId())) ;
        isStr.addNote("is a string" ) ;
        
        // regex.
        SqlExpr sCond = new S_Regex(vCol, pattern, flags) ;
        sCond.addNote(getExpr().toString()) ;
        SqlExpr sqlExpr = new S_And(isStr, sCond) ;
        return sqlExpr ;
    }
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