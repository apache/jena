/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2.index;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.util.FmtUtils;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.compiler.QC;
import com.hp.hpl.jena.sdb.compiler.QuadBlock;
import com.hp.hpl.jena.sdb.core.AliasesSql;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlexpr.*;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlRestrict;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.layout2.NodeLayout2;
import com.hp.hpl.jena.sdb.layout2.SlotCompiler2;
import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;

public class SlotCompilerIndex extends SlotCompiler2
{
    private static Log log = LogFactory.getLog(SlotCompilerIndex.class) ;
    
    private static final String NodeConstBase = AliasesSql.NodesConstantAliasBase ;
    
    Map<Node, SqlColumn> constantCols = new HashMap<Node, SqlColumn>() ;
    
    // Could be a set but it's convenient to keep thing in order for debugging.
    private List<Node> constants = new ArrayList<Node>() ;
    private List<Var>  vars = new ArrayList<Var>() ;
    
    protected TableDescTriples tripleTableDesc ;
    protected TableDescNodes   nodeTableDesc ;
    
    public SlotCompilerIndex(SDBRequest request)
    { 
        super(request) ;
        tripleTableDesc = request.getStore().getTripleTableDesc() ;
        nodeTableDesc = request.getStore().getNodeTableDesc() ;
    }
    
    @Override
    public SqlNode start(QuadBlock quads)
    {
        classify(quads, constants, vars) ;
        SqlNode sqlNode = insertConstantAccesses(constants) ;
        return sqlNode ;
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
            SqlTable nTable = new SqlTable(nodeTableDesc.getTableName(), 
                                           getRequest().genId(NodeConstBase)) ;
            
            nTable.addNote("Const: "+FmtUtils.stringForNode(n, getRequest().getPrefixMapping())) ; 
            SqlColumn cHash = new SqlColumn(nTable, nodeTableDesc.getHashColName()) ;
            // Record 
            constantCols.put(n, new SqlColumn(nTable, nodeTableDesc.getIdColName())) ;
            SqlExpr c = new S_Equal(cHash, hashValue) ;
            sqlNode = QC.innerJoin(getRequest(), sqlNode, nTable) ;
            sqlNode = SqlRestrict.restrict(sqlNode, c)  ;
        }
        return sqlNode ;
    }
    
    protected void classify(QuadBlock quadBlock, Collection<Node> constants, Collection<Var>vars)
    {
        for ( Quad quad : quadBlock )
        {
            if ( ! quad.getGraph().equals(Quad.defaultGraph) )
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
        log.fatal("Unknown Node type: "+node) ;
        throw new SDBException("Unknown Node type: "+node) ;
    }
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