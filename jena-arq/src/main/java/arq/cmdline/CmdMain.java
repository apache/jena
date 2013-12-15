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

import org.apache.jena.atlas.logging.LogCtl ;
import arq.cmd.CmdException ;
import arq.cmd.TerminationException ;

import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.sparql.util.Utils ;

/** Adds main()-like methods
 * 
 * Usage:
 *    new YourCommand(args).mainAndExit()
 *  which never returns and routes thing to System.exit.
 *  or call
 *     new YourCommand(args).mainMethod()
 *  which should not call System.exit anywhere */

public abstract class CmdMain extends CmdLineArgs
{
    // Do this very early so it happens before anything else
    // gets a chance to create a logger.
    static { LogCtl.setCmdLogging() ; }
    
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
            ex.printStackTrace(System.err) ;
            //System.err.println(ex.getMessage()) ;
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
