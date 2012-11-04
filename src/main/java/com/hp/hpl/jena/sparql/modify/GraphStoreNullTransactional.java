package com.hp.hpl.jena.sparql.modify ;

import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.core.Transactional ;
import com.hp.hpl.jena.sparql.core.TransactionalNull ;

/**
 * A transactional black hole for Quads, add as many as you want and it will forget them all.  Useful for testing.
 */
public class GraphStoreNullTransactional extends GraphStoreNull implements Transactional
{
    private final Transactional transaction = new TransactionalNull() ;

    @Override
    public void begin(ReadWrite readWrite)
    {
        transaction.begin(readWrite) ;
    }

    @Override
    public void commit()
    {
        transaction.commit() ;
    }

    @Override
    public void abort()
    {
        transaction.abort() ;
    }

    @Override
    public boolean isInTransaction()
    {
        return transaction.isInTransaction() ;
    }

    @Override
    public void end()
    {
        transaction.end() ;
    }

}
