/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import com.hp.hpl.jena.sparql.util.Timer;


public class ModTime implements ArgModuleGeneral
{
    public ModTime() {}
    
    protected final ArgDecl timeDecl = new ArgDecl(ArgDecl.NoValue, "time") ;
    
    protected Timer timer = new Timer() ;
    
    private boolean timing = false ;
    
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("Time") ;
        cmdLine.add(timeDecl, "--time", "Time the operation") ;
    }
    
    public void checkCommandLine(CmdArgModule cmdLine)
    {}

    public void processArgs(CmdArgModule cmdLine)
    {
        timing = cmdLine.contains(timeDecl) ;
    }
    
    public boolean timingEnabled() { return timing ; }
    
    public void setTimingEnabled(boolean timingEnabled) { timing = timingEnabled ; }
    
    public void startTimer()
    { timer.startTimer() ; } 
    
    public long endTimer()
    { return timer.endTimer() ; } 
    
    public long readTimer() 
    { return timer.readTimer() ; }
    
    public long getTimeInterval()
    { return timer.getTimeInterval() ; }
    
    public String timeStr(long timeInterval)
    { return Timer.timeStr(timeInterval) ; }
    
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