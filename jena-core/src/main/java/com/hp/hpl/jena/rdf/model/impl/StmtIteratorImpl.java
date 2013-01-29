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

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.Iterator;

/** An implementation of StmtIterator.
 */


public class StmtIteratorImpl extends WrappedIterator<Statement> implements StmtIterator
    {
    private Statement current;
    
    public StmtIteratorImpl( Iterator<Statement>iterator )
        { super( iterator ); }

    /**
        return *and remember* the next element. It must be remembered
        so that remove works whichever next-method is called.
    */
    @Override public Statement next()
        { return current = super.next(); }
        
    @Override public void remove()
        {
        super.remove();
        current.remove();
        }
        
    @Override
    public Statement nextStatement()
        { return next(); }
    }
