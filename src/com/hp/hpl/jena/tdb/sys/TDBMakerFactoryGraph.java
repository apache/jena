package com.hp.hpl.jena.tdb.sys;

import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;

/** An ImplFactory that creates datasets in the usual way for TDB */
public final class TDBMakerFactoryGraph implements DatasetGraphMakerTDB
{
    // Uses old "FactoryGraphTDB"
    //@Override
    public DatasetGraphTDB createDatasetGraph(Location location)
    { 
        if ( location.isMem() )
            return createDatasetGraph() ;
        return FactoryGraphTDB.createDatasetGraph(location) ;
    }

    //@Override
    public DatasetGraphTDB createDatasetGraph()
    { return FactoryGraphTDB.createDatasetGraphMem() ; }

    public void releaseDatasetGraph(DatasetGraphTDB dataset)
    {}
}