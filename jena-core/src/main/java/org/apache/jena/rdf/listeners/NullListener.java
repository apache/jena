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

package org.apache.jena.rdf.listeners;

import java.util.*;

import org.apache.jena.rdf.model.* ;

/**
    A listener that ignores everything you tell it; intended as a base class
    or an identity element. Each method is implemented as {}.
*/
public class NullListener implements ModelChangedListener
    {
    @Override
    public void addedStatement( Statement s ) {}
    @Override
    public void addedStatements( Statement [] statements ) {}
    @Override
    public void addedStatements( List<Statement> statements ) {}
    @Override
    public void addedStatements( StmtIterator statements ) {}
    @Override
    public void addedStatements( Model m ) {}
    @Override
    public void removedStatement( Statement s ) {}   
    @Override
    public void removedStatements( Statement [] statements ) {}
    @Override
    public void removedStatements( List<Statement> statements ) {}
    @Override
    public void removedStatements( StmtIterator statements ) {}
    @Override
    public void removedStatements( Model m ) {}            
    @Override
    public void notifyEvent( Model m, Object event ) {}
    }
