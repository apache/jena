/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.util.Utils;

import arq.cmd.CmdException;
import arq.cmd.TerminationException;

/** Adds main()-like methods
 * 
 * Usage:
 *    new YourCommand(args).mainAndExit()
 *  which never returns and routes thing to System.exit.
 *  or call
 *     new YourCommand(args).mainMethod()
 *  which should not call System.exit anywhere
 *    
 * @author Andy Seaborne
 */

public abstract class CmdMain extends CmdLineArgs
{

    public CmdMain(String[] args)
    {
        super(args) ;
    }

    /** Run command - exit on failure */
    public void mainRun()
    { mainRun(false, true) ; }
    
    /** Run command - choose whether to exit on failure */
    public void mainRun(boolean exitOnFailure)
    { mainRun(exitOnFailure, true) ; }
    
    /** Run command - exit on success or failure */
    public void mainAndExit()
    { mainRun(true, true) ; }
     
    /** Run command */
    public int mainRun(boolean exitOnSuccess, boolean exitOnFailure)
    {
        try { mainMethod() ; }
        catch (TerminationException ex) { System.exit(ex.getCode()) ; }
        catch (JenaException ex)
        {
            ex.printStackTrace(System.err) ;
            
            String s = Utils.className(ex) ;
//            System.err.println(s) ;
//            System.err.println(ex.getMessage()) ;
            if ( exitOnFailure ) System.exit(2) ;
            return 2 ;
        }
        catch (IllegalArgumentException ex)
        {
            System.err.println(ex.getMessage()) ;
            if ( exitOnFailure ) System.exit(1) ;
            return 1 ; 
        }
        catch (CmdException ex)
        {
            if ( ex.getMessage() != null && ex.getMessage().length() > 0 )
                System.err.println(ex.getMessage()) ;
            //ex.printStackTrace() ;
            
            if ( ex.getCause() != null )
                ex.getCause().printStackTrace(System.err) ;
            
            if ( exitOnFailure ) System.exit(1) ;
            return 1 ;
        }
        if ( exitOnSuccess ) 
            System.exit(0) ;
        return 0 ;
    }

    protected final void mainMethod()
    {
        process() ;
        exec() ;
    }
    
    protected abstract void exec() ;
    
    protected abstract String getCommandName() ;
    
    public void cmdError(String msg) { cmdError(msg, true) ;}
    
    public void cmdError(String msg, boolean exit)
    {
        System.err.println(msg) ;
        if ( exit )
            throw new TerminationException(5) ;
    }
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