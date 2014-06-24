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
import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.MappingRegistry ;
import com.hp.hpl.jena.sparql.util.Symbol ;

public class ModSymbol implements ArgModuleGeneral
{
    protected final ArgDecl setDecl = new ArgDecl(ArgDecl.HasValue, "set", "define", "defn", "def") ;
    Context context = new Context() ;
    
    public ModSymbol() { }
    
    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("Symbol definition") ;
        cmdLine.add(setDecl, "--set", "Set a configuration symbol to a value") ;
    }
    
    public void checkCommandLine(CmdArgModule cmdLine)
    {}

    @Override
    public void processArgs(CmdArgModule cmdLine)
    {
        ARQ.init();
        
        if ( cmdLine.getValues(setDecl) == null || cmdLine.getValues(setDecl).size() == 0 )
            return ;

        for ( String arg : cmdLine.getValues( setDecl ) )
        {
            String[] frags = arg.split( "=", 2 );
            if ( frags.length != 2 )
            {
                throw new RuntimeException( "Can't split '" + arg + "' -- looking for '=' to separate name and value" );
            }

            String symbolName = frags[0];
            String value = frags[1];

            // Make it a long name.
            symbolName = MappingRegistry.mapPrefixName( symbolName );
            Symbol symbol = Symbol.create( symbolName );
            context.set( symbol, value );
        }
        
        ARQ.getContext().putAll(context) ;
    }
    
    public void verbose() { verbose(System.out) ; }
    
    public void verbose(PrintStream stream)
    {
        IndentedWriter out = new IndentedWriter(stream) ;
        verbose(out) ;
        out.flush();
    }
    
    public void verbose(IndentedWriter out)
    {
        for ( Symbol symbol : context.keys() )
        {
            String value = context.getAsString( symbol );
            out.println( symbol + " -> " + value );
        }
    }
}
