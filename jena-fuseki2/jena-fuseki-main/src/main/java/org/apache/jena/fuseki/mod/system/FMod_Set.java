/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.fuseki.mod.system;

import arq.cmdline.CmdLib;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.fuseki.main.runner.ServerArgs;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.util.Context;

/**
 * Provide setting of context symbols for Fuseki.
 */
public class FMod_Set implements FusekiModule {
    @Override
    public String name() {
        return "FMod Set";
    }

    // Align with ModContext.
    private static ArgDecl argSet = new ArgDecl(ArgDecl.HasValue, "set", "define", "defn", "def");

    public static FusekiModule create() {
        return new FMod_Set();
    }

    @Override
    public void serverArgsModify(CmdGeneral fusekiCmd, ServerArgs serverArgs) {
        fusekiCmd.add(argSet, "--set", "Set a configuration symbol to a value");
    }

    @Override
    public void serverArgsPrepare(CmdGeneral fusekiCmd, ServerArgs serverArgs) {
        if ( fusekiCmd.contains(argSet) ) {
            // Isolate while processing.
            Context context = new Context();
            for ( String arg : fusekiCmd.getValues(argSet) ) {
                CmdLib.processArgSet(arg, context);
            }
            // Currently, has to be visible to the query engine throughout the JVM.
            ARQ.getContext().putAll(context);
        }
    }

}
