/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ModelChangedListener.java,v 1.10 2004-03-23 13:47:41 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model;

import java.util.*;

/**
    The interface for classes that listen for model-changed events. In all cases,
    the argument is [a copy of] the item that has been presented to the model,
    or its underlying graph, for addition or removal. For an add, the item [or parts
    of that item] may have already been present in the model; for remove, the
    item [or parts of it] need not have been absent from the item.
<p>
    NOTE that the listener is supplied with more-or-less faithful copies of the
    original items that were added to, or removed from, the model. In particular,
    graph-level updates to the model appear as statements, not triples. 
    
 	@author kers (design by andy & the team)
*/
public interface ModelChangedListener
    {
    /**
        Method to call when a single statement has been added to the attached model.
        @param s the statement that has been presented for addition.
    */
    void addedStatement( Statement s );
    
    /**
        Method to call when an array of statements has been added to the attached 
        model. NOTE. This array need not be == to the array added using 
        Model::add(Statement[]).       
        @param statements the array of added statements
    */
    void addedStatements( Statement [] statements );
    
    /**
        Method to call when a list of statements has been added to the attached model.
        NOTE. This list need not be == to the list added using Model::add(List).
        @param statements the list of statements that has been removed.
    */
    void addedStatements( List statements );
    
    /**
        Method to call when a statement iterator has supplied elements to be added
        to the attached model. <code>statements</code> is a copy of the
        original iterator.
    	@param statements
     */
    void addedStatements( StmtIterator statements );
    
    /**
        Method to call when a model has been used to define the statements to
        be added to our attached model.
    	@param m a model equivalent to [and sharing with] the added model
     */
    void addedStatements( Model m );
    
    /**
        Method to call when a single statement has been removed from the attached model.
        @param s the statement that has been presented for removal.
    */
    void removedStatement( Statement s );
    
    /**
        Method to call when an array of statements has been removed from the 
        attached model. NOTE. This array need not be == to the array added using 
        Model::remove(Statement[]).
        @param statements the array of removed statements
    */    
    void removedStatements( Statement [] statements );
    
    /**
        Method to call when a list of statements has been deleted from the attached
        model. NOTE. This list need not be == to the list added using 
        Model::remov(List).
        @param statements the list of statements that have been removed.
    */
    void removedStatements( List statements );
    
    /**
        Method to call when a statement iterator has been used to remove 
        statements from the attached model. The iterator will be a copy, in the
        correct order, of the iterator supplied for the removal.
    	@param statements a statement-type copy of the updating iterator
     */
    void removedStatements( StmtIterator statements );
    
    /**
        Method to call when a model has been used to remove statements from
        our attached model.
    	@param m a model equivalent to [and sharing with] the one removed
     */
    
    void removedStatements( Model m );
    
    void notifyEvent( Model m, Object event );
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