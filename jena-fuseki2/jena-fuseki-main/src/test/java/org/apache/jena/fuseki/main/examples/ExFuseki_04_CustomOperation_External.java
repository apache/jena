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

package org.apache.jena.fuseki.main.examples;

import java.util.ServiceLoader;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.http.HttpOp;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sys.JenaSystem;

/**
 * Example of adding a new operation to a Fuseki server by registering it with the
 * global Fuseki registries.
 *
 * The custom operation is loaded using {@link ServiceLoader} as shown in
 * {@link InitFusekiCustomOperation}.
 *
 * Doing this, adding the jar to the classpath, including the {@link ServiceLoader}
 * setup, will automatically add it to the server.
 * <p>
 * See <a href="https://jena.apache.org/documentation/notes/jena-repack.html">Repacking Jena jars</a>.
 * <p>
 * See <a href="https://jena.apache.org/documentation/notes/system-initialization.html">System Initialization</a>
 */
public class ExFuseki_04_CustomOperation_External {

    static {
        JenaSystem.init();
        FusekiLogging.setLogging();
    }

    // Example usage.
    public static void main(String...args) {

        // Imitate Service loader behaviour.
        new InitFusekiCustomOperation().start();


        // Standard Fuseki startup by commandline.
        // /ds/extra will be added because InitFusekiCustomOperation adds it to the default services.

        // ThreadLib.async(()->FusekiMainCmd.main("--port=3230", "--mem", "/ds"));
        //Lib.sleep(1000);
        // Same as the above command line except it does not block the thread (which is why the ThreadLib.async is added).
        FusekiServer.create().port(3230).add("/ds", DatasetGraphFactory.createTxnMem()).build().start();

        callOperation("extra");
        System.exit(0);
    }

    private static void callOperation(String name) {
        String x = HttpOp.httpGetString("http://localhost:3230/ds/"+name);
        if ( x == null ) {
            System.out.println("Not found : <null>");
        } else {
            System.out.print(x);
            if ( ! x.endsWith("\n") )
                System.out.println();
        }
    }
}