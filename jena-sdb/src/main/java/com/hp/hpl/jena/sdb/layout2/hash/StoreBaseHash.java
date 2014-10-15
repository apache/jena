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
import java.sql.ResultSet;
import java.sql.SQLException;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.compiler.QueryCompilerFactory;
import com.hp.hpl.jena.sdb.layout2.NodeLayout2;
import com.hp.hpl.jena.sdb.layout2.StoreBase;
import com.hp.hpl.jena.sdb.layout2.TableDescQuads;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;
import com.hp.hpl.jena.sdb.sql.RS;
import com.hp.hpl.jena.sdb.sql.ResultSetJDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.store.SQLBridgeFactory;
import com.hp.hpl.jena.sdb.store.SQLGenerator;
import com.hp.hpl.jena.sdb.store.StoreFormatter;
import com.hp.hpl.jena.sdb.store.StoreLoader;

public class StoreBaseHash extends StoreBase
{
    public StoreBaseHash(SDBConnection connection, StoreDesc desc, StoreFormatter formatter, StoreLoader loader, QueryCompilerFactory compilerF, SQLBridgeFactory sqlBridgeF, SQLGenerator sqlGenerator)
    {
        super(connection, desc, 
              formatter, loader, compilerF, sqlBridgeF, sqlGenerator,
              new TableDescTriples(),
              new TableDescQuads() ,
              new TableNodesHash()) ;
    }

    @Override
    public long getSize(Node node)
    {
        return getSize(getConnection(), getQuadTableDesc(), node) ;
    }
        
    public static long getSize(SDBConnection connection, TableDescQuads tableDescQuads, Node node)
    {    
		String lex = NodeLayout2.nodeToLex(node);
        int typeId = NodeLayout2.nodeToType(node);

        String lang = "";
        String datatype = "";

        if (node.isLiteral())
        {
            lang = node.getLiteralLanguage();
            datatype = node.getLiteralDatatypeURI();
            if (datatype == null)
                datatype = "";
        }
        
        ResultSetJDBC rsx = null ;
        long hash = NodeLayout2.hash(lex, lang, datatype, typeId) ;
        try
        {
            rsx = connection.exec("SELECT COUNT(*) FROM " + tableDescQuads.getTableName() + " WHERE g = " + hash) ;
            ResultSet res = rsx.get() ;
            res.next() ;
            long result = res.getLong(1) ;
            return result ;
        } catch (SQLException e)
        {
            throw new SDBExceptionSQL("Failed to get graph size", e) ;
        } finally
        {
            RS.close(rsx) ;
        }
	}
}
