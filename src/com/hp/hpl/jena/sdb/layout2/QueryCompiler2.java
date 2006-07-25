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
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.util.FmtUtils;
import com.hp.hpl.jena.sdb.core.Block;
import com.hp.hpl.jena.sdb.core.CompileContext;
import com.hp.hpl.jena.sdb.core.SDBConstants;
import com.hp.hpl.jena.sdb.core.compiler.BlockBGP;
import com.hp.hpl.jena.sdb.core.compiler.QC;
import com.hp.hpl.jena.sdb.core.compiler.QueryCompilerTriplePatternSlot;
import com.hp.hpl.jena.sdb.core.sqlexpr.*;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlProject;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlRestrict;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.engine.SDBConstraint;
import com.hp.hpl.jena.sdb.store.ConditionCompiler;
import com.hp.hpl.jena.sdb.store.ResultsBuilder;
import com.hp.hpl.jena.sdb.util.Pair;

public class QueryCompiler2 extends QueryCompilerTriplePatternSlot
{
    private static Log log = LogFactory.getLog(QueryCompiler2.class) ;
    
    // Only one active basic graph pattern compilation at a time.
    private Map<Node, SqlColumn> constantCols = null ;
    
    // -------- Basic Graph pattern compilation
    
//    @Override
//    protected SqlNode match(CompileContext context, Triple triple)
//    {
//        // Make a TripleTableDesc, share this code in QCTriple.
//        // Abstract maketable, processSlot
//        String alias = context.allocTableAlias() ;
//        SqlExprList conditions = new SqlExprList() ;
//        
//        TableTriples triples = new TableTriples(alias) ;
//        triples.addNote(FmtUtils.stringForTriple(triple, null)) ;
//        
//        processSlot(context, triples, conditions, triple.getSubject(),   TableTriples.subjectCol) ; 
//        processSlot(context, triples, conditions, triple.getPredicate(), TableTriples.predicateCol) ;
//        processSlot(context, triples, conditions, triple.getObject(),    TableTriples.objectCol) ;
//        
//        if ( conditions.size() == 0 )
//            return triples ;
//        
//        return SqlRestrict.restrict(triples, conditions) ;
//    }
//
//    
//    private void processSlot(CompileContext context,
//                             SqlTable table, SqlExprList conditions,
//                             Node node, String colName)
//    {
//        SqlColumn thisCol = new SqlColumn(table, colName) ;
//        
//        // Abstract : QC_1 does an encode, QC_2, finds.
//        // abstract: node=>column
//        if ( ! node.isVariable() )
//        {
//            SqlColumn colId = constantCols.get(node) ;
//            if ( colId == null )
//            {
//                log.warn("Failed to find id col for "+node) ;
//                return ;
//            }
//            SqlExpr c = new S_Equal(thisCol, colId) ;
//            c.addNote("Const condition: "+FmtUtils.stringForNode(node)) ;
//            conditions.add(c) ;
//            return ; 
//        }
//        
//        // In common with QC1
//        Var var = new Var(node) ;
//        if ( table.getIdScope().hasColumnForVar(var) )
//        {
//            SqlColumn otherCol = table.getIdScope().getColumnForVar(var) ;
//            SqlExpr c = new S_Equal(otherCol, thisCol) ;
//            conditions.add(c) ;
//            c.addNote("processVar: "+node) ;
//            return ;
//        }
//        table.setIdColumnForVar(var, thisCol) ;
//    }
    
    @Override
    protected void constantSlot(Node node, SqlColumn thisCol, SqlExprList conditions)
    {
      SqlColumn colId = constantCols.get(node) ;
      if ( colId == null )
      {
          log.warn("Failed to find id col for "+node) ;
          return ;
      }
      SqlExpr c = new S_Equal(thisCol, colId) ;
      c.addNote("Const condition: "+FmtUtils.stringForNode(node)) ;
      conditions.add(c) ;
      return ; 
    }
                                      
    @Override
    protected SqlTable makeTriplesTable(String alias)
    {
        return new TableTriples(alias) ;
    }

    //  -------- Start basic graph pattern 
    
    
    @Override
    protected SqlNode startBasicBlock(CompileContext context, BlockBGP blockBGP)
    {
        if ( constantCols != null /*|| projectVarCols != null*/ )
            log.fatal("Currently already compiling a BlockBGP") ;
        
        constantCols = new HashMap<Node, SqlColumn>() ;
        
        // Add constants to start of a block.
        // TODO See if any constants already in scope.
        Collection<Node> constants = blockBGP.getConstants() ;
        SqlNode sqlNode = insertConstantAccesses(context, constants, null) ;
        return sqlNode ;
        
    }

    @Override
    protected SqlNode finishBasicBlock(CompileContext context,
                                       SqlNode sqlNode, BlockBGP blockBGP)
    {
        sqlNode = addRestrictions(context, sqlNode, blockBGP.getConstraints()) ;
        // Intersection of defined and project?
        sqlNode = extractResults(context, blockBGP.getDefinedVars(), sqlNode) ;
        // Drop the constants mapping
        constantCols = null ;
        return sqlNode ;
    }
    
    private SqlNode insertConstantAccesses(CompileContext context, Collection<Node> constants, Object object)
    {
        SqlNode sqlNode = null ;
        for ( Node n : constants )
        {
            long hash = NodeLayout2.hash(n);
            SqlConstant hashValue = new SqlConstant(hash) ;
    
            // Access nodes table.
            
            SqlTable nTable = new TableNodes(allocNodeConstantAlias()) ;
            nTable.addNote("Const: "+FmtUtils.stringForNode(n)) ; 
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
                // Variable not actually in results. 
                continue ;
            
            // Already in scope from a condition?
            SqlColumn c2 = sqlNode.getValueScope().getColumnForVar(v) ;
            if ( c2 != null )
                // Already there
                continue ;
            
            // Not in scope -- add a table to get it (share some code with addRestrictions?) 
            // Value table.
            SqlTable nTable = new TableNodes(allocNodeResultAlias()) ;
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
            List<String> acc = new ArrayList<String>() ;
            c.getExpr().varsMentioned(acc) ;
            for ( String $ : acc )
            {
                Var v = new Var($) ;
                // For Variables used in this SQL constraint, make sure the value is available.  
                
                SqlColumn tripleTableCol = sqlNode.getIdScope().getColumnForVar(v) ;   // tripleTableCol
                if ( tripleTableCol == null )
                {
                    // Not in scope.
                    log.info("Var not in scope for value of expression: "+v) ;
                    continue ;
                }
                
                // Value table column
                SqlTable nTable =   new TableNodes(allocNodeResultAlias()) ;
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

    @Override
    protected void startCompile(CompileContext context, Block block)
    {
    }
    
    @Override
    protected SqlNode finishCompile(CompileContext context, Block block, SqlNode sqlNode)
    {
        // Generate the SQL SELECT projection
        // DRYout - choosing variable shareable with QC2 - finishCompile => makeProject and finishCompile is just a signal
        
        if ( ! block.isCompletePattern() && block.getProjectVars() != null )
            log.warn("Not a complete pattern block but there are projection variables set") ;
        
        Set<Var> x = block.getProjectVars() ;
        if ( x == null )
            x = block.getDefinedVars() ;
        SqlNode n =  makeProject(sqlNode, x) ;
        return n ;
    }
    
//    private SqlNode makeProject(List<Pair<Var, SqlColumn>>cols, SqlNode sqlNode, Set<Var> projectVars)
    private SqlNode makeProject(SqlNode sqlNode, Set<Var> projectVars)
    {
        for ( Var v : projectVars )
        {
            // See if we have a value column already.
            SqlColumn vCol = sqlNode.getValueScope().getColumnForVar(v) ;
            if ( vCol == null )
            {
                // Should be a column mentioned in the SELECT which is not mentionedd in this block 
                return sqlNode ;
            }

            SqlTable table = vCol.getTable() ; 
            Var vLex = new Var(v.getName()+"$lex") ;
            SqlColumn cLex = new SqlColumn(table, "lex") ;

            Var vDatatype = new Var(v.getName()+"$datatype") ;
            SqlColumn cDatatype = new SqlColumn(table, "datatype") ;

            Var vLang = new Var(v.getName()+"$lang") ;
            SqlColumn cLang = new SqlColumn(table, "lang") ;

            Var vType = new Var(v.getName()+"$type") ;
            SqlColumn cType = new SqlColumn(table, "type") ;

            // Get the 3 part of the RDF term and its internal type number.
            sqlNode = SqlProject.project(sqlNode, new Pair<Var, SqlColumn>(vLex,  cLex)) ; 
            sqlNode = SqlProject.project(sqlNode, new Pair<Var, SqlColumn>(vDatatype, cDatatype)) ;
            sqlNode = SqlProject.project(sqlNode, new Pair<Var, SqlColumn>(vLang, cLang)) ;
            sqlNode = SqlProject.project(sqlNode, new Pair<Var, SqlColumn>(vType, cType)) ;
        }
        return sqlNode ;
    }


    
    // TODO Move/merge with CompileContext
    private static int nodesAliasCount = 1 ;
    private static final String nodesConstantAliasBase  = "N"+SDBConstants.SQLmark ;
    private static final String nodesResultAliasBase    = "R"+SDBConstants.SQLmark ;
    
    static private String allocNodeConstantAlias()      { return allocAlias(nodesConstantAliasBase) ; }
    static private String allocNodeResultAlias()        { return allocAlias(nodesResultAliasBase) ; }
    static private String allocAlias(String aliasBase)  { return  aliasBase+(nodesAliasCount++) ; }
    
    private ConditionCompiler conditionCompiler = new ConditionCompiler2() ;
    public ConditionCompiler getConditionCompiler()
    { return conditionCompiler ; }
    
    private ResultsBuilder resultBuilder = new ResultsBuilder2() ;
    @Override
    public ResultsBuilder getResultBuilder()
    { return resultBuilder ; }
    
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