/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2.expr;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.engine.Binding;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.core.ExprPattern;
import com.hp.hpl.jena.sdb.core.Scope;
import com.hp.hpl.jena.sdb.core.sqlexpr.*;
import com.hp.hpl.jena.sdb.engine.compiler.ConditionCompiler;
import com.hp.hpl.jena.sdb.engine.compiler.SDBConstraint;
import com.hp.hpl.jena.sdb.exprmatch.Action;
import com.hp.hpl.jena.sdb.exprmatch.ActionMatchString;
import com.hp.hpl.jena.sdb.exprmatch.ActionMatchVar;
import com.hp.hpl.jena.sdb.exprmatch.MapResult;
import com.hp.hpl.jena.sdb.layout2.ValueType;

public class StringExprCompiler implements ConditionCompiler
{
    private static ExprPattern equalsString1 = new ExprPattern("?a1 = ?a2",
                                                               new Var[]{ Var.alloc("a1") , Var.alloc("a2") },
                                                               new Action[]{ new ActionMatchVar() ,
                                                                             new ActionMatchString()}) ;
    // As equalsString1 but reverse the arguments.
    private static ExprPattern equalsString2 = new ExprPattern("?a2 = ?a1",
                                                               new Var[]{ Var.alloc("a1") , Var.alloc("a2") },
                                                               new Action[]{ new ActionMatchVar() ,
                                                                             new ActionMatchString()}) ;
//    private static ExprPattern equalsString3 = new ExprPattern("str(?a1) = ?a2",
//                                                               new Var[]{ Var.alloc("a1") , Var.alloc("a2") },
//                                                               new Action[]{ new ActionMatchVar() ,
//                                                                             new ActionMatchString()}) ;
////  As equalsString3 but reverse the arguments.
//    private static ExprPattern equalsString4 = new ExprPattern("?a1 = str(?a2)",
//                                                               new Var[]{ Var.alloc("a1") , Var.alloc("a2") },
//                                                               new Action[]{ new ActionMatchString() ,
//                                                                             new ActionMatchVar() }) ;


    public SDBConstraint recognize(Expr expr)
    {
        MapResult rMap = null ;
        
        if ( ( rMap = equalsString1.match(expr) ) != null )
        {
            Var var = rMap.get("a1").getNodeVar().asVar() ;
            String str = rMap.get("a2").getConstant().getString() ;
            return new StringEqualsSqlGen(expr, equalsString1, true) ;
        }
        return null ;
    }
}

class StringEqualsSqlGen extends SDBConstraint
{
    ExprPattern exprPattern ;
    boolean completeConstraint ;
    
    public StringEqualsSqlGen(Expr expr, ExprPattern exprPattern, boolean completeConstraint)
    {
        super(expr, completeConstraint) ;
        this.exprPattern = exprPattern ;
    }

    @Override
    public SDBConstraint substitute(Binding binding)
    {
        return new StringEqualsSqlGen(getExpr().copySubstitute(binding),
                                      exprPattern,
                                      super.isComplete()) ;
    }

    @Override
    public SqlExpr compile(Scope scope)
    {
        MapResult rMap = exprPattern.match(getExpr()) ;
        
        if ( rMap == null )
            throw new SDBException("Couldn't compile after all: "+getExpr()) ;
          //log.info("equalsString - Matched: ?a1 = "+rMap.get("a1")+" : ?a2 = "+rMap.get("a2")) ;
        Var var = rMap.get("a1").getNodeVar().asVar() ;
        String str = rMap.get("a2").getConstant().getString() ;
        
        if ( ! scope.hasColumnForVar(var) )
        {
            LogFactory.getLog(this.getClass()).fatal("Variable '"+var+"' not in scope") ;
            return null ;
        }
          
        SqlColumn vCol = scope.getColumnForVar(var).getColumn() ;
        SqlColumn lexCol = new SqlColumn(vCol.getTable(), "lex") ;
        SqlColumn vTypeCol = new SqlColumn(vCol.getTable(), "type") ;
        
        // "is a string"
        SqlExpr isStr = new S_Equal(vTypeCol, new SqlConstant(ValueType.STRING.getTypeId())) ;
        isStr.addNote("is a string" ) ;
        // Equality
        SqlExpr strEquals = new S_Equal(lexCol, new SqlConstant(str)) ;
        isStr.addNote(getExpr().toString()) ; 
        return new S_And(isStr, strEquals) ;
    }
    
//  // --- starts-with
//  private static ExprPattern startsWith1 = new ExprPattern("fn:starts-with(?a1, ?a2)",
//                                                           new String[]{ "a1" , "a2" },
//                                                           new Action[]{ new ActionMatchVar() ,
//                                                                         new ActionMatchString()}) ;
//
//  private static ExprPattern startsWith2 = new ExprPattern("fn:starts-with(str(?a1), ?a2)",
//                                                           new String[]{ "a1" , "a2" },
//                                                           new Action[]{ new ActionMatchVar() ,
//                                                                         new ActionMatchString()}) ;
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