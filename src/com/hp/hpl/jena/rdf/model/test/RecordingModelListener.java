/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: RecordingModelListener.java,v 1.1 2004-06-29 14:42:03 chris-dollin Exp $
*/
package com.hp.hpl.jena.rdf.model.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;


class RecordingModelListener implements ModelChangedListener
    {
    List history = new ArrayList();
    
    public void addedStatement( Statement s )
        { record( "add", s ); }
        
    public void addedStatements( Statement [] statements )
        { record( "add[]", Arrays.asList( statements ) ); }
        
    public void addedStatements( List statements )
        { record( "addList", statements ); }
        
    public void addedStatements( StmtIterator statements )
        { record( "addIterator", ModelTestBase.iteratorToList( statements ) ); }
        
    public void addedStatements( Model m )
        { record( "addModel", m ); }
        
    public void removedStatements( Statement [] statements )
        { record( "remove[]", Arrays.asList( statements ) ); }
    
   public void removedStatement( Statement s )
        { record( "remove", s ); }
        
    public void removedStatements( List statements )
        { record( "removeList", statements ); }
        
    public void removedStatements( StmtIterator statements )
        { record( "removeIterator", ModelTestBase.iteratorToList( statements ) ); }
        
    public void removedStatements( Model m )
        { record( "removeModel", m ); }
    
    public void notifyEvent( Model m, Object event )
        { record( "someEvent", m, event ); }
    
    protected void record( String tag, Object x, Object y )
        { history.add( tag ); history.add( x ); history.add( y ); }
        
    protected void record( String tag, Object info )
        { history.add( tag ); history.add( info ); }
        
    boolean has( Object [] things ) 
        { return history.equals( Arrays.asList( things ) ); }
        
    void assertHas( Object [] things )
        {
        if (has( things ) == false)
            ModelTestBase.fail( "expected " + Arrays.asList( things ) + " but got " + history );
        }
    }

/*
(c) Copyright 2004, Hewlett-Packard Development Company, LP
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