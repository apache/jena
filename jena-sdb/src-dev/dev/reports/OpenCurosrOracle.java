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

package dev.reports;

import java.sql.Connection;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionFactory;
import com.hp.hpl.jena.sdb.store.StoreFactory;
import com.hp.hpl.jena.util.FileManager;



public class OpenCurosrOracle
{
        public static void main(String[] argv) throws Exception {
            OpenCurosrOracle test = new OpenCurosrOracle();
            test.test();
        }
        
        public void test() throws Exception {
            System.out.print("\nOracle Open Cursor Test started ");
            // Get Store description
            String configFile = "sdb.ttl";
            StoreDesc desc = StoreDesc.read(configFile);
            Connection conn = SDBFactory.createSqlConnection(configFile);       
            
            // Keep jdbc connection open to simulate pooled connection
            try {
                SDBConnection sdbConn = SDBConnectionFactory.create(conn);          
                Model model = FileManager.get().loadModel("D.ttl");
                
                for (int i=0; i <= 150; i++)
                {
                    Store store = null;
                    try {
//                      System.out.println("RUN " + i);
                        System.out.println(". "+i);

                        // create store                 
                        store = StoreFactory.create(desc, sdbConn);
        
                        // save model
                        Model m = SDBFactory.connectDefaultModel(store) ;               
                        m.begin();
                        m.removeAll();
                        m.add(model);
                        m.commit();

                    } catch (Exception e) {
                        System.out.println( "\nFailed on iteration " + i);
                        e.printStackTrace();
                        throw e;
                    } finally {
                        if (store != null) { store.close(); }
                    }
                }
            } finally {
                if (conn != null) { 
                    conn.close(); 
                }
            }
            
            System.out.println("\nTest successful!");
            
        }
}
