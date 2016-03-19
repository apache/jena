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

package jena.cmd;

import java.io.PrintStream ;

import org.apache.jena.atlas.io.IndentedWriter ;
import jena.cmd.ArgModuleGeneral;
import jena.cmd.CmdArgModule;
import jena.cmd.ModGeneral;

// Added usage + some common flags
// This is the usual starting point for any sub 

public abstract class CmdGeneral extends CmdArgModule
{
    protected ModGeneral modGeneral = new ModGeneral(this::printHelp) ;
    protected ModVersion modVersion = new ModVersion(true) ;
    
    // Could be turned into a module but these are convenient as inherited flags
    
    protected CmdGeneral(String[] argv)
    {
        super(argv) ;
        addModule(modGeneral) ;
        addModule(modVersion) ;
    }

    @Override
    public void addModule(ArgModuleGeneral argModule)
    {
        super.addModule(argModule) ;
        argModule.registerWith(this) ;
    }
    
    protected boolean isVerbose() { return modGeneral.verbose ; }
    protected boolean isQuiet()   { return modGeneral.quiet ; }
    protected boolean isDebug()   { return modGeneral.debug ; }
    protected boolean help()      { return modGeneral.help ; }

    final public void printHelp()
    {
        usage() ;
        throw new TerminationException(0) ;
    }

    @Override
    protected void processModulesAndArgs()
    { 
        if ( modVersion.getVersionFlag() )
            modVersion.printVersionAndExit() ;
    }
    
    private Usage usage = new Usage() ; 
    
    protected String cmdName = null ;

    protected abstract String getSummary() ;

    public void usage() { usage(System.err) ; }

    public void usage(PrintStream pStr)
    {
        IndentedWriter out = new IndentedWriter(pStr) ;
        out.println(getSummary()) ;
        usage.output(out) ;
    }
    
    public void add(ArgDecl argDecl, String argName, String msg)
    {
        add(argDecl) ;
        getUsage().addUsage(argName, msg) ;
    }

    public Usage getUsage() { return usage ; }
}
