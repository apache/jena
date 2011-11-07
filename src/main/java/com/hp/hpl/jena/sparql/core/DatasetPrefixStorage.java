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

package com.hp.hpl.jena.sparql.core;

import java.util.Map ;
import java.util.Set ;

import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.Sync ;

import com.hp.hpl.jena.shared.PrefixMapping ;

/** Abstract of prefix storage for graphs in an RDF dataset */

public interface DatasetPrefixStorage extends Closeable, Sync
{
    public Set<String> graphNames() ;
    public String readPrefix(String graphName, String prefix) ;
    public String readByURI(String graphName, String uriStr) ;
    public Map<String, String> readPrefixMap(String graphName) ;
    
    public void insertPrefix(String graphName, String prefix, String uri) ;
    
    public void loadPrefixMapping(String graphName, PrefixMapping pmap) ;
    public void removeFromPrefixMap(String graphName, String prefix, String uri) ;
    
    
    /** Return a PrefixMapping for the unamed graph */ 
    public PrefixMapping getPrefixMapping() ;

    /** Return a PrefixMapping for a named graph */ 
    public PrefixMapping getPrefixMapping(String graphName) ;
}
