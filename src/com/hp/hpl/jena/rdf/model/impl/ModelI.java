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
 * ModelI.java
 *
 * Created on 15 August 2000, 19:25
 */

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;

/** An internal interface not normally of interest to application developers.
 *
 * @author  bwm
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.2 $' Date='$Date: 2003-04-14 10:57:26 $'
 */
public interface ModelI {
    
    
    
/**
 * @param cont
 * @param f
 * @throws RDFException
 * @return iterator over container members
 */
    public NodeIterator listContainerMembers(Container cont,
                                             NodeIteratorFactory f)
                                                    throws RDFException;
    
/**
 * @param cont
 * @throws RDFException
 * @return size of container
 */
    public int containerSize(Container cont) throws RDFException;
    
/**
 * @param cont
 * @param n
 * @throws RDFException
 * @return index of n in container
 */
    public int containerIndexOf(Container cont, RDFNode n) throws RDFException;
    
/**
 * @param cont
 * @param n
 * @throws RDFException
 * @return true if containter contains n, false otherwise
 */
    public boolean containerContains(Container cont, RDFNode n) 
                                        throws RDFException;

}
