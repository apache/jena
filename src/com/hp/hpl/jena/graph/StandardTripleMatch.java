/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: StandardTripleMatch.java,v 1.1.1.1 2002-12-19 19:13:33 bwm Exp $
*/

package com.hp.hpl.jena.graph;

/**
 * StandardTripleMatch is the basic way
 * of constructing simple queries over Graphs.
 * While it is permitted to use any implementation
 * of TripleMatch,it should be expected that
 * Store's may optimise for this one (and
 * extensions of this class).
 * @author Jeremy Carroll
<br>
    Chris removed mask; it was used nowhere and may get in the way.
    Chris added _matches_ (and used it in various places)
    bwm   was a bit naughty, thought all this matcher class stuff was getting
          in the way so dumped it for simpler code whilst adding the get methods
 */

public class StandardTripleMatch implements TripleMatch {
        
    protected Node subject;
    protected Node predicate;
    protected Node object;
    
    public StandardTripleMatch(Node subject, Node predicate, Node object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }
        
	/**
	 * Stores may optimise to not call this method
	 * when subject!=null.
	 * @see TripleMatch#subject(Node)
	 */
	public boolean subject(Node n) {
            return matches(subject, n);
	}

	/**
	 * Stores may optimise to not call this method
	 * when predicate!=null.
	 * @see TripleMatch#predicate(Node)
	 */
	public boolean predicate(Node n) {
            return matches(predicate, n);
	}

	/**
	 * Stores may optimise to not call this method
	 * when object!=null.
	 * @see TripleMatch#object(Node)
	 */
	public boolean object(Node n) {
            return matches(object, n);
	}

	/**
	 * This method must always be used as a filter by a Store.
	 * @see TripleMatch#triple(Triple)
	 */
	public boolean triple(Triple t) {
		return true;
	}
    
    public boolean matches( Triple t )
        {
            return subject(t.getSubject())     &&
                   predicate(t.getPredicate()) && 
                   object(t.getObject())       &&
                   triple(t);
        }
        
    public String toString()
    	{ return "<stm " + subject + " " + predicate + " " + object + ">"; }
        
        /** If it is known that all triples selected by this match will
         * have a common object, return that node, otherwise return null  */
        public Node getObject() {
            return object;
        }
        
        /** If it is known that all triples selected by this match will
         * have a common predicate, return that node, otherwise return null  */
        public Node getPredicate() {
            return predicate;
        }
        
        /** If it is known that all triples selected by this filter will
         * have a common subject, return that node, otherwise return null  */
        public Node getSubject() {
            return subject;
        }
        
        protected boolean matches(Node matcherNode, Node testNode) {
            return matcherNode == null  || matcherNode.equals(testNode);
        }
    }

/*
    (c) Copyright Hewlett-Packard Company 2002
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
