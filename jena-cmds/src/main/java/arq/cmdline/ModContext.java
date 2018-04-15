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

import java.io.PrintStream ;

import jena.cmd.ArgDecl;
import jena.cmd.CmdArgModule;
import jena.cmd.CmdGeneral;
import jena.cmd.ModBase;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.MappingRegistry ;
import org.apache.jena.sparql.util.Symbol ;
import org.apache.jena.sys.JenaSystem ;

/** Set Context items */
public class ModContext extends ModBase
{
    static { JenaSystem.init(); }

    protected final ArgDecl setDecl = new ArgDecl(ArgDecl.HasValue, "set", "define", "defn", "def") ;

    private Context context = new Context() ;

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

        for ( String arg : cmdLine.getValues(setDecl) ) {
            String[] frags = arg.split("=", 2);
            if ( frags.length != 2 ) {
                throw new RuntimeException("Can't split '" + arg + "' -- looking for '=' to separate name and value");
            }

            String symbolName = frags[0];
            String value = frags[1];

            // Make it a long name.
            symbolName = MappingRegistry.mapPrefixName(symbolName);
            Symbol symbol = Symbol.create(symbolName);
            context.set(symbol, value);
        }

        ARQ.getContext().putAll(context);
    }

    public void verbose() {
        verbose(System.out);
    }

    public void verbose(PrintStream stream) {
        IndentedWriter out = new IndentedWriter(stream);
        verbose(out);
        out.flush();
    }

    public void verbose(IndentedWriter out) {
        for ( Symbol symbol : context.keys() ) {
            String value = context.getAsString(symbol);
            out.println(symbol + " -> " + value);
        }
    }
}
