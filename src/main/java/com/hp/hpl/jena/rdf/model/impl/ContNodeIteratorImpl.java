/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ContNodeIteratorImpl.java,v 1.1 2009-06-29 08:55:32 castagna Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

/** An internal class not normally of interest to application developers.
 *  An iterator over the nodes in a container.
 * @author bwm, kers
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1 $' Date='$Date: 2009-06-29 08:55:32 $'
 */
public class ContNodeIteratorImpl extends NiceIterator<RDFNode> implements NodeIterator
    {
    protected Statement recent = null;
    protected final Container cont;
    protected int size;
    protected int index = 0;
    protected int numDeleted = 0;
    protected final List<Integer> moved = new ArrayList<Integer>();
    
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
/*
 *  (c) Copyright 2000, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
