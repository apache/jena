package org.apache.jena.rdfconnection;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkAdapter;
import org.junit.Assert;
import org.junit.Test;

public class TestRDFConnectionRewrapping {

    /** RDFLinkAdapter prior to jena 4.6.0 did not delegate the newUpdate / newQuery methods */
    @Test
    public void test() {
        Dataset ds = DatasetFactory.create();

        // Create an RDFConnection instance that is NOT an RDFLink adapter
        // (otherwise unwrapping will detect the RDFLink and use that instead)
        RDFConnection conn = new RDFConnectionWrapper(RDFConnection.connect(ds));
        RDFLink link = RDFLinkAdapter.adapt(conn);

        link.newUpdate().update("INSERT DATA { <urn:s> <urn:p> <urn:o>}").build().execute();
        Assert.assertTrue(link.newQuery().query("ASK { ?s ?p ?o }").build().ask());
    }
}
