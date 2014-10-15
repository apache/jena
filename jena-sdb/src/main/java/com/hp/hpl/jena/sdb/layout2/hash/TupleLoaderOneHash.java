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

package com.hp.hpl.jena.sdb.layout2.hash;

import static org.apache.jena.atlas.lib.StrUtils.strjoinNL;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlConstant;
import com.hp.hpl.jena.sdb.layout2.NodeLayout2;
import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.sql.RS;
import com.hp.hpl.jena.sdb.sql.ResultSetJDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SQLUtils;
import com.hp.hpl.jena.sdb.store.TableDesc;
import com.hp.hpl.jena.sdb.store.TupleLoaderOne;

public class TupleLoaderOneHash extends TupleLoaderOne
{
    public TupleLoaderOneHash(SDBConnection connection)
    { super(connection) ; }

    /* Convenience constructor */
    public TupleLoaderOneHash(SDBConnection connection, TableDesc tableDesc)
    { super(connection, tableDesc) ; }

    /* Convenience constructor */
    public TupleLoaderOneHash(Store store, TableDesc tableDesc)
    { super(store.getConnection(), tableDesc) ; }
    
    
    @Override
    public SqlConstant getRefForNode(Node node) throws SQLException 
    {
        return new SqlConstant(NodeLayout2.hash(node)) ;
    }

    @Override
    public SqlConstant insertNode(Node node) throws SQLException 
    {
        int typeId  = NodeLayout2.nodeToType(node) ;
        String lex = NodeLayout2.nodeToLex(node) ;
        String lang = "" ;
        String datatype = "" ;
        
        if ( node.isLiteral() )
        {
            lang = node.getLiteralLanguage() ;
            datatype = node.getLiteralDatatypeURI() ;
            if ( datatype == null )
                datatype = "" ;
        }
        
        long hash = NodeLayout2.hash(lex,lang,datatype,typeId);
        
        // Existance check
        
        String sqlStmtTest = strjoinNL(
                "SELECT hash FROM "+TableDescNodes.name(),
                "WHERE hash = "+hash
                ) ;
        
        ResultSetJDBC rsx = null ; 
        try {
            rsx = connection().execQuery(sqlStmtTest) ;
            ResultSet rs = rsx.get();
            boolean b = rs.next();
            if ( b )
                // Exists
                return new SqlConstant(hash) ;
        } finally { RS.close(rsx) ; }
        
        String sqlStmt = strjoinNL(
                "INSERT INTO "+TableDescNodes.name()+"(hash,lex,lang,datatype,type) VALUES",
                "  ("+hash+", ",
                "   "+SQLUtils.quoteStr(lex)+", ",
                "   "+SQLUtils.quoteStr(lang)+", ",
                "   "+SQLUtils.quoteStr(datatype)+", ",
                "   "+typeId, 
                ")" ) ;
        connection().execUpdate(sqlStmt) ;
        return new SqlConstant(hash) ;
    }
}
