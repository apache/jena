/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb.cmdline;

import java.util.ArrayList;
import java.util.List;

import org.openjena.atlas.logging.Log ;

import arq.cmd.CmdException;
import arq.cmdline.ArgDecl;
import arq.cmdline.CmdArgModule;
import arq.cmdline.CmdGeneral;
import arq.cmdline.ModDataset;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils;
import com.hp.hpl.jena.sparql.core.assembler.DatasetAssemblerVocab;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.assembler.VocabTDB;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;
import com.hp.hpl.jena.util.FileManager;

public class ModTDBDataset extends ModDataset
{
    // Mixes assembler, location and "tdb"
    // Can make a single model or a dataset
    
    private ArgDecl argMem                  = new ArgDecl(ArgDecl.NoValue, "mem") ;
    private ModTDBAssembler modAssembler    =  new ModTDBAssembler() ;
    private boolean useMemory               = false ;
    
    public ModTDBDataset() {}
    
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.add(argMem) ; //, "mem", "Memory graph") ;
        cmdLine.addModule(modAssembler) ;
    }

    public void processArgs(CmdArgModule cmdLine)
    {
        useMemory = cmdLine.contains(argMem) ;
        modAssembler.processArgs(cmdLine) ;
    }        

    @Override
    public Dataset createDataset()
    {
        if (  modAssembler.getAssemblerFile() != null )
        {
            Dataset thing = null ;
            // Two variants: plain dataset with a TDB graph or a TDB dataset.
            try {
                thing = (Dataset)AssemblerUtils.build( modAssembler.getAssemblerFile(), VocabTDB.tDatasetTDB) ;
                if ( thing != null && ! ( thing.asDatasetGraph() instanceof DatasetGraphTDB ) )
                        Log.warn(this, "Unexpected: Not a TDB dataset for type DatasetTDB");
                
                if ( thing == null )
                    // Should use assembler inheritance but how do we assert the subclass relationship in a program?
                    thing = (Dataset)AssemblerUtils.build( modAssembler.getAssemblerFile(), DatasetAssemblerVocab.tDataset) ;
            }
            catch (ARQException ex)     { throw ex; }
            catch (JenaException ex)    { throw ex ; }
            catch (Exception ex)
            { throw new CmdException("Error creating", ex) ; }
            return thing ;
        }
        
        if ( modAssembler.getLocation() == null )
            throw new CmdException("No assembler file nor location provided") ;
        
        // No assembler - use location to find a database.
        Dataset ds = TDBFactory.createDataset(modAssembler.getLocation()) ;
        return ds ;
    }
    
    public Location getLocation()
    {
        List<String> x = locations() ;
        if ( x.size() == 0 )
            return null ;
        return new Location(x.get(0)) ;
    }
    
    public List<String> locations()
    {
        List<String> locations = new ArrayList<String>() ;  
        
        if ( modAssembler.getLocation() != null )
            locations.add(modAssembler.getLocation().getDirectoryPath()) ;

        // Extract the location from the assember file.
        if ( modAssembler.getAssemblerFile() != null )
        {
            // Find and clear all locations
            Model m = FileManager.get().loadModel(modAssembler.getAssemblerFile()) ;
            Query query = QueryFactory.create("PREFIX tdb:     <http://jena.hpl.hp.com/2008/tdb#> SELECT ?dir { [] tdb:location ?dir FILTER (isURI(?dir)) }") ;
            QueryExecution qExec = null ;
            try {
                qExec = QueryExecutionFactory.create(query, m) ;
                for (ResultSet rs = qExec.execSelect() ; rs.hasNext() ; )
                {
                    String x = rs.nextSolution().getResource("dir").getURI() ;
                    locations.add(x) ;
                }
            } catch ( RuntimeException ex)
            {
                if ( qExec != null )
                    qExec.close() ;
                throw ex ;
            }
        }
        
        return locations ;
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