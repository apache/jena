/*
 * (c) Copyright 2003 Hewlett-Packard Development Company, LP  
 * [see end of file]
 */

package com.hp.hpl.jena.ontology.tidy;
import com.hp.hpl.jena.graph.*;

/**
 * @author jjc
 *
 */
abstract class CNode implements CNodeI, Constants{
	static public CNode create(Node n, AbsChecker eg ) {
		CNode rslt = create1(n,eg);
		if (eg.extraInfo())
		  rslt.minimalityInfo = eg.extraInfo()?new MinimalityInfo(rslt):null;
		return rslt;
		  
	}
	  static private CNode create1(Node n, AbsChecker eg ) {
				// work out what sort of node this is.
	        if (n.isLiteral())
				return new CLit(n, eg);
			if (n.isURI()) {
				int type = Grammar.getBuiltinID(n.getURI());
                switch ( type ) {
                	/*
                case Grammar.owlNothing:
                    ((AbsChecker)eg).addProblem(
                       new SyntaxProblem(
                          "owl:Nothing is not in OWL Lite",
                          n,
                          Levels.Lite
                       )
                    );
					return new CURIref(n,eg,Grammar.classID);
					*/
                	
                case DisallowedVocab:
                
                    ((AbsChecker)eg).addProblem(
                       new SyntaxProblem(
                         "Disallowed vocabulary",
                         n,
                         Levels.DL
                       )
                    );
                    return new CURIref(n,eg);
                case BadOWL:
                    ((AbsChecker)eg).addProblem(
                       new SyntaxProblem(
                         "Unrecognised OWL vocabulary",
                         n,
                         Levels.Warning
                       )
                    );
                    return new CURIref(n,eg);
                case BadRDF:
                    ((AbsChecker)eg).addProblem(
                       new SyntaxProblem(
                         "Unrecognised RDF vocabulary",
                         n,
                         Levels.Warning
                       )
                    );
                    return new CURIref(n,eg);
                case BadXSD:
                    ((AbsChecker)eg).addProblem(
                       new SyntaxProblem(
                         "Illadvised XSD datatype",
                         n,
                         Levels.Warning
                       )
                    );
                    return new CBuiltin(n,eg,Grammar.datatypeID);
                   case Failure:
                    return new CURIref(n, eg);
                    default:
                    break;
                }
                if ( type < Grammar.MAX_SINGLETON_SET)
                   return new CBuiltin(n,eg,type);
                else
                   return new CURIref(n,eg,type);
			}
			return new OneTwoImpl(n, eg);
		}

	final AbsChecker checker;
	final Node node;
	MinimalityInfo minimalityInfo;
	CNode(Node n, AbsChecker eg) {
		checker = eg;
		node = n;
		  
		  
		  
	}
	public Node asNode() {
		return node;
	}
  public  AbsChecker getChecker() {
    	return checker;
    }
  public One asOne() {
		return (One) this;
	}
	public Two asTwo() {
		return (Two) this;
	}
	public Blank asBlank() {
		return (Blank) this;
	}
	
	public void addDisjoint(CNodeI cn){
		addDisjoint1(cn);
		cn.addDisjoint1(this);
	}
	public void addDisjoint1(CNodeI cn){
		getChecker().addDisjoint(asNode(),cn.asNode());
	}
	 void getSeen(Triple a[]) {}
	 void setSeen(Triple a[]) {}

}


/*
 * (c) Copyright 2003 Hewlett-Packard Development Company, LP
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
