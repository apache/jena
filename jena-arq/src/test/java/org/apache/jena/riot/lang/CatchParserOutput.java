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

package org.apache.jena.riot.lang;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;

class CatchParserOutput implements StreamRDF
{
    List<Triple>      triples     = new ArrayList<>() ;
    List<Quad>        quads       = new ArrayList<>() ;
    List<Pair<String,String>>     prefixes     = new ArrayList<>() ;
    List<String>     bases       = new ArrayList<>() ;
    
    int startCalled = 0 ;
    
    int finishCalled = 0 ;
    
    @Override public void start()   { startCalled++ ; }
    
    @Override public void triple(Triple triple)     { triples.add(triple) ; }
    
    @Override public void quad(Quad quad)           { quads.add(quad) ; }
    
    @Override public void base(String base)         { bases.add(base) ; }
    
    @Override public void prefix(String prefix, String iri) { prefixes.add(Pair.create(prefix, iri)) ; }
    
    @Override public void finish()  { finishCalled++ ; }
}