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
 * ResIterator.java
 *
 * Created on 28 July 2000, 13:43
 */

package com.hp.hpl.jena.rdf.model;

import java.util.NoSuchElementException;

/** An iterator which returns RDF Resources.
 *
 * <p>RDF iterators are similar to standard Java iterators, except that they
 *   return specifically typed objects, in this case RDF Resources 
 *   and have a <CODE>close()</CODE> method.
 *   This should be called to free resources
 *   if the application does not complete the iteration.</p>
 * @author bwm
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1.1.1 $' Date='$Date: 2002-12-19 19:17:54 $'
 */
public interface ResIterator {
    
    /** Determine if there any more Resources in the iteration.
     * @throws RDFException Generic RDF exception.
     * @return true if and only if there are more Resources available
     * from the iteration.
     */    
    public boolean hasNext() throws RDFException;
    
    /** Return the next Resource of the iteration.
     * @throws NoSuchElementException if there are no more to be returned.
     * @throws RDFException Generic RDF exception.
     * @return The next Resource from the iteration.
     */
    public Resource next() throws  NoSuchElementException, RDFException;
    
    /** Unsupported Operation.
     * @throws NoSuchElementException
     * @throws RDFException
     */
    public void remove() throws NoSuchElementException, RDFException;
    
    /** Terminate the iteration and free up resources.
     *
     * <p>Some implementations, e.g. on relational databases, hold resources while
     * the iterator still exists.  These will normally be freed when the iteration
     * completes.  However, if an application wishes to ensure they are freed without
     * completing the iteration, this method should be called.</p>
     * @throws RDFException Generic RDF exception.
     */
    public void close() throws RDFException;
}