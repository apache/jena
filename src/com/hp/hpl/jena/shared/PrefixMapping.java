/*
  (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: PrefixMapping.java,v 1.29 2005-03-18 13:55:00 chris-dollin Exp $
*/

package com.hp.hpl.jena.shared;

import java.util.*;
import com.hp.hpl.jena.shared.impl.*;
import com.hp.hpl.jena.vocabulary.*;

/**
    Methods for recording namepsace prefix mappings and applying and
    unapplying them to URIs.
<p>
    Note that a Model *is* a PrefixMapping, so all the PrefixMapping
    operations apply to Models, and a Model can be used to supply
    the PrefixMapping argument to setNsPrefixes.
    
 	@author kers
*/

public interface PrefixMapping
    {
    /**
        Specify the prefix name for a URI prefix string. Any existing use of 
        that prefix name is overwritten. The result is this same prefixMapping. 
        (The earlier restriction that adding second prefix for the same URI
        caused the earlier binding to be deleted has been withdrawn.)
  <p>      
        A prefix name must be a valid NCName, or the empty string. The empty string
        is reserved to mean "the default namespace".
  <p>
        Need not check the RFC2396 validity of the URI. Bad URIs are either silently 
        ignored or behave as if they were good. The earlier restriction that the URI
        should end with a non-NCName character has been removed.
        
        @param prefix the string to be used for the prefix.
        @param uri the URI prefix to be named
        @exception IllegalPrefixException if the prefix is not an XML NCName
        @return this PrefixMapping
    */
    PrefixMapping setNsPrefix( String prefix, String uri );
    
    /**
        Remove any existing maplet with the given prefix name and answer this
        mapping. If the prefix is the empty string, then this removes the default
        namespace. If the prefix is not a legal prefix string, or is not present in
        the mapping, nothing happens.
        
        <p>The reverse URI-to-prefix mapping is updated, but if there are
        multiple prefixes for the removed URI it is unspecified which of them
        will be chosen.
        
     	@param prefix the prefix string to remove
     	@return this PrefixMapping
     */
    
    PrefixMapping removeNsPrefix( String prefix );
    
    /**
        Copies the prefixes from other into this. Any existing binding of the
        same prefix is lost.  The result is this same prefixMapping.

        @param other the PrefixMapping to add
        @return this PrefixMapping
    */
    PrefixMapping setNsPrefixes( PrefixMapping other );
    
    /**
        Copies the prefix mapping from other into this. Illegal prefix mappings
        are detected. Existing binds of the same prefix are lost.  The result is this 
        same prefixMapping.
        
        @param map the Map whose maplets are to be added
        @return this PrefixMapping
    */
    PrefixMapping setNsPrefixes( Map map );
    
    /**
         Update this PrefixMapping with the bindings in <code>map</code>, only
         adding those (p, u) pairs for which neither p nor u appears in this mapping.
         Answer this PrefixMapping.
    */
    PrefixMapping withDefaultMappings( PrefixMapping map );
       
    /**
        Get the URI bound to a specific prefix, null if there isn't one.
        
        @param prefix the prefix name to be looked up
        @return the most recent URI bound to that prefix name, null if none
    */
    String getNsPrefixURI( String prefix );
    
    /**
        Answer the prefix for the given URI, or null if there isn't one.
        If there is more than one, one of them will be picked. If possible,
        it will be the most recently added prefix. (The cases where it's not
        possible is when a binding has been removed.)
        
        @param uri the uri whose prefix is to be found
        @return the prefix mapped to that uri, or null if there isn't one
    */
    String getNsURIPrefix( String uri );
    
    /**
        Return a copy of the internal mapping from names to URI strings. Updating
        this copy will have no effect on the PrefixMap.
        
        @return a copy of the internal String -> String mapping 
    */
    Map getNsPrefixMap();
    
    /**
        Expand the uri using the prefix mappings if possible. If prefixed has the
        form Foo:Bar, and Foo is a prefix bound to FooURI, return FooURI+Bar.
        Otherwise return prefixed unchanged. 
        
        @param prefixed a QName or URI
        @return the expanded string if possible, otherwise the original string
    */
    String expandPrefix( String prefixed );
    
    /**
        Compress the URI using the prefix mappings if possible. If there is a
        prefix mapping Name -> URIStart, and uri is URIStart+Tail, return Name:Tail;
        otherwise return uri unchanged. If there are multiple applicable mappings
        available, the "most recent" is chosen if that is possible, otherwise
        one is picked "at random".
    <p>    
        The result is primarily intended for human convenience: it is <i>not</i> 
        necessarily a legal QName, as Tail need not be a legal NCName; and there's
        no way to tell a shortened name from a URI with an unusual scheme.
        
        @param uri the URI string to try and prefix-compress
        @return the shortened form if possible, otherwise the unchanged argument
    */
    String shortForm( String uri );
    
    /**
        Old name for shortForm.
        @deprecated - use shortForm
    */
    String usePrefix( String uri );
    
    /**
        Answer a qname with the expansion of the given uri, or null if no such qname
        can be constructed using the mapping's prefixes.
    */
    String qnameFor( String uri );
    
    /**
        Lock the PrefixMapping so that changes can no longer be made to it.
        Primarily intended to lock Standard against mutation.
        
         @return this mapping, locked against changes 
    */
    PrefixMapping lock();
    
    /**
        Exception to throw when the prefix argument to setNsPrefix is
        illegal for some reason.
    */
    public static class IllegalPrefixException extends JenaException
        {
        public IllegalPrefixException( String prefixName ) { super( prefixName ); }     
        }
        
    /**
        Exception to throw when trying to update a locked PrefixMapping.
    */
    public static class JenaLockedException extends JenaException
        {
        public JenaLockedException( PrefixMapping pm ) { super( pm.toString() ); }
        }
        
    /**
        Factory class to create an unspecified kind of PrefixMapping.
    */
    public static class Factory
        { public static PrefixMapping create() { return new PrefixMappingImpl(); } }     

    /**
        A PrefixMapping that contains the "standard" prefixes we know about,
        viz rdf, rdfs, dc, rss, vcard, and owl.
    */
    public static final PrefixMapping Standard = PrefixMapping.Factory.create()
        .setNsPrefix( "rdfs", RDFS.getURI() )
        .setNsPrefix( "rdf", RDF.getURI() )
        .setNsPrefix( "dc", DC.getURI() )
        .setNsPrefix( "daml", DAMLVocabulary.NAMESPACE_DAML_2001_03_URI )
        .setNsPrefix( "owl", OWL.getURI() )
        .setNsPrefix( "xsd", "http://www.w3.org/2001/XMLSchema#" )
        .lock()
        ;   
    
    /**
         A PrefixMapping built on Standard with some extras
    */    
    public static final PrefixMapping Extended = PrefixMapping.Factory.create()
        .setNsPrefixes( Standard )
        .setNsPrefix( "rss", RSS.getURI() )
        .setNsPrefix( "vcard", VCARD.getURI() )
        .setNsPrefix( "jms", JMS.getURI() )
        .setNsPrefix( "eg", "http://www.example.org/" )
        .lock()
        ;   
    }

/*
    (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/