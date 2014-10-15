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

package com.hp.hpl.jena.tdb.index;

import java.util.Iterator;

import com.hp.hpl.jena.tdb.base.record.Record;


public interface RangeIndex extends Index
{
    /** Return records between min (inclusive) and max (exclusive), based on the record keys */
    public Iterator<Record> iterator(Record recordMin, Record recordMax) ;
    
    /** Return the record containing the least key - may or may not have the associated value */
    public Record minKey() ;

    /** Return the record containing the greatest key - may or may not have the associated value */
    public Record maxKey() ;
}
