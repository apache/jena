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

package org.apache.jena.sparql.vocabulary ;

import org.apache.jena.graph.Node ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;

public class ListPFunction {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();

    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://jena.apache.org/ARQ/list#";

    /** <p>The namespace of the vocabulary as a string</p> */
    private static final String NS_J2 = "http://jena.hpl.hp.com/ARQ/list#";

    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}

    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    /** <p>Length relationship of a list [subject] to a length [object]</p> */
    public static final Property length = m_model.createProperty( NS+"length" );

    /** <p>Index relationship of a list [subject] to a query list (index member)</p> */
    public static final Property index = m_model.createProperty( NS+"index" );

    /** <p>Membership relationship of a list [subject] to a member [object] c.f. rdfs:member</p> */
    public static final Property member = m_model.createProperty( NS+"member" );

    /** list:member as a node */
    public static final Node nListMember = member.asNode() ;

    /** list:length as a node */
    public static final Node nListLength = length.asNode() ;

    /** list:index as a node */
    public static final Node nListIndex = member.asNode() ;

}
