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

package com.hp.hpl.jena.vocabulary; 
import com.hp.hpl.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from vocabularies/dublin-core_types.xml
 */
public class DCTypes {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://purl.org/dc/dcmitype/";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>An aggregation of resources.</p> */
    public static final Resource Collection = m_model.createResource( "http://purl.org/dc/dcmitype/Collection" );
    
    /** <p>Data encoded in a defined structure.</p> */
    public static final Resource Dataset = m_model.createResource( "http://purl.org/dc/dcmitype/Dataset" );
    
    /** <p>A non-persistent, time-based occurrence.</p> */
    public static final Resource Event = m_model.createResource( "http://purl.org/dc/dcmitype/Event" );
    
    /** <p>A visual representation other than text.</p> */
    public static final Resource Image = m_model.createResource( "http://purl.org/dc/dcmitype/Image" );
    
    /** <p>A resource requiring interaction from the user to be understood, executed, 
     *  or experienced.</p>
     */
    public static final Resource InteractiveResource = m_model.createResource( "http://purl.org/dc/dcmitype/InteractiveResource" );
    
    /** <p>A series of visual representations imparting an impression of motion when 
     *  shown in succession.</p>
     */
    public static final Resource MovingImage = m_model.createResource( "http://purl.org/dc/dcmitype/MovingImage" );
    
    /** <p>An inanimate, three-dimensional object or substance.</p> */
    public static final Resource PhysicalObject = m_model.createResource( "http://purl.org/dc/dcmitype/PhysicalObject" );
    
    /** <p>A system that provides one or more functions.</p> */
    public static final Resource Service = m_model.createResource( "http://purl.org/dc/dcmitype/Service" );
    
    /** <p>A computer program in source or compiled form.</p> */
    public static final Resource Software = m_model.createResource( "http://purl.org/dc/dcmitype/Software" );
    
    /** <p>A resource primarily intended to be heard.</p> */
    public static final Resource Sound = m_model.createResource( "http://purl.org/dc/dcmitype/Sound" );
    
    /** <p>A static visual representation.</p> */
    public static final Resource StillImage = m_model.createResource( "http://purl.org/dc/dcmitype/StillImage" );
    
    /** <p>A resource consisting primarily of words for reading.</p> */
    public static final Resource Text = m_model.createResource( "http://purl.org/dc/dcmitype/Text" );
    
}
