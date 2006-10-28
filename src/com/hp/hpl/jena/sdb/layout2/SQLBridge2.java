/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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
import com.hp.hpl.jena.query.core.Binding;
import com.hp.hpl.jena.query.core.BindingMap;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.query.engine1.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlProject;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.store.SQLBridgeBase;
import com.hp.hpl.jena.sdb.util.Pair;

public class SQLBridge2 extends SQLBridgeBase 
{
    private static Log log = LogFactory.getLog(SQLBridge2.class) ;

    public SQLBridge2(Collection<Var> projectVars)
    { 
        super(projectVars) ;
    }
    
    public SqlNode buildProject(SqlNode sqlNode)
    {
        StringBuilder annotation = new StringBuilder() ;
        for ( Var v : getProject() )
        {
            // See if we have a value column already.
            SqlColumn vCol = sqlNode.getValueScope().getColumnForVar(v) ;
            if ( vCol == null )
            {
                // Should be a column mentioned in the SELECT which is not mentioned in this block 
                continue ;
            }
    
            String sqlVarName = getSqlName(v) ;
            
            SqlTable table = vCol.getTable() ; 
            Var vLex = new Var(sqlVarName+"$lex") ;
            SqlColumn cLex = new SqlColumn(table, "lex") ;
    
            Var vDatatype = new Var(sqlVarName+"$datatype") ;
            SqlColumn cDatatype = new SqlColumn(table, "datatype") ;
    
            Var vLang = new Var(sqlVarName+"$lang") ;
            SqlColumn cLang = new SqlColumn(table, "lang") ;
    
            Var vType = new Var(sqlVarName+"$type") ;
            SqlColumn cType = new SqlColumn(table, "type") ;
    
            // Get the 3 parts of the RDF term and its internal type number.
            sqlNode = SqlProject.project(sqlNode, new Pair<Var, SqlColumn>(vLex,  cLex)) ; 
            sqlNode = SqlProject.project(sqlNode, new Pair<Var, SqlColumn>(vDatatype, cDatatype)) ;
            sqlNode = SqlProject.project(sqlNode, new Pair<Var, SqlColumn>(vLang, cLang)) ;
            sqlNode = SqlProject.project(sqlNode, new Pair<Var, SqlColumn>(vType, cType)) ;
            
            if ( annotation.length() > 0 )
                annotation.append(" ") ;
            annotation.append(String.format("%s=%s", v, sqlVarName)) ; 
        }
        sqlNode.addNote(annotation.toString()) ; 
        return sqlNode ;
    }

    
    public QueryIterator assembleResults(ResultSet rs, Binding binding, ExecutionContext execCxt)
        throws SQLException
    {
        List<Binding> results = new ArrayList<Binding>() ;
        while(rs.next())
        {
            Binding b = new BindingMap(binding) ;
            for ( Var v : super.getProject() )
            {
                String sqlVarName = getSqlName(v) ;
                
                if ( ! v.isNamedVar() )
                    // Skip bNodes and system variables
                    continue ;

                try {
                    String lex = rs.getString(sqlVarName+"$lex") ;   // chars
                    // Same as rs.wasNull() for things that can return Java nulls.
                    
                    // byte bytes[] = rs.getBytes(n+"$lex") ;      // bytes
                    // try {
                    //     String $ = new String(bytes, "UTF-8") ;
                    //     log.info("lex bytes : "+$+"("+$.length()+")") ;
                    // } catch (Exception ex) {}
                    if ( lex == null )
                        continue ;
                    int type = rs.getInt(sqlVarName+"$type") ;
                    String datatype =  rs.getString(sqlVarName+"$datatype") ;
                    String lang =  rs.getString(sqlVarName+"$lang") ;
                    ValueType vType = ValueType.lookup(type) ;
                    Node r = makeNode(lex, datatype, lang, vType) ;
                    b.add(v.getName(), r) ;
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