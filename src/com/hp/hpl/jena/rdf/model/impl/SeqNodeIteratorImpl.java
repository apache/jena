/*
 *  (c) Copyright 2000, 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
 * SeqNodeIteratorImpl.java
 *
 * Created on 12 August 2000, 15:37
 */

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.NoSuchElementException;
import java.util.Iterator;

/** An internal class not normally of interest to developers.
 *  A sequence node iterator.
 *
 * @author  bwm
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.7 $' Date='$Date: 2005-02-21 12:14:54 $'
 */
public class SeqNodeIteratorImpl extends WrappedIterator implements NodeIterator {
    
    Statement stmt = null;
    Seq       seq;
    int       size;
    int       index=0;
    int       numDeleted=0;
    /** Creates new SeqNodeIteratorImpl 
    */
    public SeqNodeIteratorImpl (Iterator  iterator, 
                                
                                Seq       seq)  {
        super( iterator ); 
        this.seq      = seq;
        this.size     = seq.size();
    }

    public Object next() throws NoSuchElementException {
        stmt = (Statement) super.next();
        index += 1;
        return stmt.getObject();
    }
    
    public RDFNode nextNode() throws NoSuchElementException {
        return (RDFNode) next();
    }
            
    public void remove() throws NoSuchElementException {
        if (stmt == null) throw new NoSuchElementException();
        ((ContainerI)seq).remove(index-numDeleted, stmt.getObject());
        stmt = null;
        numDeleted++;
    }
    
    public void close() {
        super.close();
    }
}