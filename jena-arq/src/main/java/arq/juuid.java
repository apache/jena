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

package arq;

import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdArgModule ;
import arq.cmdline.CmdGeneral ;
import arq.cmdline.ModBase ;

import com.hp.hpl.jena.shared.uuid.JenaUUID ;
import com.hp.hpl.jena.shared.uuid.UUID_V1 ;
import com.hp.hpl.jena.shared.uuid.UUID_V1_Gen ;
import com.hp.hpl.jena.shared.uuid.UUID_V4 ;
import com.hp.hpl.jena.shared.uuid.UUID_V4_Gen ;

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
        @Override
        public void registerWith(CmdGeneral cmdLine)
        {
            cmdLine.add(argDeclNum) ;
            cmdLine.add(argDeclReset) ;
            cmdLine.add(argDeclGen) ;
            cmdLine.add(argDeclURN) ;
            cmdLine.add(argDeclURI) ;
            cmdLine.add(argDeclPlain) ;
        }
        
        @Override
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
