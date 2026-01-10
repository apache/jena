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

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.fuseki.main.runner.ServerArgs;
import org.apache.jena.fuseki.main.sys.FusekiModule;

/**
 * Enable the {@code /$/ping} endpoint via the command line.
 * (May be disabled from a Fuseki server builder.)
 */
public class FMod_Ping implements FusekiModule {

    public static FusekiModule create() {
        return new FMod_Ping();
    }

    @Override
    public void serverArgsModify(CmdGeneral fusekiCmd, ServerArgs serverArgs) {
        // Initial setting.
        serverArgs.withPing = true;
    }

    private final static ArgDecl  argWithPing = new ArgDecl(ArgDecl.NoValue,  "withPing", "ping");

    @Override
    public void serverArgsPrepare(CmdGeneral fusekiCmd, ServerArgs serverArgs) {
        // As a default.
        if ( ! fusekiCmd.contains(argWithPing) )
            serverArgs.withPing = true;
    }

}
