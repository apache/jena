/*
  (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ClosableIterator.java,v 1.6 2003-08-27 13:07:54 andy_seaborne Exp $
*/

package com.hp.hpl.jena.util.iterator;

import java.util.Iterator;

/** 
    An iterator which should be closed after use. Some iterators take up resources which 
    should be free'd as soon as possible, eg large structures which can be discarded
    early, or external resources such as database cursors.
<p>
    Users of ClosableIterators (and thus of ExtendedIterator) should close the iterator
    when they are done with it, whether because they have found a desired element
    or because they have reached the end. If they do not, resources may leak or be
    reclaimed unpredictably or much later than convenient.
<p>
    Implementors are encouraged to dispose of resources as soon as is convenient.
 
    @author bwm
    @version $Id: ClosableIterator.java,v 1.6 2003-08-27 13:07:54 andy_seaborne Exp $
 */

public interface ClosableIterator extends Iterator 
    {
    /** 
        Close the iterator. Other oeprations on this iterator may now throw an exception. A
        ClosableIterator may be closed as many times as desired - the subsequent
        calls do nothing.
    */
    public void close();
    }

/*
 *  (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
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
 * StmtIterator.java
 *
 * Created on 28 July 2000, 13:44
 */