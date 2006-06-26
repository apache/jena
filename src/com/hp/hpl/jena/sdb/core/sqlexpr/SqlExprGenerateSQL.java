/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlexpr;

import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.sdb.sql.SQLUtils;

public class SqlExprGenerateSQL implements SqlExprVisitor
{
    private IndentedWriter out ;

    SqlExprGenerateSQL(IndentedWriter out)
    {
        this.out = out ;
    }

    public void visit(SqlColumn column)     { out.print(column.asString()) ; }
    
    public void visit(SqlConstant constant) { out.print(constant.asSqlString()) ; }
    
    public void visit(SqlExpr1 expr)
    {
        out.print(expr.getFuncSymbol()) ;
        out.print("(") ;
        expr.visit(this) ;
        out.print(")") ;
    }

    public void visit(SqlExpr2 expr)
    {
        expr.getLeft().visit(this) ;
        out.print(" ") ;
        out.print(expr.getOpSymbol()) ;
        out.print(" ") ;
        expr.getRight().visit(this) ;
    }

    public void visit(S_Regex regex)
    {
        // Err ... need to choose bewteen regex and LIKE
        // TODO This code assumes a LIKE string.

        regex.getExpr().visit(this) ;
        // MySQL :: LIKE // LIKE BINARY
        out.print(" LIKE ") ;
        out.print(SQLUtils.quote(regex.getPattern())) ;
        
        
        if ( false )
        {
            out.print("regex") ;
            out.print("(") ;
            regex.getExpr().visit(this) ;
            out.print(", ") ;
            out.print(SQLUtils.quote(regex.getPattern())) ;
            if ( regex.getFlags() != null && !regex.getFlags().equals("") )
            {
                out.print(", ") ;
                out.print(regex.getFlags()) ;
            }
            out.print(")") ;
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