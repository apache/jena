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

package org.apache.jena.sparql.util;

import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.system.IRIResolver ;
import org.apache.jena.sparql.core.DatasetDescription ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.graph.GraphFactory ;
import org.apache.jena.system.Txn ;

/** Internal Dataset factory + graph equivalents. */
public class DatasetUtils
{
    /** Create a general purpose, in-memory dataset, and load data into the default graph and
     * also some named graphs.
     * @param uri               Default graph
     * @param namedSourceList   Named graphs
     * @return Dataset
     */
    public static Dataset createDataset(String uri, List<String> namedSourceList) {
        return createDataset(uri, namedSourceList, null);
    }

    /** Create a general purpose, in-memory dataset, and load data into the default graph and
     * also some named graphs.
     * @param uri               Default graph
     * @param namedSourceList   Named graphs
     * @param baseURI
     * @return Dataset
     */
    public static Dataset createDataset(String uri, List<String> namedSourceList, String baseURI) {
        List<String> uriList = Arrays.asList(uri);
        return createDataset(uriList, namedSourceList, baseURI);
    }

    /** Create a general purpose, in-memory dataset, and load some data
     * 
     * @param uriList           RDF for the default graph
     * @param namedSourceList   Named graphs.
     * @return Dataset
     */
    public static Dataset createDataset(List<String> uriList, List<String> namedSourceList) {
        return createDataset(uriList, namedSourceList, null);
    }

    /** Create a general purpose, in-memory dataset, and load data.
     * 
     * @param datasetDesc   
     * @return Dataset
     */
    public static Dataset createDataset(DatasetDescription datasetDesc) {
        return createDataset(datasetDesc, null);
    }

    /** Create a general purpose, in-memory dataset, and load data.
     * 
     * @param datasetDesc   
     * @return Dataset
     */
    public static Dataset createDataset(DatasetDescription datasetDesc, String baseURI) {
        return createDataset(datasetDesc.getDefaultGraphURIs(), datasetDesc.getNamedGraphURIs(), baseURI);
    }

    /** Create a general purpose, in-memory dataset, and load data.
     * @param uriList           Default graph
     * @param namedSourceList   Named graphs
     * @param baseURI
     * @return Dataset
     */
    public static Dataset createDataset(List<String> uriList, List<String> namedSourceList, String baseURI) {
        Dataset ds = DatasetFactory.createGeneral();
        return addInGraphs(ds, uriList, namedSourceList, baseURI);
    }

    /** Add graphs into an existing Dataset */
    public static Dataset addInGraphs(Dataset ds, List<String> uriList, List<String> namedSourceList) {
        return addInGraphs(ds, uriList, namedSourceList, null) ;
    }
    
    /** Add graphs into a Dataset
     * 
     * @param ds
     * @param uriList           Default graph
     * @param namedSourceList   Named graphs
     * @param baseURI
     * @return Dataset, as passed in.
     */
    public static Dataset addInGraphs(Dataset ds, List<String> uriList, List<String> namedSourceList, String baseURI) {
        addInGraphs(ds.asDatasetGraph(), uriList, namedSourceList, baseURI) ;
        return ds ;
    }

    // ---- DatasetGraph level.
    
    /** Create a general purpose, in-memory dataset, and load data.
     * 
     * @param datasetDesc   
     * @return Dataset
     */
    public static DatasetGraph createDatasetGraph(DatasetDescription datasetDesc) {
        return createDatasetGraph(datasetDesc.getDefaultGraphURIs(), datasetDesc.getNamedGraphURIs(), null) ;
    }

    /** Create a general purpose, in-memory dataset, and load data.
     * 
     * @param datasetDesc   
     * @param baseURI
     * @return Dataset
     */
    public static DatasetGraph createDatasetGraph(DatasetDescription datasetDesc, String baseURI) {
        return createDatasetGraph(datasetDesc.getDefaultGraphURIs(), datasetDesc.getNamedGraphURIs(), baseURI) ;
    }
        
    public static DatasetGraph createDatasetGraph(String uri, List<String> namedSourceList, String baseURI) {
        List<String> uriList = new ArrayList<>();
        uriList.add(uri);
        return createDatasetGraph(uriList, namedSourceList, baseURI);
    }

    public static DatasetGraph createDatasetGraph(List<String> uriList, List<String> namedSourceList, String baseURI) {
        DatasetGraph dsg = DatasetGraphFactory.createGeneral();
        addInGraphs(dsg, uriList, namedSourceList, baseURI);
        return dsg ;
    }
    
    /** Add graphs into a DatasetGraph
     * 
     * @param dsg
     * @param uriList           Default graph
     * @param namedSourceList   Named graphs
     */
    public static void addInGraphs(DatasetGraph dsg, List<String> uriList, List<String> namedSourceList) {
        addInGraphs(dsg, uriList, namedSourceList, null) ;
    }

    /** Add graphs into a DatasetGraph
     * 
     * @param dsg
     * @param uriList           Default graph
     * @param namedSourceList   Named graphs
     * @param baseURI
     */
    public static void addInGraphs(DatasetGraph dsg, List<String> uriList, List<String> namedSourceList, String baseURI) {
        if ( ! dsg.supportsTransactions() )
            addInGraphsWorker(dsg, uriList, namedSourceList, baseURI) ;
        
        if ( dsg.isInTransaction() )
            addInGraphsWorker(dsg, uriList, namedSourceList, baseURI);

        Txn.executeWrite(dsg, ()->addInGraphsWorker(dsg, uriList, namedSourceList, baseURI)) ;
    }

    private static void addInGraphsWorker(DatasetGraph dsg, List<String> uriList, List<String> namedSourceList, String baseURI) {
        // Merge into background graph
        if ( uriList != null ) {
            Model m = GraphFactory.makeDefaultModel();
            for ( Iterator<String> iter = uriList.iterator() ; iter.hasNext() ; ) {
                String sourceURI = iter.next();
                String absURI = null;
                if ( baseURI != null )
                    absURI = IRIResolver.resolveString(sourceURI, baseURI);
                else
                    absURI = IRIResolver.resolveString(sourceURI);
                // FileManager.readGraph?
                RDFDataMgr.read(m, sourceURI, absURI, null);
            }
            dsg.setDefaultGraph(m.getGraph());
        } else {
            dsg.setDefaultGraph(GraphFactory.createDefaultGraph());
        }

        if ( namedSourceList != null ) {
            for ( Iterator<String> iter = namedSourceList.iterator() ; iter.hasNext() ; ) {
                String sourceURI = iter.next();
                String absURI = null;
                if ( baseURI != null )
                    absURI = IRIResolver.resolveString(baseURI, sourceURI);
                else
                    absURI = IRIResolver.resolveString(sourceURI);
                Model m = GraphFactory.makeDefaultModel();
                RDFDataMgr.read(m, sourceURI, absURI, null);
                Node gn = NodeFactory.createURI(sourceURI);
                dsg.addGraph(gn, m.getGraph());
            }
        }
    }
}
