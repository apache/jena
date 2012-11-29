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

package com.hp.hpl.jena.sparql.modify;

import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.UpdateRequest ;

/** 
 * Interface for factories that accept and process SPARQL update requests.
 * <p/>
 * The streaming methods may be the only methods called when inside of a streaming-capable container, such as Fuseki.  (TODO is this fair to implementers?)
 */
public interface UpdateEngineFactory
{
    /** Answer whether this factory can produce an UpdateEngine for the UpdateRequest and GraphStore */
    public boolean accept(UpdateRequest request, GraphStore graphStore, Context context) ;
    
    /** Create the request - having returned true to accept, should not fail */  
    public UpdateEngine create(UpdateRequest request, GraphStore graphStore, Binding inputBinding, Context context) ;

    /** Answer whether this factory can produce an UpdateEngineStreaming for the specified GraphStore */
    public boolean acceptStreaming(GraphStore graphStore, Context context) ;
    
    /** Create the streaming engine - having returned true to accept, should not fail */
    public UpdateEngineStreaming createStreaming(UsingList usingList, GraphStore graphStore, Binding inputBinding, Context context);
}
