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

package com.hp.hpl.jena.rdf.model.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;


public class RecordingModelListener implements ModelChangedListener
    {
    List<Object> history = new ArrayList<>();
    
    @Override
    public void addedStatement( Statement s )
        { record( "add", s ); }
        
    @Override
    public void addedStatements( Statement [] statements )
        { record( "add[]", Arrays.asList( statements ) ); }
        
    @Override
    public void addedStatements( List<Statement> statements )
        { record( "addList", statements ); }
        
    @Override
    public void addedStatements( StmtIterator statements )
        { record( "addIterator", GraphTestBase.iteratorToList( statements ) ); }
        
    @Override
    public void addedStatements( Model m )
        { record( "addModel", m ); }
        
    @Override
    public void removedStatements( Statement [] statements )
        { record( "remove[]", Arrays.asList( statements ) ); }
    
   @Override
public void removedStatement( Statement s )
        { record( "remove", s ); }
        
    @Override
    public void removedStatements( List<Statement> statements )
        { record( "removeList", statements ); }
        
    @Override
    public void removedStatements( StmtIterator statements )
        { record( "removeIterator", GraphTestBase.iteratorToList( statements ) ); }
        
    @Override
    public void removedStatements( Model m )
        { record( "removeModel", m ); }
    
    @Override
    public void notifyEvent( Model m, Object event )
        { record( "someEvent", m, event ); }
    
    protected void record( String tag, Object x, Object y )
        { history.add( tag ); history.add( x ); history.add( y ); }
        
    protected void record( String tag, Object info )
        { history.add( tag ); history.add( info ); }
        
    public boolean has( Object [] things ) 
        { return history.equals( Arrays.asList( things ) ); }
        
    public void assertHas( Object [] things )
        {
        if (has( things ) == false)
            Assert.fail( "expected " + Arrays.asList( things ) + " but got " + history );
        }    
    
    public boolean has( List<?> things )
            { return history.equals( things ); } 
        
    public boolean hasStart( List<Object> L )
        { return L.size() <= history.size() && L.equals( history.subList( 0, L.size() ) ); }
    
    public boolean hasEnd( List<Object> L )
        { return L.size() <= history.size() && L.equals( history.subList( history.size() - L.size(), history.size() ) ); }
    
    public void assertHas( List<?> things )
        { if (has( things ) == false) Assert.fail( "expected " + things + " but got " + history ); }  
    
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
    
    public void clear()
    { history.clear(); }

    }
