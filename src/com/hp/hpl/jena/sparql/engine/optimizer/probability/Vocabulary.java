/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.probability;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Constants;

/**
 * Implements the vocabulary used for the specialized RDF index
 * required for the probabilistic index model
 * 
 * @author Markus Stocker
 */

public class Vocabulary 
{
	/** 
	 * The ARQo name space, 
	 * http://jena.hpl.hp.com/ARQo# */
	public static final String URI = Constants.arqOptimizerNS ;
	
	public static final Property size = property("size") ;
	public static final Property ssSize = property("ssSize") ;
	public static final Property soSize = property("soSize") ;
	public static final Property osSize = property("osSize") ;
	public static final Property ooSize = property("ooSize") ;
	public static final Property property = property("property") ;
	public static final Property frequency = property("frequency") ;
	public static final Property version = property("version") ;
	public static final Property resources = property("resources") ;
	public static final Property histogram = property("histogram") ;
	public static final Property lowerBound = property("lowerBound") ;
	public static final Property upperBound = property("upperBound") ;
	public static final Property classes = property("classes") ;
	public static final Property classSize = property("classSize") ;
	public static final Property joiningProperty = property("joiningProperty") ;
	public static final Property joinedProperty = property("joinedProperty") ;
	public static final Property joinType = property("joinType") ;
	public static final Property level = property("level") ;
	public static final Property exclude = property("exclude") ;
 	
	public static final Resource PFI = resource("PFI") ;
	public static final Resource Histogram = resource("Histogram") ;
	public static final Resource HistogramClass = resource("HistogramClass") ;
	public static final Resource Pattern = resource("Pattern") ;
	
	private static final Resource resource(String local)
    { return ResourceFactory.createResource( URI + local ) ; }
	
	private static final Property property(String local)
    { return ResourceFactory.createProperty( URI, local ) ; }
}


/*
 *  (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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