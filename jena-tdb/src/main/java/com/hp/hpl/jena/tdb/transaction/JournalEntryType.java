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

package com.hp.hpl.jena.tdb.transaction;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.logging.Log ;

public enum JournalEntryType 
{ 
    // Abort is used 
    Block(1), Buffer(2), Object(3), Commit(4), Abort(5), Checkpoint(6) ;
    
    final int id ;
    JournalEntryType(int x) { id = x ; }
    int getId() { return id ; }
    static public JournalEntryType type(int x)
    {
        if ( x == Block.id )                return Block ;
        else if ( x == Buffer.id )          return Buffer ;
        else if ( x == Object.id )          return Object ;
        else if ( x == Commit.id )          return Commit ;
        else if ( x == Abort.id )           return Abort ;
        else if ( x == Checkpoint.id )      return Checkpoint ;
        else
        {
            Log.fatal(JournalEntryType.class, "Unknown type: "+x) ;
            throw new InternalErrorException("Unknown type: "+x) ;
        }
    }
}
