/*
 *  (c) Copyright Hewlett-Packard Company 2000 
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * NodeIterator.java
 *
 * Created on 28 July 2000, 13:36
 */

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.util.iterator.ClosableIterator;
import java.util.NoSuchElementException;

/** An iterator which returns RDF nodes.
 *
 * <p>RDF iterators are standard Java iterators, except that they
 *    have an extra method that returns specifically typed objects,
 *    in this case RDF nodes, and have a <CODE>close()</CODE> method. 
 *    thatshould be called to free resources if the application does
 *    not complete the iteration.</p>
 * @author bwm
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.5 $' Date='$Date: 2003-06-18 22:30:44 $'
 */
public interface NodeIterator extends ClosableIterator {
    /** Determine if there any more values in the iteration.
     .
     * @return true if and only if there are more values available
     * from the iteration.
     */
    public boolean hasNext() ;
    /** Return the next RDFNode of the iteration.
     * @throws NoSuchElementException if there are no more nodes to be returned.
     .
     * @return The next RDFNode from the iteration.
     */
    public Object next() throws  NoSuchElementException;
    /** Return the next RDFNode of the iteration.
     * @throws NoSuchElementException if there are no more nodes to be returned.
     .
     * @return The next RDFNode from the iteration.
     */
    public RDFNode nextNode() throws  NoSuchElementException;
    /** Unsupported Operation.
     * @throws NoSuchElementException
     */
    public void remove() throws NoSuchElementException;
    /** Terminate the iteration and free up resources.
     *
     * <p>Some implementations, e.g. on relational databases, hold resources while
     * the iterator still exists.  These will normally be freed when the iteration
     * completes.  However, if an application wishes to ensure they are freed without
     * completing the iteration, this method should be called.</p>
     .
     */
    public void close() ;
}
