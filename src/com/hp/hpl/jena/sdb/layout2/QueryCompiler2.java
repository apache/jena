/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.core.Binding;
import com.hp.hpl.jena.query.core.BindingMap;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.query.engine1.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.query.util.FmtUtils;
import com.hp.hpl.jena.query.util.NodeUtils;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sdb.condition.SDBConstraint;
import com.hp.hpl.jena.sdb.core.*;
import com.hp.hpl.jena.sdb.core.compiler.BlockBGP;
import com.hp.hpl.jena.sdb.core.compiler.QC;

import com.hp.hpl.jena.sdb.core.compiler.QueryCompilerTriplePattern;
import com.hp.hpl.jena.sdb.core.sqlexpr.*;
import com.hp.hpl.jena.sdb.core.sqlnode.*;
import com.hp.hpl.jena.sdb.util.Pair;

public class QueryCompiler2 extends QueryCompilerTriplePattern
{
    private static Log log = LogFactory.getLog(QueryCompiler2.class) ;
    
    // TODO Check for parallel execution.
    private Map<Node, SqlColumn> constantCols = null ;
    private ArrayList<Pair<Var, SqlColumn>> projectVarCols = null ;
    
    // -------- Basic Graph pattern compilation
    
    @Override
    protected SqlNode match(CompileContext context, Triple triple)
    {
        String alias = context.allocTableAlias() ;
        SqlExprList conditions = new SqlExprList() ;
        
        TableTriples triples = new TableTriples(alias) ;
        triples.addNote(FmtUtils.stringForTriple(triple, null)) ;
        
        processSlot(context, triples, conditions, triple.getSubject(),   "s") ; 
        processSlot(context, triples, conditions, triple.getPredicate(), "p") ;
        processSlot(context, triples, conditions, triple.getObject(),    "o") ;
        
        if ( conditions.size() == 0 )
            return triples ;
        
        SqlRestrict r = new SqlRestrict(triples, conditions) ;
        return r ;
    }

    
    private void processSlot(CompileContext context,
                             TableTriples triples, SqlExprList conditions,
                             Node node, String colName)
    {
        SqlColumn thisCol = new SqlColumn(triples, colName) ;
        
        if ( ! node.isVariable() )
        {
            
            SqlColumn colId = constantCols.get(node) ;
            if ( colId == null )
            {
                log.warn("Failed to find id col for "+node) ;
                return ;
            }
            SqlExpr c = new S_Equal(thisCol, colId) ;
            conditions.add(c) ;
            return ; 
        }
        
        Var var = new Var(node) ;
        if ( triples.getScope().hasColumnForVar(var) )
        {
            SqlColumn otherCol = triples.getScope().getColumnForVar(var) ;
            SqlExpr c = new S_Equal(otherCol, thisCol) ;
            conditions.add(c) ;
            c.addNote("processVar: "+node) ;
            return ;
        }
        triples.setColumnForVar(var, thisCol) ;
    }
    

    //  -------- Start basic graph pattern 
    
    
    @Override
    protected SqlNode startBasicBlock(CompileContext context, BlockBGP blockBGP)
    {
        Collection<Node> constants = blockBGP.getConstants() ;
        SqlNode sqlNode = insertConstantAccesses(context, constants, null) ;
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
            SqlExprList conds = new SqlExprList() ;
            SqlColumn cHash = new SqlColumn(nTable, TableNodes.colHash) ;
            
            // Record 
            constantCols.put(n, new SqlColumn(nTable, "id")) ;
            
            SqlExpr c = new S_Equal(cHash, hashValue) ;
            conds.add(c) ;
            
            SqlNode r = new SqlRestrict(nTable, conds) ;
            sqlNode = QC.innerJoin(context, sqlNode, r) ;
        }
        
        return sqlNode ;

    }

    // --------- Finish basic graph pattern
    
    @Override
    protected SqlNode finishBasicBlock(CompileContext context,
                                     SqlNode sqlNode, BlockBGP blockBGP)
    {
        sqlNode = addRestrictions(context, sqlNode, blockBGP.getConstraints()) ;
        sqlNode = extractResults(context, blockBGP.getDefinedVars(), sqlNode) ; 
        return sqlNode ;
    }
    
    private SqlNode extractResults(CompileContext context,
                                   Collection<Var>vars, SqlNode sqlNode)
    {
        for ( Var v : vars )
        {
            SqlColumn c1 = sqlNode.getScope().getColumnForVar(v) ;
            if ( c1 == null )
                // Variable not actually in results. 
                continue ;
            
            SqlTable nTable = new TableNodes(allocNodeResultAlias()) ;
            nTable.addNote("Var: "+v) ;
            SqlColumn c2 = new SqlColumn(nTable, "id") ;
            
            SqlExpr cond = new S_Equal(c1, c2) ;
            // TODO Maninatin value scopes and id scopes separately.
            projectVarCols.add(new Pair<Var, SqlColumn>(v, c2)) ;
            SqlNode n = QC.innerJoin(context, sqlNode, nTable) ;
            if ( n instanceof SqlJoin )
            {
                ((SqlJoin)n).addCondition(cond) ;
                sqlNode = n ;
            }
            else
            {
                SqlNode r = new SqlRestrict(null, n, cond) ;
                sqlNode = r ;
            }
        }
        return sqlNode ;
    }

    private SqlNode addRestrictions(CompileContext context,
                                    SqlNode sqlNode,
                                    List<SDBConstraint> constraints)
    {
        if ( constraints.size() == 0 )
            return sqlNode ;

        // 1/ Get the val columns
        // 2/ Generate the SQL conditions
        
        //SqlExprList cList = new SqlExprList() ;
        for ( SDBConstraint c : constraints )
        {
            List<Var> acc = new ArrayList<Var>() ;
            c.varsMentioned(acc) ;
            for ( Var v : acc )
            {
                SqlColumn col = sqlNode.getScope().getColumnForVar(v) ;
                if ( col == null )
                {
                    // Not in scope.
                    log.info("Var not in scope for value of expression: "+v) ;
                    continue ;
                }
                // ASSUME lexical/string form needed 
                SqlTable nTable = new TableNodes(allocNodeResultAlias()) ;
                SqlColumn colId = new SqlColumn(nTable, "id") ;
                SqlColumn colLex = new SqlColumn(nTable, "lex") ;
                SqlColumn colType = new SqlColumn(nTable, "type") ;
                
                ScopeBase s = new ScopeBase() ;
                s.setColumnForVar(v, colLex) ;          // Value scope
                nTable.setColumnForVar(v, colId) ;      // Id scope
                
                
                SqlExprList l = new SqlExprList() ;
                {
                    SqlExpr e1 = c.asSqlExpr(s) ;
                    e1.addNote(c.toString()) ;
                    
                    SqlExpr e2 = new S_Equal(colType, new SqlConstant(ValueType.STRING.getTypeId())) ;
                    l.add(e1) ;
                    l.add(e2) ;
                }
                SqlNode r = new SqlRestrict(nTable, l) ;

                // Slurp the value up.
                sqlNode = QC.innerJoin(context, sqlNode, r) ;
            }
        }
        
        return sqlNode ;
    }

    @Override
    protected void startCompile(CompileContext context, Block block)
    {
        if ( constantCols != null || projectVarCols != null )
            log.fatal("Currently already compilign a BlockBGP") ;
        
        constantCols = new HashMap<Node, SqlColumn>() ;
        projectVarCols = new ArrayList<Pair<Var, SqlColumn>>() ;
    }
    
    @Override
    protected SqlNode finishCompile(CompileContext context, Block block, SqlNode sqlNode)
    {
        // Add projection
        Set<Var> x = block.getProjectVars() ;
        if ( x == null )
            x = block.getDefinedVars() ;
        SqlNode n =  makeProject(projectVarCols, sqlNode, x) ;
        
        constantCols = null ;
        projectVarCols = null ;
        return n ;
    }
    
    private SqlNode makeProject(List<Pair<Var, SqlColumn>>cols, SqlNode sqlNode, Set<Var> projectVars)
    {
        List<Pair<Var, SqlColumn>> projCol = new ArrayList<Pair<Var, SqlColumn>>() ;
        for ( Pair<Var, SqlColumn> p : cols )
        {
            // Not in the projection - skip 
            if ( ! projectVars.contains(p.getLeft()) )
                continue ;
            
            if ( ! NodeUtils.isApplicationVar(p.getLeft().asNode()) )
                // Skip blank node variables and system variables.
                continue ;
            
            String n = p.car().getName() ;
            Var vLex = new Var(n+"$lex") ;
            SqlColumn cLex = new SqlColumn(p.cdr().getTable(), "lex") ;

            Var vDatatype = new Var(n+"$datatype") ;
            SqlColumn cDatatype = new SqlColumn(p.cdr().getTable(), "datatype") ;

            Var vLang = new Var(n+"$lang") ;
            SqlColumn cLang = new SqlColumn(p.cdr().getTable(), "lang") ;
            
            Var vType = new Var(n+"$type") ;
            SqlColumn cType = new SqlColumn(p.cdr().getTable(), "type") ;

            projCol.add(new Pair<Var, SqlColumn>(vLex,  cLex)) ; 
            projCol.add(new Pair<Var, SqlColumn>(vDatatype, cDatatype)) ;
            projCol.add(new Pair<Var, SqlColumn>(vLang, cLang)) ;
            projCol.add(new Pair<Var, SqlColumn>(vType, cType)) ;
        }
        
        // Needs further refinement
        SqlNode p = new SqlProject(sqlNode, projCol) ;
        return p ;
    }


    
    // TODO Move/merge with CompileContext
    private static int nodesAliasCount = 1 ;
    private static final String nodesConstantAliasBase  = "N"+SDBConstants.SQLmark ;
    private static final String nodesResultAliasBase    = "R"+SDBConstants.SQLmark ;
    
    static private String allocNodeConstantAlias()      { return allocAlias(nodesConstantAliasBase) ; }
    static private String allocNodeResultAlias()        { return allocAlias(nodesResultAliasBase) ; }
    static private String allocAlias(String aliasBase)  { return  aliasBase+(nodesAliasCount++) ; }
    
    @Override
    protected QueryIterator assembleResults(ResultSet rs, Binding binding,
                                            Set<Var> vars, ExecutionContext execCxt)
        throws SQLException
    {
        List<Binding> results = new ArrayList<Binding>() ;
        while(rs.next())
        {
            Binding b = new BindingMap(binding) ;
            for ( Var v : vars )
            {
                String n = v.getName() ;
                if ( ! v.isNamedVar() )
                    // Skip bNodes and system variables
                    continue ;
                
                try {
                    String lex = rs.getString(n+"$lex") ;   // chars
//                    byte bytes[] = rs.getBytes(n+"$lex") ;      // bytes
//                    try {
//                        String $ = new String(bytes, "UTF-8") ;
//                        log.info("lex bytes : "+$+"("+$.length()+")") ;
//                    } catch (Exception ex) {}
                    // Same as rs.wasNull()
                    if ( lex == null )
                        continue ;
                    int type = rs.getInt(n+"$type") ;
                    String datatype =  rs.getString(n+"$datatype") ;
                    String lang =  rs.getString(n+"$lang") ;
                    ValueType vType = ValueType.lookup(type) ;
                    Node r = makeNode(lex, datatype, lang, vType) ;
                    b.add(n, r) ;
                } catch (SQLException ex)
                { // Unknown variable?
                  //log.warn("Not reconstructed: "+n) ;
                } 
            }
            results.add(b) ;
        }
        return new QueryIterPlainWrapper(results.iterator(), execCxt) ;
    }

    private Node makeNode(String lex, String datatype, String lang, ValueType vType)
    {
        switch (vType)
        {
            case BNODE:
                return Node.createAnon(new AnonId(lex)) ;
            case URI:
                return Node.createURI(lex) ;
            case STRING:
                return Node.createLiteral(lex, lang, false) ;
            case XSDSTRING:
                return Node.createLiteral(lex, null, XSDDatatype.XSDstring) ;
            case INTEGER:
                return Node.createLiteral(lex, null, XSDDatatype.XSDinteger) ;
            case DOUBLE:
                return Node.createLiteral(lex, null, XSDDatatype.XSDdouble) ;
            case DATETIME:       
                return Node.createLiteral(lex, null, XSDDatatype.XSDdateTime) ;
            case OTHER:
                RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(datatype);
                return Node.createLiteral(lex, null, dt) ;
            default:
                log.warn("Unrecognized: ("+lex+", "+lang+", "+vType+")") ;
                return Node.createLiteral("UNRECOGNIZED") ; 
        }
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