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
 *
 *     SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.fuseki.main.cmds;

import java.util.function.Consumer;

import org.apache.jena.cmd.CmdException;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.FusekiAbortException;
import org.apache.jena.fuseki.system.FusekiLogging;

class RunFuseki {
    // This class wraps an entry point so that it can take control of logging setup.
    // This class does not depend via inheritance on any Jena code
    // and does not trigger Jena initialization.
    // FusekiLogging runs before any Jena code can trigger logging setup.
    //
    // Inheritance causes initialization in the super class first, before class
    // initialization code in this class.

    static { FusekiLogging.setLogging(); }

    public static void run(String[] args, Consumer<String[]> action) {
        try {
            action.accept(args);
        } catch (IllegalArgumentException | CmdException ex ) {
            exit(1, ex.getMessage());
        } catch (FusekiAbortException ex) {
            exit(ex.getCode(), ex.getMessage());
        }
    }

    private static void exit(int exitCode, String message) {
        if ( message != null )
            Fuseki.fusekiLog.error(message);
        //Fuseki.fusekiLog.error("Exit");
        System.out.flush();
        System.err.flush();
        System.exit(exitCode);
    }
}
