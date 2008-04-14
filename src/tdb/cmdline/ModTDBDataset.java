/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb.cmdline;

import arq.cmd.CmdException;
import arq.cmdline.CmdArgModule;
import arq.cmdline.CmdGeneral;
import arq.cmdline.ModAssembler;
import arq.cmdline.ModDataset;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.pgraph.PGraphBase;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;

public class ModTDBDataset extends ModDataset
{
    ModAssembler modAssembler =  new ModAssembler() ;
    ModLocation modLocation =  new ModLocation() ;

    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.addModule(modAssembler) ;
        cmdLine.addModule(modLocation) ;
    }

    public void processArgs(CmdArgModule cmdLine)
    {
        modAssembler.processArgs(cmdLine) ;
        modLocation.processArgs(cmdLine) ;
    }

    @Override
    public Dataset createDataset()
    {
        if ( modLocation.getLocation() == null && modAssembler.getAssemblerFile() == null )
            throw new CmdException("No assembler file and no location") ;

        if ( modAssembler.getAssemblerFile() != null && modAssembler.getAssemblerFile() != null )
            throw new CmdException("Both an assembler file and a location") ;

        Model model = null ;

        if ( modAssembler.getAssemblerFile() != null )
            model = TDBFactory.assembleModel(modAssembler.getAssemblerFile()) ;
        else
            model = TDBFactory.createModel(modLocation.getLocation()) ;
        PGraphBase graph = (PGraphBase)model.getGraph() ;
        return DatasetFactory.create(model) ;
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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