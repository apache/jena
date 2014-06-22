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

package arq.cmdline;

import java.util.List ;

import org.apache.jena.riot.RDFDataMgr ;
import arq.cmd.CmdException ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.LabelExistsException ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.sparql.core.DataFormat ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.util.DatasetUtils ;

/** ModDataset: arguments to build a dataset - 
 * see also ModDatasetAssembler which extends ModDataset
 * with a description parameter. */ 

public class ModDatasetGeneral extends ModDataset
{
    // See also ModDatasetAssembler
    protected final ArgDecl graphDecl      = new ArgDecl(ArgDecl.HasValue, "graph") ;
    protected final ArgDecl dataDecl      = new ArgDecl(ArgDecl.HasValue, "data") ;
    protected final ArgDecl namedGraphDecl = new ArgDecl(ArgDecl.HasValue, "named", "namedgraph", "namedGraph", "namedData", "nameddata") ;
    //protected final ArgDecl dataFmtDecl    = new ArgDecl(ArgDecl.HasValue, "fmt", "format") ;
    //protected final ArgDecl dirDecl        = new ArgDecl(ArgDecl.HasValue, "dir") ;
    
    private List<String> dataURLs                = null ;
    private List<String> graphURLs               = null ;
    private List<String> namedGraphURLs          = null ;
    private DataFormat dataSyntax        = null ;

    protected ModDatasetGeneral() {}
    
    @Override
    public void registerWith(CmdGeneral cl)
    {
        cl.getUsage().startCategory("Dataset") ;
        cl.add(dataDecl,
               "--data=FILE",
               "Data for the datset - triple or quad formats") ;
        cl.add(graphDecl,
               "--graph=FILE",
               "Graph for default graph of the datset") ;
        cl.add(namedGraphDecl,
               "--namedGraph=FILE",
               "Add a graph into the dataset as a named graph") ;
    }
    
    @Override
    public void processArgs(CmdArgModule cmdLine)
    {
        dataURLs = cmdLine.getValues(dataDecl) ;
        graphURLs = cmdLine.getValues(graphDecl) ;
        namedGraphURLs = cmdLine.getValues(namedGraphDecl) ;
    }
    
    @Override
    public Dataset createDataset()
    {
        // If nothing specified to the module.  Leave alone and hope the query has FROM/FROM NAMED
        if (  ( dataURLs == null || dataURLs.size() == 0) &&
              (graphURLs == null || graphURLs.size() == 0) &&
              (namedGraphURLs == null || namedGraphURLs.size() == 0 ) )
            return null ;
        
        // This can auto-add graphs.
        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        Dataset ds = DatasetFactory.create(dsg) ;
        addGraphs(ds) ;
        dataset = ds ;
        return dataset ;
    }
        
    protected void addGraphs(Dataset ds)
    {
        try {
            if ( dataURLs != null )
            {
                for ( String url : dataURLs )
                    RDFDataMgr.read(ds, url) ;
            }
            
            if ( (graphURLs != null) || (namedGraphURLs != null) )
                ds = DatasetUtils.addInGraphs(ds, graphURLs, namedGraphURLs, null) ;
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
