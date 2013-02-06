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


/**
 * An UpdateEngine provides an interface to execute a SPARQL update request.
 * An update engine is use-once; that is, it executes a single SPARQL Update request
 * (there may be multiple operations in one request).
 */
public interface UpdateEngine
{
    /**
     *  Signal start of a request being executed
     */
    public void startRequest();
    
    /**
     * Signal end of a request being executed 
     */
    public void finishRequest();
    
    /**
     * Returns an {@linkplain UpdateSink} that accepts Update operations
     */
    public UpdateSink getUpdateSink();
}
