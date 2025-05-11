package org.apache.jena.sparql.exec.tracker;

import java.util.Iterator;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.Context;

public interface QueryExecWrapper
    extends QueryExec
{
    QueryExec getDelegate();

    @Override
    default Context getContext() {
        return getDelegate().getContext();
    }

    @Override
    default Query getQuery() {
        return getDelegate().getQuery();
    }

    @Override
    default String getQueryString() {
        return getDelegate().getQueryString();
    }

    @Override
    default void close() {
        getDelegate().close();
    }

    @Override
    default boolean isClosed() {
        return getDelegate().isClosed();
    }

    @Override
    default void abort() {
        getDelegate().abort();
    }

    default void beforeExec() {
    }

    default void afterExec() {
    }

    default void onException(Exception e) {
    }

    @Override
    default RowSet select() {
        beforeExec();
        try {
            return getDelegate().select();
        } catch(Exception e) {
            onException(e);
            e.addSuppressed(new RuntimeException("Error encountered."));
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default Graph construct() {
        beforeExec();
        try {
            return getDelegate().construct();
        } catch(Exception e) {
            onException(e);
            e.addSuppressed(new RuntimeException("Error encountered."));
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default Graph construct(Graph graph) {
        beforeExec();
        try {
            return getDelegate().construct(graph);
        } catch(Exception e) {
            onException(e);
            e.addSuppressed(new RuntimeException("Error encountered."));
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default Graph describe() {
        beforeExec();
        try {
            return getDelegate().describe();
        } catch(Exception e) {
            onException(e);
            e.addSuppressed(new RuntimeException("Error encountered."));
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default Graph describe(Graph graph) {
        beforeExec();
        try {
            return getDelegate().describe(graph);
        } catch(Exception e) {
            onException(e);
            e.addSuppressed(new RuntimeException("Error encountered."));
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default boolean ask() {
        beforeExec();
        try {
            return getDelegate().ask();
        } catch(Exception e) {
            onException(e);
            e.addSuppressed(new RuntimeException("Error encountered."));
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default Iterator<Triple> constructTriples() {
        beforeExec();
        try {
            return getDelegate().constructTriples();
        } catch(Exception e) {
            onException(e);
            e.addSuppressed(new RuntimeException("Error encountered."));
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default Iterator<Triple> describeTriples() {
        beforeExec();
        try {
            return getDelegate().describeTriples();
        } catch(Exception e) {
            onException(e);
            e.addSuppressed(new RuntimeException("Error encountered."));
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default Iterator<Quad> constructQuads() {
        beforeExec();
        try {
            return getDelegate().constructQuads();
        } catch(Exception e) {
            onException(e);
            e.addSuppressed(new RuntimeException("Error encountered."));
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default DatasetGraph constructDataset() {
        beforeExec();
        try {
            return getDelegate().constructDataset();
        } catch(Exception e) {
            onException(e);
            e.addSuppressed(new RuntimeException("Error encountered."));
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default DatasetGraph constructDataset(DatasetGraph dataset) {
        beforeExec();
        try {
            return getDelegate().constructDataset(dataset);
        } catch(Exception e) {
            onException(e);
            e.addSuppressed(new RuntimeException("Error encountered."));
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default JsonArray execJson() {
        beforeExec();
        try {
            return getDelegate().execJson();
        } catch(Exception e) {
            onException(e);
            e.addSuppressed(new RuntimeException("Error encountered."));
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default Iterator<JsonObject> execJsonItems() {
        beforeExec();
        try {
            return getDelegate().execJsonItems();
        } catch(Exception e) {
            onException(e);
            e.addSuppressed(new RuntimeException("Error encountered."));
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    default DatasetGraph getDataset() {
        return getDelegate().getDataset();
    }
}
