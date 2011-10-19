/*
  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: RecordingModelListener.java,v 1.1 2009-06-29 08:55:33 castagna Exp $
*/
package com.hp.hpl.jena.rdf.model.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;


public class RecordingModelListener implements ModelChangedListener
    {
    List<Object> history = new ArrayList<Object>();
    
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

/*
(c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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