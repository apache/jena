/*
 * (c) Copyright 2003 Hewlett-Packard Development Company, LP  
 * [see end of file]
 */

package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdql.*;
import com.hp.hpl.jena.shared.BrokenException;

import java.util.*;

/**
 * This class implements the OWL Syntax Checker from the OWL Test Cases WD.
 * <h4>Implementation issues concerning
 * <a href="http://www.w3.org/TR/2003/WD-owl-semantics-20030331/">OWL Semantics
 * &mp; Abstract Syntax</a></h4>
 * <ul>
 * <li>S&AS does not seem to say that the object of an annotation triple
 * must be one of the IDs, it seems to allow an arbitrary URIref.
 * We require it to have some type, reflecting the <a href=
 *  "http://lists.w3.org/Archives/Public/www-webont-wg/2003Mar/0066.html" 
 * >decision on OWL DL
 * Syntax</a>.</li>
 * <li>The <a href= 
 * "http://lists.w3.org/Archives/Public/www-webont-wg/2003Apr/0003.html" 
 * >Hamiltonian path</a> constraint on owl:equivalentClass is not implemented,
 * instead this implements <a href= 
 * "http://lists.w3.org/Archives/Public/www-webont-wg/2003Apr/0004.html"
 * >fix 2</a>.</li>
 * <li>The rule of S&AS seems to be that 
 * <pre>owl:imports rdf:type owl:OntologyProperty .
 * </pre> is permitted only if there is a triple with 
 * owl:imports as predicate.
 * This is implemented</li>
 * <li>The phrase "In keeping with their definition in RDF, rdfs:label and rdfs:
 * comment can only be used with data literals." is implemented here but missing
 * from OWL DL as triples.</li>
 * <li>Unclear what the correct treatment of XMLLiteral is.
 * I permit it, and do not require a declaration.</li>
 * </ul>
 * 
 * 
 * @author jjc
 *
 */
public class Checker extends AbsChecker {
	private GraphMaker gf;
	private Vector monotoneProblems = new Vector();
	private Vector warnings = new Vector();

	private Vector nonMonotoneProblems = null;
	private int nonMonotoneLevel;

	public Iterator getErrors() {
		nonMonotoneLevel = Levels.Lite;
		if ( monotoneProblems.size() > 0 )
		  return monotoneProblems.iterator();
		snapCheck();
		return nonMonotoneProblems.iterator();
	}
	public Iterator getProblems() {
		return new ConcatenatedIterator(
		   getErrors(),
		   warnings.iterator()
		);
	}

	private void snapCheck() {
		if (nonMonotoneProblems == null) {
			nonMonotoneProblems = new Vector();
			nonMonotoneLevel = Levels.Lite;
			Model m = ModelFactory.createModelForGraph(asGraph());
		
		/*
		 * Easy problems to check.
		 */	
			check(CategorySet.untypedSets,new NodeAction() {
				public void apply(Node n){
					nonMonProblem("Untyped node",n);
				}
			}, m);
			check(CategorySet.orphanSets,new NodeAction() {
			public void apply(Node n){
				nonMonProblem("Orphaned rdf:List or owl:OntologyProperty node",n);
			}
		}, m);
		if ( wantLite )
		check(CategorySet.dlOrphanSets,new NodeAction() {
		public void apply(Node n){
			nonMonProblem("Orphaned blank owl:Class or owl:Restriction is in OWL DL",n,Levels.Lite);
		}
	}, m);
		/*
		 * Slightly harder
		 */
		 check(CategorySet.structuredOne,new NodeAction() {
			 public void apply(Node n){
			 	if (getCNode(n).asOne().incomplete())
				  nonMonProblem("Incomplete blank owl:Class or owl:AllDifferent",n);
			 }
		 }, m);
		 check(CategorySet.structuredTwo,new NodeAction() {
			 public void apply(Node n){
				if (getCNode(n).asTwo().incomplete())
				  nonMonProblem("Incomplete rdf:List or owl:Restriction",n);
			 }
		 }, m);
		 
		 /*
		  * Getting harder ...
		  * We could optimise by first find non-cyclic orphaned unnamed 
		  * individuals,
		  * mark those as non-cyclic - but that's too much like hardwork.
		  * 
		  * We check the potentially cyclic
		  * nodes.
		  */
		  Iterator it =
		  asGraph().find(Node.ANY,Vocab.cyclicState,Node.ANY);
		  List list = new Vector();
		  while ( it.hasNext() )
		    list.add(it.next());
		  asGraph().getBulkUpdateHandler().delete(list);
		  check(CategorySet.cyclicSets, new NodeAction() {
			public void apply(Node n){
			// If this is a description then it's busted.
			  CNodeI cn = getCNode(n);
			  if (
			  Q.intersect(Grammar.descriptionsX,CategorySet.getSet(cn.getCategories()))
			 || Q.intersect(Grammar.restrictionsX,CategorySet.getSet(cn.getCategories())) ) {

				nonMonProblem("Cyclic blank owl:Class or owl:Restriction",n);
			 }
			 
			 if (
			 Q.intersect(Grammar.listsX,CategorySet.getSet(cn.getCategories()))
			 ) {

			   nonMonProblem("Cyclic rdf:List",n);
			}
			
			// If this is an individual then we have to check it.
			
			if ( 
			Q.member(Grammar.unnamedIndividual,CategorySet.getSet(cn.getCategories()))
			) {
              isCyclic((CBlank)cn,n);
		   }
			}
		  }, m);
		 
			/*
			* Hardest (well exlcuding Hamiltonian paths)
			* Check the disjointUnion blank nodes
			*/
		    
			  // TODO disjointUnion blank nodes
			
			
		}
	}
	
	private boolean isCyclic( CBlank blk, Node n ){
		int st = blk.getCyclicState();
		switch ( st ) {
			case CBlank.Checking:
			blk.setCyclicState(CBlank.IsCyclic);
			nonMonProblem("Cyclic unnamed individual",n);
			case CBlank.IsCyclic:
			  return true;
			case CBlank.Undefined:
			  blk.setCyclicState(CBlank.Checking);
			  Triple t = blk.get(2);
			  boolean rslt;
			  if ( t == null )
			     rslt = false;
			  else
			    rslt = isCyclic( (CBlank)getCNode(t.getSubject()), n);
			  blk.setCyclicState( rslt ? CBlank.IsCyclic : CBlank.NonCyclic );
			  return rslt;
			case CBlank.NonCyclic:
			  return false;
			default:
			  throw new BrokenException("Impossible case in switch.");
		}
		   
	}
	private void check(Q q,NodeAction a, Model m){
		Query rdql = q.asRDQL();
		rdql.setSource(m);
		QueryExecution qe = new QueryEngine(rdql);
		QueryResults results = qe.exec();
		while ( results.hasNext() ) {
			ResultBinding rb = (ResultBinding)results.next() ;
			RDFNode nn = (RDFNode)rb.get("x");
			a.apply(nn.asNode());
		}
	}
	private void nonMonProblem(String shortD,Node n) {
		nonMonProblem(shortD, n,Levels.DL);
	}
	private void nonMonProblem(String shortD,Node n, int lvl) {
		Model m = ModelFactory.createDefaultModel();
		Graph mg = m.getGraph();
		if ( nonMonotoneLevel <= lvl )
		   nonMonotoneLevel = lvl + 1;
		   
		if ( lvl == Levels.Lite && !wantLite )
		  return;
		  
		EnhNode enh = ((EnhGraph)m).getNodeAs(n,RDFNode.class);
		Iterator it = this.hasBeenChecked.find(n,null,null);
		while ( it.hasNext() )
		  mg.add((Triple)it.next());
		   
		this.nonMonotoneProblems.add(
		  new SyntaxProblem(shortD,enh,lvl)
		);
	}

	public Checker(boolean lite, GraphMaker gf) {
		super(lite, gf);
		this.gf = gf;
	}
	public Checker() {
		this(false);
	}
	public Checker(boolean lite) {
		this(lite, new SimpleGraphMaker());
	}
	public void add(Graph g) {
		// Add every triple
		ClosableIterator it = null;
		try {
			it = g.find(null, null, null);
			while (it.hasNext()) {
				add((Triple) it.next(), true);
			}
		} finally {
			if (it != null)
				it.close();
		}
	}
	
	public void load(String url){
        // create an ontology model with no reasoner and the default doc manager
        OntModel m = ModelFactory.createOntologyModel( new OntModelSpec( ModelFactory.createMemModelMaker(),
                                                                         null, null, ProfileRegistry.OWL_LANG ), null );
		//OntModel m = ModelFactory.createOntologyModel();
        m.getDocumentManager().setProcessImports( true );
	
		m.read(url);
        
        // since we specified the null reasoner, the graph of the model is the union graph
        add( m.getGraph() );
	}
	//private boolean wantLite = true;

	void addProblem(int lvl, Triple t) {
		super.addProblem(lvl,t);
		if ( lvl == Levels.Lite && !wantLite )
		  return;
		Graph min =
			new MinimalSubGraph(lvl == Levels.Lite, t, this).getContradiction();
		addProblem(
			new SyntaxProblem(
				"Not a " + Levels.toString(lvl) + " subgraph",
				min,
				lvl));
	}
	void addProblem(SyntaxProblem sp) {
		super.addProblem(sp);
		switch ( sp.level ) {
			case Levels.Warning:
			warnings.add(sp);
			case Levels.Lite:
			   if (!wantLite )
			      return;
			default:
			monotoneProblems.add(sp);
			  
		}
	}
	public String getSubLanguage() {
		if ( monotoneLevel < Levels.Full && monotoneProblems.size() == 0)
   		   snapCheck();
   		int m = monotoneLevel < nonMonotoneLevel ? nonMonotoneLevel : monotoneLevel;
   		if ( wantLite && m == Levels.DL)
   		   return "DL or Full";
		return Levels.toString(m);
	}
	void actions(long key, CNodeI s, CNodeI o, Triple t) {
		if (SubCategorize.tripleForObject(key))
			o.asBlank().addObjectTriple(t);

		switch (SubCategorize.action(key)) {
			case SubCategorize.FirstOfOne :
				s.asOne().first(t);
				break;
			case SubCategorize.FirstOfTwo :
				s.asTwo().first(t);
				break;
			case SubCategorize.SecondOfTwo :
				s.asTwo().second(t);
				break;
		}
	}
	static public void main(String argv[]) {
		GraphMaker gf = ModelFactory.createMemModelMaker().getGraphMaker();
        
        // create an ontology model with no reasoner and the default doc manager
		OntModel m = ModelFactory.createOntologyModel( new OntModelSpec( ModelFactory.createMemModelMaker(),
                                                                         null, null, ProfileRegistry.OWL_LANG ), null );
        m.getDocumentManager().setProcessImports( true );
	
		//Model m = ModelFactory.createDefaultModel();
		m.read(argv[0]);
        //m.write(System.out);
		// m.getDocumentManager();
        
        // the ont model graph must be the union graph, since we specified the null reasoner (hence no inf graph)
        Graph g = m.getGraph();

		Checker chk = new Checker(argv.length==2 && argv[1].equalsIgnoreCase("Lite"), gf);
		chk.add(g);
      //  System.err.println("g added.");
		String subLang = chk.getSubLanguage();
		System.out.println(subLang);

		if (argv.length > 1) {
			if (argv[1].equals(subLang))
				return;
			if (argv[1].equalsIgnoreCase("Full") || subLang.equals("Lite")) {
				System.err.println(
					"All constructs were in OWL " + subLang + ".");
				return;
			}
		}

			Iterator it = chk.getProblems();
			while (it.hasNext()) {
				SyntaxProblem sp = (SyntaxProblem) it.next();
				System.err.println(sp.longDescription());
			}

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
