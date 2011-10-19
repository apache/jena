/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ObjectListener.java,v 1.1 2009-06-29 08:55:39 castagna Exp $
*/

package com.hp.hpl.jena.rdf.listeners;

import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

/**
    Listener that funnels all the changes into add/removed(Object) x, ie, leaves 
    discrimination to be done on the type of object added or removed.
    
 	@author kers
*/
public class ObjectListener implements ModelChangedListener
    {
    /**
        Override this to track all the objects added; each object will be a Statement, a
        Statement [], a List (Statement), an Iterator (Statement), or a Model.
    */
    public void added( Object x ) {}
    /**
        Override this to track all the objects removed; each object will be a Statement, a
        Statement [], a List (Statement), an Iterator (Statement), or a Model.
    */
    public void removed( Object x ) {}
/* */
    @Override
    public void addedStatement( Statement s ) { added( s ); }
    @Override
    public void addedStatements( Statement [] statements ) { added( statements ); }
    @Override
    public void addedStatements( List<Statement> statements ) { added( statements ); }
    @Override
    public void addedStatements( StmtIterator statements ) { added( statements ); }
    @Override
    public void addedStatements( Model m ) { added( m ); }
    @Override
    public void removedStatement( Statement s ) { removed( s ); }   
    @Override
    public void removedStatements( Statement [] statements ) { removed( statements ); }
    @Override
    public void removedStatements( List<Statement> statements ) { removed( statements ); }
    @Override
    public void removedStatements( StmtIterator statements ) { removed( statements ); }
    @Override
    public void removedStatements( Model m ) { removed( m ); }       
    @Override
    public void notifyEvent( Model m, Object event ) {}         
    }

/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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