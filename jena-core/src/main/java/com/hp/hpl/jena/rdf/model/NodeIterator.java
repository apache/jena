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

import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import java.util.NoSuchElementException;

/** An iterator which returns RDF nodes.
 *
 * <p>RDF iterators are standard Java iterators, except that they
 *    have an extra method that returns specifically typed objects,
 *    in this case RDF nodes, and have a <CODE>close()</CODE> method. 
 *    thatshould be called to free resources if the application does
 *    not complete the iteration.</p>
 */
public interface NodeIterator extends ExtendedIterator<RDFNode>
    {
    /** Determine if there any more values in the iteration.
     * @return true if and only if there are more values available
     * from the iteration.
     */
    @Override
    public boolean hasNext();
    
    /** Return the next RDFNode of the iteration.
     * @throws NoSuchElementException if there are no more nodes to be returned.
     * @return The next RDFNode from the iteration.
     */
    @Override
    public RDFNode next() throws  NoSuchElementException;

    /** Return the next RDFNode of the iteration.
     * @throws NoSuchElementException if there are no more nodes to be returned.
     * @return The next RDFNode from the iteration.
     */
    public RDFNode nextNode() throws  NoSuchElementException;
    
    /** Unsupported Operation.
     * @throws NoSuchElementException
     */
    @Override
    public void remove() throws NoSuchElementException;
    
    /** Terminate the iteration and free up resources.
     *
     * <p>Some implementations, e.g. on relational databases, hold resources while
     * the iterator still exists.  These will normally be freed when the iteration
     * completes.  However, if an application wishes to ensure they are freed without
     * completing the iteration, this method should be called.</p>
     .
     */
    @Override
    public void close() ;
}
