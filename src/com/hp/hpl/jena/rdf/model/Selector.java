/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Selector.java,v 1.3 2003-07-22 07:19:55 chris-dollin Exp $ 
*/
package com.hp.hpl.jena.rdf.model;

/** A Statement selector.
 *
 * <p>Model includes list and query methods which will return all the
 * statements which are selected by a selector object.  This is the interface
 * of such selector objects.
 * 
 * @author bwm, kers
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.3 $' Date='$Date: 2003-07-22 07:19:55 $'
*/

public interface Selector {
    /** Determine whether a Statement should be selected.
     * @param s The statement to be considered.
     * @return true if the statement has been selected.
     */
    boolean test( Statement s );
    
    /**
        Answer true iff this Selector is completely characterised by its subject,
        predicate, and object fields. If so, the <code>test</code> predicate need
        not be called to decide if a statement is acceptable. This allows query engines
        lattitude for optimisation (and our memory-based and RDB-based model
        implementations both exploit this licence).
    */
    boolean isSimple();
      
    /**
        Answer the only subject Resource that this Selector will match, or null if it
        can match more that a single resource.
    */
    Resource getSubject();
    
    /**
        Answer the only predicate Property that this Selector will match, or null
        if it can match more than a single property.
    */
    Property getPredicate();
    
    /**
        Answer the only RDFNode object that this Selector will match, or null if
        it can match more than a single node. 
    */
    RDFNode getObject();
    
}
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
 * Selector.java
 *
 * Created on 28 July 2000, 13:33
 */
