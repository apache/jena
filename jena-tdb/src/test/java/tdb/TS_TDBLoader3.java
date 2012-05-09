package tdb;

import org.apache.jena.tdb.store.bulkloader3.TestMultiThreadedSortedDataBag;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    
    TestMultiThreadedSortedDataBag.class, 
    TestTDBLoader3.class
    
})

public class TS_TDBLoader3 {
    public TS_TDBLoader3() {}
}