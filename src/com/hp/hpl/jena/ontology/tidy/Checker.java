package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.enhanced.*;
import java.util.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.impl.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdql.*;
import com.hp.hpl.jena.vocabulary.*;

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
	private GraphFactory gf;
	private Vector monotoneProblems = new Vector();

	private Vector nonMonotoneProblems = null;
	private int nonMonotoneLevel;

	public Iterator getProblems() {
		snapCheck();
		return new AddIterator(
			monotoneProblems.iterator(),
			nonMonotoneProblems.iterator());
	}

	private void snapCheck() {
		if (nonMonotoneProblems == null) {
			nonMonotoneProblems = new Vector();
			nonMonotoneLevel = Levels.Lite;
			Model m = ModelFactory.createModelForGraph(hasBeenChecked);
		
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
		  * We first find non-cyclic orphaned unnamed individuals,
		  * mark those as non-cyclic, then check the remaining cyclic
		  * nodes and then unmark the non-cyclic orphaned unnamed individuals
		  */
		 
			// TODO unnamed individual cycle 
			/*
			* Hardest (well exlcuding Hamiltonian paths)
			* Check the disjointUnion blank nodes
			*/
		    
			  // TODO disjointUnion blank nodes
			
			
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
		EnhNode enh = ((EnhGraph)m).getNodeAs(n,RDFNode.class);
		Iterator it = this.hasBeenChecked.find(n,null,null);
		while ( it.hasNext() )
		  mg.add((Triple)it.next());
		   
		this.nonMonotoneProblems.add(
		  new SyntaxProblem(shortD,enh,lvl)
		);
	}

	Checker(boolean lite, GraphFactory gf) {
		super(lite, gf);
		this.gf = gf;
	}
	Checker(boolean lite) {
		this(lite, new DefaultGraphFactory());
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
	private boolean wantLite = true;

	void addProblem(int lvl, Triple t) {
		Graph min =
			new MinimalSubGraph(lvl == Levels.Lite, t, this).getContradiction();
		monotoneProblems.add(
			new SyntaxProblem(
				"Not a " + Levels.toString(lvl) + " subgraph",
				min,
				lvl));
	}
	void addProblem(SyntaxProblem sp) {
		monotoneProblems.add(sp);
	}
	public String getSubLanguage() {
		if ( monotoneLevel < Levels.Full )
   		   snapCheck();
   		int m = monotoneLevel < nonMonotoneLevel ? nonMonotoneLevel : monotoneLevel;
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
		OntDocumentManager dm = new OntDocumentManager();
		dm.setProcessImports(true);
		OntModel m = ModelFactory.createOntologyModel(OWL.NAMESPACE, null, dm);
		m.read(argv[0]);

		// m.getDocumentManager();
		GraphFactory gf = dm.getDefaultGraphFactory();
		Checker chk = new Checker(false, gf);
		chk.add(m.getGraph());

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

		if (subLang.equals("Full")) {
			Iterator it = chk.getProblems();
			while (it.hasNext()) {
				SyntaxProblem sp = (SyntaxProblem) it.next();
			}
		}

	}

}
