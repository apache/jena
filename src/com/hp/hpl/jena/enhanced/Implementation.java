/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Implementation.java,v 1.7 2005-02-21 12:03:40 andy_seaborne Exp $
*/

package com.hp.hpl.jena.enhanced;
import com.hp.hpl.jena.graph.*;

/**
 * <p>
 * Interface defining a generic factory interface for generating enhanced nodes
 * from normal graph nodes. Implementation classes should have a public final 
 * member variable called factory of this type.
 * </p>
 * 
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a> (original code)<br>
 *         <a href="mailto:Chris.Dollin@hp.com">Chris Dollin</a> (original code)<br>
 *         <a href="mailto:Ian.Dickinson@hp.com">Ian Dickinson</a> (refactoring and commentage)
 */
public abstract class Implementation {

     /** 
      * Create a new EnhNode wrapping a Node in the context of an EnhGraph
      * @param node The node to be wrapped
      * @param eg The graph containing the node
      * @return A new enhanced node which wraps node but presents the interface(s)
      *         that this factory encapsulates.
      */
     public abstract EnhNode wrap( Node node,EnhGraph eg );
     
     /**
        true iff wrapping (node, eg) would succeed.
        @param node the node to test for suitability
        @param eg the enhanced graph the node appears in
        @return true iff the node can represent our type in that graph
     */
     public abstract boolean canWrap( Node node, EnhGraph eg );
     
}

/*
	(c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
	All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions
	are met:

	1. Redistributions of source code must retain the above copyright
	   notice, this list of conditions and the following disclaimer.

	2. Redistributions in binary form must reproduce the above copyright
	   notice, this list of conditions and the following disclaimer in the
	   documentation and/or other materials provided with the distribution.

	3. The name of the author may not be used to endorse or promote products
	   derived from this software without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
	IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
	OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
	IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
	INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
	DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
	THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
	THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

