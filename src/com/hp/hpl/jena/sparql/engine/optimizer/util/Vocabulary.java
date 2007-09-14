/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.util;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Constants;

/**
 * Implements the ARQo vocabulary
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class Vocabulary 
{
	/** 
	 * The ARQo name space, 
	 * http://jena.hpl.hp.com/ARQo# */
	public static final String URI = Constants.arqOptimizerNS ;
	/** 
	 * Property for manually specified heuristics, 
	 * http://jena.hpl.hp.com/ARQo#heuristic */
	public static final Property heuristic = property("heuristic") ;
	/** 
	 * Property for the number of triples in an ontology or as result set for a (joined) triple pattern,
	 * http://jena.hpl.hp.com/ARQo#triples */
	public static final Property triples = property("triples") ;
	/**
	 * Property for the number of resources in an ontology, 
	 * http://jena.hpl.hp.com/ARQo#resources */
	public static final Property resources = property("resources") ;
	/**
	 * Property for the join type involved for joined triple patterns, 
	 * http://jena.hpl.hp.com/ARQo#joinType */
	public static final Property joinType = property("joinType") ;
	/**
	 * Property for the joining property URI, 
	 * i.e. the property URI of the triple pattern which joins another pattern, 
	 * http://jena.hpl.hp.com/ARQo#joiningProperty */
	public static final Property joiningProperty = property("joiningProperty") ;
	/**
	 * Property for the joined property URI, 
	 * i.e. the property URI of the triple pattern which is joined by another pattern, 
	 * http://jena.hpl.hp.com/ARQo#joinedProperty */
	public static final Property joinedProperty = property("joinedProperty") ;
	/**
	 * Property for the selectivity of triple patterns or their elements, 
	 * http://jena.hpl.hp.com/ARQo#selectivity */
	public static final Property selectivity = property("selectivity") ;
	/**
	 * Property to specify a set of properties,
	 * http://jena.hpl.hp.com/ARQo#properties */
	public static final Property properties = property("properties") ;
	/**
	 * Property to specify a reference to a histogram, 
	 * http://jena.hpl.hp.com/ARQo#histogram */
	public static final Property histogram = property("histogram") ;
	/**
	 * Property for a property URI, 
	 * http://jena.hpl.hp.com/ARQo#property */
	public static final Property property = property("property") ;
	/**
	 * Property to specify a reference to the classes of a histogram, 
	 * http://jena.hpl.hp.com/ARQo#classes */
	public static final Property classes = property("classes") ;
	/**
	 * Property to specify the class size of the classes of a histogram, 
	 * http://jena.hpl.hp.com/ARQo#classSize */
	public static final Property classSize = property("classSize") ;
	/**
	 * Property to specify the lower bound, either of a histogram or histogram class,
	 * http://jena.hpl.hp.com/ARQo#lowerBound */
	public static final Property lowerBound = property("lowerBound") ;
	/**
	 * Property to specify the upper bound, either of a histogram or histogram class,
	 * http://jena.hpl.hp.com/ARQo#upperBound */
	public static final Property upperBound = property("upperBound") ;
	/**
	 * Property to flag if the BGP is enabled and running,
	 * http://jena.hpl.hp.com/ARQo#isEnabled */
	public static final Property isEnabled = property("isEnabled") ;
	/**
	 * The class of PF resources,
	 * http://jena.hpl.hp.com/ARQo#PF */
	public static final Resource PF = resource("PF") ;
	/**
	 * The class of Property resources, used as reification of ontology properties (instances of rdf:Property), 
	 * http://jena.hpl.hp.com/ARQo#Property */
	public static final Resource Property = resource("Property") ;
	/**
	 * The class of Histogram resources, 
	 * http://jena.hpl.hp.com/ARQo#Histogram */
	public static final Resource Histogram = resource("Histogram") ;
	/**
	 * The class of HistogramClass resources, 
	 * http://jena.hpl.hp.com/ARQo#HistogramClass */
	public static final Resource HistogramClass = resource("HistogramClass") ;
	
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