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

import java.util.NoSuchElementException;
import java.util.Iterator;

/** An internal class not normally of interest to developers.
 *  A sequence node iterator.
 */
public class SeqNodeIteratorImpl extends NiceIterator<RDFNode> implements NodeIterator 
    {
    Seq       seq;
    int       size;
    int       index = 0;
    Statement stmt = null;
    Iterator<Statement> base;
    
    private int       numDeleted=0;
    
    /** Creates new SeqNodeIteratorImpl 
    */
    public SeqNodeIteratorImpl ( Iterator<Statement>  iterator, Seq seq )  {
        this.base = iterator; 
        this.seq = seq;
        this.size = seq.size();
    }
    
    @Override public boolean hasNext()
        { return base.hasNext(); }

    @Override public RDFNode next() {
        stmt = base.next();
        index += 1;
        return stmt.getObject();
    }
    
    @Override
    public RDFNode nextNode() {
        return next();
    }
            
    @Override public void remove() {
        if (stmt == null) throw new NoSuchElementException();
        ((ContainerI)seq).remove(index-numDeleted, stmt.getObject());
        stmt = null;
        numDeleted++;
    }
}
