/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import com.hp.hpl.jena.graph.Graph;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils;

import com.hp.hpl.jena.query.Dataset;

import com.hp.hpl.jena.tdb.assembler.VocabTDB;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.store.FactoryGraphTDB;

/** Public factory for creating objects (graphs, datasest) associated with TDB */
public class TDBFactory
{
    /** Interface to maker of the actual implementations of TDB graphs and datasets */ 
    public interface ImplFactory 
    {
        /** Make a memory implementation of a TDB graph (memory graphs are for testing, not efficiency) */
        public Graph createGraph() ;
        /** Make a TDB graph with persistent data at the location */
        public Graph createGraph(Location loc) ;
        
        public DatasetGraph createDatasetGraph() ;
        public DatasetGraph createDatasetGraph(Location location) ;
    }

    static ImplFactory factory = null ;

    // Standard implementation factory
    public final static ImplFactory stdFactory = new ImplFactory()
    {
        @Override
        public Graph createGraph()
        { return FactoryGraphTDB.createGraphMem() ; }
    
        @Override
        public Graph createGraph(Location loc)      
        { return FactoryGraphTDB.createGraph(loc) ; }

        @Override
        public DatasetGraph createDatasetGraph(Location location)
        { return FactoryGraphTDB.createDatasetGraph(location) ; }

        @Override
        public DatasetGraph createDatasetGraph()
        { return FactoryGraphTDB.createDatasetGraphMem() ; }
    };
    
    static { 
        TDB.init(); 
        setImplFactory(stdFactory) ;
    }

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

    /** Create a graph, at the given location */
    public static Graph createGraph(Location loc)       { return _createGraph(loc) ; }

    /** Create a graph, at the given location */
    public static Graph createGraph(String dir)
    {
        Location loc = new Location(dir) ;
        return createGraph(loc) ;
    }
    
    /** Create a TDB model backed by an in-memory block manager. For testing. */  
    public static Model createModel()
    {
        return ModelFactory.createModelForGraph(createGraph()) ;
    }
    
    /** Create a TDB graph backed by an in-memory block manager. For testing. */  
    public static Graph createGraph()   { return _createGraph() ; }
    
    /** Create or connect to a TDB-backed dataset */ 
    public static Dataset createDataset(String dir)
    { return createDataset(new Location(dir)) ; }

    /** Create or connect to a TDB-backed dataset */ 
    public static Dataset createDataset(Location location)
    { return new DatasetImpl(factory.createDatasetGraph(location)) ; }
    
    /** Create or connect to a TDB dataset backed by an in-memory block manager. For testing.*/ 
    public static Dataset createDataset()
    { return new DatasetImpl(factory.createDatasetGraph()) ; }

    // Point at which actual graphs are made.
    
    private static Graph _createGraph()
    { return factory.createGraph() ; }

    private static Graph _createGraph(Location loc)
    {
        // The code to choose the optimizer is in GraphTDBFactory.chooseOptimizer
        return factory.createGraph(loc) ;
    }

    /** Set the implementation factory.  Not normally needed - only systems that wish
     * to create usually combinations of indexes and ndoe tables need to use this call.
     * A detailed knowledge of how TDB works, and internal assumptions, is needed to
     * create full functional TDB graphs or datasets.  Beware    
     */
    public static void setImplFactory(ImplFactory f) { factory = f ; }
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