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

package com.hp.hpl.jena.rdf.listeners;

import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

/**
    Listener that funnels all the changes into add/removed(Object) x, ie, leaves 
    discrimination to be done on the type of object added or removed.
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
