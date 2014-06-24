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

import java.util.*;

/** An internal class not normally of interest to application developers.
 *  An iterator over the nodes in a container.
 */
public class ContNodeIteratorImpl extends NiceIterator<RDFNode> implements NodeIterator
    {
    protected Statement recent = null;
    protected final Container cont;
    protected int size;
    protected int index = 0;
    protected int numDeleted = 0;
    protected final List<Integer> moved = new ArrayList<>();
    
    protected final Iterator<Statement> iterator;
    
    /** Creates new ContNodeIteratorImpl */
    public ContNodeIteratorImpl (Iterator<Statement>iterator, Object ignored, Container  cont )  
        {
        this.iterator = iterator;
        this.cont = cont;
        this.size = cont.size();
        }

    @Override public RDFNode next() throws NoSuchElementException 
        {
        recent = iterator.next();
        index += 1;
        return recent.getObject();
        }
    
    @Override public boolean hasNext()
        { return iterator.hasNext(); }
    
    @Override
    public RDFNode nextNode() throws NoSuchElementException 
        { return next(); }
            
    @Override public void remove() throws NoSuchElementException
        {
        if (recent == null) throw new NoSuchElementException();
        iterator.remove();
        if (index > (size - numDeleted)) 
            {
            ((ContainerI) cont).remove( moved.get(size-index).intValue(), recent.getObject() );
            } 
        else 
            {
            cont.remove( recent );
            moved.add( new Integer( index ) );
            }
        recent = null;
        numDeleted += 1;
        }
    }
