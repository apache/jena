/*
  (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ChangedListener.java,v 1.5 2004-12-06 13:50:14 andy_seaborne Exp $
*/

package com.hp.hpl.jena.rdf.listeners;

import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

/**
    Class that merely notes that a change has occurred. The only method its user
    should be interested in is <code>hasChanged()</code>.
    
 	@author kers
*/
public class ChangedListener implements ModelChangedListener
    {
    /**
        True iff a change has occurred since the last check
    */
    private boolean changed = false;
    /**
        Record that a change has occurred by setting <code>changed</code> true.
    */
    protected void setChanged() { changed = true; }
    /**
        Answer true iff a change has occurred since the last hasChanged and set changed
        false.
        @return true iff a change has occurred since the last call to hasChanged
    */
    public boolean hasChanged() { try { return changed; } finally { changed = false; } }
    public void addedStatement( Statement s ) { setChanged(); }
    public void addedStatements( Statement [] statements ) { setChanged(); }
    public void addedStatements( List statements ) { setChanged(); }
    public void addedStatements( StmtIterator statements ) { setChanged(); }
    public void addedStatements( Model m ) { setChanged(); }
    public void removedStatement( Statement s ) { setChanged(); }   
    public void removedStatements( Statement [] statements ) { setChanged(); }
    public void removedStatements( List statements ) { setChanged(); }
    public void removedStatements( StmtIterator statements ) { setChanged(); }
    public void removedStatements( Model m ) { setChanged(); }          
    public void notifyEvent( Model m, Object event ) {}
    }       


/*
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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