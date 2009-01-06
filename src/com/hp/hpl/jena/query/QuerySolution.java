/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query;
import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * A single answer from a SELECT query.
 * 
 * @author Andy Seaborne
 */

public interface QuerySolution
{
    /** Return the value of the named variable in this binding.
     *  A return of null indicates that the variable is not present in this solution.
     *  @param varName
     *  @return RDFNode
     */
    public RDFNode get(String varName);

    /** Return the value of the named variable in this binding, casting to a Resource.
     *  A return of null indicates that the variable is not present in this solution.
     *  An exception indicates it was present but not a resource.
     *  @param varName
     *  @return Resource
     */
    public Resource getResource(String varName);

    /** Return the value of the named variable in this binding, casting to a Literal.
     *  A return of null indicates that the variable is not present in this solution.
     *  An exception indicates it was present but not a literal.
     *  @param varName
     *  @return Resource
     */
    public Literal getLiteral(String varName);

    
    /** Return true if the named variable is in this binding */
    public boolean contains(String varName);

    /** Iterate over the variable names (strings) in this QuerySolution.
     * @return Iterator of strings
     */ 
    public Iterator<String> varNames() ;
    
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
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
 *
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
 */