/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import java.util.*;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.util.IndentedLineBuffer;
import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.query.util.Utils;
import com.hp.hpl.jena.sdb.core.AnnotationsBase;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;


public abstract class SqlNodeBase extends AnnotationsBase implements SqlNode
{
    private String aliasName ;
    
    public SqlNodeBase(String aliasName) { this.aliasName = aliasName ; }
    
    public boolean isJoin() { return false ; }

    public SqlJoin getJoin() { classError(SqlJoin.class) ; return null  ; }

    public boolean isRestrict() { return false ; }

    public SqlRestrict getRestrict() { classError(SqlRestrict.class) ; return null  ; }

    public boolean isProject() { return false ; }
    
    public SqlProject getProject()  { classError(SqlProject.class) ; return null  ; }

    public boolean isTable() { return false ; }

    public SqlTable getTable() { classError(SqlTable.class) ; return null  ; }

    public void output(IndentedWriter out)
    {
        this.visit(new SqlNodeTextVisitor(out)) ;
    }
    
    // Scope
    
    public boolean hasColumnForVar(Var var) { return getColumnForVar(var) != null ; }
//    public Iterator<Var> vars()
//    { return getVars().iterator() ; }
    
    public boolean usesColumn(SqlColumn c) { return false ; }
    
    final
    public String getAliasName() { return aliasName ; }

    private void classError(Class wanted)
    {
        throw new ClassCastException("Wanted class: "+Utils.className(wanted)+" :: Actual class "+Utils.className(this) ) ;
    }
    
    public Collection<SqlTable> tablesInvolved()
    {
        TableFinder t = new TableFinder() ;
        SqlNodeWalker.walk(this, t) ;
        return t.acc ;
    }

    @Override public String toString()
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        output(buff.getIndentedWriter()) ;
        return buff.asString() ;
    }
}

class TableFinder implements SqlNodeVisitor
{
    Set<SqlTable> acc = new HashSet<SqlTable>() ;
    
    public void visit(SqlProject sqlNode)   {}

    public void visit(SqlRestrict sqlNode)  {}

    public void visit(SqlTable sqlNode)
    {
        acc.add(sqlNode) ;
    }

    public void visit(SqlJoinInner sqlNode) {}

    public void visit(SqlJoinLeftOuter sqlNode) {}
    
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