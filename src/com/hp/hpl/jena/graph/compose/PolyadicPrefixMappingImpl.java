/*
 * (c) Copyright 2004, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: PolyadicPrefixMappingImpl.java,v 1.5 2004-11-19 14:38:11 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.compose;

import java.util.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.util.CollectionFactory;


public class PolyadicPrefixMappingImpl extends PrefixMappingImpl implements PrefixMapping
    {
    private Polyadic poly;
    private PrefixMapping pending = new PrefixMappingImpl();
    
    public PolyadicPrefixMappingImpl( Polyadic p )
        { poly = p; 
        }
           
    private PrefixMapping getBaseMapping()
        { 
        Graph base = poly.getBaseGraph();
        return base == null ? pending : base.getPrefixMapping(); 
        }
    
    public PrefixMapping setNsPrefix( String prefix, String uri ) 
        {
        checkUnlocked();
        getBaseMapping().setNsPrefix( prefix, uri );
        return this;
        }
    
    public PrefixMapping removeNsPrefix( String prefix )
        {
        checkUnlocked();
        getBaseMapping().removeNsPrefix( prefix );
        return this;
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
        checkUnlocked();
        getBaseMapping().setNsPrefixes( other );
        return this;
        }
         
    public String getNsPrefixURI( String prefix ) 
        {
        PrefixMapping bm = getBaseMapping();
        String s = bm.getNsPrefixURI( prefix );
        if (s == null)
            {
            List graphs = poly.getSubGraphs();
            for (int i = 0; i < graphs.size(); i += 1)
                {
                String ss = ((Graph) graphs.get(i)).getPrefixMapping().getNsPrefixURI( prefix );
                if (ss != null) return ss;
                }
            }
        return s;
        }
        
    public Map getNsPrefixMap()
        { 
        Map result = CollectionFactory.createHashedMap();
        List graphs = poly.getSubGraphs();
        for (int i = 0; i < graphs.size(); i += 1)
            result.putAll( ((Graph) graphs.get(i)).getPrefixMapping().getNsPrefixMap() );
        result.putAll( getBaseMapping().getNsPrefixMap() );
        return result; 
        }
        
    public String getNsURIPrefix( String uri )
        {
        String s = getBaseMapping().getNsURIPrefix( uri );
        if (s == null)
            {
            List graphs = poly.getSubGraphs();
            for (int i = 0; i < graphs.size(); i += 1)
                {
                String ss = ((Graph) graphs.get(i)).getPrefixMapping().getNsURIPrefix( uri );
                if (ss != null) return ss;
                }
            }
        return s;
        }
        
    /**
        Expand a prefixed URI. There's an assumption that any URI of the form
        Head:Tail is subject to mapping if Head is in the prefix mapping. So, if
        someone takes it into their heads to define eg "http" or "ftp" we have problems.
    */
    public String expandPrefix( String prefixed )
        {
        String s = getBaseMapping().expandPrefix( prefixed );
        if (s.equals( prefixed ))
            {
            List graphs = poly.getSubGraphs();
            for (int i = 0; i < graphs.size(); i += 1)
                {
                String ss = ((Graph) graphs.get(i)).getPrefixMapping().expandPrefix( prefixed );
                if (!ss.equals( prefixed )) return ss;
                }
            }
        return s;
        }
        
    /**
        Answer a readable (we hope) representation of this prefix mapping.
    */
    public String toString()
        { return "<polyadic prefix map>"; }
        
    /**
        Compress the URI using the prefix mapping. This version of the code looks
        through all the maplets and checks each candidate prefix URI for being a
        leading substring of the argument URI. There's probably a much more
        efficient algorithm available, preprocessing the prefix strings into some
        kind of search table, but for the moment we don't need it.
    */
    public String shortForm( String uri )
        {
        String s = getBaseMapping().shortForm( uri );
        if (s.equals( uri ))
            {
            List graphs = poly.getSubGraphs();
            for (int i = 0; i < graphs.size(); i += 1)
                {
                String ss = ((Graph) graphs.get(i)).getPrefixMapping().shortForm( uri );
                if (!ss.equals( uri )) return ss;
                }
            }
        return s;
        }
    
    public String qnameFor( String uri )
        {
        String result = getBaseMapping().qnameFor( uri );
        if (result == null)
            {
            List graphs = poly.getSubGraphs();
            for (int i = 0; i < graphs.size(); i += 1)
                {
                String ss = ((Graph) graphs.get(i)).getPrefixMapping().qnameFor( uri );
                if (ss != null) return ss;
                }
            }
        return result;
        }
    }

/*
 * (c) Copyright 2004, Hewlett-Packard Development Company, LP
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