/*
 * (c) Copyright 2003 Hewlett-Packard Development Company, LP  
 * [see end of file]
 */

package com.hp.hpl.jena.ontology.tidy;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.util.iterator.*;

/**
 * @author jjc
 *
 */
abstract class CNode extends EnhNode implements CNodeI {
	final static public Implementation factory = new Implementation() {
        public boolean canWrap( Node n, EnhGraph eg )
            { return true; }
        public EnhNode wrap(Node n, EnhGraph eg) {
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
                	
                case Grammar.DisallowedVocab:
                
                    ((AbsChecker)eg).addProblem(
                       new SyntaxProblem(
                         "Disallowed vocabulary",
                         n,
                         Levels.DL
                       )
                    );
                    return new CURIref(n,eg);
                case Grammar.BadOWL:
                    ((AbsChecker)eg).addProblem(
                       new SyntaxProblem(
                         "Unrecognised OWL vocabulary",
                         n,
                         Levels.Warning
                       )
                    );
                    return new CURIref(n,eg);
                case Grammar.BadRDF:
                    ((AbsChecker)eg).addProblem(
                       new SyntaxProblem(
                         "Unrecognised RDF vocabulary",
                         n,
                         Levels.Warning
                       )
                    );
                    return new CURIref(n,eg);
                case Grammar.BadXSD:
                    ((AbsChecker)eg).addProblem(
                       new SyntaxProblem(
                         "Illadvised XSD datatype",
                         n,
                         Levels.Warning
                       )
                    );
                    return new CBuiltin(n,eg,Grammar.datatypeID);
                   case Grammar.Failure:
                    return new CURIref(n, eg);
                    default:
                    break;
                }
                if ( type < Grammar.MAX_SINGLETON_SET)
                   return new CBuiltin(n,eg,type);
                else
                   return new CURIref(n,eg,type);
			}
			return new CBlank(n, eg);
		}
	};
	CNode(Node n, EnhGraph eg) {
		super(n, eg);
	}
    AbsChecker getChecker() {
    	return (AbsChecker)getGraph();
    }
	Node getAttribute(Node property) {
		Graph g = getGraph().asGraph();
		ClosableIterator it = g.find(asNode(), property, null);
		Node rslt = null;
		try {
			if (it.hasNext()) {
				rslt = ((Triple) it.next()).getObject();
				if (it.hasNext()) {
					throw new SyntaxException(
						"Internal error: <"
							+ property.getURI()
							+ "> may have at most one value.");
				}
			}
		} finally {
			it.close();
		}
		return rslt;
	}
	void setAttribute(Node property, Node obj) {
		Graph g = getGraph().asGraph();
		ClosableIterator it = g.find(asNode(), property, null);
		Triple old = null;
		try {
			if (it.hasNext()) {
				old = (Triple) it.next();
				if (it.hasNext()) {
					throw new SyntaxException(
						"Internal error: <"
							+ property.getURI()
							+ "> may have at most one value.");
				}
			}
		} finally {
			it.close();
		}
		if (old != null)
			g.delete(old);
		g.add(new Triple(asNode(), property, obj));
	}

	int getIntAttribute(Node property, int def) {
		Node obj = getAttribute(property);
		if (obj != null) {
			return ((Number) obj.getLiteral().getValue()).intValue();
		}
		return def;
	}
	void setIntAttribute(Node p, int v) {
		// looks horribly inefficient :(
		setAttribute(p, Node.createLiteral(new LiteralLabel(new Integer(v))));
	}
	void incrAttribute(Node property, int diff) {
		int old = getIntAttribute(property, 0);
		setIntAttribute(property, old + diff);
	}
	public One asOne() {
		return (One) viewAs(One.class);
	}
	public Two asTwo() {
		return (Two) viewAs(Two.class);
	}
	public Blank asBlank() {
		return (Blank) viewAs(Blank.class);
	}
	
	public void addDisjoint(CNodeI cn){
		addDisjoint1(cn);
		cn.addDisjoint1(this);
	}
	public void addDisjoint1(CNodeI cn){
		// TODO
	}

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
