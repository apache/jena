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

package org.apache.jena.rdf.model;

/**
    The interface for visiting (ie type-dispatching) an RDF Node.
*/
public interface RDFVisitor
    {
    /**
        Method to call when visiting a blank node r with identifier id.
        @param r the blank RDF node being visited
        @param id the identifier of that node
        @return value to be returned from the visit
    */
    Object visitBlank( Resource r, AnonId id );
    
    /**
        Method to call when visiting a URI node r with the given uri.
        @param r the URI node being visited
        @param uri the URI string of that node
        @return value to be returned from the visit
    */
    Object visitURI( Resource r, String uri );
    
    /**
        Method to call when visiting a literal RDF node l.
        @param l the RDF Literal node
        @return a value to be returned from the visit
    */
    Object visitLiteral( Literal l );
    }
