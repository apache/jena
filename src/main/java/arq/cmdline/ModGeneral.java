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

public class ModGeneral extends ModBase
{
    private CallbackHelp helpCallback = null ;

    public ModGeneral(CallbackHelp callback) { this.helpCallback = callback ; }
    
    // Could be turned into a module but these are convenient as inherited flags 
    private final ArgDecl argDeclHelp        = new ArgDecl(false, "help", "h");
    private final ArgDecl argDeclVerbose     = new ArgDecl(false, "v", "verbose");
    private final ArgDecl argDeclQuiet       = new ArgDecl(false, "q", "quiet");
    private final ArgDecl argDeclDebug       = new ArgDecl(false, "debug");

    protected boolean verbose = false ;
    protected boolean quiet = false ;
    protected boolean debug = false ;
    protected boolean help = false ;
    
    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("General") ;
        cmdLine.add(argDeclVerbose, "-v   --verbose", "Verbose") ;
        cmdLine.add(argDeclQuiet, "-q   --quiet", "Run with minimal output") ;
        cmdLine.add(argDeclDebug, "--debug", "Output information for debugging") ;
        cmdLine.add(argDeclHelp, "--help", null) ;
    }

    @Override
    public void processArgs(CmdArgModule cmdLine)
    {
        verbose = cmdLine.contains(argDeclVerbose) ;
        quiet   = cmdLine.contains(argDeclQuiet) ;
        debug   = cmdLine.contains(argDeclDebug) ;
        if ( debug )
            verbose = true ;
        help = cmdLine.contains(argDeclHelp) ;
        if ( help )
            helpCallback.doHelp() ;
    }
}
