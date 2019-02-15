/**
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

package org.apache.jena.query.text ;

import java.util.List ;
import java.util.Map ;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.graph.Node ;

/** TextIndex abstraction */ 
public interface TextIndex extends Closeable //, Transactional 
{
    // Transactional operations
    void prepareCommit() ;
    void commit() ;
    void rollback() ;
    
    
    // Update operations
    void addEntity(Entity entity) ;
    void updateEntity(Entity entity) ;
    void deleteEntity(Entity entity) ;
    
    
    // read operations
    /** Get all entries for uri */
    Map<String, Node> get(String uri) ;

    /** Access the index - limit if -1 for as many as possible 
     * Throw QueryParseException for syntax errors in the query string.
     */ 
    List<TextHit> query(Node property, String qs, String graphURI, String lang, int limit) ;
    
    List<TextHit> query(Node property, String qs, String graphURI, String lang) ;

    List<TextHit> query(Node property, String qs, String graphURI, String lang, int limit, String highlight) ;

    default List<TextHit> query(String subjectUri, Node property, String qs, String graphURI, String lang, int limit, String highlight){
        return query(property, qs, graphURI, lang, limit, highlight);
    }

    EntityDefinition getDocDef() ;
}
