/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: RecordingListener.java,v 1.3 2003-07-11 10:16:11 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

import com.hp.hpl.jena.graph.*;

import junit.framework.*;
import java.util.*;

/**
    This testing listener records the event names and data, and provides
    a method for comparing the actual with the expected history. 
*/    
class RecordingListener implements GraphListener
    {
    List history = new ArrayList();
    
    public void notifyAdd( Triple t )
        { record( "add", t ); }
        
    public void notifyAdd( Triple [] triples )
        { record( "add[]", triples ); }
        
    public void notifyAdd( List triples )
        { record( "addList", triples ); }
        
    public void notifyAdd( Iterator it )
        { record( "addIterator", GraphTestBase.iteratorToList( it ) ); }
        
    public void notifyDelete( Triple t )
        { record( "delete", t ); }
        
    public void notifyDelete( Triple [] triples )
        { record( "delete[]", triples ); }
        
    public void notifyDelete( List triples )
        { record( "deleteList", triples ); }
        
    public void notifyDelete( Iterator it )
        { record( "deleteIterator", GraphTestBase.iteratorToList( it ) ); }
        
    protected void record( String tag, Object info )
        { history.add( tag ); history.add( info ); }
        
    public void clear()
        { history.clear(); }
        
    public boolean has( Object [] things )
        { return history.equals( Arrays.asList( things ) ); } 
        
    void assertHas( Object [] things )
        {
        if (has( things ) == false)
            Assert.fail( "expected " + Arrays.asList( things ) + " but got " + history );
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