/*
 * (c) Copyright 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

/* CVS $Id: LocationMappingVocab.java,v 1.2 2004-11-29 18:44:47 andy_seaborne Exp $ */

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

/*
 *  (c) Copyright 2004 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
