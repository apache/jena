/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
