/* CVS $Id: LocationMappingVocab.java,v 1.1 2004-08-31 08:24:40 andy_seaborne Exp $ */

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
