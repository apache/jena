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
    void addedStatements( List<Statement> statements );
    
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
    void removedStatements( List<Statement> statements );
    
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
