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

package org.apache.jena.sparql.transaction;

import static org.apache.jena.query.ReadWrite.WRITE ;

import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sys.ThreadAction ;
import org.apache.jena.sys.ThreadTxn ;
import org.junit.Assert ;
import org.junit.Test ;

/** Isolation tests */
public abstract class AbstractTestTransactionIsolation {
    
    protected abstract DatasetGraph create() ; 
    static Quad q1 = SSE.parseQuad("(_ :s :p 111)") ;
    
    @Test
    public void isolation_01() {
        // Start a read transaction on another thread.
        // The transaction has begin() by the time threadTxnRead
        // returns but the action of the ThreadTxn is not triggered
        // until other.run() is called.
        DatasetGraph dsg = create() ;
        ThreadAction other = ThreadTxn.threadTxnRead(dsg, ()-> Assert.assertTrue(dsg.isEmpty()) ) ;
        dsg.begin(WRITE) ;
        dsg.add(q1) ;
        dsg.commit() ;
        dsg.end() ;
        other.run() ;
    }
}
