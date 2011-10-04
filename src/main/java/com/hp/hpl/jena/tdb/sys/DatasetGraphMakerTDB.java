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

package com.hp.hpl.jena.tdb.sys;

import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;

/** Interface to maker of the actual implementations of TDB graphs and datasets */ 
public interface DatasetGraphMakerTDB 
{
    /** Create an in-memory dataset */
    public DatasetGraphTDB createDatasetGraph() ;
    /** Create a TDB-backed dataset at a given location */
    public DatasetGraphTDB createDatasetGraph(Location location) ;
    
    /** Release a TDB-backed dataset which is already closed */
    public void releaseDatasetGraph(DatasetGraphTDB dataset) ;
    
    /** Release a TDB-backed dataset which is already closed */
    public void releaseLocation(Location location) ;

}
