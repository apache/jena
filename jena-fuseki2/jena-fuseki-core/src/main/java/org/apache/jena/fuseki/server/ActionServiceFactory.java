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

package org.apache.jena.fuseki.server;

import org.apache.jena.fuseki.servlets.ActionProcessor;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.rdf.model.Resource;

/**
 * Factory for creating the {@link ActionProcessor} for an endpoint. Each endpoint -
 * a service name for a dataset has an associated {@link ActionProcessor} to handle
 * the request. This created when the configuration file is read or the server built
 * programmatically.
 * 
 * {@link ActionService} is a common super class of request handlers, includes counters
 * for operations and has a validate-execute lifecycle.   
 * 
 * @see ActionService
 */
@FunctionalInterface 
public interface ActionServiceFactory {
    /**
     * Create an {@linkService} (which can be shared with endpoints), given the description 
     * which is a link into the server configuration graph.
     */
    public ActionService newActionService(Operation operation, Resource endpoint);  
}
