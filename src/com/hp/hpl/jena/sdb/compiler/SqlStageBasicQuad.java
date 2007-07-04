/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.compiler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.core.AliasesSql;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlRestrict;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.layout2.TableDescQuads;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

public class SqlStageBasicQuad implements SqlStage
{
    private static Log log = LogFactory.getLog(SqlStageBasicQuad.class) ;
    private Quad quad ;

    public SqlStageBasicQuad(Quad quad)
    {
        this.quad = quad ;
    }
    
    // There are three special graph names:
    //   Quad.defaultGraph - the quad marker for a query pattern for the default graph.
    //   Quad.defaultGraphName - the explicit internal name for the default graph
    //   Quad.unionDefaultGraph - the internal name for the union of all named graphs (excluding the degault graph).
    //
    // If SDB.unionDefaultGraph is true in the context, then patterns on the 
    // default graph are made against the union graph.
    //
    // If the explicit names (defaultGraphName, unionDefaultGraph) are used in GRAPH, 
    // then the default or union beahviour is avilable regardless of the setting of
    //  SDB.unionDefaultGraph.
    

    public SqlNode build(SDBRequest request, SlotCompiler slotCompiler)
    {
        SqlExprList conditions = new SqlExprList() ;
        boolean defaultGraph = quad.isDefaultGraph() ;
        boolean defaultUnionGraph = quad.isDefaultUnionGraph() ;
        
        if ( defaultGraph && request.getContext().isTrue(SDB.unionDefaultGraph) )
        {
            // Treat the default graph as the union of all triples in named graphs.
            defaultGraph = false ;
            defaultUnionGraph = true ;
        }
        
        // The default graph table may be a specialized table (e.g. triples, not quads).
        
        TableDescQuads tableDesc = null ;
        String alias = null ;
        
        if ( defaultGraph )
        {
            tableDesc = request.getStore().getTripleTableDesc() ;
            alias = request.genId(AliasesSql.TriplesTableBase) ;
        }
        else
        {
            tableDesc = request.getStore().getQuadTableDesc() ;
            alias = request.genId(AliasesSql.QuadTableBase) ;
        }
        
        SqlTable table = new SqlTable(tableDesc.getTableName(), alias) ;
        if ( defaultGraph )
            table.addNote(FmtUtils.stringForTriple(quad.getTriple(), request.getPrefixMapping())) ;
        else
            table.addNote(FmtUtils.stringForQuad(quad, request.getPrefixMapping())) ;

        // Only constrain the G column 
        // IF there is a graph column (so it's not the triples table)
        // AND if we are not unioning the named graphs. 
        
        if ( tableDesc.getGraphColName() != null && ! defaultUnionGraph )
                slotCompiler.processSlot(request, table, conditions, quad.getGraph(),
                                     tableDesc.getGraphColName()) ;
        slotCompiler.processSlot(request, table, conditions, quad.getSubject(),
                                 tableDesc.getSubjectColName()) ; 
        slotCompiler.processSlot(request, table, conditions, quad.getPredicate(),
                                 tableDesc.getPredicateColName()) ;
        slotCompiler.processSlot(request, table, conditions, quad.getObject(),
                                 tableDesc.getObjectColName()) ;
        
        return SqlRestrict.restrict(table, conditions) ;
    }
    
    private SqlTable accessTriplesTable(SDBRequest request, String alias)
    {
        return new SqlTable(request.getStore().getTripleTableDesc().getTableName(), alias) ;
    }

    @Override
    public String toString() { return "Table: "+quad ; } 
    
    public void output(IndentedWriter out)
    {  out.print(toString()) ; }

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