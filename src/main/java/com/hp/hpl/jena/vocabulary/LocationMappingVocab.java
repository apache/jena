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

/* CVS $Id: LocationMappingVocab.java,v 1.1 2009-06-29 08:55:36 castagna Exp $ */

package com.hp.hpl.jena.vocabulary;
import com.hp.hpl.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from Vocabularies/location-mapping-rdfs.n3 
 */

public class LocationMappingVocab {
    /** <p>The namespace of the vocabalary as a string ({@value})</p> */
    public static final String NS = "http://jena.hpl.hp.com/2004/08/location-mapping#";
    
    /** <p>The namespace of the vocabalary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabalary as a resource</p> */
    public static final Resource NAMESPACE = ResourceFactory.createResource( NS );
    
    /** <p>Range is a STRING, not a URI, to allow for any symbols</p> */
    public static final Property name = ResourceFactory.createProperty( "http://jena.hpl.hp.com/2004/08/location-mapping#name" );
    
    public static final Property altPrefix = ResourceFactory.createProperty( "http://jena.hpl.hp.com/2004/08/location-mapping#altPrefix" );
    
    public static final Property mapping = ResourceFactory.createProperty( "http://jena.hpl.hp.com/2004/08/location-mapping#mapping" );
    
    /** <p>Range is a STRING, not a URI, to allow for any symbols</p> */
    public static final Property prefix = ResourceFactory.createProperty( "http://jena.hpl.hp.com/2004/08/location-mapping#prefix" );
    
    public static final Property altName = ResourceFactory.createProperty( "http://jena.hpl.hp.com/2004/08/location-mapping#altName" );
    
    public static final Resource LocationMapping = ResourceFactory.createResource( "http://jena.hpl.hp.com/2004/08/location-mapping#LocationMapping" );
    
}
