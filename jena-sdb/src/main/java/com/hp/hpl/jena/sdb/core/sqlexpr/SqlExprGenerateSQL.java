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

package com.hp.hpl.jena.sdb.core.sqlexpr;

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.sdb.sql.SQLUtils;
import com.hp.hpl.jena.sdb.util.RegexUtils;

public class SqlExprGenerateSQL implements SqlExprVisitor
{
    /* Precedence (from MySQL)
     * :=
||, OR, XOR
&&, AND
NOT
BETWEEN, CASE, WHEN, THEN, ELSE
=, <=>, >=, >, <=, <, <>, !=, IS, LIKE, REGEXP, IN
|
&
<<, >>
-, +
*, /, DIV, %, MOD
^
- (unary minus), ~ (unary bit inversion)
!
BINARY, COLLATE
     */
    /* Decreasing : PostgreSQL
Operator/Element    Associativity   Description
.   left    table/column name separator
::  left    PostgreSQL-style typecast
[ ] left    array element selection
-   right   unary minus
^   left    exponentiation
* / %   left    multiplication, division, modulo
+ - left    addition, subtraction
IS      IS TRUE, IS FALSE, IS UNKNOWN, IS NULL
ISNULL      test for null
NOTNULL     test for not null
(any other) left    all other native and user-defined operators
IN      set membership
BETWEEN     range containment
OVERLAPS        time interval overlap
LIKE ILIKE SIMILAR      string pattern matching
< >     less than, greater than
=   right   equality, assignment
NOT right   logical negation
AND left    logical conjunction
OR  left    logical disjunction
*/
    
    private IndentedWriter out ;

    SqlExprGenerateSQL(IndentedWriter out)
    { this.out = out ; }

    @Override
    public void visit(SqlColumn column)     { out.print(column.asString()) ; }
    
    @Override
    public void visit(SqlConstant constant) { out.print(constant.asSqlString()) ; }
    
    @Override
    public void visit(SqlFunction1 expr)
    {
        out.print(expr.getFuncSymbol()) ;
        out.print("(") ;
        expr.getExpr().visit(this) ;
        out.print(")") ;
    }

    @Override
    public void visit(SqlExpr1 expr)
    {
        printExpr(expr.getExpr()) ;
        out.print(" ") ;
        out.print(expr.getExprSymbol()) ;
    }

    @Override
    public void visit(SqlExpr2 expr)
    {
        printExpr(expr.getLeft()) ;
        out.print(" ") ;
        out.print(expr.getOpSymbol()) ;
        out.print(" ") ;
        printExpr(expr.getRight()) ;
    }

    @Override
    public void visit(S_Like pattern)
    {
        if ( pattern.isCaseInsensitive() )
        {
            out.print("lower(") ;
            pattern.getExpr().visit(this) ;
            out.print(") LIKE ") ;
            out.print(SQLUtils.quoteStr(pattern.getPattern().toLowerCase())) ;
        }
        else
        {
            pattern.getExpr().visit(this) ;
            out.print(" LIKE ") ;
            out.print(SQLUtils.quoteStr(pattern.getPattern())) ;
        }
        return ;
    }
    
    public String RegexOperator = "REGEXP" ; 
    
    @Override
    public void visit(S_Regex regex)
    {
        // TODO Make per-store dependent for syntax and case sensitiveity reasons.
        // including "binary" for MySQL
        regex.getExpr().visit(this) ;
        
        String pattern = regex.getPattern() ;
        String patternLike = RegexUtils.regexToLike(pattern) ;
        if ( patternLike != null )
        {
            out.print(" LIKE ") ;
            out.print(SQLUtils.quoteStr(patternLike)) ;
            return ;
        }
        
        // MySQL :: LIKE // LIKE BINARY
        out.print(" ") ; out.print(RegexOperator) ; out.print(" ") ;
        if ( regex.getFlags() != null && ! regex.getFlags().equals("i") )
            out.print("BINARY ") ;
        out.print(SQLUtils.quoteStr(regex.getPattern())) ;
    }
    
    private void printExpr(SqlExpr expr)
    {
        boolean atomic = expr.isColumn() || expr.isConstant() ;
        if ( ! atomic )
            out.print("( ") ;
        expr.visit(this) ;
        if ( ! atomic )
            out.print(" )") ;
    }
    
    
}
