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

package org.apache.jena.fuseki;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class BaseServerTest extends BaseTest
{
    public static final int port             = 3535 ;
    public static final String datasetPath   = "/dataset" ;
    public static final String serviceUpdate = "http://localhost:"+ServerTest.port+datasetPath+"/update" ; 
    public static final String serviceQuery  = "http://localhost:"+ServerTest.port+datasetPath+"/query" ; 
    public static final String serviceREST   = "http://localhost:"+ServerTest.port+datasetPath+"/data" ; // ??????
    
    protected static final String gn1       = "http://graph/1" ;
    protected static final String gn2       = "http://graph/2" ;
    protected static final String gn99      = "http://graph/99" ;
    
    protected static final Node n1          = Node.createURI("http://graph/1") ;
    protected static final Node n2          = Node.createURI("http://graph/2") ;
    protected static final Node n99         = Node.createURI("http://graph/99") ;
    
    protected static final Graph graph1     = SSE.parseGraph("(base <http://example/> (graph (<x> <p> 1)))") ;
    protected static final Graph graph2     = SSE.parseGraph("(base <http://example/> (graph (<x> <p> 2)))") ;
    
    protected static final Model model1     = ModelFactory.createModelForGraph(graph1) ;
    protected static final Model model2     = ModelFactory.createModelForGraph(graph2) ;
}
