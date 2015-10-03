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

package org.apache.jena.sdb.layout2;

import java.sql.ResultSet ;
import java.sql.SQLException ;
import java.util.Collection ;

import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.TypeMapper ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.sdb.compiler.SqlBuilder ;
import org.apache.jena.sdb.core.AliasesSql ;
import org.apache.jena.sdb.core.SDBRequest ;
import org.apache.jena.sdb.core.Scope ;
import org.apache.jena.sdb.core.ScopeEntry ;
import org.apache.jena.sdb.core.sqlexpr.S_Equal ;
import org.apache.jena.sdb.core.sqlexpr.SqlColumn ;
import org.apache.jena.sdb.core.sqlexpr.SqlExpr ;
import org.apache.jena.sdb.core.sqlnode.SqlNode ;
import org.apache.jena.sdb.core.sqlnode.SqlTable ;
import org.apache.jena.sdb.sql.ResultSetJDBC ;
import org.apache.jena.sdb.sql.SQLUtils ;
import org.apache.jena.sdb.store.SQLBridgeBase ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingFactory ;
import org.apache.jena.sparql.engine.binding.BindingMap ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class SQLBridge2 extends SQLBridgeBase 
{
    private static Logger log = LoggerFactory.getLogger(SQLBridge2.class) ;
    
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
            
            // Need to allocate aliases because otherwise we need to access
            // "table.column" as a label and "." is illegal in a label

            String vLex = SQLUtils.gen(sqlVarName,"lex") ;
            // Call overrideable helper method for lex column (Oracle)
            SqlColumn cLex = getLexSqlColumn(table);

            String vLexNChar = SQLUtils.gen(sqlVarName,"lexNChar") ;
            // Call overrideable helper method for lex column (Oracle)
            SqlColumn cLexNChar = getLexNCharSqlColumn(table);
    
            String vDatatype = SQLUtils.gen(sqlVarName,"datatype") ;
            SqlColumn cDatatype = new SqlColumn(table, "datatype") ;
    
            String vLang = SQLUtils.gen(sqlVarName,"lang") ;
            SqlColumn cLang = new SqlColumn(table, "lang") ;
    
            String vType = SQLUtils.gen(sqlVarName,"type") ;
            SqlColumn cType = new SqlColumn(table, "type") ;
    
            addProject(cLex, vLex) ;
            // Oracle NCLOB support
            if (cLexNChar != null) {
                addProject(cLexNChar, vLexNChar);
            }
            addProject(cDatatype, vDatatype) ;
            addProject(cLang, vLang) ;
            addProject(cType, vType) ;
            
            addAnnotation(sqlVarName+"="+v.toString()) ;
        }
        setAnnotation() ; 
    }
    
    /**
     * Intended to be overridden by an Oracle-specific impl to handle nclob selection. 
     * @see SQLBridge2Oracle
     * @param table
     * 
     */
    protected SqlColumn getLexSqlColumn(SqlTable table) {
        return new SqlColumn(table, "lex") ;
    }
    
    /**
     * Intended to be overridden by an Oracle-specific impl to handle nclob
     * alternate selection
     * @see SQLBridge2Oracle
     * @param table
     * @return SqlColumn
     */
    protected SqlColumn getLexNCharSqlColumn(SqlTable table) {
        return null;
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
        TableDescNodes nodeTableDesc = request.getStore().getNodeTableDesc() ;
        
        String tableAlias = request.genId(NodeBase) ; 
        SqlTable nTable = new SqlTable(tableAlias, nodeTableDesc.getTableName()) ;
        String nodeKeyColName = nodeTableDesc.getNodeRefColName() ;
        SqlColumn c2 = new SqlColumn(nTable, nodeKeyColName) ;

        nTable.setValueColumnForVar(var, c2) ;
        // Condition for value: triple table column = node table id/hash 
        nTable.addNote("Var: "+var) ;

        SqlExpr cond = new S_Equal(c1, c2) ;
        SqlNode n = SqlBuilder.leftJoin(request, sqlNode, nTable, cond) ;
        return n ;
    }
    
    @Override
    protected Binding assembleBinding(ResultSetJDBC rsHolder, Binding parent)
    {
        BindingMap b = BindingFactory.create(parent) ;
        ResultSet rs = rsHolder.get() ;
        for ( Var v : super.getProject() )
        {
            if ( ! v.isNamedVar() )
                // Skip bNodes and system variables
                continue ;

            String codename = super.getSqlName(v) ;
            if ( codename == null )
                // Not mentioned in query.
                continue ;
            try {
                int type        = rs.getInt(SQLUtils.gen(codename,"type")) ;
                // returns 0 on null : type 0 is not allocated
                // Test with "wasNull()" for safety
                if ( rs.wasNull() )
                    continue ;

                String lexColName = SQLUtils.gen(codename,"lex") ;
                // Get lexical - overriden by Oracle-specific code.
                String lex = getLexFromResultSet(rs, codename);
//                String lex = rs.getString(lexColName) ;
                if ( lex == null )
                    lex = "" ;
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
        return b ;
    }
    
    protected String getLexFromResultSet(ResultSet rs, String codename) 
    throws SQLException
    {
        String lex = rs.getString(SQLUtils.gen(codename,"lex")) ;
        if ( lex == null )
            lex = "" ;
        return lex;
    }

    private static Node makeNode(String lex, String datatype, String lang, ValueType vType)
    {
        switch (vType)
        {
            case BNODE:
                return NodeFactory.createBlankNode(lex) ;
            case URI:
                return NodeFactory.createURI(lex) ;
            case STRING:
                return NodeFactory.createLiteral(lex, lang) ;
            case XSDSTRING:
                return NodeFactory.createLiteral(lex, XSDDatatype.XSDstring) ;
            case INTEGER:
                return NodeFactory.createLiteral(lex, XSDDatatype.XSDinteger) ;
            case DOUBLE:
                return NodeFactory.createLiteral(lex, XSDDatatype.XSDdouble) ;
            case DATETIME:       
                return NodeFactory.createLiteral(lex, XSDDatatype.XSDdateTime) ;
            case OTHER:
                RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(datatype);
                return NodeFactory.createLiteral(lex, dt) ;
            default:
                log.warn("Unrecognized: ("+lex+", "+lang+", "+vType+")") ;
            return NodeFactory.createLiteral("UNRECOGNIZED") ; 
        }
    }
}
