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

package rdfpatch;

import java.io.IOException;
import java.io.InputStream;

import arq.cmdline.ModContext;
import arq.cmdline.ModTime;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.sys.JenaSystem;

/** Abstract base class to work on patch files given on the command line */
public abstract class CmdRDFPatch extends CmdGeneral {
    static {
        LogCtl.setLogging();
        JenaSystem.init();
    }

    protected ModTime modTime = new ModTime();
    protected ModContext modContext = new ModContext();
    protected ArgDecl strictDecl = new ArgDecl(ArgDecl.NoValue, "strict");

    protected CmdRDFPatch(String[] argv) {
        super(argv);
    }

    @Override
    protected String getSummary() {
        return getCommandName() + " FILE...";
    }

    @Override
    protected void processModulesAndArgs() {
        super.processModulesAndArgs();
    }

    @Override
    protected void exec() {
        execStart();
        try {
            // Patches
            if ( getPositional().isEmpty() )
                execOne("Stdin", System.in);
            else {
                getPositional().forEach(fn -> {
                    try (InputStream in = IO.openFile(fn)) {
                        execOne(fn, in);
                    } catch (IOException ex) {
                        IO.exception(ex);
                        return;
                    }
                });
            }
        }
        finally { execFinish(); }
    }

    protected abstract void execStart();
    protected abstract void execOne(String source, InputStream input);
    protected abstract void execFinish();
}
