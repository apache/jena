/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import lib.FileOps;
import tdb.cmdline.CmdTDB;
import arq.cmd.CmdUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

public class tdbclean extends CmdTDB
{
    static public void main(String... argv)
    { 
        CmdUtils.setLog4j() ;
        new tdbclean(argv).main() ;
    }

    protected tdbclean(String[] argv)
    {
        super(argv) ;
    }

    @Override
    protected String getSummary()
    {
        return getCommandName()+" [--desc=assembler|--loc=DIR]" ;
    }

    @Override
    protected void exec()
    {
        if ( modLocation.getLocation() != null )
            clean(modLocation.getLocation().getDirectoryPath()) ;

        // Extract the location from the assember file.
        if ( modAssembler.getAssemblerFile() != null )
        {
            // Find and clear all locations
            Model m = FileManager.get().loadModel(modAssembler.getAssemblerFile()) ;
            Query query = QueryFactory.create("SELECT ?dir { [] tdb:location ?dir FILTER (isURI(?dir) }") ;
            QueryExecution qExec = null ;
            try {
                qExec = QueryExecutionFactory.create(query, m) ;
                for (ResultSet rs = qExec.execSelect() ; rs.hasNext() ; )
                    clean(rs.nextSolution().getResource("dir").getURI()) ;
            } catch ( RuntimeException ex)
            {
                if ( qExec != null )
                    qExec.close() ;
                throw ex ;
            }
        }
    }
    
    private void clean(String dir)
    {
        if ( isVerbose() )
            System.out.println("Clean: "+dir) ;
        FileOps.clearDirectory(dir) ;
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