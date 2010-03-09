/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb;



import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils;
import com.hp.hpl.jena.tdb.assembler.VocabTDB;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;
import com.hp.hpl.jena.tdb.sys.TDBMaker;

/** Public factory for creating objects (graphs, datasest) associated with TDB */
public class TDBFactory
{
    /** Read the file and assembler a model, of type TDB persistent graph */ 
    public static Model assembleModel(String assemblerFile)
    {
        return (Model)AssemblerUtils.build(assemblerFile, VocabTDB.tGraphTDB) ;
    }
    
    /** Read the file and assembler a graph, of type TDB persistent graph */ 
    public static Graph assembleGraph(String assemblerFile)
    {
        Model m = assembleModel(assemblerFile) ;
        Graph g = m.getGraph() ;
        return g ;
    }

    /** Read the file and assembler a dataset */ 
    public static Dataset assembleDataset(String assemblerFile)
    {
        return (Dataset)AssemblerUtils.build(assemblerFile, VocabTDB.tDatasetTDB) ;
    }
    
    /** Create a model, at the given location */
    public static Model createModel(Location loc)
    {
        return ModelFactory.createModelForGraph(createGraph(loc)) ;
    }

    /** Create a model, at the given location */
    public static Model createModel(String dir)
    {
        return ModelFactory.createModelForGraph(createGraph(dir)) ;
    }

    /** Create a TDB model backed by an in-memory block manager. For testing. */  
    public static Model createModel()
    { return ModelFactory.createModelForGraph(createGraph()) ; }

    
    /** Create a TDB model for named model */  
    public static Model createNamedModel(String name, String location)
    { return createDataset(location).getNamedModel(name) ; }
    
    /** Create a TDB model for named model */  
    public static Model createNamedModel(String name, Location location)
    { return createDataset(location).getNamedModel(name) ; }

    // Meaningless unless there is only one in-memeory dataset */
//    /** Create a TDB model for named model for an in-memory */  
//    public static Model createNamedModel(String name)
//    { return createDataset().getNamedModel(name) ; }
    
    /** Create or connect to a TDB-backed dataset */ 
    public static Dataset createDataset(String dir)
    { return createDataset(new Location(dir)) ; }

    /** Create or connect to a TDB-backed dataset */ 
    public static Dataset createDataset(Location location)
    { return new DatasetImpl(TDBMaker._createDatasetGraph(location)) ; }

    /** Create or connect to a TDB dataset backed by an in-memory block manager. For testing.*/ 
    public static Dataset createDataset()
    { return new DatasetImpl(TDBMaker._createDatasetGraph()) ; }

    /** Create a graph, at the given location */
    public static Graph createGraph(Location loc)       { return TDBMaker._createGraph(loc) ; }

    /** Create a graph, at the given location */
    public static Graph createGraph(String dir)
    {
        Location loc = new Location(dir) ;
        return createGraph(loc) ;
    }
    
    /** Create a TDB graph backed by an in-memory block manager. For testing. */  
    public static Graph createGraph()   { return TDBMaker._createGraph() ; }

    /** Create a TDB graph for named graph */  
    public static Graph createNamedGraph(String name, String location)
    { return createDatasetGraph(location).getGraph(Node.createURI(name)) ; }
    
    /** Create a TDB graph for named graph */  
    public static Graph createNamedGraph(String name, Location location)
    { return createDatasetGraph(location).getGraph(Node.createURI(name)) ; }

    // Meaningless unless there is only one in-memory dataset */
//    /** Create a TDB model for named model for an in-memory */  
//    public static Graph createNamedGraph(String name)
//    { return createDataset().getNamedModel(name) ; }
    
    /** Create or connect to a TDB-backed dataset (graph-level) */
    public static DatasetGraphTDB createDatasetGraph(String directory)
    { return TDBMaker._createDatasetGraph(new Location(directory)) ; }
    
    /** Create or connect to a TDB-backed dataset (graph-level) */
    public static DatasetGraphTDB createDatasetGraph(Location location)
    { return TDBMaker._createDatasetGraph(location) ; }

    /** Create or connect to a TDB-backed dataset (graph-level) */
    public static DatasetGraphTDB createDatasetGraph()
    { return TDBMaker._createDatasetGraph() ; }
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