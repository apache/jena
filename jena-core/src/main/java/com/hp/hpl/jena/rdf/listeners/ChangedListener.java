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
    Class that merely notes that a change has occurred. The only method its user
    should be interested in is <code>hasChanged()</code>.
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
    @Override
    public void addedStatement( Statement s ) { setChanged(); }
    @Override
    public void addedStatements( Statement [] statements ) { setChanged(); }
    @Override
    public void addedStatements( List<Statement> statements ) { setChanged(); }
    @Override
    public void addedStatements( StmtIterator statements ) { setChanged(); }
    @Override
    public void addedStatements( Model m ) { setChanged(); }
    @Override
    public void removedStatement( Statement s ) { setChanged(); }   
    @Override
    public void removedStatements( Statement [] statements ) { setChanged(); }
    @Override
    public void removedStatements( List<Statement> statements ) { setChanged(); }
    @Override
    public void removedStatements( StmtIterator statements ) { setChanged(); }
    @Override
    public void removedStatements( Model m ) { setChanged(); }          
    @Override
    public void notifyEvent( Model m, Object event ) {}
    }
