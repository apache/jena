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

package com.hp.hpl.jena.sdb.layout2.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.compiler.QuadBlock;
import com.hp.hpl.jena.sdb.compiler.SqlBuilder;
import com.hp.hpl.jena.sdb.core.AliasesSql;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlexpr.S_Equal;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlConstant;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.layout2.NodeLayout2;
import com.hp.hpl.jena.sdb.layout2.SlotCompiler2;
import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.util.FmtUtils;

public class SlotCompilerIndex extends SlotCompiler2
{
    private static Logger log = LoggerFactory.getLogger(SlotCompilerIndex.class) ;
    
    private static final String NodeConstBase = AliasesSql.NodesConstantAliasBase ;
    
    Map<Node, SqlColumn> constantCols = new HashMap<Node, SqlColumn>() ;
    
    // Could be a set but it's convenient to keep thing in order for debugging.
    private List<Node> constants ; // = new ArrayList<Node>() ;
    private List<Var>  vars ; // = new ArrayList<Var>() ;
    
    protected TableDescTriples tripleTableDesc ;
    protected TableDescNodes   nodeTableDesc ;
    
    private SqlNode constantsSqlNode ;
    
    public SlotCompilerIndex(SDBRequest request)
    { 
        super(request) ;
        tripleTableDesc = request.getStore().getTripleTableDesc() ;
        nodeTableDesc = request.getStore().getNodeTableDesc() ;
    }
    
    @Override
    public SqlNode start(QuadBlock quads)
    {
        // Need to work out when constants are in-scope from earlier.
        
        // Reset context.
        constants = new ArrayList<Node>() ;
        vars = new ArrayList<Var>() ;
        
        classify(quads, constants, vars) ;
        constantsSqlNode = insertConstantAccesses(constants) ;
        // can be hold this back until the end of a block?
        return constantsSqlNode ;
    }
    
    @Override
    protected void constantSlot(SDBRequest request, Node node, SqlColumn thisCol, SqlExprList conditions)
    {
        SqlColumn colId = constantCols.get(node) ;
        if ( colId == null )
        {
            log.warn("Failed to find id col for "+node) ;
            return ;
        }
        SqlExpr c = new S_Equal(thisCol, colId) ;
        c.addNote("Const condition: "+FmtUtils.stringForNode(node, getRequest().getPrefixMapping())) ;
        conditions.add(c) ;
        return ; 
    }

    protected SqlNode insertConstantAccesses(Collection<Node> constants)
    {
        SqlNode sqlNode = null ;
        for ( Node n : constants )
        {
            long hash = NodeLayout2.hash(n);
            SqlConstant hashValue = new SqlConstant(hash) ;

            // Access nodes table.
            SqlTable nTable = new SqlTable(getRequest().genId(NodeConstBase), 
                                           nodeTableDesc.getTableName()) ;
            
            nTable.addNote("Const: "+FmtUtils.stringForNode(n, getRequest().getPrefixMapping())) ; 
            SqlColumn cHash = new SqlColumn(nTable, nodeTableDesc.getHashColName()) ;
            // Record 
            constantCols.put(n, new SqlColumn(nTable, nodeTableDesc.getIdColName())) ;
            SqlExpr c = new S_Equal(cHash, hashValue) ;
            sqlNode = SqlBuilder.innerJoin(getRequest(), sqlNode, nTable) ;
            sqlNode = SqlBuilder.restrict(getRequest(), sqlNode, c)  ;
        }
        return sqlNode ;
    }
    
    protected void classify(QuadBlock quadBlock, Collection<Node> constants, Collection<Var>vars)
    {
        for ( Quad quad : quadBlock )
        {
            // Some constants are only markers and are not stored in the database.
            if ( ! Quad.isDefaultGraph(quad.getGraph()) && ! quad.isUnionGraph() )  // quad.isDefaultGraph ARQ 2.8.4 and later
                acc(constants, vars, quad.getGraph()) ;
            acc(constants, vars, quad.getSubject()) ;
            acc(constants, vars, quad.getPredicate()) ;
            acc(constants, vars, quad.getObject()) ;
        }
    }

    private static void acc(Collection<Node>constants,  Collection<Var>vars, Node node)
    { 
        if ( node.isLiteral() || node.isBlank() || node.isURI() )
        {
            if ( ! constants.contains(node) )
                constants.add(node) ;
            return ;
        }
        if ( Var.isVar(node) )
        {
            vars.add(Var.alloc(node)) ;
            return ;
        }
        if ( node.isVariable() )
        {
            log.warn("Node_Varable but not a Var; bodged") ;
            vars.add(Var.alloc(node)) ;
            return ;
        }
        log.error("Unknown Node type: "+node) ;
        throw new SDBException("Unknown Node type: "+node) ;
    }
}
