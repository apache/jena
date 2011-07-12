/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb.cmdline;

import arq.cmd.CmdException ;
import arq.cmdline.ArgDecl ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;

public abstract class CmdTDBGraph extends CmdTDB
{
    private static final ArgDecl argNamedGraph          = new ArgDecl(ArgDecl.HasValue, "graph") ;
    protected String graphName = null ;
    
    protected CmdTDBGraph(String[] argv)
    {
        super(argv) ;
        super.add(argNamedGraph, "--graph=IRI", "Act on a named graph") ;
    }
    
    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        if ( contains(argNamedGraph) )
            graphName = getValue(argNamedGraph) ; 
    }
    
    protected Model getModel()
    {
        Dataset ds = tdbDatasetAssembler.getDataset() ;
        
        if ( graphName != null )
        {
            Model m = ds.getNamedModel(graphName) ;
            if ( m == null )
                throw new CmdException("No such named graph (is this a TDB dataset?)") ;
            return m ;
        }
        else
            return ds.getDefaultModel() ;
    }
    
    protected GraphTDB getGraph()
    {
        if ( graphName != null )
            return (GraphTDB)tdbDatasetAssembler.getDataset().getNamedModel(graphName).getGraph() ;
        else
            return (GraphTDB)tdbDatasetAssembler.getDataset().getDefaultModel().getGraph() ;
    }
    
    @Override
    protected String getCommandName()
    {
        return Utils.className(this) ;
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