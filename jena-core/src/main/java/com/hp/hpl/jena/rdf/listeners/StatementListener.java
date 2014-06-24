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
    A listener that filters all its listening down to the single-statement level. Users of this
    class override <code>addedStatement(Statement)</code> and 
    <code>removedStatement(Statement)</code>.
*/
public class StatementListener implements ModelChangedListener
    {
    /**
        Override this to listen to all incoming added statements
    */
    @Override
    public void addedStatement( Statement s ) {}
    /**
        Override this to listen to all incoming removed statements
    */
    @Override
    public void removedStatement( Statement s ) {}   
/* */
    @Override
    public void addedStatements( Statement [] statements ) 
        {
            for ( Statement statement : statements )
            {
                addedStatement( statement );
            }
        }
    @Override
    public void addedStatements( List<Statement> statements ) 
        {
            for ( Statement statement : statements )
            {
                addedStatement( statement );
            }
        }
    @Override
    public void addedStatements( StmtIterator statements ) 
        { while (statements.hasNext()) addedStatement( statements.nextStatement() ); }
    @Override
    public void addedStatements( Model m ) 
        { addedStatements( m.listStatements() ); }
    @Override
    public void removedStatements( Statement [] statements ) 
        {
            for ( Statement statement : statements )
            {
                removedStatement( statement );
            }
        }
    @Override
    public void removedStatements( List<Statement> statements ) 
        {
            for ( Statement statement : statements )
            {
                removedStatement( statement );
            }
        }
    @Override
    public void removedStatements( StmtIterator statements ) 
        { while (statements.hasNext()) removedStatement( statements.nextStatement() ); }
    @Override
    public void removedStatements( Model m ) 
        { removedStatements( m.listStatements() ); }            
    @Override
    public void notifyEvent( Model m, Object event ) 
        {}
    }
