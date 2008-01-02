/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.probability.impl;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * The class represents a pattern of two triple patterns, i.e. 
 * an element of the specialized index which represents the
 * result set size of the corresponding pattern.
 * 
 * @author Markus Stocker
 */

public class Pattern 
{
	/* The property of the first triple pattern, i.e. the one which joins the other */
	private Property joiningP ;
	/* The property of the second triple pattern, i.e. the one which is joined */
	private Property joinedP ;
	/* The join type, either SS, SO, OS or OO, bound or unbound */
	private Resource joinT ;
	
	/**
	 * Constructor
	 * 
	 * @param joiningP
	 * @param joinedP
	 * @param joinT
	 */
 	public Pattern(Property joiningP, Property joinedP, Resource joinT)
	{
 		this.joiningP = joiningP ;
 		this.joinedP = joinedP ;
 		this.joinT = joinT ;
	}
 	
 	/**
 	 * Return the joining property of this index pattern,
 	 * i.e. the property of the first triple pattern
 	 * 
 	 * @return Property
 	 */
 	public Property getJoiningProperty()
 	{ return joiningP ; }
 	
 	/**
 	 * Return the joined property of this index pattern,
 	 * i.e. the property of the second triple pattern
 	 * 
 	 * @return Property
 	 */
 	public Property getJoinedProperty()
 	{ return joinedP ; }
 	
 	/**
 	 * Return the join type of this index pattern,
 	 * a resource holding the URI for SS, SO, OS, OO 
 	 * (bound or unbound)
 	 * 
 	 * @return Resource
 	 */
 	public Resource getJoinType()
 	{ return joinT ; }
 	
 	/**
 	 * Returns a string representation of the pattern
 	 * 
 	 * @return String
 	 */
 	public String toString()
 	{ return "[" + joiningP.getURI() + " " + joinedP.getURI() + " " + joinT.getURI() + "]" ; }
 	
 	/**
 	 * Override the equals method for objects
 	 * 
 	 * @param o
 	 */
 	public boolean equals(Object o)
 	{
 		if (! (o instanceof Pattern))
 			throw new ClassCastException("Object is not an instance of Pattern") ;
 		
 		Pattern p = (Pattern)o ;

  		return (p.joiningP.equals(joiningP)
 			    && p.joinedP.equals(joinedP)
 			    && p.joinT.equals(joinT)) ;
 	}
 	
 	/**
 	 * Return the hash code of the pattern, 
 	 * which is the hash code of the string representation
 	 * 
 	 * @return int
 	 */
 	public int hashCode()
 	{ return toString().hashCode() ; }
}


/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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