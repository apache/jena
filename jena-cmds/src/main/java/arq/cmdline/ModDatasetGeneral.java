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

import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.atlas.lib.ListUtils;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.sparql.util.DatasetUtils ;
import org.apache.jena.system.Txn;

/** ModDataset: arguments to build a dataset -
 * see also ModDatasetAssembler which extends ModDataset
 * with a description parameter. */

public class ModDatasetGeneral extends ModDataset
{
    // See also ModDatasetAssembler
    protected final ArgDecl graphDecl      = new ArgDecl(ArgDecl.HasValue, "graph") ;
    protected final ArgDecl dataDecl       = new ArgDecl(ArgDecl.HasValue, "data") ;
    protected final ArgDecl namedGraphDecl = new ArgDecl(ArgDecl.HasValue, "named", "namedgraph", "namedGraph", "namedData", "nameddata") ;

    private List<String> dataURLs                = null ;
    private List<String> graphURLs               = null ;
    private List<String> namedGraphURLs          = null ;
    protected ModDatasetGeneral() {}

    @Override
    public void registerWith(CmdGeneral cl) {
        cl.getUsage().startCategory("Dataset") ;
        cl.add(dataDecl,
               "--data=FILE",
               "Data for the dataset - triple or quad formats") ;
        cl.add(graphDecl,
               "--graph=FILE",
               "Graph for default graph of the datset") ;
        cl.add(namedGraphDecl,
               "--namedGraph=FILE",
               "Add a graph into the dataset as a named graph");
    }

    @Override
    public void processArgs(CmdArgModule cmdLine) {
        dataURLs = cmdLine.getValues(dataDecl);
        graphURLs = cmdLine.getValues(graphDecl);
        namedGraphURLs = cmdLine.getValues(namedGraphDecl);
    }

    @Override
    public Dataset createDataset() {
        // If nothing specified for this module, Leave alone and hope the query has FROM/FROM NAMED
        if ( (dataURLs == null || dataURLs.size() == 0) &&
             (graphURLs == null || graphURLs.size() == 0) &&
             (namedGraphURLs == null || namedGraphURLs.size() == 0) )
            return null ;

        Dataset ds = DatasetFactory.createTxnMem() ;
        addGraphs(ds) ;
        dataset = ds ;
        return dataset ;
    }

    static <X> boolean hasEntries(List<X> list) {
        if ( list == null )
            return false ;
        return ! list.isEmpty() ;
    }

    protected void addGraphs(Dataset ds) {
        try {
            if ( hasEntries(dataURLs) ) {
                if ( ds.supportsTransactions() ) {
                    Txn.executeWrite(ds, () -> {
                        for ( String url : dataURLs )
                            RDFDataMgr.read(ds, url);
                    });
                } else {
                    for ( String url : dataURLs )
                        RDFDataMgr.read(ds, url);
                }
            }
            if ( hasEntries(graphURLs) ||  hasEntries(namedGraphURLs) ) {
                // Resolve named graph URLs so the graphname is an absolute IRI.
                List<String> x = ListUtils.toList(namedGraphURLs.stream().map(IRILib::filenameToIRI));
                DatasetUtils.addInGraphs(ds, graphURLs, x, null) ;
            }
        }
        catch (JenaException ex)
        { throw ex ; }
        catch (Exception ex)
        { throw new CmdException("Error creating dataset", ex) ; }
    }

    public List<String> getGraphURLs() {
        return graphURLs;
    }

    public List<String> getNamedGraphURLs() {
        return namedGraphURLs;
    }
}
