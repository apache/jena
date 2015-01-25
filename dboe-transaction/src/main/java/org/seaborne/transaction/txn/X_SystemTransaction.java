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

package org.seaborne.transaction.txn;

import java.nio.ByteBuffer ;

// SysTrans is now/currently a purely internal 
public interface X_SystemTransaction
{
    /**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */ 
    public void begin() ;
    
    /** Prepare for a commit.
     * Returns some bytes that will be written to the journal.
     */
    public ByteBuffer prepare() ;

    /** Commit a transaction (make durable) */  
    public void commit() ;
    
    /** Abort a transaction (undo the effect of a transaction) */   
    public void abort() ;
    
    /** Finalization - the coordinator will not mention the transaction again
     *  although recovery after a crash may do.
     */
    public void complete() ;
}
