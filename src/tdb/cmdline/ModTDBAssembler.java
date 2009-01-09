/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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
        super.assemblerDescDecl.addName("tdb") ;
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

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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