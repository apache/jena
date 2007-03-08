/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.compiler;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.ScopeEntry;
import com.hp.hpl.jena.sdb.core.sqlexpr.S_Equal;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sparql.core.Var;

public abstract class SlotCompiler
{
    private SDBRequest request ;
    
    public abstract SqlNode start(QuadBlock quads) ;
    public abstract SqlNode finish(SqlNode sqlNode, QuadBlock quads) ;
    
    public SlotCompiler(SDBRequest request)
    {
        this.request = request ;
    }
    
    protected SDBRequest getRequest() { return request ; }
    
    public final void processSlot(SDBRequest request,
                                  SqlTable table, SqlExprList conditions,
                                  Node node, String colName)
 {
     SqlColumn thisCol = new SqlColumn(table, colName) ;
     if ( ! node.isVariable() )
     {
         constantSlot(request, node, thisCol, conditions) ;
         return ;
     }
     
     Var var = Var.alloc(node) ;
     if ( table.getIdScope().hasColumnForVar(var) )
     {
         ScopeEntry e = table.getIdScope().findScopeForVar(var) ;
         SqlColumn otherCol = e.getColumn() ;
         SqlExpr c = new S_Equal(otherCol, thisCol) ;
         conditions.add(c) ;
         c.addNote("processVar: "+node) ;
         return ;
     }
     table.setIdColumnForVar(var, thisCol) ;
 }

    protected abstract void constantSlot(SDBRequest request, Node node, SqlColumn thisCol, SqlExprList conditions) ;
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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