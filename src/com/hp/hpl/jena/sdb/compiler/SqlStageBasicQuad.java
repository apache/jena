/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.compiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.core.AliasesSql;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.layout2.TableDescQuads;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import org.openjena.atlas.io.IndentedWriter;

public class SqlStageBasicQuad implements SqlStage
{
    private static Logger log = LoggerFactory.getLogger(SqlStageBasicQuad.class) ;
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
        boolean defaultGraph = Quad.isDefaultGraph(quad.getGraph()) ; // ARQ 2.8.4 quad.isDefaultGraph() ;
        boolean unionGraph = quad.isUnionGraph() ;
        
        // ---- Choose the mode of access.
        
        boolean accessStoredDefaultGraph = Quad.isDefaultGraph(quad.getGraph()); //quad.isDefaultGraph() ;
        boolean accessUnionGraph = false ;
        
        if ( accessStoredDefaultGraph && request.getContext().isTrue(SDB.unionDefaultGraph) )
        {
            // Treat the default graph as the union of all triples in named graphs.
            defaultGraph = false ;
            unionGraph = true ;
            accessStoredDefaultGraph = false ;
            accessUnionGraph = true ;
        }
        
        // GRAPH <name of default graph> { } 
        if ( quad.isDefaultGraphExplicit() )
        {
            // "named" access to the default graph
            accessStoredDefaultGraph = true ;
            accessUnionGraph = false ;
        }
        
        if ( quad.isUnionGraph() )
        {
            // "named" access to the union of the named graphs
            accessStoredDefaultGraph = false ;
            accessUnionGraph = true ;
            // Add DISTINCT over the join of SqlStageBasicQuad for RDF merge 
        }
        
        // ---- Choose the table to access
        // The default graph table may be a specialized table (e.g. triples, not quads).
        
        TableDescQuads tableDesc = null ;
        String alias = null ;
        
        if ( accessStoredDefaultGraph )
        {
            tableDesc = request.getStore().getTripleTableDesc() ;
            alias = request.genId(AliasesSql.TriplesTableBase) ;
        }
        else
        {
            tableDesc = request.getStore().getQuadTableDesc() ;
            alias = request.genId(AliasesSql.QuadTableBase) ;
        }
        
        SqlTable table = new SqlTable(alias, tableDesc.getTableName()) ;
        if ( accessStoredDefaultGraph )
            table.addNote(FmtUtils.stringForTriple(quad.asTriple(), request.getPrefixMapping())) ;
        else
            table.addNote(FmtUtils.stringForQuad(quad, request.getPrefixMapping())) ;

        // Only constrain the G column 
        // IF there is a graph column (so it's not the triples table)
        // AND if we are not unioning the named graphs. 
        
        if ( ! accessStoredDefaultGraph && ! accessUnionGraph )
                slotCompiler.processSlot(request, table, conditions, quad.getGraph(),
                                     tableDesc.getGraphColName()) ;
        slotCompiler.processSlot(request, table, conditions, quad.getSubject(),
                                 tableDesc.getSubjectColName()) ; 
        slotCompiler.processSlot(request, table, conditions, quad.getPredicate(),
                                 tableDesc.getPredicateColName()) ;
        slotCompiler.processSlot(request, table, conditions, quad.getObject(),
                                 tableDesc.getObjectColName()) ;
        
        return SqlBuilder.restrict(request, table, conditions) ;
    }

    @Override
    public String toString() { return "Table: "+quad ; } 
    
    public void output(IndentedWriter out)
    {  out.print(toString()) ; }

}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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