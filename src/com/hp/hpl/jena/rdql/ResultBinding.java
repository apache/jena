/*
 * (c) Copyright 2004, Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.rdql;
import java.util.* ;
import com.hp.hpl.jena.rdf.model.Model;
//import com.hp.hpl.jena.rdf.model.RDFNode;

/** rdql2.ResultBinding
 * 
 * @author Andy Seaborne
 * @version $Id: ResultBinding.java,v 1.14 2004-07-30 17:11:00 andy_seaborne Exp $
 */

public interface ResultBinding
{
    /** Return an iterator of the items in this result binding. 
     *  This iterator has operations to access the current variable name and variable value.
     *  
     * @return ResultBindingIterator
     */
    public ResultBindingIterator iterator();

    /** Return the value of the named variable in this binding.  Object will be
        an RDFNode.
     */
    public Object get(String varName);

    /** Iterator over the names in this binding */ 
    public Iterator names() ;

    // Operations that map results into triples that originally matched.
    // When implementing a ResultBinding, these can be do-nothinbg operations
    // if you don't care about turning variable bidning style results into
    // matching subgraphs.
    
    public Model mergeTriples(Model model) ;
    public Set getTriples() ;
}

/*
 * (c) Copyright 2004 Hewlett-Packard Development Company, LP
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
