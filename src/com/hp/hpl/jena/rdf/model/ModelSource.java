/*
  (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ModelSource.java,v 1.6 2005-02-21 12:14:11 andy_seaborne Exp $
*/

package com.hp.hpl.jena.rdf.model;


/**
    The revised and soon-to-be-core interface for sources of models,
    typically generated from RDF descriptions.
    
    <p>ModelSources can supply models in a variety of ways.
    
    <ul>
    <li>some fresh model of the kind this ModelSource supplies
    <li>the particular model this ModelSource supplies
    <li>a named model from the collection this ModelSource supplies
    </ul>
    
    A ModelSource is free to "forget" named models if it so wishes;
    for example, it may be a discard-if-getting-full cache.
 	
 	@author hedgehog
*/
public interface ModelSource
    {
    /**
        Answer this ModelSource's default model. Every ModelSource
        has a default model. That model need not be created until the
        first call on getModel. Multiple calls of getModel will
        yield the *same* model. This method never returns <code>null</code>.
    */
    Model getModel();
    
    /**
        Answer a Model that satisfies this ModelSource's shape. Different
        calls may return different models, or they may all return the same
        model. This method never returns <code>null</code>.
    */
    Model createModel();
    
    /**
     	Answer a model. Different ModelSources may implement this
     	in very different ways - ModelSource imposes few constraints
     	other than the result is a proper Model. A ModelSource may
     	use the name to identify an existing Model and re-use it,
     	or it may create a fresh Model each time.         
        
        <p>It is expected that uses of different names will answer 
        different models (different in the strong sense of not having 
        the same underlying graph, too).
        
        <p>If the ModelSource does not have a model with this name,
        and if it is not prepared to create one, it should throw a
        DoesNotExistException. This method never returns <code>null</code>.
    */
    Model openModel( String name );

    /**
     	Answer the model named by <code>string</code> in this ModelSource,
        if it [still] has one, or <code>null</code> if there isn't one. 
        The ModelSource should <i>not</i> create a fresh model if it 
        doesn't already have one.
    */
    Model openModelIfPresent( String string );
    }

/*
	(c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
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