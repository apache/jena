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

import com.hp.hpl.jena.Jena ;
import com.hp.hpl.jena.sparql.util.Version ;

public class ModVersion implements ArgModuleGeneral
{
    protected final ArgDecl versionDecl = new ArgDecl(ArgDecl.NoValue, "version") ;
    protected boolean version = false ;
    protected boolean printAndExit = false ;
    
    private Version versionMgr = new Version() ; 
    
    public ModVersion(boolean printAndExit)
    {
        this.printAndExit = printAndExit ;
        versionMgr.addClass(Jena.class) ;
    }
    
    public void addClass(Class<?> c) { versionMgr.addClass(c) ; }
    
    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.add(versionDecl, "--version", "Version information") ;
    }

    @Override
    public void processArgs(CmdArgModule cmdLine)
    {
        if ( cmdLine.contains(versionDecl) )
            version = true ;
        // The --version flag causes us to print and exit. 
        if ( version && printAndExit )
            printVersionAndExit() ;
    }

    public boolean getVersionFlag() { return version ; }
    
    public void printVersion()
    {
        versionMgr.print() ;
    }  
     
    public void printVersionAndExit()
    {
        printVersion() ;
        System.exit(0) ;
    }
}
