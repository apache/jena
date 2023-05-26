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

package org.apache.jena.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.jena.Jena;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.lib.Version;

/**
 * Version information.
 * <p>
 * The version is the manifest entry in the jar file for a
 * class. It is not available if the class comes from a development tree where the
 * class file is from "target" (maven).
 */
public class ModVersion extends ModBase
{
    protected final ArgDecl versionDecl = new ArgDecl(ArgDecl.NoValue, "version");
    protected boolean version = false;
    protected boolean printAndExit = false;

    // (system name, version string)
    private List<Pair<String, Optional<String>>> descriptions = new ArrayList<>();

    public ModVersion(boolean printAndExit) {
        this.printAndExit = printAndExit;
    }

    /** Add a class for the version number */
    public void addClass(Class<? > c) {
        addClass(c.getSimpleName(), c);
    }

    /** Add a label and a class for the version number */
    public void addClass(String name, Class<? > cls) {
        Pair<String, Optional<String>> desc = Pair.create(name, Version.versionForClass(cls));
        descriptions.add(desc);
    }

    @Override
    public void registerWith(CmdGeneral cmdLine) {
        cmdLine.add(versionDecl, "--version", "Version information");
    }

    @Override
    public void processArgs(CmdArgModule cmdLine) {
        if ( cmdLine.contains(versionDecl) )
            version = true;
        // The --version flag causes us to print and exit.
        if ( version && printAndExit )
            printVersionAndExit();
    }

    public boolean getVersionFlag() {
        return version;
    }

    public void printVersion() {
        if ( descriptions.isEmpty() ) {
            Version.printVersion(System.out, null, Version.versionForClass(Jena.class));
            return;
        }
        descriptions.forEach(p->Version.printVersion(System.out, p.getLeft(), p.getRight()));
    }

    public void printVersionAndExit() {
        printVersion();
        System.exit(0);
    }
}
