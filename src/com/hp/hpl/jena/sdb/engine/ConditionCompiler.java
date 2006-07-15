/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine1.plan.PlanFilter;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.core.Scope;
import com.hp.hpl.jena.sdb.core.sqlexpr.*;
import com.hp.hpl.jena.sdb.exprmatch.Action;
import com.hp.hpl.jena.sdb.exprmatch.ActionMatchString;
import com.hp.hpl.jena.sdb.exprmatch.ActionMatchVar;
import com.hp.hpl.jena.sdb.exprmatch.MapResult;
import com.hp.hpl.jena.sdb.layout2.ValueType;

public class ConditionCompiler
{
    private static Log log = LogFactory.getLog(ConditionCompiler.class) ;
    
    private ConditionCompiler() {}
    
    // -------- Constraints

    // --- regex : testing a term (in a variable)
    private static ExprPattern regex1 = new ExprPattern("regex(?a1, ?a2)",
                                                        new String[]{ "a1" , "a2" },
                                                        new Action[]{ new ActionMatchVar() ,
                                                                      new ActionMatchString()}) ;
    
    private static ExprPattern regex2 = new ExprPattern("regex(?a1, ?a2, 'i')",
                                                        new String[]{ "a1" , "a2" },
                                                        new Action[]{ new ActionMatchVar() ,
                                                                      new ActionMatchString()}) ;
    // --- regex : testing the lexical form of a term (in a variable)
    private static ExprPattern regex3 = new ExprPattern("regex(str(?a1), ?a2)",
                                                        new String[]{ "a1" , "a2" },
                                                        new Action[]{ new ActionMatchVar() ,
                                                                      new ActionMatchString()}) ;
    private static ExprPattern regex4 = new ExprPattern("regex(str(?a1), ?a2, 'i')",
                                                        new String[]{ "a1" , "a2" },
                                                        new Action[]{ new ActionMatchVar() ,
                                                                      new ActionMatchString()}) ;
    
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

    private static ExprPattern equalsString3 = new ExprPattern("str(?a1) = ?a2",
                                                               new String[]{ "a1" , "a2" },
                                                               new Action[]{ new ActionMatchVar() ,
                                                                             new ActionMatchString()}) ;
    // As equalsString3 but reverse the arguments.
    private static ExprPattern equalsString4 = new ExprPattern("?a1 = str(?a2)",
                                                               new String[]{ "a1" , "a2" },
                                                               new Action[]{ new ActionMatchString() ,
                                                                             new ActionMatchVar() }) ;

    // Better structure ???????????
    
    public static SDBConstraint match(PlanFilter planFilter)
    {
        Expr expr = planFilter.getConstraint().getExpr() ;
        
        // Need to set the partial flag better.
        if ( regex1.match(expr)         != null ||
             startsWith1.match(expr)    != null || 
             equalsString1.match(expr)  != null )
        {
            return new SDBConstraint(expr, true) ;    
        }

        return null ;
    }
    
    public static SqlExpr compile(SDBConstraint planConstraint, Scope scope)
    {
        try {
            return compile(planConstraint.getExpr(), scope) ;
        } catch (NullPointerException ex)
        {
            throw new SDBException("Couldn't compile: "+planConstraint) ;
        }
            
    }
    
    // Layout 2 only here.
    private static SqlExpr compile(Expr expr, Scope scope)
    {
        MapResult rMap = null ;
        if ( (rMap = regex1.match(expr)) != null )
        {
            //log.info("Matched: ?a1 = "+rMap.get("a1")+" : ?a2 = "+rMap.get("a2")) ;
            Var var = new Var(rMap.get("a1").getVar()) ;
            String pattern = rMap.get("a2").getConstant().getString() ;
            
            SqlColumn vCol = scope.getColumnForVar(var) ;

            // LAYOUT2
            // Ensure its the lex column
            SqlColumn lexCol = new SqlColumn(vCol.getTable(), "lex") ;
            SqlColumn vTypeCol = new SqlColumn(vCol.getTable(), "type") ;

            // "is a string"
            SqlExpr isStr = new S_Equal(vTypeCol, new SqlConstant(ValueType.STRING.getTypeId())) ;
            isStr.addNote("is a string" ) ;
            
            // regex.
            SqlExpr sCond = new S_Regex(vCol, pattern, null) ;
            sCond.addNote(expr.toString()) ;
            
            SqlExpr sqlExpr = new S_And(isStr, sCond) ;
            return sqlExpr ;
        }
        
        if ( (rMap = startsWith1.match(expr)) != null )
        {
            log.info("startsWith - Matched: ?a1 = "+rMap.get("a1")+" : ?a2 = "+rMap.get("a2")) ;
            Var var = new Var(rMap.get("a1").getVar()) ;
            String pattern = rMap.get("a2").getConstant().getString() ;
            // Unfinished
            return null ;
            // becomes; isNotNull(var) AND var LIKE 'pattern%'
        }
        
        if ( ( rMap = equalsString1.match(expr)) != null )
        {
            // TODO WRONG later - the str() form should not have the same type check
            // still needs to check for bNodes.  How - this is layout1 as well? 
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
        
        // Not recognized
        return null ;

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