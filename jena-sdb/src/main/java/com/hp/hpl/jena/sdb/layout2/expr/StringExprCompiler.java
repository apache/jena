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

public class StringExprCompiler implements ConditionCompiler
{
    private static ExprPattern equalsString1 = new ExprPattern("?a1 = ?a2",
                                                               new Var[]{ Var.alloc("a1") , Var.alloc("a2") },
                                                               new ActionMatch[]{ new ActionMatchVar() ,
                                                                             new ActionMatchString()}) ;
    // As equalsString1 but reverse the arguments.
    private static ExprPattern equalsString2 = new ExprPattern("?a2 = ?a1",
                                                               new Var[]{ Var.alloc("a1") , Var.alloc("a2") },
                                                               new ActionMatch[]{ new ActionMatchVar() ,
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


    @Override
    public SDBConstraint recognize(Expr expr)
    {
        MapResult rMap = null ;
        
        if ( ( rMap = equalsString1.match(expr) ) != null )
        {
            Var var = rMap.get("a1").getExprVar().asVar() ;
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
        Var var = rMap.get("a1").getExprVar().asVar() ;
        String str = rMap.get("a2").getConstant().getString() ;
        
        if ( ! scope.hasColumnForVar(var) )
        {
            LoggerFactory.getLogger(this.getClass()).error("Variable '"+var+"' not in scope") ;
            return null ;
        }
        
        SqlColumn vCol = scope.findScopeForVar(var).getColumn() ;
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
