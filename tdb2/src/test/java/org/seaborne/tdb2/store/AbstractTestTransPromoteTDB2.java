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

// **** COPIED from Jena 3.4.0 development.
// After Mantis moves to 3.4.0 or later, this can be removed.

//package org.apache.jena.sparql.transaction ;
package org.seaborne.tdb2.store;

import static org.junit.Assert.fail ;

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.transaction.AbstractTestTransPromote;
import org.apache.log4j.Logger ;
import org.junit.Ignore;
import org.junit.Test ;

/** Tests for transactions that start read and then promote to write */
public abstract class AbstractTestTransPromoteTDB2 extends AbstractTestTransPromote {

    protected AbstractTestTransPromoteTDB2(Logger[] loggers) {
        super(loggers);
    }
    
    // Copy in Jena 3.4.0 development
    // write-end becomes an exception.
    
    @Override
    @Ignore
    @Test public void promote_snapshot_05()         { }
    @Test public void promote_snapshot_05_x()         { run_05_X(false) ; }

    @Override
    @Ignore
    @Test public void promote_readCommitted_05()    { }

    @Test public void promote_readCommitted_05_x()    { run_05_X(true) ; }
    
    private void run_05_X(boolean readCommitted) {
        //Assume.assumeTrue( ! readCommitted || supportsReadCommitted());
        
        setReadCommitted(readCommitted);
        DatasetGraph dsg = create() ;
        dsg.begin(ReadWrite.READ) ;
        dsg.add(q1) ;
        
        try {
            dsg.end() ;
            fail("begin(W);end() did not throw an exception");
        } catch ( JenaTransactionException ex) {}

        assertCount(0, dsg) ;
    }
}
