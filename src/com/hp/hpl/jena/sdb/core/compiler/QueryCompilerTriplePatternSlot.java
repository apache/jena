/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.compiler;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.util.FmtUtils;
import com.hp.hpl.jena.sdb.core.CompileContext;
import com.hp.hpl.jena.sdb.core.sqlexpr.S_Equal;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlRestrict;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.layout2.TableTriples;

/**
 * Compile a query requiring only dealing with constant slots in triples and knowing the triples table name.
 */

public abstract class QueryCompilerTriplePatternSlot extends QueryCompilerTriplePattern
{
    @Override
    final
    protected SqlNode match(CompileContext context, Triple triple)
    {
        String alias = context.allocTableAlias() ;
        SqlExprList conditions = new SqlExprList() ;
        
        SqlTable triples = accessTriplesTable(alias) ;
        triples.addNote(FmtUtils.stringForTriple(triple, context.getQuery().getPrefixMapping())) ;
        
        processSlot(context, triples, conditions, triple.getSubject(),   TableTriples.subjectCol) ; 
        processSlot(context, triples, conditions, triple.getPredicate(), TableTriples.predicateCol) ;
        processSlot(context, triples, conditions, triple.getObject(),    TableTriples.objectCol) ;
        
        if ( conditions.size() == 0 )
            return triples ;
        
        return SqlRestrict.restrict(triples, conditions) ;
    }
    
    final
    protected void processSlot(CompileContext context,
                               SqlTable table, SqlExprList conditions,
                               Node node, String colName)
    {
        SqlColumn thisCol = new SqlColumn(table, colName) ;
        if ( ! node.isVariable() )
        {
            // Is this constant already loaded?
            constantSlot(context, node, thisCol, conditions) ;
            return ;
        }
        
        // In common
        Var var = new Var(node) ;
        if ( table.getIdScope().hasColumnForVar(var) )
        {
            SqlColumn otherCol = table.getIdScope().getColumnForVar(var) ;
            SqlExpr c = new S_Equal(otherCol, thisCol) ;
            conditions.add(c) ;
            c.addNote("processVar: "+node) ;
            return ;
        }
        table.setIdColumnForVar(var, thisCol) ;
    }
    
    /** Deal with an access to a constant in the query tripe pattern */
    protected abstract void constantSlot(CompileContext context, Node node, SqlColumn thisCol, SqlExprList conditions) ;
    
    /** Retrun an SqlTable for the relevant triples table (use the alias given) */ 
    protected abstract SqlTable accessTriplesTable(String alias) ;
}

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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