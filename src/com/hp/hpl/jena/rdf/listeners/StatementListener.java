/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: StatementListener.java,v 1.4 2004-03-23 13:47:41 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.listeners;

import com.hp.hpl.jena.rdf.model.*;
import java.util.*;

/**
    A listener that filters all its listening down to the single-statement level. Users of this
    class override <code>addedStatement(Statement)</code> and 
    <code>removedStatement(Statement)</code>.
 	@author kers
*/
public class StatementListener implements ModelChangedListener
    {
    /**
        Override this to listen to all incoming added statements
    */
    public void addedStatement( Statement s ) {}
    /**
        Override this to listen to all incoming removed statements
    */
    public void removedStatement( Statement s ) {}   
/* */
    public void addedStatements( Statement [] statements ) 
        { for (int i = 0; i < statements.length; i += 1) addedStatement( statements[i] ); }
    public void addedStatements( List statements ) 
        { for (int i = 0; i < statements.size(); i += 1) addedStatement( (Statement) statements.get(i) ); }
    public void addedStatements( StmtIterator statements ) 
        { while (statements.hasNext()) addedStatement( statements.nextStatement() ); }
    public void addedStatements( Model m ) 
        { addedStatements( m.listStatements() ); }
    public void removedStatements( Statement [] statements ) 
        { for (int i = 0; i < statements.length; i += 1) removedStatement( statements[i] ); }
    public void removedStatements( List statements ) 
        { for (int i = 0; i < statements.size(); i += 1) removedStatement( (Statement) statements.get(i) ); }
    public void removedStatements( StmtIterator statements ) 
        { while (statements.hasNext()) removedStatement( statements.nextStatement() ); }
    public void removedStatements( Model m ) 
        { removedStatements( m.listStatements() ); }            
    public void notifyEvent( Model m, Object event ) 
        {}
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