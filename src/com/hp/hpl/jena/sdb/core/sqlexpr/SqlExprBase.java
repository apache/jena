/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlexpr;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.sdb.core.AnnotationsBase;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer;

public abstract class SqlExprBase extends AnnotationsBase implements SqlExpr
{
    @Override
    public final String toString()
    {
        return asSQL(this) ;
    }
    
    public String asSQL() { return asSQL(this) ; } 
    
    public static String asSQL(SqlExpr expr)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        SqlExprVisitor v = new SqlExprGenerateSQL(buff.getIndentedWriter()) ;
        expr.visit(v) ;
        return buff.toString() ; 
    }
    
    public Set<SqlColumn> getColumnsNeeded()
    {
        Set<SqlColumn> acc = new HashSet<SqlColumn>() ;
        SqlExprVisitor v = new SqlExprColumnsUsed(acc) ;
        SqlExprWalker.walk(this, v) ;
        return acc ;
    }
    
    public boolean isColumn()   { return false ; }
    public boolean isConstant() { return false ; }

}

/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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