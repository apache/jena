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

package com.hp.hpl.jena.update;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;

/** A collection of graphs that an update can be applied to.
 *  The collection is one unnamed graph and zero or more named graphs, like
 *  a SPARQL dataset. */
public interface GraphStore extends DatasetGraph
{
    /** Convert to a dataset (for query) */
    public Dataset toDataset() ;

    /** Signal start of a request being executed */ 
    public void startRequest() ;
    /** Signal end of a request being executed */ 
    public void finishRequest() ;
    
//    public void sync() ;
//    public void close() ;
}
