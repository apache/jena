package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import java.util.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
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
public class Checker extends EnhGraph {
	// TODO personality factories
	static private Personality personality =
		new GraphPersonality()
        .add(CNodeI.class, CNode.factory)
        .add(Blank.class,CBlank.factory)
        .add(One.class,OneImpl.factory)
        .add(Two.class,TwoImpl.factory);
	private Graph hasBeenChecked;
	private Vector monotoneProblems = new Vector();

	private Vector nonMonotoneProblems = null;

	public Iterator getProblems() {
		snapCheck();
		return new AddIterator(
			monotoneProblems.iterator(),
			nonMonotoneProblems.iterator());
	}

	private void snapCheck() {
		if (nonMonotoneProblems == null) {
			nonMonotoneProblems = new Vector();
			// TODO points checks ...
		}
	}

	private int errorCnt = 0;

	Checker(Graph g) {
		super(g, personality);
	}

	void add(Graph g) {
		// Add every triple
		ClosableIterator it = null;
		try {
			it = g.find(null, null, null);
			while (it.hasNext()) {
				add((Triple) it.next());
			}
		} finally {
			if (it != null)
				it.close();
		}
	}
	private boolean wantLite = true;
	
	// TODO Find minimal closure
	void addProblem(int lvl, Triple t) {
		
	}
	void addProblem(SyntaxProblem sp){
		monotoneProblems.add(sp);
	}
	/**
	 * 
	 * @param t A triple from a graph being checked.
	 */
	private void add(Triple t) {
		CNodeI s = (CNodeI) getNodeAs(t.getSubject(), CNodeI.class);
		CNodeI p = (CNodeI) getNodeAs(t.getPredicate(), CNodeI.class);
		CNodeI o = (CNodeI) getNodeAs(t.getObject(), CNodeI.class);
		int s0 =
		s.getCategories();
		int p0 =
		p.getCategories();
		int o0 =
		o.getCategories();
		final long key =
			SubCategorize.refineTriple(s0,p0,o0);
		if (key == Grammar.Failure) {
			addProblem(Levels.DL,t);
		} else {
			if ( wantLite && SubCategorize.dl(key))
			      addProblem(Levels.Lite,t);
			      // TODO recursive
			o.setCategories(SubCategorize.object(key,o0));
			p.setCategories(SubCategorize.prop(key,p0));
			s.setCategories(SubCategorize.subject(key,s0));
			
			if ( SubCategorize.tripleForObject(key))
			   o.asBlank().addObjectTriple(t);
			
			switch (SubCategorize.action(key)   ) {
				case SubCategorize.FirstOfOne:
				    s.asOne().first(t);
				    break;
				    case SubCategorize.FirstOfTwo:
				    s.asTwo().first(t);
				    break;
				    case SubCategorize.SecondOfTwo:
				    s.asTwo().second(t);
				    break;
			}
		}
	}
	// TODO getSubLanguage()
	public String getSubLanguage() {
		return null;
	}

	static public void main(String argv[]) {
		OntDocumentManager dm = new OntDocumentManager();

		dm.setProcessImports(true);

		OntModel m = ModelFactory.createOntologyModel(OWL.NAMESPACE, null, dm);

		m.read(argv[0]);

		// m.getDocumentManager();
		GraphFactory gf = dm.getDefaultGraphFactory();

		Checker chk = new Checker(gf.getGraph());

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
