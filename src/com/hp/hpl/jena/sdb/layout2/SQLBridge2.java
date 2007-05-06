/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;

import com.hp.hpl.jena.sdb.compiler.QC;
import com.hp.hpl.jena.sdb.core.*;
import com.hp.hpl.jena.sdb.core.sqlexpr.S_Equal;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlRestrict;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.sql.RS;
import com.hp.hpl.jena.sdb.sql.SQLUtils;
import com.hp.hpl.jena.sdb.store.TableDescNode;
import com.hp.hpl.jena.sdb.store.SQLBridgeBase;

public class SQLBridge2 extends SQLBridgeBase 
{
    private static Log log = LogFactory.getLog(SQLBridge2.class) ;
    
    // Result nodes tables 
    private static final String NodeBase = AliasesSql.NodesResultAliasBase ;
    //private Generator genNodeResultAlias = null ;

    public SQLBridge2(SDBRequest request, SqlNode sqlNode, Collection<Var> projectVars)
    { 
        super(request, sqlNode, projectVars) ;
    }
    
    @Override
    protected void buildValues()
    {
        SqlNode sqlNode = getSqlNode() ;
        for ( Var v : getProject() )
            sqlNode = insertValueGetter(request, sqlNode, v) ;
        setSqlNode(sqlNode) ;
    }
    
    @Override
    protected void buildProject()
    {
        for ( Var v : getProject() )
        {
            if ( ! v.isNamedVar() )
                continue ;
            ScopeEntry e = getSqlNode().getNodeScope().findScopeForVar(v) ;
            if ( e == null )
            {
                // Should be a column mentioned in the SELECT which is not mentioned in this block
                continue ;
            }
            SqlColumn vCol = e.getColumn() ; 
    
            SqlTable table = vCol.getTable() ;
            String sqlVarName = allocSqlName(v) ;
            
            // Need to allocate aliases because other wise we need to access
            // "table.column" as a label and "." is illegal in a label
            Var vLex = Var.alloc(SQLUtils.gen(sqlVarName,"lex")) ;
            SqlColumn cLex = new SqlColumn(table, "lex") ;
    
            Var vDatatype = Var.alloc(SQLUtils.gen(sqlVarName,"datatype")) ;
            SqlColumn cDatatype = new SqlColumn(table, "datatype") ;
    
            Var vLang = Var.alloc(SQLUtils.gen(sqlVarName,"lang")) ;
            SqlColumn cLang = new SqlColumn(table, "lang") ;
    
            Var vType = Var.alloc(SQLUtils.gen(sqlVarName,"type")) ;
            SqlColumn cType = new SqlColumn(table, "type") ;
    
            addProject(vLex, cLex) ;
            addProject(vDatatype, cDatatype) ;
            addProject(vLang, cLang) ;
            addProject(vType, cType) ;
            addAnnotation(sqlVarName+"="+v.toString()) ;
        }
        setAnnotation() ; 
    }
    
    private SqlNode insertValueGetter(SDBRequest request, SqlNode sqlNode, Var var)
    {
        ScopeEntry e1 = sqlNode.getIdScope().findScopeForVar(var) ;
        if ( e1 == null )
        {
            // Debug.
            Scope scope = sqlNode.getIdScope() ;
            // Variable not actually in results.
            return sqlNode ;
        }
        
        // Already in scope (from a condition)?
        ScopeEntry e2 = sqlNode.getNodeScope().findScopeForVar(var) ;
        if ( e2 != null )
            // Already there
            return sqlNode ;
        
        SqlColumn c1 = e1.getColumn() ;
        // Not in scope -- add a table to get it
        TableDescNode nodeTableDesc = request.getStore().getNodeTableDesc() ;
        
        String tableAlias = request.genId(NodeBase) ; 
        SqlTable nTable = new SqlTable(nodeTableDesc.getTableName(), tableAlias) ;
        String nodeKeyColName = nodeTableDesc.getKeyColName() ;
        SqlColumn c2 = new SqlColumn(nTable, nodeKeyColName) ;

        nTable.setValueColumnForVar(var, c2) ;
        // Condition for value: triple table column = node table id/hash 
        nTable.addNote("Var: "+var) ;

        SqlExpr cond = new S_Equal(c1, c2) ;
        SqlNode n = QC.leftJoin(request, sqlNode, nTable) ;
        SqlNode sqlNode2 = SqlRestrict.restrict(n, cond) ;
        return sqlNode2 ;
    }
    
    public QueryIterator assembleResults(ResultSet rs, Binding binding, ExecutionContext execCxt)
        throws SQLException
    {
        if ( false )
            RS.printResultSet(rs) ;
        
        List<Binding> results = new ArrayList<Binding>() ;
        while(rs.next())
        {
            Binding b = new BindingMap(binding) ;
            for ( Var v : super.getProject() )
            {
                if ( ! v.isNamedVar() )
                    // Skip bNodes and system variables
                    continue ;

                String codename = super.getSqlName(v) ;
                try {
                    String lex = rs.getString(SQLUtils.gen(codename,"lex")) ;   // chars
                    
                    // byte bytes[] = rs.getBytes(SQLUtils.gen(codename,"lex")) ; // bytes
                    // try {
                    //     String $ = new String(bytes, "UTF-8") ;
                    //     log.info("lex bytes : "+$+"("+$.length()+")") ;
                    // } catch (Exception ex) {}
                    // Same as rs.wasNull() for things that can return Java nulls.
                    if ( lex == null )
                        continue ;
                    int type        = rs.getInt(SQLUtils.gen(codename,"type")) ;
                    String datatype = rs.getString(SQLUtils.gen(codename,"datatype")) ;
                    String lang     = rs.getString(SQLUtils.gen(codename,"lang")) ;
                    ValueType vType = ValueType.lookup(type) ;
                    Node r          = makeNode(lex, datatype, lang, vType) ;
                    b.add(v, r) ;
                } catch (SQLException ex)
                { // Unknown variable?
                    //log.warn("Not reconstructed: "+n) ;
                } 
            }
            results.add(b) ;
        }
        return new QueryIterPlainWrapper(results.iterator(), execCxt) ;
    }


    private static Node makeNode(String lex, String datatype, String lang, ValueType vType)
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
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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