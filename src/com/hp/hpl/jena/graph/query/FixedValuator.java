/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: FixedValuator.java,v 1.1 2004-07-20 19:39:09 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

/**
 	A FixedValuator is a Valuator that delivers a constant value
 	(supplied when it is constructed).
 	
 	@author hedgehog
 */
public class FixedValuator implements ObjectValuator
	{
	private Object value;
	
	/**
	 	Initialise this FixedValuator with a specific value
	*/
	public FixedValuator( Object value )
	    { this.value = value; }
	
	/**
	 	Answer this FixedValuator's value, which must be a Boolean
	 	object, as a <code>boolean</code>. The index values
	 	are irrelevant.
	*/
	public boolean evalBool( IndexValues iv )
	    { return ((Boolean) eval( iv )).booleanValue(); }
	        
	/**
	 	Answer this FixedValuator's value, as supplied when it was constructed.
	*/
	public Object eval( IndexValues iv )
	    { return value; }
	}

/*
	(c) Copyright 2004, Hewlett-Packard Development Company, LP
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