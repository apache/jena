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

package org.apache.jena.sparql.core.mem;

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.Transactional ;

/** Interface that encapsulates the transaction lifecycle for a component in a transaction.
 *  This is the system interface. {@link Transactional} is the application view of a set of 
 *  collection of components that together provide transactions.
 */

public interface TransactionalComponent 
{
    // This interface may evolve in the future.
    // Having it isolates such changes from the applications usage of Transactional.
    // One example is the introduction of an explicit Transaction object.
    
    /** Start either a READ or WRITE transaction */ 
    public void begin(ReadWrite readWrite) ;
    
    /** Commit a transaction - finish the transaction and make any changes permanent (if a "write" transaction) */  
    public void commit() ;
    
    /** Abort a transaction - finish the transaction and undo any changes (if a "write" transaction) */  
    public void abort() ;
    
    /** Finish the transaction - if a write transaction and commit() has not been called, then abort */  
    public void end() ;

}
