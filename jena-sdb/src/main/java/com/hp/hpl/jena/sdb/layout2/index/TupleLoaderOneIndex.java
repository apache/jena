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

import static org.apache.jena.atlas.lib.StrUtils.strjoinNL ;

import java.sql.ResultSet ;
import java.sql.SQLException ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sdb.SDBException ;
import com.hp.hpl.jena.sdb.Store ;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlConstant ;
import com.hp.hpl.jena.sdb.layout2.NodeLayout2 ;
import com.hp.hpl.jena.sdb.layout2.TableDescNodes ;
import com.hp.hpl.jena.sdb.sql.RS ;
import com.hp.hpl.jena.sdb.sql.ResultSetJDBC ;
import com.hp.hpl.jena.sdb.sql.SDBConnection ;
import com.hp.hpl.jena.sdb.sql.SQLUtils ;
import com.hp.hpl.jena.sdb.store.TableDesc ;
import com.hp.hpl.jena.sdb.store.TupleLoaderOne ;

public class TupleLoaderOneIndex extends TupleLoaderOne
{
    private static Logger log = LoggerFactory.getLogger(TupleLoaderOneIndex.class);

    public TupleLoaderOneIndex(SDBConnection connection)
    { super(connection) ; }

    /* Convenience constructor */
    public TupleLoaderOneIndex(SDBConnection connection, TableDesc tableDesc)
    { super(connection, tableDesc) ; }

    /* Convenience constructor */
    public TupleLoaderOneIndex(Store store, TableDesc tableDesc)
    { super(store.getConnection(), tableDesc) ; }
    


    @Override
    public SqlConstant getRefForNode(Node node) throws SQLException 
    {
        return new SqlConstant(getIndex(connection(), node, false)) ;
    }

    @Override
    public SqlConstant insertNode(Node node) throws SQLException 
    {
        return new SqlConstant(getIndex(connection(), node, true)) ;
    }
    
    /// ----------
    
    private static int getIndex(SDBConnection conn, Node node, boolean create) throws SQLException
    {
        long hash = NodeLayout2.hash(node) ;
        String lex  = NodeLayout2.nodeToLex(node) ;
        String hashStr = Long.toString(hash) ;
        String sqlStmt = "SELECT id FROM Nodes WHERE hash = "+hashStr ;
        
        ResultSetJDBC rsx = null ;
        try {
            rsx = conn.execQuery(sqlStmt) ;
            ResultSet rs = rsx.get();
            if ( ! rs.next() )
            {
                if ( ! create )
                    throw new SDBException("No such node in table: "+node) ;
                insertNode(conn, lex, node) ;
                // And get it again to find the auto-allocate ID.
                return getIndex(conn, node, false) ;
            }

            int id = rs.getInt("id") ;
            if ( rs.next() )
                log.warn("More than one hit for : "+sqlStmt+" (ignored)") ;
            return id ;    
        } catch (SQLException ex)
        {
            log.warn("SQLException: "+ex.getMessage()) ;
            throw ex ;
        } finally { RS.close(rsx) ; }
    }
    
    private static void insertNode(SDBConnection conn, String lex,  Node node) throws SQLException
    {
        int typeId  = NodeLayout2.nodeToType(node) ;
        
        String lang = "" ;
        String datatype = "" ;
        
        if ( node.isLiteral() )
        {
            lang = node.getLiteralLanguage() ;
            datatype = node.getLiteralDatatypeURI() ;
            if ( datatype == null )
                datatype = "" ;
        }
//        // Value of the node
//        ValueType vType = ValueType.lookup(node) ;
//        int valInt = 0 ;
//        if ( vType == ValueType.INTEGER )
//            valInt = Integer.parseInt(lex) ;
//        
//        double valDouble = 0 ;
//        if ( vType == ValueType.DOUBLE )
//            valDouble = Double.parseDouble(lex) ;
//
//        String valDateTime = "0000-01-01 00:00:00" ;
//        if ( vType == ValueType.DATETIME )
//            valDateTime = SQLUtils.toSQLdatetimeString(lex) ;
//        
//        valDateTime = SQLUtils.quoteStr(valDateTime) ;
        
        long hash = NodeLayout2.hash(lex,lang,datatype,typeId);
        
        String sqlStmt = strjoinNL(
                "INSERT INTO "+TableDescNodes.name()+"(hash,lex,lang,datatype,type) VALUES",
                "  ("+hash+", ",
                "   "+SQLUtils.quoteStr(lex)+", ",
                "   "+SQLUtils.quoteStr(lang)+", ",
                "   "+SQLUtils.quoteStr(datatype)+", ",
                "   "+typeId, 
                ")" ) ;
        conn.execUpdate(sqlStmt) ;
    }
}
