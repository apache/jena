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

package org.apache.jena.fuseki.mod.admin;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.ArgModuleGeneral;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.mgt.FusekiApp;

public class ArgModuleAdmin implements ArgModuleGeneral {
    // Add a static of "extra command"

    private ArgDecl argAdmin = new ArgDecl(true, "admin");
    private ArgDecl argAdminArea = new ArgDecl(true, "adminArea", "adminBase");

    public ArgModuleAdmin() { }

    @Override
    public void processArgs(CmdArgModule cmdLine) {
        System.out.println("ArgModuleAdmin");
        String admin = cmdLine.getValue(argAdmin);
        if ( admin == null ) {
            return;
        }

        if ( admin.equals("localhost") ) {}
        else {
            String pwFile = admin;
        }

        String dirStr = cmdLine.getValue(argAdminArea);
        Path directory = Path.of(dirStr);

        if ( ! Files.isDirectory(directory) )
            throw new FusekiConfigException("Not a directory: "+dirStr);

        if ( ! Files.isWritable(directory)  )
            throw new FusekiConfigException("Not writable: "+dirStr);

        FusekiApp.FUSEKI_BASE = directory;
    }

    @Override
    public void registerWith(CmdGeneral cmdLine) {
        cmdLine.add(argAdmin,     "--admin=[UserPasswordFile|localhost]", "Enable the admin module");
        cmdLine.add(argAdminArea, "--run=DIR", "Admin state directory");
    }
}