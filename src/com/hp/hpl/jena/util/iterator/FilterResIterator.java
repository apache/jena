/*
 *  (c) Copyright Hewlett-Packard Company 1999-2001 
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
 * $Id: FilterResIterator.java,v 1.1.1.1 2002-12-19 19:21:14 bwm Exp $
 *
 */

package com.hp.hpl.jena.util.iterator;

import java.util.*;
import com.hp.hpl.jena.rdf.model.*;

/** A sub iterator over Resource's.
   See FilterIterator and ResIterator.
 * @author jjc
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1.1.1 $' Date='$Date: 2002-12-19 19:21:14 $'
 * @see com.hp.hpl.jena.rdf.model.ResIterator
 * @see com.hp.hpl.jena.xmloutput.FilterIterator
 */

public class FilterResIterator implements ResIterator {
    Filter f;
    ResIterator enum;
    Resource current;
    public FilterResIterator(Filter fl, ResIterator e) {
        enum = e;
        f = fl;
        current = null;
    }
    synchronized public boolean hasNext() throws RDFException {
        if (current != null)
            return true;
        while (enum.hasNext()) {
            current = enum.next();
            try {
                if (f.accept(current))
                    return true;
            } catch (RuntimeRDFException rdfe) {
                throw rdfe.getUnderlyingException();
            }
        }
        current = null;
        return false;
    }
    synchronized public Resource next() throws RDFException {
        if (hasNext()) {
            Resource r = current;
            current = null;
            return r;
        }
        throw new NoSuchElementException();
    }
    /**
     * This is more restrictive than the usual <CODE>remove()</CODE>
     * in that it must be called after a call to <CODE>next()</CODE> and before
     * any calls to either <CODE>next()</CODE> or <CODE>hasNext()</CODE>.
    */
    synchronized public void remove() throws RDFException {
        if (current != null)
            throw new NoSuchElementException();
        enum.remove();

    }
    synchronized public void close() throws RDFException {
        enum.close();
        current = null;
    }
}
