/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.util;

import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Vocabulary;

/**
 * The class contains some constants used in ARQo.
 * 
 * @author Markus Stocker
 */

public class Constants 
{
	/** IRI for ARQo */  
    public static final String arqOptimizerIRI = "http://jena.hpl.hp.com/#arqo" ;
    /** Root of ARQo-defined parameter names */  
    public static final String arqOptimizerNS = "http://jena.hpl.hp.com/ARQo#" ;
	/** The basic pattern join name space */
	public static final String joinTypeNS  = "http://jena.hpl.hp.com/ARQo/join#" ;
	/** The localhost name space */
	public static final String localhostNS = "http://localhost/#" ;
	/** Check flag if the BGP optimizer is enabled */
	public static final Symbol isEnabled = ARQConstants.allocSymbol(Vocabulary.isEnabled.getURI()) ;
	/** The Probabilistic Framework Symbol used for the ARQ context */
	public static final Symbol PF = ARQConstants.allocSymbol(Vocabulary.PF.getURI()) ;
	/** The heuristic Symbol used for the ARQ context */
	public static final Symbol heuristic = ARQConstants.allocSymbol(Vocabulary.heuristic.getURI()) ;
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