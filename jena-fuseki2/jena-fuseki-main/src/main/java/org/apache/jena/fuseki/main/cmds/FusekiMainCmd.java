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

package org.apache.jena.fuseki.main.cmds;

import org.apache.jena.fuseki.system.FusekiLogging;

/** Fuseki command that runs a Fuseki server without the admin UI, just SPARQL services.
 * <p>
 * Use {@code --conf=} for multiple datasets and specific service names. 
 * <p>
 * The command line dataset setup only supports a single dataset.
 */

public class FusekiMainCmd {
    // This class wraps FusekiBasicMain so that it can take control of logging setup.
    // It does not depend via inheritance on any Jena code - FusekiBasicMain does.
    // Inheritance causes initialization in the super class first, before class
    // initialization code in this class.
    
    static { FusekiLogging.setLogging(); }

    /**
     * Build and run, a server based on command line syntax. This operation does not
     * return. See {@link FusekiMain#build} to build a server using command line
     * syntax but not start it.
     */
    static public void main(String... argv) {
        FusekiMain.innerMain(argv);
    }
}
