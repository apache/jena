/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tdb.cmdline;

import arq.cmdline.ArgDecl ;
import arq.cmdline.ArgModuleGeneral ;
import arq.cmdline.CmdArgModule ;
import arq.cmdline.CmdGeneral ;

public class ModLangParse implements ArgModuleGeneral
{
    private ArgDecl argCheck    = new ArgDecl(ArgDecl.NoValue, "check") ;
    private ArgDecl argNoCheck    = new ArgDecl(ArgDecl.NoValue, "nocheck") ;
    private ArgDecl argSink     = new ArgDecl(ArgDecl.NoValue, "sink", "null") ;

    private ArgDecl argSkip     = new ArgDecl(ArgDecl.NoValue, "skip") ;
    private ArgDecl argNoSkip   = new ArgDecl(ArgDecl.NoValue, "noSkip") ;
    private ArgDecl argStop     = new ArgDecl(ArgDecl.NoValue, "stopOnError", "stoponerror", "stop") ;

    private boolean check       = true ;
    private boolean skipOnError = false ;
    private boolean stopOnError = false ;
    private boolean bitbucket   = false ; 

    
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("Parser control") ;
        cmdLine.add(argSink,    "--sink",       "Parse but throw away output") ;
        cmdLine.add(argCheck,   "--check",      "Addition checking of RDF terms") ;
        cmdLine.add(argNoCheck, "--nocheck",    "Turn off checking of RDF terms") ;
        cmdLine.add(argSkip,    "--skip",       "Skip (do not output) triples failing the RDF term tests") ;
        cmdLine.add(argNoSkip,  "--noSkip",     "Include triples failing the RDF term tests (not recommended)") ;
        cmdLine.add(argStop,    "--stop",       "Stop parsing if an RDF term fails testing") ;
    }

    public void processArgs(CmdArgModule cmdLine)
    {
        if ( cmdLine.contains(argNoCheck) )
            check = false ;
        if ( cmdLine.contains(argCheck) )
            check = true ;   
        
        if ( cmdLine.contains(argSkip) )
            skipOnError = true ; 
        if ( cmdLine.contains(argNoSkip) )
            skipOnError = false ;
        
        stopOnError = cmdLine.contains(argStop) ;
        bitbucket = cmdLine.contains(argSink) ; 
    }

    public boolean isCheck()
    {
        return check ;
    }

    public boolean isSkipOnError()
    {
        return skipOnError ;
    }

    public boolean isStopOnError()
    {
        return stopOnError ;
    }

    public boolean toBitBucket()
    {
        return bitbucket ;
    }

}

/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */