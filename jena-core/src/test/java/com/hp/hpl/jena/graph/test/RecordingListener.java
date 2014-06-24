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

package com.hp.hpl.jena.graph.test;

import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.junit.Assert ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.GraphListener ;
import com.hp.hpl.jena.graph.Triple ;

/**
    This testing listener records the event names and data, and provides
    a method for comparing the actual with the expected history. 
*/    
public class RecordingListener implements GraphListener
    {
    public List<Object> history = new ArrayList<>();
    
    @Override
    public void notifyAddTriple( Graph g, Triple t )
        { record( "add", g, t ); }
        
    @Override
    public void notifyAddArray( Graph g, Triple [] triples )
        { record( "add[]", g, triples ); }
        
    @Override
    public void notifyAddList( Graph g, List<Triple> triples )
        { record( "addList", g, triples ); }
        
    @Override
    public void notifyAddIterator( Graph g, Iterator<Triple> it )
        { record( "addIterator", g, GraphTestBase.iteratorToList( it ) ); }
        
    @Override
    public void notifyAddGraph( Graph g, Graph added )
        { record( "addGraph", g, added ); }
        
    @Override
    public void notifyDeleteTriple( Graph g, Triple t )
        { record( "delete", g, t ); }
        
    @Override
    public void notifyDeleteArray( Graph g, Triple [] triples )
        { record( "delete[]", g, triples ); }
        
    @Override
    public void notifyDeleteList( Graph g, List<Triple> triples )
        { record( "deleteList", g, triples ); }
        
    @Override
    public void notifyDeleteIterator( Graph g, Iterator<Triple> it )
        { record( "deleteIterator", g, GraphTestBase.iteratorToList( it ) ); }
        
    @Override
    public void notifyDeleteGraph( Graph g, Graph removed )
        { record( "deleteGraph", g, removed ); }
    
    @Override
    public void notifyEvent( Graph source, Object event )
        { record( "someEvent", source, event ); }
        
    protected void record( String tag, Object x, Object y )
        { history.add( tag ); history.add( x ); history.add( y ); }
    
    protected void record( String tag, Object info )
        { history.add( tag ); history.add( info ); }
        
    public void clear()
        { history.clear(); }

    public boolean has( List<Object> things )
        { return Arrays.deepEquals(history.toArray(), things.toArray() ); } 
    
    public boolean hasStart( List<Object> L )
        { return L.size() <= history.size() && L.equals( history.subList( 0, L.size() ) ); }
    
    public boolean hasEnd( List<Object> L )
        { return L.size() <= history.size() && L.equals( history.subList( history.size() - L.size(), history.size() ) ); }
    
    public boolean has( Object [] things )
        { return Arrays.deepEquals(history.toArray(), things ); } 
        
    public void assertHas( List<Object> things )
        { if (has( things ) == false) Assert.fail( "expected " + things + " but got " + history ); }  
    
    public void assertHas( Object [] things )
        { assertHas( Arrays.asList( things ) ); }
    
    public void assertHasStart( Object [] start )
        { 
        List<Object> L = Arrays.asList( start );
        if (hasStart( L ) == false) Assert.fail( "expected " + L + " at the beginning of " + history );
        }
    
    public void assertHasEnd( Object [] end )
        {
        List<Object> L = Arrays.asList( end );
        if (hasEnd( L ) == false) Assert.fail( "expected " + L + " at the end of " + history );        
        }
    }
