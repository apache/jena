/**
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

package dev;

public class DevSDB
{
    // Stage generator
    
    // Check named models for union graph work.
    
    // Drop support for SQL server 2000
    // - use nvarchar(max) not ntext
    // - use TOP 

    // Scope and Join bug.
    // Need to clear constant calculation across left and right in a join.
    // Temporary fix applied (TransformSDB.transform(OpJoin) does not combine SQL)
    
    // ListSubjects etc - QueryHandlerSDB
    
	// javadoc is javadoc all!

    // Document assembler for models including named graphs.

    // maven: 
    //   Assembly: Store/
    //   Update in line with ARQ
    // Jars?
    
	// MySQL cursors: jdbc:mysql://127.0.0.1/rdswip?useCursorFetch=true&defaultFetchSize=8192&profileSQL=false&enableQueryTimeouts=false&netTimeoutForStreamingResults=0 

    // Slot compilation / index form. 
    // Slightly better would be keep constant lookups separate from the SqlNode expression until the
    // unit is compiled.  Currently, can end up with multiple lookups of the same thing (but they will be
    // cached in the DB but if not, the query is very expensive anyway and an extra lookup will not
    // add obseravble cost).
}
