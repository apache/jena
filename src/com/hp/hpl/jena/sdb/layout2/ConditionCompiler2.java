/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine1.plan.PlanFilter;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.core.ExprCompile;
import com.hp.hpl.jena.sdb.core.ExprToSqlCompiler;
import com.hp.hpl.jena.sdb.core.Scope;
import com.hp.hpl.jena.sdb.core.sqlexpr.*;
import com.hp.hpl.jena.sdb.engine.ExprPattern;
import com.hp.hpl.jena.sdb.engine.SDBConstraint;
import com.hp.hpl.jena.sdb.exprmatch.Action;
import com.hp.hpl.jena.sdb.exprmatch.ActionMatchString;
import com.hp.hpl.jena.sdb.exprmatch.ActionMatchVar;
import com.hp.hpl.jena.sdb.exprmatch.MapResult;
import com.hp.hpl.jena.sdb.store.ConditionCompiler;

public class ConditionCompiler2 implements ConditionCompiler
{
    private static Log log = LogFactory.getLog(ConditionCompiler2.class) ;
    
    public ConditionCompiler2() {}
    
    // -------- Constraints

    // --- regex : testing a term (in a variable)
    private static ExprPattern regex1 = new ExprPattern("regex(?a1, ?a2)",
                                                        new String[]{ "a1" , "a2" },
                                                        new Action[]{ new ActionMatchVar() ,
                                                                      new ActionMatchString()}) ;
    
    private static ExprPattern regex1_i = new ExprPattern("regex(?a1, ?a2, 'i')",
                                                           new String[]{ "a1" , "a2" },
                                                           new Action[]{ new ActionMatchVar() ,
                                                                      new ActionMatchString()}) ;
    
    private static ExprCompile regex1_compile = new ExprCompile() 
    {
        public SqlExpr compile(Expr expr, ExprPattern exprPattern, Scope scope)
        {
            MapResult rMap = regex1.match(expr) ;
            if ( rMap == null )
                throw new SDBException("Couldn't compile after all: "+expr) ;
            Var var = new Var(rMap.get("a1").getVar()) ;
            String pattern = rMap.get("a2").getConstant().getString() ;
            
            SqlColumn vCol = scope.getColumnForVar(var) ;

            // Ensure it's the lex column
            SqlColumn lexCol = new SqlColumn(vCol.getTable(), "lex") ;
            SqlColumn vTypeCol = new SqlColumn(vCol.getTable(), "type") ;
            
            // "is a string"
            SqlExpr isStr = new S_Equal(vTypeCol, new SqlConstant(ValueType.STRING.getTypeId())) ;
            isStr.addNote("is a string" ) ;
            
            // regex.
            SqlExpr sCond = new S_Regex(vCol, pattern, 
                                        (exprPattern==regex1_i)?"i":null) ;
            sCond.addNote(expr.toString()) ;
            SqlExpr sqlExpr = new S_And(isStr, sCond) ;
            return sqlExpr ;
        }
    } ;
    
    
    // --- regex : testing the lexical form of a term (in a variable)
    private static ExprPattern regex2 = new ExprPattern("regex(str(?a1), ?a2)",
                                                        new String[]{ "a1" , "a2" },
                                                        new Action[]{ new ActionMatchVar() ,
                                                                      new ActionMatchString()}) ;
    private static ExprPattern regex2_i = new ExprPattern("regex(str(?a1), ?a2, 'i')",
                                                        new String[]{ "a1" , "a2" },
                                                        new Action[]{ new ActionMatchVar() ,
                                                                      new ActionMatchString()}) ;
    
    private static ExprCompile regex2_compile = new ExprCompile() 
    {
        public SqlExpr compile(Expr expr, ExprPattern exprPattern, Scope scope)
        {
            MapResult rMap = exprPattern.match(expr) ;
            if ( rMap == null )
                throw new SDBException("Couldn't compile after all: "+expr) ;
            Var var = new Var(rMap.get("a1").getVar()) ;
            String pattern = rMap.get("a2").getConstant().getString() ;
            
            SqlColumn vCol = scope.getColumnForVar(var) ;

            // Ensure it's the lex column
            SqlColumn lexCol = new SqlColumn(vCol.getTable(), "lex") ;
            SqlColumn vTypeCol = new SqlColumn(vCol.getTable(), "type") ;
            
            // "not a bNode"
            SqlExpr isStr = new S_NotEqual(vTypeCol, new SqlConstant(ValueType.BNODE.getTypeId())) ;
            isStr.addNote("not a bNode" ) ;
            
            // regex.
            SqlExpr sCond = new S_Regex(vCol, pattern, 
                                        (exprPattern==regex2_i)?"i":null) ;
            sCond.addNote(expr.toString()) ;
            SqlExpr sqlExpr = new S_And(isStr, sCond) ;
            return sqlExpr ;
        }
    } ;
    
    // --- starts-with
    private static ExprPattern startsWith1 = new ExprPattern("fn:starts-with(?a1, ?a2)",
                                                             new String[]{ "a1" , "a2" },
                                                             new Action[]{ new ActionMatchVar() ,
                                                                           new ActionMatchString()}) ;

    private static ExprPattern startsWith2 = new ExprPattern("fn:starts-with(str(?a1), ?a2)",
                                                             new String[]{ "a1" , "a2" },
                                                             new Action[]{ new ActionMatchVar() ,
                                                                           new ActionMatchString()}) ;
    
    private static ExprPattern equalsString1 = new ExprPattern("?a1 = ?a2",
                                                               new String[]{ "a1" , "a2" },
                                                               new Action[]{ new ActionMatchVar() ,
                                                                             new ActionMatchString()}) ;
    // As equalsString1 but reverse the arguments.
    private static ExprPattern equalsString2 = new ExprPattern("?a1 = ?a2",
                                                               new String[]{ "a1" , "a2" },
                                                               new Action[]{ new ActionMatchString() ,
                                                                             new ActionMatchVar() }) ;
    
    private static ExprCompile equalsString1_compile = new ExprCompile() 
    {
        public SqlExpr compile(Expr expr, ExprPattern exprPattern, Scope scope)
        {
            MapResult rMap = exprPattern.match(expr) ;
            if ( rMap == null )
                throw new SDBException("Couldn't compile after all: "+expr) ;
          //log.info("equalsString - Matched: ?a1 = "+rMap.get("a1")+" : ?a2 = "+rMap.get("a2")) ;
          Var var = new Var(rMap.get("a1").getVar()) ;
          String str = rMap.get("a2").getConstant().getString() ;
          SqlColumn vCol = scope.getColumnForVar(var) ;
          SqlColumn lexCol = new SqlColumn(vCol.getTable(), "lex") ;
          SqlColumn vTypeCol = new SqlColumn(vCol.getTable(), "type") ;
          
          // "is a string"
          SqlExpr isStr = new S_Equal(vTypeCol, new SqlConstant(ValueType.STRING.getTypeId())) ;
          isStr.addNote("is a string" ) ;
          // Equality
          SqlExpr strEquals = new S_Equal(lexCol, new SqlConstant(str)) ;
          isStr.addNote(expr.toString()) ; 
          return new S_And(isStr, strEquals) ;
        } 
    } ;

    private static ExprPattern equalsString3 = new ExprPattern("str(?a1) = ?a2",
                                                               new String[]{ "a1" , "a2" },
                                                               new Action[]{ new ActionMatchVar() ,
                                                                             new ActionMatchString()}) ;
    // As equalsString3 but reverse the arguments.
    private static ExprPattern equalsString4 = new ExprPattern("?a1 = str(?a2)",
                                                               new String[]{ "a1" , "a2" },
                                                               new Action[]{ new ActionMatchString() ,
                                                                             new ActionMatchVar() }) ;
    
    // ********
    
    static ExprCompile notImplemented = new ExprCompile(){

        public SqlExpr compile(Expr expr, ExprPattern ePattern, Scope scope)
        {
            log.warn("Not implemented: compile: "+expr) ;
            throw new SDBException("Not implemented: compile: "+expr) ;
        }
    } ;

    // Associates patterns with the code to do something.
    // Remember, the code is executed on the substituted expression, not the one matched.
    static ExprToSqlCompiler reg[] = { 
        new ExprToSqlCompiler(regex1, regex1_compile) ,
        new ExprToSqlCompiler(regex1_i, regex1_compile) ,
        new ExprToSqlCompiler(regex2, regex2_compile) ,
        new ExprToSqlCompiler(regex2_i, regex2_compile) ,
        new ExprToSqlCompiler(equalsString1, equalsString1_compile) ,
    } ;

    public SDBConstraint recognize(PlanFilter planFilter)
    {
        Expr expr = planFilter.getConstraint().getExpr() ;
        
        for ( int i = 0 ; i < reg.length ; i++ )
        {
            MapResult rMap = reg[i].getPattern().match(expr) ; 
            if (  rMap != null )
                return new SDBConstraint(expr, reg[i], true) ;   
        }
        return null ;
    }
    
    public SqlExpr compile(SDBConstraint planConstraint, Scope scope)
    {
        try {
            Expr expr = planConstraint.getExpr() ;
            ExprToSqlCompiler c = planConstraint.getSqlExprCompiler() ;
            return c.getMaker().compile(expr, c.getPattern(), scope) ;
        } catch (NullPointerException ex)
        {
            throw new SDBException("Couldn't compile: "+planConstraint) ;
        }
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