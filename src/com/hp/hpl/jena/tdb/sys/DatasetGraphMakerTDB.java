package com.hp.hpl.jena.tdb.sys;

import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;

/** Interface to maker of the actual implementations of TDB graphs and datasets */ 
public interface DatasetGraphMakerTDB 
{
    /** Create an in-memory dataset */
    public DatasetGraphTDB createDatasetGraph() ;
    /** Create a TDB-backed dataset at a given location */
    public DatasetGraphTDB createDatasetGraph(Location location) ;
    
    /** Release a TDB-backed dataset which is already closed */
    public void releaseDatasetGraph(DatasetGraphTDB dataset) ;
}