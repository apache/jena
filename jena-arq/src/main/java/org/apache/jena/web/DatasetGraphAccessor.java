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

package org.apache.jena.web;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;

public interface DatasetGraphAccessor
{
    public Graph httpGet() ; 
    public Graph httpGet(Node graphName) ;
    
    public boolean httpHead() ; 
    public boolean httpHead(Node graphName) ;
    
    // Replace/create graph
    public void httpPut(Graph data) ;
    public void httpPut(Node graphName, Graph data) ;

    // Remove graph
    public void httpDelete() ;
    public void httpDelete(Node graphName) ;

    // Update graph
    public void httpPost(Graph data) ;
    public void httpPost(Node graphName, Graph data) ;

    // Update graph
    public void httpPatch(Graph data) ;
    public void httpPatch(Node graphName, Graph data) ;
}
