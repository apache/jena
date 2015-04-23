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

package org.apache.jena.sdb.layout2.index;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.jena.graph.Node ;
import org.apache.jena.sdb.StoreDesc ;
import org.apache.jena.sdb.compiler.QueryCompilerFactory ;
import org.apache.jena.sdb.layout2.NodeLayout2 ;
import org.apache.jena.sdb.layout2.StoreBase ;
import org.apache.jena.sdb.layout2.TableDescQuads ;
import org.apache.jena.sdb.layout2.TableDescTriples ;
import org.apache.jena.sdb.sql.RS ;
import org.apache.jena.sdb.sql.ResultSetJDBC ;
import org.apache.jena.sdb.sql.SDBConnection ;
import org.apache.jena.sdb.sql.SDBExceptionSQL ;
import org.apache.jena.sdb.store.SQLBridgeFactory ;
import org.apache.jena.sdb.store.SQLGenerator ;
import org.apache.jena.sdb.store.StoreFormatter ;
import org.apache.jena.sdb.store.StoreLoader ;
import org.apache.jena.sparql.util.NodeUtils ;

public class StoreBaseIndex extends StoreBase
{
    public StoreBaseIndex(SDBConnection connection, StoreDesc desc, StoreFormatter formatter, StoreLoader loader, QueryCompilerFactory compilerF, SQLBridgeFactory sqlBridgeF, SQLGenerator sqlGenerator)
    {
        super(connection, desc, 
              formatter, loader, compilerF, sqlBridgeF, sqlGenerator,
              new TableDescTriples(),
              new TableDescQuads(),
              new TableNodesIndex()) ;
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
            if ( NodeUtils.isSimpleString(node) || NodeUtils.isLangString(node) )
                datatype = "";
        }

        ResultSetJDBC rsx = null ;
        long hash = NodeLayout2.hash(lex, lang, datatype, typeId) ;
        try
        {
            rsx = connection.exec("SELECT id FROM Nodes WHERE hash = " + hash) ;
            ResultSet res = rsx.get() ;
            int id = -1 ;
            if (res.next()) 
                id = res.getInt(1) ;
            else
                // no graph, size == 0
                return 0 ;
            rsx.close();
            rsx = connection.exec("SELECT COUNT(*) FROM " + tableDescQuads.getTableName() + " WHERE g = " + id) ;
            res = rsx.get() ;
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
