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

package com.hp.hpl.jena.tdb;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils ;
import com.hp.hpl.jena.tdb.assembler.VocabTDB ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.TDBMaker ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTransaction ;

/** Public factory for creating objects datasets backed by TDB storage */
public class TDBFactory
{
    private TDBFactory() {} 
    
    /** Read the file and assembler a dataset */ 
    public static Dataset assembleDataset(String assemblerFile)
    {
        return (Dataset)AssemblerUtils.build(assemblerFile, VocabTDB.tDatasetTDB) ;
    }
    
    /** Create or connect to a TDB-backed dataset */ 
    public static Dataset createDataset(String dir)
    { return createDataset(new Location(dir)) ; }

    /** Create or connect to a TDB-backed dataset */ 
    public static Dataset createDataset(Location location)
    { return createDataset(createDatasetGraph(location)) ; }

    /** Create or connect to a TDB dataset backed by an in-memory block manager. For testing.*/ 
    public static Dataset createDataset()
    { return createDataset(createDatasetGraph()) ; }

    /** Create a dataset around a DatasetGraphTDB */ 
    private static Dataset createDataset(DatasetGraph datasetGraph)
    { return DatasetFactory.create(datasetGraph) ; }
    
    // Meaningless unless there is only one in-memory dataset */
    //    /** Create a TDB model for named model for an in-memory */  
    //    public static Graph createNamedGraph(String name)
    //    { return createDataset().getNamedModel(name) ; }

    /** Create or connect to a TDB-backed dataset (graph-level) */
    public static DatasetGraph createDatasetGraph(String directory)
    { return createDatasetGraph(new Location(directory)) ; }

    /** Create or connect to a TDB-backed dataset (graph-level) */
    public static DatasetGraph createDatasetGraph(Location location)
    { return _createDatasetGraph(location) ; }

    /** Create a TDB-backed dataset (graph-level) in memory (for testing) */
    public static DatasetGraph createDatasetGraph()
    {
        return _createDatasetGraph() ;
    }
    
    /** Create a TDB-backed Dataset directly over the storage in memory (not transactional) (testing only) */  
    public static void release(Dataset dataset)
    {
        _release(location(dataset)) ;
    }
    
    /** Create a TDB-backed Dataset directly over the storage in memory (not transactional) (testing only) */  
    public static void release(DatasetGraph dataset)
    {
        _release(location(dataset)) ;
    }
    
    /** Reset internal state, releasing all datasets.
     *  No checking done, do not call while TDB is execution queries or updates.
     *  Mainly for the tests to have a known clean state. 
     */
    public static void reset()
    {
        TDBMaker.reset() ;
    }

    private static DatasetGraph _createDatasetGraph(Location location)
    {
        return TDBMaker.createDatasetGraphTransaction(location) ;
    }
    
    private static DatasetGraph _createDatasetGraph()
    {
        return TDBMaker.createDatasetGraphTransaction() ;
    }
    
    private static void _release(Location location)
    {
        if ( location == null )
            return ;
        TDBMaker.releaseLocation(location) ;
    }

    /** Return the location of a dataset if it is backed by TDB, else null */ 
    public static boolean isBackedByTDB(Dataset dataset)
    {
        DatasetGraph dsg = dataset.asDatasetGraph() ;
        return isBackedByTDB(dsg) ;
    }
    
    /** Return the location of a dataset if it is backed by TDB, else null */ 
    public static boolean isBackedByTDB(DatasetGraph datasetGraph)
    {
        if ( datasetGraph instanceof DatasetGraphTransaction )
            // The swicthing "connection" for TDB 
            return true ;
        if ( datasetGraph instanceof DatasetGraphTDB )
            // A transaction or the base storage.
            return true ;
        return false ;
    }
    
        

    /** Return the location of a dataset if it is backed by TDB, else null */ 
    public static Location location(Dataset dataset)
    {
        DatasetGraph dsg = dataset.asDatasetGraph() ;
        return location(dsg) ;
    }

    /** Return the location of a DatasetGraph if it is backed by TDB, else null */ 
    public static Location location(DatasetGraph datasetGraph)
    {
        if ( datasetGraph instanceof DatasetGraphTDB )
            return ((DatasetGraphTDB)datasetGraph).getLocation() ;
        if ( datasetGraph instanceof DatasetGraphTransaction )
            return ((DatasetGraphTransaction)datasetGraph).getLocation() ;
        return null ;
    }

    /** Read the file and assembler a graph, of type TDB persistent graph
     *  @deprecated Assemble a Dataset and use the default graph.
     */
    @Deprecated 
    public static Graph assembleGraph(String assemblerFile)
    {
        Model m = assembleModel(assemblerFile) ;
        Graph g = m.getGraph() ;
        return g ;
    }

    /** Read the file and assembler a model, of type TDB persistent graph
     *  @deprecated Assemble a Dataset and use the default model.
     */
    @Deprecated 
    public static Model assembleModel(String assemblerFile)
    {
        return (Model)AssemblerUtils.build(assemblerFile, VocabTDB.tGraphTDB) ;
    }

    /** Create a model, at the given location.
     *  It is better to create a dataset and get the default model from that.
     *  This Model is not connected to the TDB transaction system.
     *  @deprecated Create a Dataset and use the default model.
     */
    @Deprecated
    public static Model createModel(Location loc)
    {
        return ModelFactory.createModelForGraph(createGraph(loc)) ;
    }

    /** Create a model, at the given location 
     *  It is better to create a dataset and get the default model from that.
     *  This Model is not connected to the TDB transaction system. 
     *  @deprecated Create a Dataset and get the default model.
     */
    @Deprecated
    public static Model createModel(String dir)
    {
        return ModelFactory.createModelForGraph(createGraph(dir)) ;
    }

    /** Create a TDB model backed by an in-memory block manager. For testing. */
    @Deprecated
    
    public static Model createModel()
    { return ModelFactory.createModelForGraph(createGraph()) ; }

    /** Create a TDB model for named model
     * It is better to create a dataset and get the named model from that.
     * This Model is not connected to the TDB transaction system.
     *  @deprecated Create a Dataset and get the name model.
     */
    @Deprecated
    public static Model createNamedModel(String name, String location)
    { return createDataset(location).getNamedModel(name) ; }

    /** Create a TDB model for named model.
     * It is better to create a dataset and get the named model from that.
     * This Model is not connected to the TDB transaction system.
     *  @deprecated Create a Dataset and get the name model.
     */  
    @Deprecated
    public static Model createNamedModel(String name, Location location)
    { return createDataset(location).getNamedModel(name) ; }

    /** Create a graph, at the given location 
     * @deprecated Create a DatasetGraph and use the default graph.
     */
    @Deprecated
    public static Graph createGraph(Location loc)       { return createDatasetGraph(loc).getDefaultGraph() ; }

    /** Create a graph, at the given location 
     * @deprecated Create a DatasetGraph and use the default graph.
     */
    @Deprecated
    public static Graph createGraph(String dir)
    {
        Location loc = new Location(dir) ;
        return createGraph(loc) ;
    }
    
    /** Create a TDB graph backed by an in-memory block manager. For testing. */  
    @Deprecated
    public static Graph createGraph()   { return createDatasetGraph().getDefaultGraph() ; }

    /** Create a TDB graph for named graph
     * @deprecated Create a DatasetGraph and get the name graph from that.
     */  
    @Deprecated
    public static Graph createNamedGraph(String name, String location)
    { return createDatasetGraph(location).getGraph(NodeFactory.createURI(name)) ; }
    
    /** Create a TDB graph for named graph
     * @deprecated Create a DatasetGraph and get the name graph from that.
     */  
    @Deprecated
    public static Graph createNamedGraph(String name, Location location)
    { return createDatasetGraph(location).getGraph(NodeFactory.createURI(name)) ; }
}
