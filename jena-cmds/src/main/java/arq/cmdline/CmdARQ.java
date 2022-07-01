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

package arq.cmdline;

import org.apache.jena.Jena;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.query.ARQ;
import org.apache.jena.sys.JenaSystem;

public abstract class CmdARQ extends CmdGeneral {
    static {
        JenaSystem.init();
    }

    protected ModContext modContext = new ModContext();
    ArgDecl strictDecl = new ArgDecl(ArgDecl.NoValue, "strict");

    protected boolean cmdStrictMode = false;

    protected CmdARQ(String[] argv) {
        super(argv);
        modVersion.addClass(Jena.class);
        super.add(strictDecl, "--strict", "Operate in strict SPARQL mode (no extensions of any kind)");
        addModule(modContext);
    }

    @Override
    protected void processModulesAndArgs() {
        super.processModulesAndArgs();
        if ( super.contains(strictDecl) )
            ARQ.setStrictMode();
        cmdStrictMode = super.contains(strictDecl);
    }

    @Override
    protected String getCommandName() {
        return Lib.className(this);
    }
}
