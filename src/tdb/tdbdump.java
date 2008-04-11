/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import tdb.cmdline.ModFormat;
import arq.cmd.CmdException;
import arq.cmd.CmdUtils;
import arq.cmdline.CmdARQ;
import arq.cmdline.ModAssembler;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;

import com.hp.hpl.jena.sparql.util.Utils;

public class tdbdump extends CmdARQ
{
    ModAssembler modAssembler =  new ModAssembler() ;
    ModFormat modFormat =  new ModFormat() ;
    
    static public void main(String... argv)
    { 
        CmdUtils.setLog4j() ;
        new tdbdump(argv).main() ;
    }

    protected tdbdump(String[] argv)
    {
        super(argv) ;
        super.addModule(modAssembler) ;
        super.addModule(modFormat) ;
    }

    @Override
    protected String getSummary()
    {
        return Utils.className(this)+" --desc=DIR [--format=FORMAT]" ;
    }

    @Override
    protected String getCommandName()
    {
        return "tdbdump" ;
    }

    @Override
    protected void exec()
    {
        if ( modAssembler.getAssemblerFile() == null )
            throw new CmdException("No assembler file") ;
        
        Model model = TDBFactory.assembleModel(modAssembler.getAssemblerFile()) ;
        //Graph graph = (PGraphBase)model.getGraph() ;
        String format = modFormat.getFormat("N3-TRIPLES") ;
        model.write(System.out, format) ;
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