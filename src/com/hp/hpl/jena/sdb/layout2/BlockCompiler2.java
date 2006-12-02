/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.util.FmtUtils;
import com.hp.hpl.jena.sdb.core.*;
import com.hp.hpl.jena.sdb.core.compiler.BlockBGP;
import com.hp.hpl.jena.sdb.core.compiler.BlockCompilerBasic;
import com.hp.hpl.jena.sdb.core.compiler.QC;
import com.hp.hpl.jena.sdb.core.sqlexpr.*;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlRestrict;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.engine.SDBConstraint;


public class BlockCompiler2 extends BlockCompilerBasic
{
    private static Log log = LogFactory.getLog(BlockCompiler2.class) ;
    Map<Node, SqlColumn> constantCols = new HashMap<Node, SqlColumn>() ;

    private int nodesAliasCount = 1 ;
    private final String nodesConstantAliasBase  = "N"+SDBConstants.SQLmark ;
    private final String nodesResultAliasBase    = "R"+SDBConstants.SQLmark ;

    Generator genNodeConstantAlias = new Gensym(nodesConstantAliasBase) ;
    Generator genNodeResultAlias = new Gensym(nodesResultAliasBase) ;
    
    @Override
    protected SqlNode startBasicBlock(CompileContext context, BlockBGP blockBGP)
    {
        Collection<Node> constants = blockBGP.getConstants() ;
        addMoreConstants(constants) ;
        SqlNode sqlNode = insertConstantAccesses(context, constants) ;
        return sqlNode ;

    }

    // Hook for specialized engines.
    protected void addMoreConstants(Collection<Node> constants)
    {}
    
    @Override
    protected SqlNode finishBasicBlock(CompileContext context,
                                       SqlNode sqlNode, BlockBGP blockBGP)
    {
        sqlNode = addRestrictions(context, sqlNode, blockBGP.getConstraints()) ;
        Set<Var> projectVars = QC.exitVariables(blockBGP) ;
        sqlNode = extractResults(context, projectVars , sqlNode) ;
        return sqlNode ;
    }

    @Override
    public SqlNode compile(Triple triple, CompileContext context)
    {
        return super.compile(triple, context) ;
    }
    
    private SqlNode insertConstantAccesses(CompileContext context, Collection<Node> constants)
    {
        SqlNode sqlNode = null ;
        for ( Node n : constants )
        {
            long hash = NodeLayout2.hash(n);
            SqlConstant hashValue = new SqlConstant(hash) ;

            // Access nodes table.

            SqlTable nTable = new TableNodes(genNodeConstantAlias.next()) ;
            nTable.addNote("Const: "+FmtUtils.stringForNode(n, context.getPrefixMapping())) ; 
            SqlColumn cHash = new SqlColumn(nTable, TableNodes.colHash) ;
            // Record 
            constantCols.put(n, new SqlColumn(nTable, "id")) ;
            SqlExpr c = new S_Equal(cHash, hashValue) ;
            sqlNode = QC.innerJoin(context, sqlNode, nTable) ;
            sqlNode = SqlRestrict.restrict(sqlNode, c)  ;
        }

        return sqlNode ;

    }

    private SqlNode extractResults(CompileContext context,
                                   Collection<Var>vars, SqlNode sqlNode)
    {
        // for each var and it's id column, make sure there is value column. 
        for ( Var v : vars )
        {
            SqlColumn c1 = sqlNode.getIdScope().getColumnForVar(v) ;
            if ( c1 == null )
            {
                // Debug.
                Scope scope = sqlNode.getIdScope() ;
                // Variable not actually in results.
                continue ;
            }

            // Already in scope from a condition?
            SqlColumn c2 = sqlNode.getValueScope().getColumnForVar(v) ;
            if ( c2 != null )
                // Already there
                continue ;

            // Not in scope -- add a table to get it (share some code with addRestrictions?) 
            // Value table.
            SqlTable nTable = new TableNodes(genNodeResultAlias.next()) ;
            c2 = new SqlColumn(nTable, "id") ;                  // nTable.getColFor("id") ;

            nTable.setValueColumnForVar(v, c2) ;
            // Condition for value: triple table column = node table id 
            nTable.addNote("Var: "+v) ;


            SqlExpr cond = new S_Equal(c1, c2) ;
            SqlNode n = QC.innerJoin(context, sqlNode, nTable) ;
            sqlNode = SqlRestrict.restrict(n, cond) ;
        }
        return sqlNode ;
    }

    private SqlNode addRestrictions(CompileContext context,
                                    SqlNode sqlNode,
                                    List<SDBConstraint> constraints)
    {
        if ( constraints.size() == 0 )
            return sqlNode ;

        // Add all value columns
        for ( SDBConstraint c : constraints )
        {
            @SuppressWarnings("unchecked")
            Set<Var> vars = c.getExpr().getVarsMentioned() ;
            for ( Var v : vars )
            {
                // For Variables used in this SQL constraint, make sure the value is available.  

                SqlColumn tripleTableCol = sqlNode.getIdScope().getColumnForVar(v) ;   // tripleTableCol
                if ( tripleTableCol == null )
                {
                    // Not in scope.
                    log.info("Var not in scope for value of expression: "+v) ;
                    continue ;
                }

                // Value table column
                SqlTable nTable =   new TableNodes(genNodeResultAlias.next()) ;
                SqlColumn colId =   new SqlColumn(nTable, "id") ;
                SqlColumn colLex =  new SqlColumn(nTable, "lex") ;
                SqlColumn colType = new SqlColumn(nTable, "type") ;

                nTable.setValueColumnForVar(v, colLex) ;        // ASSUME lexical/string form needed 
                nTable.setIdColumnForVar(v, colId) ;            // Id scope => join
                sqlNode = QC.innerJoin(context, sqlNode, nTable) ;
            }

            SqlExpr sqlExpr = c.compile(sqlNode.getValueScope()) ;
            sqlNode = SqlRestrict.restrict(sqlNode, sqlExpr) ;
        }
        return sqlNode ;
    }

    // -------- Slot compilation
    @Override
    protected void constantSlot(CompileContext context, Node node, SqlColumn thisCol, SqlExprList conditions)
    {
        SqlColumn colId = constantCols.get(node) ;
        if ( colId == null )
        {
            log.warn("Failed to find id col for "+node) ;
            return ;
        }
        SqlExpr c = new S_Equal(thisCol, colId) ;
        c.addNote("Const condition: "+FmtUtils.stringForNode(node, context.getPrefixMapping())) ;
        conditions.add(c) ;
        return ; 
    }

    @Override
    protected SqlTable accessTriplesTable(String alias)
    {
        return new TableTriples(alias) ;
    }
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