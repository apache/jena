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

package com.hp.hpl.jena.sdb.store;

/** Control of the main tables (triples, nodes) in a Store.
 *  This class does not manage secondary tables like PTables 
 */

public interface StoreFormatter
{
    /** Create the main tables and all indexes */
    public void create() ;
    
    /** Add indexes - these are kept upto date for any future additions or deletions of data */
    void addIndexes() ;

    /** Drop indexes for triple/node tables */
    void dropIndexes() ;
    
    /** Format the store - create tables but not secondary indexes */ 
    void format() ;
    
    /** Truncate tables - clearing the store but leaving all indexes inplace */
    void truncate() ;
}
