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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.store.TableDesc;
import com.hp.hpl.jena.sparql.core.Quad;
import org.apache.jena.atlas.io.IndentedWriter;

/** A (description of a) table that holds a cached/optimized
 * version of a pattern.
 */ 

public class PatternTable extends TableDesc
{
    private static Logger log = LoggerFactory.getLogger(PatternTable.class) ;
    
    static final String subjColName = "subject" ;
    
    // Property => column name
    Map <Node, String> cols = new HashMap<Node, String>() ;
    
    public PatternTable(String tableName) { super(tableName) ; }    
    
    public void add(Node property, String colname)
    { cols.put(property, colname) ; }
    
    public Map <Node, String> getCols() { return cols ; }
    
    @Override
    public boolean hasColumn(String colName)
    { return cols.containsKey(colName) ; }
   
    @Override
    public Iterator<String> colNames()
    { return cols.values().iterator() ; }
    
    
    // trigger if we see a prediate this table supports.
    public boolean trigger(Quad quad)
    {
        for ( Node p : cols.keySet() )
        {
            if ( p.equals(quad.getPredicate()) )
                return true ;
        }
        return false ;
    }

    // Start a table from the i'th quad 
    // Must remove the quad at index i if return is not null.
    public SqlStage process(int i, QuadBlock quadBlock)
    {
        QuadBlock tableQuads = new QuadBlock() ;
        Set<Node> predicates = new HashSet<Node>(cols.keySet()) ;

        // Use the fact that i'th quad is the trigger  
        Quad trigger = quadBlock.get(i) ;
        Node subject = trigger.getSubject() ;
        Node graph = trigger.getGraph() ;
        
        for ( Node p : predicates )
        {
            int idx = quadBlock.findFirst(i, graph, subject, p, null) ;
            if ( idx < 0 )
                // No match.
                // Conservative - must find all predicates
                //return null ;
                // Liberal - any predicates
                continue ;
            
            Quad q = quadBlock.get(idx) ;
            tableQuads.add(q) ;
        }
        
        quadBlock.removeAll(tableQuads) ;
        SqlStagePatternTable stage = new SqlStagePatternTable(graph, subject, tableQuads) ;
        return stage ;
    }

    class SqlStagePatternTable implements SqlStage
    {
        private QuadBlock tableQuads ;
        private Node graphNode ;
        private Node subject ;

        public SqlStagePatternTable(Node graphNode, Node subject, QuadBlock tableQuads)
        { 
            this.tableQuads = tableQuads ;
            this.subject = subject ;
            this.graphNode = graphNode ;
        }

        @Override
        public SqlNode build(SDBRequest request, SlotCompiler slotCompiler)
        {
            SqlTable sqlTable = new SqlTable("ALIAS", "TABLE") ;
            
            SqlExprList conditions = new SqlExprList() ;
            
            if ( ! Quad.isDefaultGraphGenerated(graphNode) )
                log.error("Not the default graph in SqlStagePTable.build") ;
            if ( false )
                slotCompiler.processSlot(request, sqlTable, conditions, graphNode, subjColName) ;
            slotCompiler.processSlot(request, sqlTable, conditions, subject, subjColName) ;

            for ( Quad quad : tableQuads )
            {
                String colName = cols.get(quad.getPredicate()) ;
                SqlColumn col = new SqlColumn(sqlTable, colName) ;
                Node obj = quad.getObject() ;
                slotCompiler.processSlot(request, sqlTable, conditions, obj, colName) ;
            }

            if ( false )
            {
                for ( Node pred : cols.keySet() )
                {
                    int idx = tableQuads.findFirst(graphNode, subject, pred, null) ;
                    if ( idx < 0 )
                    {
                        // Liberal
                        continue ;
                        //log.error("Can't find quad in SqlStagePTable.build") ;
                        //throw new SDBException("SqlStagePTable.build") ;
                    }
                    
                    Quad q = tableQuads.remove(idx) ;
                    
                    String colName = cols.get(pred) ;
                    SqlColumn col = new SqlColumn(sqlTable, colName) ;
                    
                    Node obj = q.getObject() ;
    //                if ( Var.isVar(obj) )
    //                    sqlTable.setIdColumnForVar(Var.alloc(obj), col) ;
                    
                    slotCompiler.processSlot(request, sqlTable, conditions, obj, colName) ;
                }
            }
            return SqlBuilder.restrict(request, sqlTable, conditions) ;
        }
        
        @Override
        public String toString() { return "PTable"; }

        @Override
        public void output(IndentedWriter out)
        {  out.print("PTable") ; }
    }
}
