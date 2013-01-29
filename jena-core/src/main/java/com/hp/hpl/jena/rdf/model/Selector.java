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

package com.hp.hpl.jena.rdf.model;

/** A Statement selector.
 *
 * <p>Model includes list and query methods which will return all the
 * statements which are selected by a selector object.  This is the interface
 * of such selector objects.
 *
*/

public interface Selector {
    /** Determine whether a Statement should be selected.
     * @param s The statement to be considered.
     * @return true if the statement has been selected.
     */
    boolean test( Statement s );
    
    /**
        Answer true iff this Selector is completely characterised by its subject,
        predicate, and object fields. If so, the <code>test</code> predicate need
        not be called to decide if a statement is acceptable. This allows query engines
        lattitude for optimisation (and our memory-based implementation both exploits
        this licence).
    */
    boolean isSimple();
      
    /**
        Answer the only subject Resource that this Selector will match, or null if it
        can match more that a single resource.
    */
    Resource getSubject();
    
    /**
        Answer the only predicate Property that this Selector will match, or null
        if it can match more than a single property.
    */
    Property getPredicate();
    
    /**
        Answer the only RDFNode object that this Selector will match, or null if
        it can match more than a single node. 
    */
    RDFNode getObject();
    
}
