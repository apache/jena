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

package org.apache.jena.test.rdfconnection;

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiLib;
import org.apache.jena.fuseki.main.FusekiServer ;
import org.apache.jena.rdfconnection.AbstractTestRDFConnection;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.system.Txn ;
import org.junit.AfterClass ;
import org.junit.Before ;
import org.junit.BeforeClass ;

public class TestRDFConnectionRemote extends AbstractTestRDFConnection {
    private static FusekiServer server ;
    private static DatasetGraph serverdsg = DatasetGraphFactory.createTxnMem() ;
    protected static int PORT; 
    
    @BeforeClass
    public static void beforeClass() {
        PORT = FusekiLib.choosePort();
        server = FusekiServer.create()
            .port(PORT)
            .add("/ds", serverdsg)
            .build() ;
        LogCtl.setLevel(Fuseki.serverLogName,  "WARN");
        LogCtl.setLevel(Fuseki.actionLogName,  "WARN");
        LogCtl.setLevel(Fuseki.requestLogName, "WARN");
        LogCtl.setLevel(Fuseki.adminLogName,   "WARN");
        LogCtl.setLevel(Fuseki.adminLogName,   "WARN");
        LogCtl.setLevel("org.eclipse.jetty",   "WARN");
        server.start() ;
    }

    @Before
    public void beforeTest() {
        // Clear server
        Txn.executeWrite(serverdsg, ()->serverdsg.clear()) ;
    }

//  @After
//  public void afterTest() {}
    
    @AfterClass
    public static void afterClass() {
        server.stop(); 
    }
    
    @Override
    protected boolean supportsAbort() { return false ; }

    @Override
    protected RDFConnection connection() {
        return RDFConnectionFactory.connect("http://localhost:"+PORT+"/ds");
    }
}

