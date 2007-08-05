/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.pldms;

import java.sql.SQLException;

import com.hp.hpl.jena.graph.test.NodeCreateUtils;

import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexOracle;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.ResultSetJDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionDesc;
import com.hp.hpl.jena.sdb.store.StoreLoaderPlus;
import com.hp.hpl.jena.sdb.store.TableDesc;

public class Scratch {

        /**
         * @param args
         * @throws SQLException 
         */
        public static void main(String[] args) throws SQLException {
                
        		Store store;
        		SDBConnection conn;
        	
                JDBC.loadDriverOracle();
                SDBConnectionDesc desc = SDBConnectionDesc.blank();
                desc.setHost("localhost:1521");
                desc.setName("XE");
                desc.setUser("jena");
                desc.setPassword("swara");
                desc.setType("oracle:thin");
                conn = SDBFactory.createConnection(desc);
                
                store = new StoreTriplesNodesIndexOracle(conn);
                store.getTableFormatter().format();
                store.getTableFormatter().addIndexes();
                store.close();
                conn.close();
                
                conn = SDBFactory.createConnection(desc);
                store = new StoreTriplesNodesIndexOracle(conn);
                
                StoreLoaderPlus loader = (StoreLoaderPlus) store.getLoader();
                
                TableDesc descT = store.getTripleTableDesc();
                
                loader.startBulkUpdate();
                loader.addTuple(descT, NodeCreateUtils.create("a"), NodeCreateUtils.create("a"), NodeCreateUtils.create("a"));
                loader.addTuple(descT, NodeCreateUtils.create("b"), NodeCreateUtils.create("a"), NodeCreateUtils.create("a"));
                loader.addTuple(descT, NodeCreateUtils.create("c"), NodeCreateUtils.create("a"), NodeCreateUtils.create("a"));
                loader.finishBulkUpdate();
                System.err.println("Nodes: " + getSize("Nodes", conn));
                System.err.println("Triples: " + getSize("Triples", conn));
                loader.startBulkUpdate();
                loader.deleteTuple(descT, NodeCreateUtils.create("a"), NodeCreateUtils.create("a"), NodeCreateUtils.create("a"));
                loader.deleteTuple(descT, NodeCreateUtils.create("b"), NodeCreateUtils.create("a"), NodeCreateUtils.create("a"));
                loader.deleteTuple(descT, NodeCreateUtils.create("c"), NodeCreateUtils.create("a"), NodeCreateUtils.create("a"));
                loader.finishBulkUpdate();
                System.err.println("Nodes: " + getSize("Nodes", conn));
                System.err.println("Triples: " + getSize("Triples", conn));
                store.close();
                conn.close();
        }
        
        public static Integer getSize(String table, SDBConnection conn) {
        	Integer size = -1;
        	
        	try {
        		ResultSetJDBC result = conn.execQuery("SELECT COUNT(*) AS NUM FROM " + table);
        		
        		if (result.get().next()) {
        			size = result.get().getInt("NUM");
        		}
				result.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
        	
        	return size;
        }
}
/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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
