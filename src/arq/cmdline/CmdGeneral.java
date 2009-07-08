/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import java.io.PrintStream;

import arq.cmd.CmdUtils;
import arq.cmd.TerminationException;

import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

// Added usage + some common flags
// This is the usual starting point for any sub 

public abstract class CmdGeneral extends CmdArgModule implements CallbackHelp//, VersionCallback
{
    static { ALog.setLog4j() ; CmdUtils.setN3Params() ; }

    protected ModGeneral modGeneral = new ModGeneral(this) ;
    protected ModVersion modVersion = new ModVersion(true) ;
    
    // Could be turned into a module but these are convenient as inherited flags
    // ModGeneral.
    // Which can set the globals here.
    
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

    final public void doHelp()
    {
        usage() ;
        throw new TerminationException(0) ;
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

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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