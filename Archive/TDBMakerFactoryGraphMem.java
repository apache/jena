package com.hp.hpl.jena.tdb.sys;

import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;

/** ImplFactory for many in-memory datasets. Mainly for testing. */ 
public final class TDBMakerFactoryGraphMem implements DatasetGraphMakerTDB
{
    //@Override
    public DatasetGraphTDB createDatasetGraph(Location location)
    { return createDatasetGraph() ; }

    //@Override
    public DatasetGraphTDB createDatasetGraph()
    { 
        //return FactoryGraphTDB.createDatasetGraphMem() ;
        return SetupTDB.buildDataset(Location.mem()) ;
    }

    //@Override
    public void releaseDatasetGraph(DatasetGraphTDB dataset)
    {}
}