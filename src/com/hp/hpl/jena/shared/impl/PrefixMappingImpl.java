/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: PrefixMappingImpl.java,v 1.3 2003-04-29 13:41:48 chris-dollin Exp $
*/

package com.hp.hpl.jena.shared.impl;

import com.hp.hpl.jena.shared.PrefixMapping;

import java.util.*;
import org.apache.xerces.util.XMLChar;

/**
 	@author kers
*/
public class PrefixMappingImpl implements PrefixMapping
    {

    private Map map;
    
    public PrefixMappingImpl()
        { map = new HashMap(); }
           
    public void setNsPrefix( String prefix, String uri ) 
        {
        checkLegal( prefix );
        map.put( prefix, uri ); 
        }
        
    /**
        Checks that a prefix is "legal". This code is probably wrong.
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