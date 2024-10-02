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

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.fuseki.main.FusekiServer;

public class CustomisedFusekiMain extends FusekiMain {

    private final ArgDecl customFlag = new ArgDecl(ArgDecl.NoValue, "custom-flag");
    private final ArgDecl customArg = new ArgDecl(ArgDecl.HasValue, "custom-arg");

    public CustomisedFusekiMain(String[] args) {
        super(args);

        add(customFlag);
        add(customArg);
    }

    public static FusekiServer buildCustom(String[] args) {
        // Parses command line, sets arguments.
        CustomisedFusekiMain inner = new CustomisedFusekiMain(args);
        // Process command line args according to the argument specified.
        inner.process();
        // Apply command line/serverConfig to a builder.
        FusekiServer.Builder builder = inner.builder();
        inner.applyServerArgs(builder, inner.serverConfig);
        return builder.build();
    }

    @Override
    protected void processModulesAndArgs() {
        super.processModulesAndArgs();

        this.serverConfig.extra.put("flag", contains(customFlag));
        this.serverConfig.extra.put("arg", getValue(customArg));
    }

    @Override
    protected void applyServerArgs(FusekiServer.Builder builder, ServerConfig serverConfig) {
        super.applyServerArgs(builder, serverConfig);

        builder.addServletAttribute("flag", this.serverConfig.extra.get("flag"));
        builder.addServletAttribute("arg", this.serverConfig.extra.get("arg"));
    }
}
