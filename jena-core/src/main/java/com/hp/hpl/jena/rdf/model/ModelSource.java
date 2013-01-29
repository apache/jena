/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
*/
public interface ModelSource extends ModelGetter
    {
    /**
        Answer this ModelSource's default model. Every ModelSource
        has a default model. That model need not exist until the
        first call on createDefaultModel. Multiple calls of getModel will
        yield the *same* model. This method never returns <code>null</code>.
    */
    Model createDefaultModel();
    
    /**
        Answer a Model that satisfies this ModelSource's shape. Different
        calls return different models - they are not permitted to return
        the same model. (Doing this on a database model will create new,
        pseudo-anonymous, models.) This method never returns <code>null</code>.
    */
    Model createFreshModel();
    
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
