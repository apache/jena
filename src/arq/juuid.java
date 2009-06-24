/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */



/**
 * @author   Andy Seaborne
 * @version  $Id: juuid.java,v 1.6 2009/01/16 17:23:57 andy_seaborne Exp $
 */

package arq;

import com.hp.hpl.jena.shared.uuid.*;

import arq.cmdline.ArgDecl;
import arq.cmdline.CmdArgModule;
import arq.cmdline.CmdGeneral;
import arq.cmdline.ModBase;

public class juuid extends CmdGeneral
{
    ModJUUID modJUUID = new ModJUUID() ;
    int number = 1 ;
    boolean resetEachTime = false ;
    int uuidType = 0 ;
    boolean asURN = false ;
    boolean asURI = false ;
    boolean asPlain = false ;

    public static void main (String [] argv)
    {
        new juuid(argv).mainAndExit() ;
    }
    
    private juuid(String argv[])
    {
        super(argv) ;
        super.addModule(modJUUID) ;
    }

    @Override
    protected String getSummary()
    {
        return getCommandName()+" [--num=N] [--reset] [--type={1|4}]" ;
    }

    @Override
    protected void exec()
    {
        if ( uuidType == UUID_V1.version )
            JenaUUID.setFactory(new UUID_V1_Gen()) ;
        if ( uuidType == UUID_V4.version )
            JenaUUID.setFactory(new UUID_V4_Gen()) ;

        for ( int i = 0 ; i < number ; i++ )
        {
            if ( resetEachTime && i != 0)
                JenaUUID.reset() ;
            JenaUUID uuid = JenaUUID.generate() ;
            String str = null ;
            if ( asURN )
                str = uuid.asURN() ; 
            else if ( asURI )
                str = uuid.asURI() ; 
            else if ( asPlain )
                str = uuid.asString() ; 
            if ( str == null )
                str = uuid.asString() ;
            System.out.println(str) ;
        }
    }

    @Override
    protected String getCommandName()
    {
        return "uuid" ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        
    }
    
    static ArgDecl argDeclNum      = new ArgDecl(ArgDecl.HasValue,  "num", "n") ;
    static ArgDecl argDeclReset    = new ArgDecl(ArgDecl.NoValue,   "reset") ;
    static ArgDecl argDeclGen      = new ArgDecl(ArgDecl.HasValue,  "gen", "scheme", "type", "ver") ;
    static ArgDecl argDeclURN      = new ArgDecl(ArgDecl.NoValue,   "urn") ;
    static ArgDecl argDeclURI      = new ArgDecl(ArgDecl.NoValue,   "uri") ;
    static ArgDecl argDeclPlain    = new ArgDecl(ArgDecl.NoValue,   "plain") ;

    class ModJUUID extends ModBase
    {
        public void registerWith(CmdGeneral cmdLine)
        {
            cmdLine.add(argDeclNum) ;
            cmdLine.add(argDeclReset) ;
            cmdLine.add(argDeclGen) ;
            cmdLine.add(argDeclURN) ;
            cmdLine.add(argDeclURI) ;
            cmdLine.add(argDeclPlain) ;
        }
        
        public void processArgs(CmdArgModule cmdLine)
        {
            String numStr = null ;
            
            if ( getNumPositional() > 1)
                cmdError("Too many positional arguments") ;
            
            if ( cmdLine.contains(argDeclNum) )
            {
                if ( getNumPositional() != 0 )
                    cmdError("--num and positional arguments don't go together") ;
                numStr = getValue(argDeclNum) ;
            }
            
            if ( numStr == null && cmdLine.getNumPositional() == 1 )
                numStr = cmdLine.getPositionalArg(0) ;
            
            if ( numStr != null )
            {
                try
                {
                    number = Integer.parseInt(numStr) ;
                    if ( number < 0 || number > 10000 )
                        cmdLine.cmdError("Number out of range:" + numStr);
                }
                catch (NumberFormatException e)
                {
                    cmdLine.cmdError("Bad argument: " + numStr);
                }
            }

            resetEachTime = cmdLine.contains(argDeclReset) ;
            
            if ( contains(argDeclGen) ) 
            {
                String s = getValue(argDeclGen) ;
                if ( s.equalsIgnoreCase("time") || s.equalsIgnoreCase("1"))
                    uuidType = UUID_V1.version ;
                else if ( s.equalsIgnoreCase("random") || s.equalsIgnoreCase("rand") || s.equalsIgnoreCase("4"))
                    uuidType = UUID_V4.version ;
                else
                    cmdError("Unrecognized UUID scheme: "+s) ;
            }
            
            if ( contains(argDeclURN) || contains(argDeclURI) || contains(argDeclPlain) )
            {
                asURN = contains(argDeclURN) ;
                asURI = contains(argDeclURI) ;
                asPlain = contains(argDeclPlain) ;
            }
            else
            {
                // Defaults
                asURN = true ;
                asURI = false ;
                asPlain = false ;
            }
        }
    }
}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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