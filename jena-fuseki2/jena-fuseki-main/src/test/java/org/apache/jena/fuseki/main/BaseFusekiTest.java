/**
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

package org.apache.jena.fuseki.main;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.http.UpdateExecHTTP;
import org.apache.jena.sparql.modify.request.Target;
import org.apache.jena.sparql.modify.request.UpdateDrop;
import org.apache.jena.system.Txn;
import org.apache.jena.update.Update;

public class BaseFusekiTest
{
    // Must be set by test
    protected static DatasetGraph dsgTesting = DatasetGraphFactory.createTxnMem();
    protected static FusekiServer server;
    protected static int port;
    protected static String serverURL;

    public static final String datasetName()    { return "database"; }
    public static final String datasetPath()    { return "/"+datasetName(); }


    // Whether to use a transaction on the dataset or to use SPARQL Update.
    protected static boolean CLEAR_DSG_DIRECTLY = true;

    protected static void resetDatabase() {
        if ( CLEAR_DSG_DIRECTLY ) {
            Txn.executeWrite(dsgTesting, ()->dsgTesting.clear());
        } else {
            Update clearRequest = new UpdateDrop(Target.ALL);
            try {
                UpdateExecHTTP.service(serviceUpdate()).update(clearRequest).execute();
            }
            catch (Throwable e) {e.printStackTrace(); throw e;}
        }
    }

    // Abstraction that runs a SPARQL server for tests.
    protected static final String urlRoot()        { return serverURL; }
    protected static final String databaseURL()    { return serverURL+datasetName(); }

    protected static final String serviceUpdate()  { return databaseURL()+"/update"; }
    protected static final String serviceQuery()   { return databaseURL()+"/query"; }
    protected static final String serviceGSP_R()   { return databaseURL()+"/get"; }
    protected static final String serviceGSP()     { return databaseURL()+"/data"; }
}

