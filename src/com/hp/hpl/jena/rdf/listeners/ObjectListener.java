/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ObjectListener.java,v 1.3 2003-08-27 13:05:53 andy_seaborne Exp $
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
    public void addedStatement( Statement s ) { added( s ); }
    public void addedStatements( Statement [] statements ) { added( statements ); }
    public void addedStatements( List statements ) { added( statements ); }
    public void addedStatements( StmtIterator statements ) { added( statements ); }
    public void addedStatements( Model m ) { added( m ); }
    public void removedStatement( Statement s ) { removed( s ); }   
    public void removedStatements( Statement [] statements ) { removed( statements ); }
    public void removedStatements( List statements ) { removed( statements ); }
    public void removedStatements( StmtIterator statements ) { removed( statements ); }
    public void removedStatements( Model m ) { removed( m ); }               
    }

/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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