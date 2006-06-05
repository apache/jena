/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb.cmd;

import static java.lang.String.format;

import java.util.List;

import arq.cmd.TerminateException;
import arq.cmdline.ArgDecl;
import arq.cmdline.CmdMain;

public abstract class CmdGeneral extends CmdMain
{
    static {
        //  Tune N3 output for result set output.
        System.setProperty("usePropertySymbols",   "false") ;
        System.setProperty("objectLists" ,         "false") ;
        System.setProperty("minGap",               "2") ;
        System.setProperty("propertyColumn",       "14") ;
    }    
   
    protected final ArgDecl argDeclHelp        = new ArgDecl(false, "help", "h");
    protected final ArgDecl argDeclTime        = new ArgDecl(true, "time");
    protected final ArgDecl argDeclVerbose     = new ArgDecl(false, "v", "verbose");
    protected final ArgDecl argDeclQuiet       = new ArgDecl(false, "q", "quiet");
    protected final ArgDecl argDeclDebug       = new ArgDecl(false, "debug");

    protected boolean verbose = false ;
    protected boolean quiet = false ;
    protected boolean timeCommand = true ;
    protected boolean debug = false ;

    private String [] usage = new String[]{ "Complain: someone forgot the usage string" } ;
    protected String cmdName = null ;

    protected CmdGeneral(String name, String argv[])
    {
        super(argv) ;
        this.cmdName = name ;
        add(argDeclVerbose) ;
        add(argDeclQuiet) ;
        add(argDeclTime) ;
        add(argDeclDebug) ;
        add(argDeclHelp) ;
    }
    
    @Override 
    public void process()
    {
        try {
            super.process();
        } catch (IllegalArgumentException ex)
        {
            System.err.println(ex.getMessage()) ;
            usage() ;
            throw new TerminateException(1) ;
        }
        
        if ( contains(argDeclHelp) )
        {
            usage() ;
            throw new TerminateException(0) ;
        }
        
        verbose = contains(argDeclVerbose) ;
        quiet = contains(argDeclQuiet) ;
        debug = contains(argDeclDebug) ;
        if ( debug )
            verbose = true ;

        if ( contains(argDeclTime) )
        {
            String v = getArg(argDeclTime).getValue() ;
            
            if ( v.equalsIgnoreCase("off") || v.equalsIgnoreCase("no") ||v.equalsIgnoreCase("false") )
                timeCommand = false ;
            else if ( v.equalsIgnoreCase("on") || v.equalsIgnoreCase("yes") ||v.equalsIgnoreCase("true") )
                timeCommand = true ;
            else
                cmdError("Unrecognized value for --time: "+v, true) ;
        }
    }


    protected boolean inTimer = false ;
    protected long timeStart  = 0 ;
    protected long timeFinish = -1 ;
    
    
    protected void startTimer()
    { 
        if ( inTimer )
            cmdError("Already in timer", false) ;
        
        timeStart = System.currentTimeMillis() ;
        timeFinish = -1 ;
        inTimer = true ;
    }
    
    protected long endTimer()
    { 
        if ( ! inTimer )
            cmdError("Not in timer", false) ;
        timeFinish = System.currentTimeMillis() ;
        inTimer = false ;
        return getTimeInterval() ;
    }
    
    protected long readTimer() 
    {
        if ( ! inTimer )
        {
            cmdError("Not in timer", false) ;
            return -1 ;
        }
        return System.currentTimeMillis()-timeStart  ;
    }
    
    protected long getTimeInterval()
    {
        if ( inTimer )
            cmdError("Still timing", false) ;
        if ( timeFinish == -1 )
        {
            cmdError("No valid interval", false) ;
            return -1 ;
        }
        
        return  timeFinish-timeStart ;
    }
    
    protected String timeStr(long timeInterval)
    {
        return format("%.3f", new Double(timeInterval/1000.0)) ;
    }

    protected String timeStr(long timePoint, long startTimePoint)
    {
      return timeStr(timePoint-startTimePoint) ;
    }

    
    protected abstract void checkCommandLine() ;
    
    protected void cmdError(String msg, boolean exit)
    {
        System.err.println(msg) ;
        if ( exit )
            throw new TerminateException(5) ;
    }

    protected void addGeneralUsage(List<String> u)
    {
        u.add("--help") ;
        u.add("--time") ;
        u.add("-v   --verbose") ;
        u.add("--debug") ;
        u.add("-q   --quiet") ;
    }
    
    protected abstract List<String> getUsage() ;
    
    protected void usage()
    {
        for ( String s : getUsage() )
        {
            System.err.println(s) ;
        }
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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