/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import java.util.List;

import arq.cmd.CmdException;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.LabelExistsException;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.core.DataFormat;
import com.hp.hpl.jena.sparql.util.DatasetUtils;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.LocationMapper;

/** ModDataset: arguments to build a dataset - 
 * see also ModDatasetAssembler which extends ModDataset
 * with a description parameter.
 * 
 * @author Andy Seaborne
 */ 

public class ModDatasetGeneral extends ModDataset
{
    // See also ModDatasetAssembler
    protected final ArgDecl graphDecl      = new ArgDecl(ArgDecl.HasValue, "graph", "data") ;
    protected final ArgDecl namedGraphDecl = new ArgDecl(ArgDecl.HasValue, "named", "namedgraph", "namedGraph", "namedData", "nameddata") ;
    //protected final ArgDecl dataFmtDecl    = new ArgDecl(ArgDecl.HasValue, "fmt", "format") ;
    //protected final ArgDecl dirDecl        = new ArgDecl(ArgDecl.HasValue, "dir") ;
    protected final ArgDecl lmapDecl       = new ArgDecl(ArgDecl.HasValue, "lmap") ;
 
    private List<String> graphURLs               = null ;
    private List<String> namedGraphURLs          = null ;
    private DataFormat dataSyntax        = null ;
    private FileManager fileManager     = FileManager.get() ;    

    public void registerWith(CmdGeneral cl)
    {
        cl.getUsage().startCategory("Dataset") ;
        cl.add(graphDecl,
               "--graph",
               "Graph for default graph of the datset") ;
        cl.add(namedGraphDecl,
               "--namedGraph",
               "Add a graph into the dataset as a named graph") ;
        //cl.add(dirDecl) ;
        //cl.add(dataFmtDecl) ;
        cl.add(lmapDecl,
               "--lmap",
               "Specify a location mapping file") ;
    }
    
    public void processArgs(CmdArgModule cmdLine)
    {
        graphURLs = cmdLine.getValues(graphDecl) ;
        namedGraphURLs = cmdLine.getValues(namedGraphDecl) ;
        
        if ( cmdLine.contains(lmapDecl) )
        {
            String lmapFile = cmdLine.getValue(lmapDecl) ;
            LocationMapper locMap = new LocationMapper(lmapFile) ;
            fileManager = new FileManager(locMap) ;
        }
    }
    
    @Override
    public Dataset createDataset()
    {
        // If nothing specified to the module.  Leave alone and hope the query has FROM/FROM NAMED
        if ( (graphURLs == null || graphURLs.size() == 0) &&
              (namedGraphURLs == null || namedGraphURLs.size() == 0 ) )
            return null ;
        
        DataSource ds = DatasetFactory.create() ;
        addGraphs(ds) ;
        dataset = ds ;
        return dataset ;
    }
        
    protected void addGraphs(DataSource ds)
    {
        try {
            if ( (graphURLs != null) || (namedGraphURLs != null) )
                dataset = 
                    DatasetUtils.addInGraphs(ds, graphURLs, namedGraphURLs, fileManager, null) ;
        } 
        catch (LabelExistsException ex)
        { throw new CmdException(ex.getMessage()) ; }
        catch (JenaException ex)
        { throw ex ; }
        catch (Exception ex)
        { throw new CmdException("Error creating dataset", ex) ; }
    }

    public List<String> getGraphURLs()
    {
        return graphURLs ;
    }

    public List<String> getNamedGraphURLs()
    {
        return namedGraphURLs ;
    }
}


/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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