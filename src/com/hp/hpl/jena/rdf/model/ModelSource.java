/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ModelSource.java,v 1.4 2005-02-11 19:20:06 chris-dollin Exp $
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
    
    ModelSources may choose to not implement certain aspects of the
    full interface, but must document which aspects are available.
 	
 	@author hedgehog
*/
public interface ModelSource
    {
    /**
        Answer this ModelSource's default model. Every ModelSource
        has a default model. That model may not be created until the
        first call on getModel. Multiple calls of getModel will
        yield the *same* model.
    */
    // Model getModel();
    
    /**
        Answer a Model that satisfies this ModelSource's shape. Different
        calls may return different models, or they may all return the same
        model. 
    */
    Model createModel();
    
    /**
     	Answer a model. Different ModelSources may implement this
     	in very different ways - ModelSource imposes few constraints
     	other than the result is a proper Model. A ModelSource may
     	use the name to identify an existing Model and re-use it,
     	or it may create a fresh Model each time. However it *is*
     	expected that uses of different names will answer different models
     	(different in the strong sense of not having the same underlying
     	graph, too).
     	
     	TODO implement that last sentence as a test.
    */
    Model openModel( String name );

    /**
     	If this ModelSource admits to having a Model with the given
     	<code>name</code>, answer that Model, otherwise answer null.
     	A ModelSource may freely forget existing Models, even ones 
     	that have just been created via openModel. (ModelMakers are not
     	allowed to do that - as a subinterface they satisfy extra constraints.)
    */
    Model getExistingModel( String name );
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