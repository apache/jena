/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: PrefixMapping.java,v 1.7 2003-06-19 12:58:47 chris-dollin Exp $
*/

package com.hp.hpl.jena.shared;

import java.util.*;

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
        Specify the prefix name for a URI prefix string. Any existing use of that prefix
        name is overwritten. Any existing prefix for the same URI is removed.
        (We still have to decide what happens if there are *overlapping* URIs.)
  <p>      
        The empty string is allowed as a prefix, for internal historical reasons.
        Do not rely on this continuing.
        
        @param prefix the string to be used for the prefix.
        @param uri the URI prefix to be named
        @exception IllegalPrefixException if the prefix is not an XML NCName
    */
    PrefixMapping setNsPrefix( String prefix, String uri );
    
    /**
        Copies the prefixes from other into this. Any existing binding of the
        same prefix is lost. 
        
        @param other the PrefixMapping to add
    */
    PrefixMapping setNsPrefixes( PrefixMapping other );
    
    /**
        Copies the prefix mapping from other into this. Illegal prefix mappings
        are detected. Existing binds of the same prefix are lost.
        
        @param map the Map whose maplets are to be added
    */
    PrefixMapping setNsPrefixes( Map map );
       
    /**
        Get the URI bound to a specific prefix, null if there isn't one.
        
        @param prefix the prefix name to be looked up
        @return the most recent URI bound to that prefix name, null if none
    */
    String getNsPrefixURI( String prefix );
    
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
        available, which one is chosen is unspecified at the time of writing.
        
        @param uri the URI string to try and prefix-compress
        @return the QName form if possible, otherwise the unchanged argument
    */
    String usePrefix( String uri );
    
    /**
        Exception to throw when the prefix argument to setNsPrefix is
        illegal for some reason.
    */
    public static class IllegalPrefixException extends JenaException
        {
        public IllegalPrefixException( String prefixName ) { super( prefixName ); }     
        }
    }


/*
    (c) Copyright Hewlett-Packard Company 2003
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