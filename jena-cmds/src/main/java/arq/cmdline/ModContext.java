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

package arq.cmdline;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sys.JenaSystem;

/** Set Context items */
public class ModContext extends ModBase
{
    static { JenaSystem.init(); }

    protected final ArgDecl setDecl = new ArgDecl(ArgDecl.HasValue, "set", "define", "defn", "def");

    public ModContext() {}

    @Override
    public void registerWith(CmdGeneral cmdLine) {
        cmdLine.getUsage().startCategory("Symbol definition");
        cmdLine.add(setDecl, "--set", "Set a configuration symbol to a value");
    }

    public void checkCommandLine(CmdArgModule cmdLine) {}

    @Override
    public void processArgs(CmdArgModule cmdLine) {
        if ( cmdLine.getValues(setDecl) == null || cmdLine.getValues(setDecl).size() == 0 )
            return;

        // Isolate while processing.
        Context context = new Context();

        for ( String arg : cmdLine.getValues(setDecl) ) {
            CmdLib.processArgSet(arg, context);
        }

        ARQ.getContext().putAll(context);
    }
}
