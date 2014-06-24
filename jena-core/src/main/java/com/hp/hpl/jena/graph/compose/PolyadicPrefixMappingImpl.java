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
        { poly = p; }
    
    @Override protected boolean equals( PrefixMappingImpl other )
        { return equalsByMap( other ); }
    
    @Override protected boolean sameAs( PrefixMappingImpl other )
        { return equalsByMap( other ); }
           
    private PrefixMapping getBaseMapping()
        { 
        Graph base = poly.getBaseGraph();
        return base == null ? pending : base.getPrefixMapping(); 
        }
    
    @Override public PrefixMapping setNsPrefix( String prefix, String uri ) 
        {
        checkUnlocked();
        getBaseMapping().setNsPrefix( prefix, uri );
        return this;
        }
    
    @Override public PrefixMapping removeNsPrefix( String prefix )
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
    @Override public PrefixMapping setNsPrefixes( PrefixMapping other )
        { return setNsPrefixes( other.getNsPrefixMap() ); }
        
    /**
        Add the bindings in the map to our own. This will fail with a ClassCastException
        if any key or value is not a String; we make no guarantees about order or
        completeness if this happens. It will fail with an IllegalPrefixException if
        any prefix is illegal; similar provisos apply.
        
         @param other the Map whose bindings we are to add to this.
    */
    @Override public PrefixMapping setNsPrefixes( Map<String, String> other )
        {
        checkUnlocked();
        getBaseMapping().setNsPrefixes( other );
        return this;
        }
         
    @Override public String getNsPrefixURI( String prefix ) 
        {
        PrefixMapping bm = getBaseMapping();
        String s = bm.getNsPrefixURI( prefix );
        if (s == null && prefix.length() > 0)
            {
            List<Graph> graphs = poly.getSubGraphs();
                for ( Graph graph : graphs )
                {
                    String ss = graph.getPrefixMapping().getNsPrefixURI( prefix );
                    if ( ss != null )
                    {
                        return ss;
                    }
                }
            }
        return s;
        }
        
    @Override public Map<String, String> getNsPrefixMap()
        { 
        Map<String, String> result = CollectionFactory.createHashedMap();
        List<Graph> graphs = poly.getSubGraphs();
        for (int i = graphs.size(); i > 0;)
            result.putAll( graphs.get( --i ).getPrefixMapping().getNsPrefixMap() );
        result.remove( "" );
        result.putAll( getBaseMapping().getNsPrefixMap() );
        return result; 
        }
        
    @Override public String getNsURIPrefix( String uri )
        {
        String s = getBaseMapping().getNsURIPrefix( uri );
        if (s == null)
            {
            List<Graph> graphs = poly.getSubGraphs();
                for ( Graph graph : graphs )
                {
                    String ss = graph.getPrefixMapping().getNsURIPrefix( uri );
                    if ( ss != null && ss.length() > 0 )
                    {
                        return ss;
                    }
                }
            }
        return s;
        }
        
    /**
        Expand a prefixed URI. There's an assumption that any URI of the form
        Head:Tail is subject to mapping if Head is in the prefix mapping. So, if
        someone takes it into their heads to define eg "http" or "ftp" we have problems.
    */
    @Override public String expandPrefix( String prefixed )
        {
        String s = getBaseMapping().expandPrefix( prefixed );
        if (s.equals( prefixed ))
            {
            List<Graph> graphs = poly.getSubGraphs();
                for ( Graph graph : graphs )
                {
                    String ss = graph.getPrefixMapping().expandPrefix( prefixed );
                    if ( !ss.equals( prefixed ) )
                    {
                        return ss;
                    }
                }
            }
        return s;
        }
        
    /**
        Answer a readable (we hope) representation of this prefix mapping.
    */
    @Override  public String toString()
        { return "<polyadic prefix map>"; }
        
    /**
        Compress the URI using the prefix mapping. This version of the code looks
        through all the maplets and checks each candidate prefix URI for being a
        leading substring of the argument URI. There's probably a much more
        efficient algorithm available, preprocessing the prefix strings into some
        kind of search table, but for the moment we don't need it.
    */
    @Override public String shortForm( String uri )
        {
        String s = getBaseMapping().shortForm( uri );
        if (s.equals( uri ))
            {
            List<Graph> graphs = poly.getSubGraphs();
                for ( Graph graph : graphs )
                {
                    String ss = graph.getPrefixMapping().shortForm( uri );
                    if ( !ss.equals( uri ) )
                    {
                        return ss;
                    }
                }
            }
        return s;
        }
    
    @Override public String qnameFor( String uri )
        {
        String result = getBaseMapping().qnameFor( uri );
        if (result == null)
            {
            List<Graph> graphs = poly.getSubGraphs();
                for ( Graph graph : graphs )
                {
                    String ss = graph.getPrefixMapping().qnameFor( uri );
                    if ( ss != null )
                    {
                        return ss;
                    }
                }
            }
        return result;
        }
    }
