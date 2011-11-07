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

package tdb.cmdline;

import java.io.File;

import com.hp.hpl.jena.tdb.base.file.Location;

import arq.cmd.CmdException;
import arq.cmdline.CmdArgModule;
import arq.cmdline.CmdGeneral;
import arq.cmdline.ModAssembler;

/**  Extends ModAssembler to include --tdb.
 *   Defaulting to "tdb.ttl" is done in ModTDBDataset because it interacts
 *   with --location
 */  
public class ModTDBAssembler extends ModAssembler
{
    private ModLocation modLocation     =  new ModLocation() ;

    public static final String defaultAssemblerFile = "tdb.ttl" ;
    protected boolean useDefaultAssemblerFile = false ;
    
    public ModTDBAssembler()
    { 
        super() ;
        ModAssembler.assemblerDescDecl.addName("tdb") ;
    }
    
    @Override
    public void processArgs(CmdArgModule cmdLine)
    {
        int count = 0 ;

        modLocation.processArgs(cmdLine) ;
        super.processArgs(cmdLine) ;
        if ( super.getAssemblerFile() != null ) count++ ;
        if ( modLocation.getLocation() != null ) count++ ;    
        
        if ( count == 0 )
        {
            useDefaultAssemblerFile = true ;
            // throw new CmdException("No assembler file and no location") ;
        }
            
        if ( count > 1 )
            throw new CmdException("Only one of an assembler file and a location") ;
    }
   
    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        super.registerWith(cmdLine) ;
        cmdLine.addModule(modLocation) ;
        //cmdLine.getUsage().startCategory("Dataset") ;
        cmdLine.getUsage().addUsage("--tdb=", "Assembler description file") ;
    }
 
    public Location getLocation() { return modLocation.getLocation() ; }
    
    @Override
    public String getAssemblerFile()
    {
        if ( useDefaultAssemblerFile )
        {
            File f = new File(defaultAssemblerFile) ;
            if ( f.exists() )
                return defaultAssemblerFile ; 
        }
        return super.getAssemblerFile() ;
    }
}
