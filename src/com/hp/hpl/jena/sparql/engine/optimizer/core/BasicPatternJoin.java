/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.core;

import java.util.*;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Constants;

/**
 * The class implements a number of operation applicable on joined
 * triple patterns (BGP)
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class BasicPatternJoin 
{
	/** Bound subject - subject join */
	public static final String bSS = Constants.joinTypeNS + "bSS" ;
	/** Bound subject - predicate join */
	public static final String bSP = Constants.joinTypeNS + "bSP" ;
	/** Bound subject - object join */
	public static final String bSO = Constants.joinTypeNS + "bSO" ;
	/** Bound predicate - subject join */
	public static final String bPS = Constants.joinTypeNS + "bPS" ;
	/** Bound predicate - predicate join */
	public static final String bPP = Constants.joinTypeNS + "bPP" ;
	/** Bound predicate - object join */
	public static final String bPO = Constants.joinTypeNS + "bPO" ;
	/** Bound object - subject join */
	public static final String bOS = Constants.joinTypeNS + "bOS" ;
	/** Bound object - predicate join */
	public static final String bOP = Constants.joinTypeNS + "bOP" ;
	/** Bound object - object join */
	public static final String bOO = Constants.joinTypeNS + "bOO" ;
	/** Unbound subject - subject join */
	public static final String uSS = Constants.joinTypeNS + "uSS" ;
	/** Unbound subject - subject join */
	public static final String uSP = Constants.joinTypeNS + "uSP" ;
	/** Unbound subject - subject join */
	public static final String uSO = Constants.joinTypeNS + "uSO" ;
	/** Unbound subject - subject join */
	public static final String uPS = Constants.joinTypeNS + "uPS" ;
	/** Unbound subject - subject join */
	public static final String uPP = Constants.joinTypeNS + "uPP" ;
	/** Unbound subject - subject join */
	public static final String uPO = Constants.joinTypeNS + "uPO" ;
	/** Unbound subject - subject join */
	public static final String uOS = Constants.joinTypeNS + "uOS" ;
	/** Unbound subject - subject join */
	public static final String uOP = Constants.joinTypeNS + "uOP" ;
	/** Unbound subject - subject join */
	public static final String uOO = Constants.joinTypeNS + "uOO" ;
	/** Generic subject - subject join, either bound or unbound */
	public static final String SS = Constants.joinTypeNS + "SS" ;
	/** Generic subject - predicate join, either bound or unbound */
	public static final String SP = Constants.joinTypeNS + "SP" ;
	/** Generic subject - object join, either bound or unbound */
	public static final String SO = Constants.joinTypeNS + "SO" ;
	/** Generic predicate - subject join, either bound or unbound */
	public static final String PS = Constants.joinTypeNS + "PS" ;
	/** Generic predicate - predicate join, either bound or unbound */
	public static final String PP = Constants.joinTypeNS + "PP" ;
	/** Generic predicate - object join, either bound or unbound */
	public static final String PO = Constants.joinTypeNS + "PO" ;
	/** Generic object - subject join, either bound or unbound */
	public static final String OS = Constants.joinTypeNS + "OS" ;
	/** Generic object - predicate join, either bound or unbound */
	public static final String OP = Constants.joinTypeNS + "OP" ;
	/** Generic object - object join, either bound or unbound */
	public static final String OO = Constants.joinTypeNS + "OO" ;
	
 	/**
	 * The method returns true if two GraphNodes are joined, 
	 * because of matching S/P/O. The method returns true,
	 * if one of the nine possible combinations matches.
	 * 
	 * @param node1
	 * @param node2
	 * @return boolean
	 */
	public static boolean isJoined(GraphNode node1, GraphNode node2)
	{
		Triple triple1 = node1.triple() ;
		Triple triple2 = node2.triple() ;
		
		return isJoined(triple1, triple2) ;
	}
	
	/**
	 * The method returns true if two Triple are joined, 
	 * because of matching S/P/O. The method returns true,
	 * if one of the nine possible combinations matches.
	 * 
	 * @param triple1
	 * @param triple2
	 * @return boolean
	 */
	public static boolean isJoined(Triple triple1, Triple triple2)
	{
		if (triple1.subjectMatches(triple2.getSubject()))
			return true ;
		if (triple1.subjectMatches(triple2.getPredicate()))
			return true ;
		if (triple1.subjectMatches(triple2.getObject()))
			return true ;

		if (triple1.predicateMatches(triple2.getSubject()))
			return true ;
		if (triple1.predicateMatches(triple2.getPredicate()))
			return true ;
		if (triple1.predicateMatches(triple2.getObject()))
			return true ;
		
		if (triple1.objectMatches(triple2.getSubject()))
			return true ;
		if (triple1.objectMatches(triple2.getPredicate()))
			return true ;
		if (triple1.objectMatches(triple2.getObject()))
			return true ;
		
		return false ;
	}
	
	/**
	 * The method returns a list of join types defined
	 * for two triples. The specific types are identified,
	 * i.e. the method differentiates between bound and unbound
	 * joins.
	 * 
	 * @param triple1
	 * @param triple2
	 * @return List<String>
	 */
	public static List specificTypes(Triple triple1, Triple triple2)
	{
		List types = new ArrayList() ; // List<String>
		
		if (triple1.getSubject().isVariable())
		{
			if (triple1.subjectMatches(triple2.getSubject()))
				types.add(uSS) ;
			if (triple1.subjectMatches(triple2.getPredicate()))
				types.add(uSP) ;
			if (triple1.subjectMatches(triple2.getObject()))
				types.add(uSO) ;
		}
		else
		{
			if (triple1.subjectMatches(triple2.getSubject()))
				types.add(bSS) ;
			if (triple1.subjectMatches(triple2.getPredicate()))
				types.add(bSP) ;
			if (triple1.subjectMatches(triple2.getObject()))
				types.add(bSO) ;
		}

		if (triple1.getPredicate().isVariable())
		{
			if (triple1.predicateMatches(triple2.getSubject()))
				types.add(uPS) ;
			if (triple1.predicateMatches(triple2.getPredicate()))
				types.add(uPP);
			if (triple1.predicateMatches(triple2.getObject()))
				types.add(uPO) ;
		}
		else
		{
			if (triple1.predicateMatches(triple2.getSubject()))
				types.add(bPS) ;
			if (triple1.predicateMatches(triple2.getPredicate()))
				types.add(bPP);
			if (triple1.predicateMatches(triple2.getObject()))
				types.add(bPO) ;
		}

		if (triple1.getObject().isVariable())
		{
			if (triple1.objectMatches(triple2.getSubject()))
				types.add(uOS);
			if (triple1.objectMatches(triple2.getPredicate()))
				types.add(uOP) ;
			if (triple1.objectMatches(triple2.getObject()))
				types.add(uOO) ;
		}
		else
		{
			if (triple1.objectMatches(triple2.getSubject()))
				types.add(bOS);
			if (triple1.objectMatches(triple2.getPredicate()))
				types.add(bOP) ;
			if (triple1.objectMatches(triple2.getObject()))
				types.add(bOO) ;
		}
		
		return types ;
	}

	/**
	 * The method returns the first identified join type between the triples.
	 * The specific type is identified, i.e. the method differentiates between 
	 * bound and unbound joins. If the triple patterns are not joined, the
	 * method returns null.
	 * 
	 * @param triple1
	 * @param triple2
	 * @return String
	 */
	public static String specificType(Triple triple1, Triple triple2)
	{
		List types = specificTypes(triple1, triple2) ; // List<String>
		
		if (types.size() > 0)
			return (String)types.get(0) ;
		
		return null ;
	}
	
	/**
	 * The method returns generic types, i.e. it doesn't consider
	 * if the joins are bound or unbound.
	 * 
	 * @param triple1
	 * @param triple2
	 * @return List<String>
	 */
	public static List genericTypes(Triple triple1, Triple triple2)
	{
		List types = new ArrayList() ; // List<String>
		
		if (triple1.subjectMatches(triple2.getSubject()))
			types.add(SS) ;
		if (triple1.subjectMatches(triple2.getPredicate()))
			types.add(SP) ;
		if (triple1.subjectMatches(triple2.getObject()))
			types.add(SO) ;

		if (triple1.predicateMatches(triple2.getSubject()))
			types.add(PS) ;
		if (triple1.predicateMatches(triple2.getPredicate()))
			types.add(PP) ;
		if (triple1.predicateMatches(triple2.getObject()))
			types.add(PO) ;
		
		if (triple1.objectMatches(triple2.getSubject()))
			types.add(OS) ;
		if (triple1.objectMatches(triple2.getPredicate()))
			types.add(OP) ;
		if (triple1.objectMatches(triple2.getObject()))
			types.add(OO) ;
		
		return types ;
	}
	
	/**
	 * The method returns the first identified generic join type,
	 * i.e. it doesn't consider if the joins are bound or unbound.
	 * If the patterns are not joine, the method returns null.
	 * 
	 * @param triple1
	 * @param triple2
	 * @return String
	 */
	public static String genericType(Triple triple1, Triple triple2)
	{
		List types = genericTypes(triple1, triple2) ; // List<String>
		
		if (types.size() > 0)
			return (String)types.get(0) ;
		
		return null ;
	}
	
	/**
	 * Return the number of variables contained in the corresponding
	 * triple patterns.
	 * 
	 * @param triple1
	 * @param triple2
	 * @return int
	 */
	public static int numOfVars(Triple triple1, Triple triple2)
	{
		int numOfVars = 1 ;
		
		if (triple1.getSubject().isVariable())
			numOfVars++ ;
		if (triple1.getPredicate().isVariable())
			numOfVars++ ;
		if (triple1.getObject().isVariable())
			numOfVars++ ;
		if (triple2.getSubject().isVariable())
			numOfVars++ ;
		if (triple2.getPredicate().isVariable())
			numOfVars++ ;
		if (triple2.getObject().isVariable())
			numOfVars++ ;
		
		return numOfVars ;
	}
	
	/**
	 * Check if the triples contains variables only
	 * 
	 * @param triple1
	 * @param triple2
	 * @return boolean
	 */
	public static boolean varsOnly(Triple triple1, Triple triple2)
	{
		return (numOfVars(triple1, triple2) == 6) ;
	}
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