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

package org.apache.jena.query.spatial ;

import java.util.List;

import org.apache.jena.atlas.lib.Closeable;
import org.apache.lucene.spatial.query.SpatialOperation;

import com.hp.hpl.jena.graph.Node;
import com.spatial4j.core.shape.Shape;

/** SpatialIndex abstraction */ 
public interface SpatialIndex extends Closeable //, Transactional 
{
    // Update operations
    public abstract void startIndexing() ;
    public abstract void add(String EntityURI, Shape... shapes) ;
    public abstract void finishIndexing() ;
    public abstract void abortIndexing() ;
    
    /** Access the index - limit if -1 for as many as possible */    
    public abstract List<Node> query(Shape shape, int limit, SpatialOperation operation);

    public abstract EntityDefinition getDocDef() ;
}
