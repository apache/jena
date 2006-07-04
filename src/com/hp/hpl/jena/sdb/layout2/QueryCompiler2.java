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
import com.hp.hpl.jena.query.core.*;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.query.engine1.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.query.util.*;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sdb.condition.SDBConstraint;
import com.hp.hpl.jena.sdb.core.*;
import com.hp.hpl.jena.sdb.core.sqlexpr.*;
import com.hp.hpl.jena.sdb.core.sqlnode.*;
import com.hp.hpl.jena.sdb.store.CompiledConstraint;
import com.hp.hpl.jena.sdb.store.ConditionCompiler;
import com.hp.hpl.jena.sdb.util.Pair;

/**
 * Query generation for layout2
 * 
 * @author Andy Seaborne
 * @version $Id: QueryCompiler2.java,v 1.4 2006/04/23 18:40:23 andy_seaborne Exp $
 */

public class QueryCompiler2 extends QueryCompilerBase
{
    private static Log log = LogFactory.getLog(QueryCompiler2.class) ;
    private static final String classShortName = Utils.classShortName(QueryCompiler2.class)  ;
    
    // TODO Ensure repeated and concurrent use works
    // TODO Access constants only if needed (nested blocks).
    // TODO Variable flow.  Offramp only and FILTER only
    // TODO Refactor.
    
    // Need a per-QueryBlock mechanism
    // --- This is the "context" information.
    // NB Concurrency issue.  But schemas do not need to be shared. 
    
    // Used to record the boundVars at each basic block.
    Stack<List<Var>> boundVars = null ;

    // Used to record the constants in each block
    Stack<Map<Node, SqlColumn>> constants = null ;
    
    // Record projection vars.  err - why isn't this a Map?
    List<Pair<Var, SqlColumn>> projectVarCols = null ; 
    
    public QueryCompiler2() { }
    
    public ConditionCompiler getConditionCompiler()
    {
        // TODO ConditionCompiler
        // Anything not null will cause the fixed algorithm to be used
        return new ConditionCompiler(){
            public CompiledConstraint compile(Constraint constraint)
            {
                return null ;
            }} ; 
    }

    // Assumes there is only one basic block in progress at anyone time per compiler
    
    @Override
    protected SqlNode startQueryBlock(CompileContext context, Block block, SqlNode sqlNode)
    {
        boundVars = new Stack<List<Var>>() ;
        constants = new Stack<Map<Node, SqlColumn>>() ;
        projectVarCols = new ArrayList<Pair<Var, SqlColumn>>() ;
        return sqlNode ;
    }
    
    @Override
    protected SqlNode finishQueryBlock(CompileContext context, Block block, SqlNode sqlNode)
    { 
        // Add projection
        List<Var> x = block.getProjectVars() ;
        if ( x == null )
            x = block.getDefinedVars() ;
        SqlNode n =  makeProject(projectVarCols, sqlNode, x) ;
        
        // Clearup
        boundVars = null ;
        constants = null ;
        projectVarCols = null ; // Especially this, as this was only ever added to.
        return n ;
    }
        
    private SqlNode makeProject(List<Pair<Var, SqlColumn>>cols, SqlNode sqlNode, List<Var> project)
    {
        List<Pair<Var, SqlColumn>> projCol = new ArrayList<Pair<Var, SqlColumn>>() ;
        for ( Pair<Var, SqlColumn> p : cols )
        {
            // Not in the projection - skip 
            if ( ! project.contains(p.getLeft()) )
                continue ;
            
            if ( ! NodeUtils.isApplicationVar(p.getLeft().asNode()) )
                // Skip blank node variables and system variables.
                continue ;
            
            // TODO Check this - use of Node here is odd
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

    @Override
    protected SqlNode  startBasicBlock(CompileContext context, BasicPattern basicPattern, SqlNode sqlNode)
    { 
        boundVars.push(new ArrayList<Var>()) ;
        constants.push(new HashMap<Node, SqlColumn>()) ;
        List<Node>consts = new ArrayList<Node>() ;
        
        for ( Triple t : basicPattern )
        {
            addConstant(t.getSubject(), consts) ;
            addConstant(t.getPredicate(), consts) ;
            addConstant(t.getObject(), consts) ;
        }
        
        sqlNode = insertConstantAccesses(context, consts, sqlNode) ;
        return sqlNode ;
    }
    
    private void addConstant(Node node, List<Node>consts)
    {
        if ( !node.isVariable() && !consts.contains(node) )
            consts.add(node) ;
    }
    
    @Override
    protected SqlNode finishBasicBlock(CompileContext context,
                                       BasicPattern basicPattern, List<SDBConstraint> constraints,
                                       SqlNode sqlNode)
    { 
        constants.pop() ;
        // Find new vars in this block - insert the joins to get the values
        List<Var> vars = boundVars.pop() ;
        sqlNode = extractResults(context, vars, sqlNode) ; 
        sqlNode = addRestrictions(context, sqlNode, constraints) ;
        return sqlNode ;
    }
    
    
    // Does not use hashed into the triple table, just to access the node table.
    private SqlNode insertConstantAccesses(CompileContext context, List<Node> consts, SqlNode sqlNode)
    {   
        Map<Node, SqlColumn> constantsHere = constants.peek() ;
        
        for ( Node n : consts )
        {
            long hash = NodeLayout2.hash(n);
            SqlConstant hashValue = new SqlConstant(hash) ;

            // Access nodes table.
            
            SqlTable nTable = new TableNodes(allocNodeConstantAlias()) ;
            nTable.addAnnotation("Const: "+FmtUtils.stringForNode(n)) ; 
            SqlExprList conds = new SqlExprList() ;
            SqlColumn cHash = new SqlColumn(nTable, TableNodes.colHash) ;
            
            SqlExpr c = new S_Equal(cHash, hashValue) ;
            conds.add(c) ;
            constantsHere.put(n, new SqlColumn(nTable, "id")) ;
            
            SqlNode r = new SqlRestrict(nTable, conds) ;
            sqlNode = innerJoin(context, sqlNode, r) ;
        }
        
        return sqlNode ;
    }

    

    private SqlNode addRestrictions(CompileContext context, SqlNode sqlNode,
                                    List<SDBConstraint> constraints)
    {
        // TODO Place restriction in best place.
        // i.e. inlne with basic block processing
        // Sort out out of scope/place issues

        
        // This looks like assignConditions in QueryCompilerBase.
        if ( constraints.size() == 0 )
            return sqlNode ;

        // 1/ Get the val columns
        // 2/ Generate the SQL conditions
        
        ScopeBase valScope = new ScopeBase(sqlNode) ;
        Scope scope = sqlNode ;
        
        SqlExprList cList = new SqlExprList() ;
        for ( SDBConstraint c : constraints )
        {
            List<Var> acc = new ArrayList<Var>() ;
            c.varsMentioned(acc) ;
            for ( Var v : acc )
            {
                SqlColumn col = scope.getColumnForVar(v) ;
                if ( col == null )
                {
                    // Not in scope.
                    log.info("Var not in scope for value of expression: "+v) ;
                    continue ;
                }
                // ASSUME lexical/string form needed 
                SqlTable nTable = new TableNodes(allocNodeResultAlias()) ;
                // Slurp the value up.
                sqlNode = innerJoin(context, sqlNode, nTable) ;
                // TODO Need to know the value type.
                SqlColumn vCol = new SqlColumn(nTable, "lex") ;
                // Record it
                valScope.setColumnForVar(v, vCol) ;
            }
        }
        // valContext is now all the required values.
        SqlExprList exprs = new SqlExprList() ;
        for ( SDBConstraint c : constraints )
        {
            SqlExpr e = c.asSqlExpr(valScope) ;
            exprs.add(e) ;
        }
        sqlNode = new SqlRestrict(/*context.allocAlias("R")*/null, sqlNode, exprs) ;
        return sqlNode ;
    }


    private SqlNode extractResults(CompileContext context,
                                   List<Var>vars, SqlNode sqlNode)
    {
        for ( Var v : vars )
        {
            SqlColumn c1 = context.getScope().getColumnForVar(v) ;
            if ( c1 == null )
                continue ;
            
            SqlTable nTable = new TableNodes(allocNodeResultAlias()) ;
            nTable.addAnnotation("Var: "+v) ;
            SqlColumn c2 = new SqlColumn(nTable, "id") ;
            
            SqlExpr cond = new S_Equal(c1, c2) ;
            projectVarCols.add(new Pair<Var, SqlColumn>(v, c2)) ;
            SqlNode n = innerJoin(context, sqlNode, nTable) ;
            SqlNode r = new SqlRestrict(null, n, cond) ;
            sqlNode = r ;
        }
        return sqlNode ;
    }

    @Override
    protected SqlNode match(CompileContext context, Triple triple)
    {
        String alias = context.allocTableAlias() ;
        SqlExprList conditions = new SqlExprList() ;
        
        TableTriples triples = new TableTriples(alias) ;
        triples.addAnnotation(FmtUtils.stringForTriple(triple, null)) ;
        
        processTripleSlot(context, triples, conditions, triple.getSubject(),   "s") ; 
        processTripleSlot(context, triples, conditions, triple.getPredicate(), "p") ;
        processTripleSlot(context, triples, conditions, triple.getObject(),    "o") ;
        
        if ( conditions.size() == 0 )
            return triples ;
        
        SqlRestrict r = new SqlRestrict(triples, conditions) ;
        return r ;
    }
    
    private void processTripleSlot(CompileContext context, TableTriples triples,
                                   SqlExprList conditions, Node node, String colName)
    {
        SqlColumn thisCol = new SqlColumn(triples, colName) ;
        
        if ( ! node.isVariable() )
        {
            Map<Node, SqlColumn> constantsHere = constants.peek() ;
            
            SqlColumn colId = constantsHere.get(node) ;
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
        if ( context.getScope().hasColumnForVar(var) )
        {
            SqlColumn otherCol = context.getScope().getColumnForVar(var) ;
            SqlExpr c = new S_Equal(otherCol, thisCol) ;
            conditions.add(c) ;
            return ;
        }
        
        // New variable mentioned
        triples.setColumnForVar(var, thisCol) ;
        // Record for this block.
        boundVars.peek().add(var) ;
    }

    private static int nodesAliasCount = 1 ;
    private static final String nodesConstantAliasBase = "N"+SDBConstants.SQLmark ;
    private static final String nodesResultAliasBase = "R"+SDBConstants.SQLmark ;
    
    static private String allocNodeConstantAlias()      { return allocAlias(nodesConstantAliasBase) ; }
    static private String allocNodeResultAlias()        { return allocAlias(nodesResultAliasBase) ; }
    static private String allocAlias(String aliasBase)  { return  aliasBase+(nodesAliasCount++) ; }
    
    @Override
    protected QueryIterator assembleResults(ResultSet rs, Binding binding,
                                            List<Var> vars, ExecutionContext execCxt)
        throws SQLException
    {
        List<Binding> results = new ArrayList<Binding>() ;
        while(rs.next())
        {
            Binding b = new BindingMap(binding) ;
            for ( Var v : vars )
            {
                String n = v.getName() ;
                if ( v.isBlankNodeVar() )
                    // Skip bNodes.
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
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AS IS'' AND ANY EXPRESS OR
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