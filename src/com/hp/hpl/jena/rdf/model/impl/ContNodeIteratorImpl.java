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
 * ContNodeIteratorImpl.java
 *
 * Created on 12 August 2000, 09:57
 */

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.Vector;

/** An internal class not normally of interest to application developers.
 *  An iterator over the nodes in a container.
 * @author bwm
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.2 $' Date='$Date: 2003-02-01 14:35:31 $'
 */
public class ContNodeIteratorImpl 
  extends Object implements NodeIterator{
    
    Iterator  iterator;
    Object    object;
    Statement stmt = null;
    Container cont;
    int       size;
    int       index=0;
    int       numDeleted=0;
    Vector    moved = new Vector();
    /** Creates new ContNodeIteratorImpl */
    public ContNodeIteratorImpl (Iterator  iterator, 
                                Object     object,
                                Container  cont) throws RDFException {
        this.iterator = iterator;
        this.object   = object;
        this.cont     = cont;
        this.size     = cont.size();
    }

    public boolean hasNext() throws RDFException {
        return iterator.hasNext();
    }
    
    public Object next() throws NoSuchElementException, RDFException {
        if (iterator != null) {
            stmt = (Statement) iterator.next();
            index++;
            return stmt.getObject();
        } else {
            throw new RDFException(RDFException.ITERATORCLOSED);  
        }
    }
    
    public RDFNode nextNode() throws NoSuchElementException, RDFException {
        return (RDFNode) next();
    }
            
    public void remove() throws NoSuchElementException, RDFException {
        if (stmt == null) throw new NoSuchElementException();
        iterator.remove();
        
        if (index > (size-numDeleted)) {
            ((ContainerI)cont).remove(((Integer) moved.elementAt(size-index))
                                                      .intValue(),
                                       stmt.getObject());
        } else {
            cont.remove(stmt);
            moved.add(new Integer(index));
        }
        stmt = null;
        numDeleted++;
    }
    
    public void close() throws RDFException{
        if (iterator instanceof ClosableIterator) {
            ((ClosableIterator) iterator).close();
        }
        iterator = null;
        object   = null;
    }
}