package org.apache.jena.sparql.core.pergraph;

import static org.apache.jena.query.DatasetFactory.wrap;

import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.core.DatasetGraphPerGraphLocking;
import org.apache.jena.sparql.transaction.AbstractTestTransactionLifecycle;
import org.junit.Ignore;
import org.junit.Test;

public class TransactionLifecycleTest extends AbstractTestTransactionLifecycle {

    @Override
    protected Dataset create() {
        return wrap(new DatasetGraphPerGraphLocking());
    }

    @Test
    @Override
    @Ignore("Block this test in the superclass because we can have multiple writers.")
    public synchronized void transaction_concurrency_writer() {}
}
