/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: PrefixMappingImpl.java,v 1.8 2003-06-19 12:58:47 chris-dollin Exp $
*/

package com.hp.hpl.jena.shared.impl;

import com.hp.hpl.jena.shared.PrefixMapping;

import java.util.*;
import org.apache.xerces.util.XMLChar;

/**
    An implementation of PrefixMapping. The mappings are stored in a hash
    map, with the reverse lookup done with a linear search. This may need
    improving but could get complicated. The test for a legal prefix is left to
    xerces's XMLChar.isValidNCName() predicate.
        
 	@author kers
*/
public class PrefixMappingImpl implements PrefixMapping
    {
    private Map map;
    
    public PrefixMappingImpl()
        { 
        map = new HashMap(); 
        }
           
    public PrefixMapping setNsPrefix( String prefix, String uri ) 
        {
        checkLegal( prefix );
        if (!prefix.equals( "" )) removeExisting( uri );
        checkProper( uri );
        map.put( prefix, uri );
        return this;
        }
        
    private void checkProper( String uri )
        {
        // if (!isNiceURI( uri )) throw new RuntimeException( "horrible " + uri );
        }
        
    public static boolean isNiceURI( String uri )
        {
        if (uri.equals( "" )) return false;
        char last = uri.charAt( uri.length() - 1 );
        return last == '/' || last == '#';
        }
 
    private void removeExisting( String uri )
        {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext())
            {
            Map.Entry e = (Map.Entry) it.next();
            if (e.getValue().equals( uri )) 
                {
                map.remove( e.getKey() );
                return;
                }
            }
        }
        
    /**
        Add the bindings of other to our own. We defer to the general case 
        because we have to ensure the URIs are checked.
        
        @param other the PrefixMapping whose bindings we are to add to this.
    */
    public PrefixMapping setNsPrefixes( PrefixMapping other )
        { return setNsPrefixes( other.getNsPrefixMap() ); }
        
    /**
        Add the bindings in the map to our own. This will fail with a ClassCastException
        if any key or value is not a String; we make no guarantees about order or
        completeness if this happens. It will fail with an IllegalPrefixException if
        any prefix is illegal; similar provisos apply.
        
         @param other the Map whose bindings we are to add to this.
    */
    public PrefixMapping setNsPrefixes( Map other )
        {
        Iterator it = other.entrySet().iterator();
        while (it.hasNext())
            {
            Map.Entry e = (Map.Entry) it.next();
            setNsPrefix( (String) e.getKey(), (String) e.getValue() );
            }
        return this;
        }
         
    /**
        Checks that a prefix is "legal" - it must be a valid XML NCName.
    */
    private void checkLegal( String prefix )
        {
        if (prefix.length() > 0 && !XMLChar.isValidNCName( prefix ))
            throw new PrefixMapping.IllegalPrefixException( prefix ); 
        }
        
    public String getNsPrefixURI( String prefix ) 
        { return (String) map.get( prefix ); }
        
    public Map getNsPrefixMap()
        { return new HashMap( map ); }
        
    /**
        Expand a prefixed URI. There's an assumption that any URI of the form
        Head:Tail is subject to mapping if Head is in the prefix mapping. So, if
        someone takes it into their heads to define eg "http" or "ftp" we have problems.
    */
    public String expandPrefix( String prefixed )
        {
        int colon = prefixed.indexOf( ':' );
        if (colon < 0) 
            return prefixed;
        else
            {
            String prefix = prefixed.substring( 0, colon );
            String uri = (String) map.get( prefix );
            return uri == null ? prefixed : uri + prefixed.substring( colon + 1 );
            } 
        }
        
    /**
        Compress the URI using the prefix mapping. This version of the code looks
        through all the maplets and checks each candidate prefix URI for being a
        leading substring of the argument URI. There's probably a much more
        efficient algorithm available, preprocessing the prefix strings into some
        kind of search table, but for the moment we don't need it.
    */
    public String usePrefix( String uri )
        {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext())
            {
            Map.Entry e = (Map.Entry) it.next();
            String ss = (String) e.getValue();
            int where = uri.indexOf( ss );
            if (where == 0) return e.getKey() + ":" + uri.substring( ss.length() );
            } 
        return uri; 
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