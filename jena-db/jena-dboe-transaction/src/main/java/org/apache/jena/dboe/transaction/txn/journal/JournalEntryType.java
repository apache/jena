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

package org.apache.jena.dboe.transaction.txn.journal;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.logging.Log ;

/** 
 * Types of Journal entry.
 * This set is quite general and so not all cases may be used in practice.
 * <p>
 * The id must be stable across new versions on the code as it ends up
 * in the journal on-disk so we are explicit about id even though there is
 * {@link Enum#ordinal} 
 */
public enum JournalEntryType 
{ 
    /*
     * REDO, UNDO -- Actions (UNDO unused)
     * COMMIT, ABORT -- Transaction action (ABORT unused)
     * CHECKPOINT -- data written to the journal as a safe spill file (unused)
     */
    REDO(1), UNDO(2), COMMIT(3), ABORT(4) 
    /*, CHECKPOINT(6)*/
    ;

    final int id ;
    JournalEntryType(int x) { id = x ; }
    int getId() { return id ; }
    static public JournalEntryType type(int x)
    {
        if      ( x == REDO.id )            return REDO ;
        else if ( x == UNDO.id )            return UNDO ;
        else if ( x == COMMIT.id )          return COMMIT ;
        else if ( x == ABORT.id )           return ABORT ;
        //else if ( x == CHECKPOINT.id )      return CHECKPOINT ;
        else {
            Log.error(JournalEntryType.class, "Unknown type: "+x) ;
            throw new InternalErrorException("Unknown type: "+x) ;
        }
    }
}
