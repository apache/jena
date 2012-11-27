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
import org.apache.jena.atlas.io.IndentedWriter;

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
    

    @Override
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
    
    @Override
    public void output(IndentedWriter out)
    {  out.print(toString()) ; }

}
