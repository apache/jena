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

package org.apache.jena.tdb2.loader.main;

import java.util.Collections;
import java.util.List;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.tdb2.store.NodeId;

public class LoaderConst {

    /** Chunk size for the triple->tuples output pipe */  
    public final static int ChunkSize = 100_000 ;

    /** Queue size for chunks of tuples Tuples */
    public final static int QueueSizeTuples = 10;

    //public final static int pipeSize = 10;
    
    /* package */ static final List<Tuple<NodeId>> END_TUPLES      = Collections.emptyList();

    /*package*/ static final int QueueSizeData = 10;

}
