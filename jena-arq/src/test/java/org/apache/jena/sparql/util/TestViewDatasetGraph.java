package org.apache.jena.sparql.util;

import org.apache.jena.atlas.junit.BaseTest;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphZero;
import org.junit.Test;

public abstract class TestViewDatasetGraph<T extends ViewDatasetGraph> extends BaseTest {

    public abstract T testInstance(DatasetGraph left, DatasetGraph right, Context c);

    private T emptyDsg() {
        return testInstance(new DatasetGraphZero(), new DatasetGraphZero(), Context.emptyContext);
    }

    @Test(expected = NullPointerException.class)
    public void nullDatasetGraphsNotAllowed() {
        testInstance(null, null, Context.emptyContext);
    }

    @Test(expected = NullPointerException.class)
    public void nullContextNotAllowed() {
        testInstance(new DatasetGraphZero(), new DatasetGraphZero(), null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noAddingQuads() {
        emptyDsg().add(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noAddingQuads2() {
        emptyDsg().add(null, null, null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noDeletingQuads() {
        emptyDsg().delete(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noDeletingQuads2() {
        emptyDsg().delete(null, null, null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noDeletingAnyQuads() {
        emptyDsg().deleteAny(null, null, null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noAddingGraphs() {
        emptyDsg().addGraph(null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noDeletingGraphs() {
        emptyDsg().removeGraph(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noClearing() {
        emptyDsg().clear();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noWriting() {
        emptyDsg().begin(ReadWrite.WRITE);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noCommitting() {
        final DatasetGraph dsg = emptyDsg();
        assertFalse(dsg.isInTransaction());
        dsg.begin(ReadWrite.READ);
        assertTrue(dsg.isInTransaction());
        dsg.commit();
    }

    @Test
    public void canUseEndToFinishTransaction() {
        try {
            DatasetGraph dsg = emptyDsg();
            assertFalse(dsg.isInTransaction());
            dsg.begin(ReadWrite.READ);
            assertTrue(dsg.isInTransaction());
            dsg.end();
            assertFalse(dsg.isInTransaction());
        } catch (UnsupportedOperationException e) {
            fail();
        }
    }

    @Test
    public void canUseAbortToFinishTransaction() {
        try {
            DatasetGraph dsg = emptyDsg();
            assertFalse(dsg.isInTransaction());
            dsg.begin(ReadWrite.READ);
            assertTrue(dsg.isInTransaction());
            dsg.abort();
            assertFalse(dsg.isInTransaction());
        } catch (UnsupportedOperationException e) {
            fail();
        }
    }

}
