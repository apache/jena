/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package reports;
import java.io.StringReader ;

import junit.framework.TestCase ;
import org.openjena.atlas.lib.FileOps ;

import com.hp.hpl.jena.query.DataSource ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;


public class Report_Prefixes extends TestCase {
    static final String GRAPH_NAME = "file:pop-wales.ttl";

    static final String TEST_URI = "http://statistics.data.wales.gov.uk/def/dimension/region";

    static final String DATA = 
        "@prefix sw-dim: <http://statistics.data.wales.gov.uk/def/dimension/> .\n" +
        "@prefix qb:      <http://purl.org/linked-data/cube#> .\n" +
        "<http://statistics.data.wales.gov.uk/dataset/pca-population/2011-02-25/2008NAWAC44> a qb:Observation; \n" +
        "<http://statistics.data.wales.gov.uk/def/dimension/region>  <http://data.ordnancesurvey.co.uk/id/7000000000041309> .";

    public void testPrefixPreservation() {

        String DB = "DB" ;
        
        if ( false )
        {
            // This creates the test data. 
            FileOps.clearDirectory(DB) ;

            DatasetGraphTDB tdb = TDBFactory.createDatasetGraph(DB);
            DataSource src = DatasetFactory.create(tdb);
            Model m = src.getNamedModel(GRAPH_NAME);
            m.read(new StringReader(DATA), null, "Turtle");

            assertEquals("sw-dim:region", m.shortForm(TEST_URI));
            TDB.sync(src);
            tdb.close();
        }
        
        DatasetGraphTDB tdb = TDBFactory.createDatasetGraph(DB);
        DataSource src = DatasetFactory.create(tdb);
        Model m = src.getNamedModel(GRAPH_NAME);

        if ( false )
            // Flushes everything into the PrefixMappingImpl cache.
            m.getNsPrefixMap() ;
        
        // Alternative.
//        // With this uncommented the lookup works and the list shows all expected prefixes
//        for (Map.Entry<String, String> binding : m.getNsPrefixMap().entrySet()) {
//            System.out.println(binding.getKey() + " = " + binding.getValue());
//        }

        m.qnameFor(TEST_URI) ;
        m.shortForm(TEST_URI) ;
        
        assertEquals("sw-dim:region", m.shortForm(TEST_URI));
        tdb.close();
    }
}

