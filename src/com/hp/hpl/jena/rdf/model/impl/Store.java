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
 * Store.java
 *
 * Created on 11 January 2001, 10:09
 */

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import java.util.Iterator;

/** Interface to a triple store.
 *
 * @author  bwm
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1.1.1 $' Date='$Date: 2002-12-19 19:18:40 $'
 */
public interface Store {
    
    
    
    
 int size();   
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

/** Add a statement to the store
 * @param s the statement to add
 */    
   public void add(Statement s) throws RDFException;
    
/** remove a statement from the store
 * @param s the statement to remove
 */   
    public void remove(Statement s) throws RDFException;
    
    
    
    

/** return an iterator over all the statements which match the given
 *  subject predicate and object.
 *
 *<p>The store is searched for all statements which match the given
 *   subject predicate and object.  A null arguement matches anything.
 *   </p>
 *
 * @param subject the subject of the statements to be returned or null
 * @param predicate the predicate of the statments to be returned or null
 * @param object the object of the statements to be returned or null
 * @return an iterator over all statements in the store which match the
 * given subject predicate and object
 */   
    public Iterator list(Resource subject, Property predicate, RDFNode object)
      throws RDFException;
    
    
    
    
    
    
/** Close the store and free up resources held.
 *
 *  <p>Not all implementations of Store require this method to be called.  But
 *     some do, so in general its best to call it when done with the object,
 *     rather than leave it to the finalizer.</p>
 */        
    public void close();
}
