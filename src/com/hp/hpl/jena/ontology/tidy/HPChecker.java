/*
   (c) Copyright 2003 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: HPChecker.java,v 1.1 2004-01-11 21:20:29 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy;

import java.util.Iterator;

import com.hp.hpl.jena.rdf.arp.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.tidy.impl.CheckerImpl;
import com.hp.hpl.jena.datatypes.*;
import java.util.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import org.xml.sax.*;
import java.io.*;
import java.net.URL;

/**
 * 
 * This class is a high performance streaming
 * implementation of the OWL Syntax Checker.
 * 
 * The three methods {@link #getProblems()}
 * {@link #getErrors()} and {@link #getSubLanguage()}
 * can all be used repeatedly and at any point. They
 * report on what has been added so far.
 * When constructing a checker, you must choose whether
 * to record errors and problems concerning non-OWL Lite
 * constructs, or only concerning non-OWL DL constructs.
 * For either choice {@link #getSubLanguage()} functions
 * correctly (i.e. the grammar used is identical). However,
 * if the Checker has been constructed with the liteflag as false,
 * it is not possible to access a rationale for an ontology
 * being in OWL DL rather than OWL Lite.
 * 
 * 
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
public class HPChecker extends CheckerImpl {
	
	public String[] getLoaded() {
		String rslt[] = new String[imported.size()];
		int i = 0;
		Iterator it = imported.iterator();
		while (it.hasNext()) {
			rslt[i] = r.redirect((String)it.next());
			i++;
		}
		return rslt;
	}
	public int getTripleCount() {
		int x = cnt;
		return tripleCnt;
	}
	private Redirect r = new Redirect();
	public Redirect getRedirect() {
		return r;
	}
	private static String OWLIMPORTS = OWL.imports.getURI();


/**
 * Create a new checker - indicate whether error reports
 * are wanted for non-OWL Lite constructions
 * or only non-OWL DL constructions.
 * @param liteFlag If true 
 *     {@link #getErrors()} and 
 *     {@link #getProblems()} will indicate any OWL DL or OWL Full construction.
 */
	public HPChecker(boolean liteFlag) {
		super(liteFlag);
		this.setOptimizeMemory(true);
	}
	/**
	 * Answer an Iterator over {@link SyntaxProblem}'s which
	 * are errors found by the syntax checker.
	 * If the liteFlag was set in the constructor then
	 * all OWL DL and OWL Full constructs are errors.
	 * If the liteFlag was not set, then only OWL Full constructs
	 * are errors.
	 */
	public Iterator getErrors() {
		return super.getErrors();
	}
	/**
	 * Answer an Iterator over {@link SyntaxProblem}'s which
	 * are errors or warnings found by the syntax checker.
	 */
	public Iterator getProblems() {
		return super.getProblems();
	}
	
	private Set imported;
	/**
	 * Include an ontology and its imports
	 * in the check.
	 * @param url Load the ontology from this URL.
	 */
	synchronized public void load(String url) {
		load(url,true);
	}
	static	Node convert(ALiteral lit)  {
			String dtURI = lit.getDatatypeURI();
			if (dtURI == null)
				return Node.createLiteral(lit.toString(), lit.getLang(), false);
			else {
				if (lit.isWellFormedXML()) {
					return Node.createLiteral(lit.toString(),null, true);
				} else {
					RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(dtURI);
        
					return Node.createLiteral(lit.toString(),null,dt);
				}
			}
		}
    static int cnt = 0;
		static Node convert(AResource r){
			if (r.isAnonymous()) {
				String id = r.getAnonymousID();
				Node rr = (Node) r.getUserData();
				if (rr == null) {
					rr = Node.createAnon(new AnonId(""+cnt++));
					r.setUserData(rr);
				}
				return rr;
			} else {
				return Node.createURI(r.getURI());
			}
		}
		static Triple convert(AResource s,AResource p, AResource o){
			return new Triple(convert(s),convert(p),convert(o));
		}
		static Triple convert(AResource s,AResource p, ALiteral o){
			return new Triple(convert(s),convert(p),convert(o));
		}
	private StatementHandler sh = new StatementHandler(){

		public void statement(AResource subj, AResource pred, AResource obj) {
			tripleCnt++;
			add(convert(subj,pred,obj));
			if ( pred.getURI().equals(OWLIMPORTS))
			   load(obj.getURI(),false);
		}

		public void statement(AResource subj, AResource pred, ALiteral lit) {
			tripleCnt++;
			add(convert(subj,pred,lit));
		}
		
	};

	private ExtendedHandler eh = new ExtendedHandler(){

		public void endBNodeScope(AResource bnode) {
		 endBNode((Node)bnode.getUserData());
			
		}

		public boolean discardNodesWithNodeID() {
			return false;
		}

		public void startRDF() {
		}

		public void endRDF() {
		}
		
	};
	
	private void load(String url, boolean top) {
			ARP arp = new ARP();
			if (top)
			  imported = new HashSet();
			if (imported.contains(url))
			  return;
		  imported.add(url);
		  String loadURL = r.redirect(url);
		  arp.setStatementHandler(sh);
		  
		  arp.setExtendedHandler(eh);
		  try {
		  arp.load(new URL(loadURL).openStream(),url);

		  }
		  catch (Exception e) {
		  	e.printStackTrace();
		  	System.err.println("Exception: "+ e.getMessage());
		  	System.err.println("Exception handling not yet implemented");
		  }
	}
	/**
	 * Which subLanguage is this document in.
	 * @return "Lite", "DL" or "Full".
	 */
	public String getSubLanguage() {
		return super.getSubLanguage();
	}

	/**
	 * If set, (the default) the syntax checker will conserve space
	 * at the expense of the quality of the
	 * {@link SyntaxProblem#longDescription} of errors.
	 * @param big
	 */
	public void setOptimizeMemory(boolean big) {
		super.setOptimizeMemory(big);
		
	}
	private int tripleCnt = 0;
}
/*
   (c) Copyright 2003 Hewlett-Packard Development Company, LP
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