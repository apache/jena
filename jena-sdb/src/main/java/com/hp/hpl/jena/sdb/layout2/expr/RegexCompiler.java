/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sdb.layout2.expr;

import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.compiler.ConditionCompiler;
import com.hp.hpl.jena.sdb.compiler.SDBConstraint;
import com.hp.hpl.jena.sdb.core.ExprPattern;
import com.hp.hpl.jena.sdb.core.Scope;
import com.hp.hpl.jena.sdb.core.sqlexpr.*;
import com.hp.hpl.jena.sdb.exprmatch.ActionMatch;
import com.hp.hpl.jena.sdb.exprmatch.ActionMatchString;
import com.hp.hpl.jena.sdb.exprmatch.ActionMatchVar;
import com.hp.hpl.jena.sdb.exprmatch.MapResult;
import com.hp.hpl.jena.sdb.layout2.ValueType;

public class RegexCompiler implements ConditionCompiler
{
    // --- regex : testing a term (in a variable)
    // Change to SSE?  ?? for meta variables  (regex ??a1 ??a2 ["i"])
    // Map of meta variable to action? 
    private static ExprPattern regex1 = new ExprPattern("regex(?a1, ?a2)",
                                                        new Var[]{ Var.alloc("a1") , Var.alloc("a2") },
                                                        new ActionMatch[]{ new ActionMatchVar() ,
                                                                      new ActionMatchString()}) ;
    
    private static ExprPattern regex1_i = new ExprPattern("regex(?a1, ?a2, 'i')",
                                                          new Var[]{ Var.alloc("a1") , Var.alloc("a2") },
                                                          new ActionMatch[]{ new ActionMatchVar() ,
                                                                        new ActionMatchString()}) ;

    
    @Override
    public SDBConstraint recognize(Expr expr)
    {
        MapResult rMap = null ;
        
        if ( ( rMap = regex1.match(expr) ) != null )
        {
            Var var = rMap.get("a1").getExprVar().asVar() ;
            String pattern = rMap.get("a2").getConstant().getString() ;
            return new RegexSqlGen(expr, regex1, pattern, null, true) ;
        }
        if ( ( rMap = regex1_i.match(expr) ) != null )
        {
            Var var = rMap.get(Var.alloc("a1")).getExprVar().asVar() ;
            String pattern = rMap.get(Var.alloc("a2")).getConstant().getString() ;
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
        
        Var var = rMap.get(Var.alloc("a1")).getExprVar().asVar() ;
        String pattern = rMap.get(Var.alloc("a2")).getConstant().getString() ;
        
        if ( ! scope.hasColumnForVar(var) )
        {
            LoggerFactory.getLogger(this.getClass()).error("Variable '"+var+"' not in scope") ;
            return null ;
        }
          
        SqlColumn vCol = scope.findScopeForVar(var).getColumn() ;

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
