/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlexpr;

import com.hp.hpl.jena.query.util.IndentedWriter;
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
    {
        this.out = out ;
    }

    public void visit(SqlColumn column)     { out.print(column.asString()) ; }
    
    public void visit(SqlConstant constant) { out.print(constant.asSqlString()) ; }
    
    public void visit(SqlFunction1 expr)
    {
        out.print(expr.getFuncSymbol()) ;
        out.print("(") ;
        expr.getExpr().visit(this) ;
        out.print(")") ;
    }

    public void visit(SqlExpr1 expr)
    {
        printExpr(expr.getExpr()) ;
        out.print(" ") ;
        out.print(expr.getExprSymbol()) ;
    }

    public void visit(SqlExpr2 expr)
    {
        printExpr(expr.getLeft()) ;
        out.print(" ") ;
        out.print(expr.getOpSymbol()) ;
        out.print(" ") ;
        printExpr(expr.getRight()) ;
    }

    public String RegexOperator = "REGEXP" ; 
    
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
        if ( regex.flags != null && ! regex.flags.equals("i") )
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