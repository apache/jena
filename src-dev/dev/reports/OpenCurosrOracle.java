/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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