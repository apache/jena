/*
 *  (c)     Copyright 2000, 2001, 2002, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 *   All rights reserved.
 * [See end of file]
 *  $Id: Unparser.java,v 1.34 2005-02-21 12:22:32 andy_seaborne Exp $
 */

package com.hp.hpl.jena.xmloutput.impl;

/*
 * @author Jeremy Carroll
 *
 * Want todo List
 * - easy efficiency gains in listSubjects() and modelListSubjects()
 *   by removing those subjects that we have already considered.

 * - Set Default language during first pass.
 *
 *
 *Notes on ID and BagID:
 *Our preferences are follows:
 * for a Stating with an explicit local ID we avoid explicitly
 * constructing the reification, and try and use rule 6.12 with an idAttr.
 * If the Stating is anonymous or non-local then we construct the reification
 * explicitly.
 *
 *
 * Notes:
 * The following rules are not supported by the current Jena RDF parser:
 *   6.8
 *
 *
 * [6.1] RDF            ::= ['<rdf:RDF>'] obj* ['</rdf:RDF>']
 * [6.2] obj            ::= description | container
 * [6.3] description    ::= '<rdf:Description' idAboutAttr? bagIdAttr? propAttr* '/>'
 *                        | '<rdf:Description' idAboutAttr? bagIdAttr? propAttr* '>'
 *                                       propertyElt* '</rdf:Description>'
 *                        | typedNode
 [6.4] container      ::= sequence | bag | alternative
 [6.5] idAboutAttr    ::= idAttr | aboutAttr | aboutEachAttr
 [6.6] idAttr         ::= ' ID="' IDsymbol '"'
 [6.7] aboutAttr      ::= ' about="' URI-reference '"'
[6.8] aboutEachAttr  ::= ' aboutEach="' URI-reference '"'
                        | ' aboutEachPrefix="' string '"'
 [6.9] bagIdAttr      ::= ' bagID="' IDsymbol '"'
 [6.10] propAttr       ::= typeAttr
                          | propName '="' string '"' (with embedded quotes escaped)
 [6.11] typeAttr       ::= ' type="' URI-reference '"'
 [6.12] propertyElt    ::= '<' propName idAttr? '>' value '</' propName '>'
                         | '<' propName idAttr? parseLiteral '>'
                               literal '</' propName '>'
                         | '<' propName idAttr? parseResource '>'
                               propertyElt* '</' propName '>'
                         | '<' propName idRefAttr? bagIdAttr? propAttr* '/>'

 [daml.1 - 6.12 cont.]   | '<' propName idAttr? parseDamlCollection '>'
                            obj*
                            '</' propName '>'
 [daml.2] parseDamlCollection ::= ' parseType="daml:collection"'

 [6.13] typedNode      ::= '<' typeName idAboutAttr? bagIdAttr? propAttr* '/>'
                          | '<' typeName idAboutAttr? bagIdAttr? propAttr* '>'
 *                                           propertyElt* '</' typeName '>'
 * [6.14] propName       ::= Qname
 * [6.15] typeName       ::= Qname
 * [6.16] idRefAttr      ::= idAttr | resourceAttr
 * [6.17] value          ::= obj | string
 * [6.18] resourceAttr   ::= ' resource="' URI-reference '"'
 * [6.19] Qname          ::= [ NSprefix ':' ] name
 * [6.20] URI-reference  ::= string, interpreted per [URI]
 * [6.21] IDsymbol       ::= (any legal XML name symbol)
 * [6.22] name           ::= (any legal XML name symbol)
 * [6.23] NSprefix       ::= (any legal XML namespace prefix)
 * [6.24] string         ::= (any XML text, with "<", ">", and "&" escaped)
 * [6.25] sequence       ::= '<rdf:Seq' idAttr? '>' member* '</rdf:Seq>'
 * | '<rdf:Seq' idAttr? memberAttr* '/>'
 * [6.26] bag            ::= '<rdf:Bag' idAttr? '>' member* '</rdf:Bag>'
 * | '<rdf:Bag' idAttr? memberAttr* '/>'
 * [6.27] alternative    ::= '<rdf:Alt' idAttr? '>' member+ '</rdf:Alt>'
 *                                | '<rdf:Alt' idAttr? memberAttr? '/>'
 * [6.28] member         ::= referencedItem | inlineItem
 * [6.29] referencedItem ::= '<rdf:li' resourceAttr '/>'
 * [6.30] inlineItem     ::= '<rdf:li' '>' value </rdf:li>'
 *                         | '<rdf:li' parseLiteral '>' literal </rdf:li>'
 *                         | '<rdf:li' parseResource '>' propertyElt* </rdf:li>'
 * [6.31] memberAttr     ::= ' rdf:_n="' string '"' (where n is an integer)
 * [6.32] parseLiteral   ::= ' parseType="Literal"'
 * [6.33] parseResource  ::= ' parseType="Resource"'
 * [6.34] literal        ::= (any well-formed XML)
 *
 */
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.rdf.arp.*;
import com.hp.hpl.jena.shared.*;

import java.util.*;
import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.util.XMLChar;

/** An Unparser will output a model in the abbreviated syntax.
 ** @version  Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.34 $' Date='$Date: 2005-02-21 12:22:32 $'

 */
class Unparser {
	static private Property LI = new PropertyImpl(RDF.getURI(), "li");
	static private Property DESCRIPTION =
		new PropertyImpl(RDF.getURI(), "Description");

	static protected Log logger = LogFactory.getLog(Unparser.class);

	/** Creates an Unparser for the specified model.
	 * The localName is the URI (typical URL) intended for
	 * the output file. No trailing "#" should be used.
	 * This will control the use of <I>ID</I> or <I>about</I> or <I>resource</I>
	 * on various rules.
	 * @param localName The intended URI of the output file. No trailing "#".
	 * @param m The model.
	 * @param w The output.
	 */
	Unparser(Abbreviated parent, String localName, Model m, PrintWriter w) {
		setLocalName(localName);
		prettyWriter = parent;
		out = w;
		model = m;
		addTypeNameSpaces();
		objectTable = new HashMap();
		StmtIterator ss = m.listStatements();
		try {
			while (ss.hasNext()) {
				Statement s = ss.nextStatement();
				RDFNode rn = s.getObject();
				if (rn instanceof Resource) {
					increaseObjectCount((Resource) rn);
				}
			}
		} finally {
			ss.close();
		}
		try {
			res2statement = new HashMap();
			statement2res = new HashMap();
			ClosableIterator reified = new MapFilterIterator(new MapFilter() {
				public Object accept(Object o) {
					Resource r = (Resource) o;
					return (
						r.hasProperty(RDF.subject)
							&& r.hasProperty(RDF.object)
							&& r.hasProperty(RDF.predicate))
						? r
						: null;

				}
			}, model.listSubjectsWithProperty(RDF.type, RDF.Statement));
			while (reified.hasNext()) {
				Resource r = (Resource) reified.next();
				try {
					/**
					 *  This block of code assumes that really we
					 *  are dealing with a reification.
					 *  We may, on the contrary, be dealing with a random
					 *  collection of triples that do not make sense.
					 */
					Statement subj = r.getRequiredProperty(RDF.subject);
					Statement pred = r.getRequiredProperty(RDF.predicate);
					Statement obj = r.getRequiredProperty(RDF.object);
					RDFNode nobj = obj.getObject();
					Resource rsubj = (Resource) subj.getObject();
					Resource rpred = (Resource) pred.getObject();

					Property ppred =
						model.createProperty(((Resource) rpred).getURI());

					Statement statement =
						model.createStatement((Resource) rsubj, ppred, nobj);
					res2statement.put(r, statement);
					statement2res.put(statement, r);
				} catch (Exception ignored) {
				}
			}
		} finally {
			ss.close();
		}
	}

	/**
	    Note: must work with uri being null.
	*/
	private void setLocalName(String uri) {
		if (uri == null || uri.equals(""))
			localName = "";
		else
			try {
				URI u = new URI(uri);
				u.setFragment(null);
				localName = u.getURIString();
			} catch (MalformedURIException e) {
				throw new BadURIException( uri, e);
			}
	}

	/** Should be called exactly once for each Unparser.
	 * Calling it a second time will have undesired results.
	 */
	void write() {
		prettyWriter.workOutNamespaces();
		wRDF();
		/*
		System.out.print("Coverage = ");
		for (int i=0;i<codeCoverage.length;i++)
		    System.out.print(" c[" + i + "] = " + codeCoverage[i]+ ";");
		System.out.println();
		 **/
	}
	/** Set a list of types of objects that will be expanded at the
	 *  top-level of the file.
	 *  @param types An array of rdf:Class'es.
	 *
	 */
	void setTopLevelTypes(Resource types[]) {
		pleasingTypes = types;
		pleasingTypeSet = new HashSet(Arrays.asList(types));
	}

	private String xmlBase;
	void setXMLBase(String b) {
		xmlBase = b;
	}
	/* THE MORE INTERESTING MEMBER VARIABLES.
	 * Note there are others scattered throughout the file,
	 * but those are only used by one or two methods.
	 */

	final private static String rdfns = RDF.type.getNameSpace();
	final private static Integer one = new Integer(1);

	private String localName;
	private Map objectTable; // This is a map from Resource to Integer
	// which indicates how many times each resource
	// occurs as an object of a triple.
	private Model model;
	private PrintWriter out;
	private Set doing = new HashSet(); // Some of the resources that
	// are currently being written.
	private Set doneSet = new HashSet(); // The triples that have been output.
	private Set haveReified = new HashSet(); // Those local resources that are
	// the id's of a reification, used to ensure that anonymous
	// resources are made non-anonymous when reified in certain ways.

	private Resource pleasingTypes[] = null;
	private Set pleasingTypeSet = new HashSet();

	final private Abbreviated prettyWriter;

	private boolean avoidExplicitReification = true;
	// We set this to false as we start giving up on elegance.

	// Reification stuff.

	Map res2statement;
	Map statement2res;

	/* The top-down recursive descent unparser.
	 * The methods starting in w all refer to one of the rules
	 * of the grammar, which they implement.
	 * boolean valued rules first check whether they are applicable
	 * and return false if not. Otherwise they create appropriate output
	 * (using recursive descent) and return true. Note all necessary
	   checks are made before any output or any recursive descent.
	 * The void w- methods just implement the rule, which typically does not
	 * involve any choice.
	 */
	/*
	[6.1] RDF            ::= ['<rdf:RDF>'] obj* ['</rdf:RDF>']
	 */
	private void wRDF() {
		tab();
		print("<");
		print(prettyWriter.rdfEl("RDF"));
		indentPlus();
		printNameSpaceDefn();
		if (xmlBase != null) {
			setLocalName(xmlBase);
			tab();
			print("xml:base=" + quote(xmlBase));
		}
		print(">");
		wObjStar();
		indentMinus();
		tab();
		print("</");
		print(prettyWriter.rdfEl("RDF"));
		print(">");
		tab();
	}

	/**
	 *  All subjects get listed, for top level use only.
	 */
	private void wObjStar() {
		Iterator rs = listSubjects();
		while (rs.hasNext()) {
			Resource r = (Resource) rs.next();
			increaseObjectCount(r);
			// This forces us to not be anonymous unless
			// we are never an object. See isGenuineAnon().
			wObj(r, true);
		}
		closeAllResIterators();
	}

	/*
	[6.12] propertyElt    ::= '<' propName idAttr? '>' value '</' propName '>'
	                       | '<' propName idAttr? parseLiteral '>'
	                             literal '</' propName '>'
	                       | '<' propName idAttr? parseResource '>'
	                             propertyElt* '</' propName '>'
	                       | '<' propName idRefAttr? bagIdAttr? propAttr* '/>'
	[daml.1 - 6.12 cont.]   | '<' propName idAttr? parseDamlCollection '>'
	                          obj*
	                          '</' propName '>'
	[daml.2] parseDamlCollection ::= ' parseType="daml:collection"'
	
	For daml collections we prefer the special syntax otherwise:
	We prefer choice 4 where possible, except in the case where the
	statement is reified and the object is not anonymous in which case
	we use one of the others (e.g. choice 1).
	For embedded XML choice 2 is obligatory.
	For untyped, anonymous resource valued items choice 3 is used.
	Choice 1 is the fall back.
	 */
	private boolean wPropertyElt(
		WType wt,
		Property prop,
		Statement s,
		RDFNode val) {
		return wPropertyEltCompact(wt, prop, s, val) || // choice 4
		wPropertyEltDamlCollection(wt, prop, s, val) || // choice daml.1
		wPropertyEltLiteral(wt, prop, s, val) || // choice 2
		wPropertyEltResource(wt, prop, s, val) || // choice 3
		wPropertyEltDatatype(wt, prop, s, val)
			|| wPropertyEltValue(wt, prop, s, val);
		// choice 1.
	}

	/* [6.12.4] propertyElt    ::= '<' propName idRefAttr? bagIdAttr? propAttr* '/>'
	 */
	private boolean wPropertyEltCompact(
		WType wt,
		Property prop,
		Statement s,
		RDFNode val) {
		// Conditions
		if (!(val instanceof Resource))
			return false;
		Resource r = (Resource) val;
		if (!(allPropsAreAttr(r) || doing.contains(r)))
			return false;
		// '<' propName '/>'   is 6.12.1 rather than 6.12.4
		// and it becomes an empty string value.
		// Whether this is a mistake or not is debatable.
		// We avoid the construction.
		if ((!hasProperties(r)) && isGenuineAnon(r))
			return false;
		// Write out
		done(s);
		tab();
		print("<");
		wt.wTypeStart(prop);
		indentPlus();
		wIdRefAttrOpt(s, r);
		if (!doing.contains(r)) {
			wPropAttrAll(r);
		} else if (isGenuineAnon(r)) {
			// ???
			error("Genuine anon resource in cycle?");
		}
		indentMinus();
		print("/>");
		return true;
	}

	/*
	[6.12.2] propertyElt    ::=  '<' propName idAttr? parseLiteral '>'
	                             literal '</' propName '>'
	 */
	private boolean wPropertyEltLiteral(
		WType wt,
		Property prop,
		Statement s,
		RDFNode r) {
		if (prettyWriter.sParseTypeLiteralPropertyElt)
			return false;
		if (!((r instanceof Literal) && ((Literal) r).getWellFormed())) {
			return false;
		}
		// print out.
		done(s);
		tab();
		print("<");
		wt.wTypeStart(prop);
		wIdAttrReified(s);
		maybeNewline();
		wParseLiteral();
		maybeNewline();
		print(">");
		print(((Literal) r).getLexicalForm());
		print("</");
		wt.wTypeEnd(prop);
		print(">");
		return true;
	}

	private boolean wPropertyEltDatatype(
		WType wt,
		Property prop,
		Statement s,
		RDFNode r) {
		if (!((r instanceof Literal)
			&& ((Literal) r).getDatatypeURI() != null)) {
			return false;
		}
		// print out.
		done(s);
		tab();
		print("<");
		wt.wTypeStart(prop);
		wIdAttrReified(s);
		maybeNewline();
		wDatatype(((Literal) r).getDatatypeURI());
		maybeNewline();
		print(">");
		print(
			Util.substituteEntitiesInElementContent(
				((Literal) r).getLexicalForm()));
		print("</");
		wt.wTypeEnd(prop);
		print(">");
		return true;
	}

	/*
	[6.12.3] propertyElt    ::=  '<' propName idAttr? parseResource '>'
	                             propertyElt* '</' propName '>'
	 */
	private boolean wPropertyEltResource(
		WType wt,
		Property prop,
		Statement s,
		RDFNode r) {
		if (prettyWriter.sParseTypeResourcePropertyElt)
			return false;
		if (r instanceof Literal)
			return false;
		Resource res = (Resource) r;
		if (!isGenuineAnon(res))
			return false;
		if (getType(res) != null)
			return false; // preferred typed node construction.
		// print out.
		done(s);
		tab();
		print("<");
		wt.wTypeStart(prop);
		indentPlus();
		wIdAttrReified(s);
		wParseResource();
		print(">");
		wPropertyEltStar(res);
		indentMinus();
		tab();
		print("</");
		wt.wTypeEnd(prop);
		print(">");
		return true;
	}

	/*
	[6.12] propertyElt    ::= '<' propName idAttr? '>' value '</' propName '>'
	 */
	private boolean wPropertyEltValue(
		WType wt,
		Property prop,
		Statement s,
		RDFNode r) {
		return wPropertyEltValueString(wt, prop, s, r)
			|| wPropertyEltValueObj(wt, prop, s, r);
	}

	/*
	[6.12] propertyElt    ::= '<' propName idAttr? '>' value '</' propName '>'
	 */
	private boolean wPropertyEltValueString(
		WType wt,
		Property prop,
		Statement s,
		RDFNode r) {
		if (r instanceof Literal) {
			done(s);
			Literal lt = (Literal) r;
			String lang = lt.getLanguage();
			tab();
			print("<");
			wt.wTypeStart(prop);
			wIdAttrReified(s);
			maybeNewline();
			if (lang != null && lang.length() > 0)
				print(" xml:lang=" + q(lang));
			maybeNewline();
			print(">");
			wValueString(lt);
			print("</");
			wt.wTypeEnd(prop);
			print(">");
			return true;
		} else {
			return false;
		}
	}

	/*
	[6.17.2] value          ::=  string
	 */
	private void wValueString(Literal lt) {
		String val = lt.getString();
		print(Util.substituteEntitiesInElementContent(val));
	}

	/*
	[6.12] propertyElt    ::= '<' propName idAttr? '>' value '</' propName '>'
	[6.17.1] value          ::= obj
	 */
	private boolean wPropertyEltValueObj(
		WType wt,
		Property prop,
		Statement s,
		RDFNode r) {
		if (r instanceof Resource && !prettyWriter.sResourcePropertyElt) {
			Resource res = (Resource) r;
			done(s);
			tab();
			print("<");
			wt.wTypeStart(prop);
			wIdAttrReified(s);
			print(">");
			tab();
			indentPlus();
			wObj(res, false);
			indentMinus();
			tab();
			print("</");
			wt.wTypeEnd(prop);
			print(">");
			return true;
		} else {
			return false;
		}
	}

	/*
	[daml.1 - 6.12 cont.]   | '<' propName idAttr? parseDamlCollection '>'
	                          obj*
	                          '</' propName '>'
	 */
	private boolean wPropertyEltDamlCollection(
		WType wt,
		Property prop,
		Statement s,
		RDFNode r) {
		boolean daml = true;
		Statement list[][] = getDamlList(r);
		if (list == null) {
			daml = false;
			list = getRDFList(r);
		}
		if (list == null)
			return false;
		// print out.
		done(s);
		// record all done's first - they may impact the
		// way we print the values.
		for (int i = 0; i < list.length; i++) {
			done(list[i][0]);
			done(list[i][1]);
			if (daml)
				done(list[i][2]);
		}
		tab();
		print("<");
		wt.wTypeStart(prop);
		indentPlus();
		wIdAttrReified(s);
		if (daml)
			wParseDamlCollection();
		else
			wParseCollection();

		print(">");
		for (int i = 0; i < list.length; i++) {
			wObj((Resource) list[i][0].getObject(), false);
		}
		indentMinus();
		tab();
		print("</");
		wt.wTypeEnd(prop);
		print(">");
		return true;
	}

	// propAttr* with no left over statements.
	private void wPropAttrAll(Resource r) {
		wPropAttrSome(r);
		if (hasProperties(r))
			error("Bad call to wPropAttrAll");
	}

	// propAttr* possibly with left over statements.
	private void wPropAttrSome(Resource r) {
		ClosableIterator ss = listProperties(r);
		try {
			Set seen = new HashSet();
			while (ss.hasNext()) {
				Statement s = (Statement) ss.next();
				RDFNode val = s.getObject();
				if (canBeAttribute(s, seen)) {
					done(s);
					wPropAttr(s.getPredicate(), s.getObject());
				}
			}
		} finally {
			ss.close();
		}
	}

	/*
	[6.2] obj            ::= description | container
	[6.3] description    ::= '<rdf:Description' idAboutAttr? bagIdAttr? propAttr* '/>'
	                       | '<rdf:Description' idAboutAttr? bagIdAttr? propAttr* '>'
	                              propertyElt* '</rdf:Description>'
	                       | typedNode
	[6.4] container      ::= sequence | bag | alternative
	We use:
	[6.2a] obj            ::= description | container  | typedNode
	[6.3a] description    ::= '<rdf:Description' idAboutAttr? bagIdAttr? propAttr* '/>'
	                       | '<rdf:Description' idAboutAttr? bagIdAttr? propAttr* '>'
	                              propertyElt* '</rdf:Description>'
	 *
	 * This method has got somewhat messy. If we are not at the topLevel we
	 * may choose to not expand a node but just use a
	 *  typedNode ::= '<' typeName idAboutAttr '/>'
	 * rule.
	 * This rules also applies to Bags that we feel unconfortable with,
	 * such as a Bag arising from a BagId rule that we don't handle properly.
	 *
	
	 */
	private boolean wObj(Resource r, boolean topLevel) {
		try {
			doing.add(r);
			Statement typeStatement = getType(r);
			if (typeStatement != null) {
				Resource t = typeStatement.getResource();
				if (!topLevel) {
					if (pleasingTypeSet.contains(t) && (!isGenuineAnon(r))) {
						return wTypedNodeNoProperties(r);
					}
				}
				return wTypedNode(r) || wDescription(r);
			} else
				return wDescription(r);
		} finally {
			doing.remove(r);
		}
	}

	abstract private class WType {
		abstract void wTypeStart(Resource uri);
		abstract void wTypeEnd(Resource uri);
	}

	static private int RDF_HASH = RDF.getURI().length();

	private WType wdesc = new WType() {
		void wTypeStart(Resource u) {
			print(prettyWriter.rdfEl(u.getURI().substring(RDF_HASH)));
		}
		void wTypeEnd(Resource u) {
			print(prettyWriter.rdfEl(u.getURI().substring(RDF_HASH)));
		}
	};

	private WType wtype = new WType() {
		void wTypeStart(Resource u) {
			print(prettyWriter.startElementTag(u.getURI()));
		}
		void wTypeEnd(Resource u) {
			print(prettyWriter.endElementTag(u.getURI()));
		}
	};

	/*
	[6.3a] description    ::= '<rdf:Description' idAboutAttr? bagIdAttr? propAttr* '/>'
	                       | '<rdf:Description' idAboutAttr? bagIdAttr? propAttr* '>'
	                              propertyElt* '</rdf:Description>'
	 */
	private boolean wDescription(Resource r) {
		return wTypedNodeOrDescription(wdesc, DESCRIPTION, r);
	}
	/*
	[6.13] typedNode      ::= '<' typeName idAboutAttr? bagIdAttr? propAttr* '/>'
	                       | '<' typeName idAboutAttr? bagIdAttr? propAttr* '>'
	                             propertyElt* '</' typeName '>'
	 */
	private boolean wTypedNode(Resource r) {
		Statement st = getType(r);
		if (st == null)
			return false;
		Resource type = st.getResource();
		done(st);
		return wTypedNodeOrDescription(wtype, type, r);
	}

	private boolean wTypedNodeOrDescription(
		WType wt,
		Resource ty,
		Resource r) {
		// preparation - look for the li's.
		Vector found = new Vector();
		ClosableIterator ss = listProperties(r);
		try {
			int greatest = 0;
			if (!prettyWriter.sListExpand)
				while (ss.hasNext()) {
					Statement s = (Statement) ss.next();
					int ix = s.getPredicate().getOrdinal();
					if (ix != 0) {
						if (ix > greatest) {
							found.setSize(ix);
							greatest = ix;
						}
						found.set(ix - 1, s);
					}
				}
		} finally {
			ss.close();
		}
		int last = found.indexOf(null);
		List li = last == -1 ? found : found.subList(0, last);

		return wTypedNodeOrDescriptionCompact(wt, ty, r, li)
			|| wTypedNodeOrDescriptionLong(wt, ty, r, li);
	}

	/* [6.13.1] typedNode      ::= '<' typeName idAboutAttr? bagIdAttr? propAttr* '/>'
	 */
	private boolean wTypedNodeOrDescriptionCompact(
		WType wt,
		Resource ty,
		Resource r,
		List li) {
		// Conditions
		if ((!li.isEmpty()) || !allPropsAreAttr(r))
			return false;
		// Write out
		tab();
		print("<");
		wt.wTypeStart(ty);
		indentPlus();
		wIdAboutAttrOpt(r);
		wPropAttrAll(r);
		print("/>");
		indentMinus();
		return true;
	}

	/* [6.13.1] typedNode      ::= '<' typeName idAboutAttr  '/>'
	 */
	private boolean wTypedNodeNoProperties(Resource r) {
		// Conditions
		if (isGenuineAnon(r))
			return false;
		Statement st = getType(r);
		if (st == null)
			return false;
		Resource type = st.getResource();
		done(st);
		// Write out
		tab();
		print("<");
		wtype.wTypeStart(type);
		indentPlus();
	//	if (hasProperties(r))
	//		wAboutAttr(r);
	//	else
			wIdAboutAttrOpt(r);
		print("/>");
		indentMinus();
		return true;
	}

	/*
	[6.13.2] typedNode      ::=  '<' typeName idAboutAttr? bagIdAttr? propAttr* '>'
	                             propertyElt* '</' typeName '>'
	 */
	private boolean wTypedNodeOrDescriptionLong(
		WType wt,
		Resource ty,
		Resource r,
		List li) {
		Iterator it = li.iterator();
		while (it.hasNext()) {
			done((Statement) it.next());
		}

		tab();
		print("<");
		wt.wTypeStart(ty);
		indentPlus();
		wIdAboutAttrOpt(r);
		wPropAttrSome(r);
		print(">");
		wLiEltStar(li.iterator());
		wPropertyEltStar(r);
		indentMinus();
		tab();
		print("</");
		wt.wTypeEnd(ty);
		print(">");
		return true;
	}

	private void wPropertyEltStar(Resource r) {
		ClosableIterator ss = this.listProperties(r);
		try {
			while (ss.hasNext()) {
				Statement s = (Statement) ss.next();
				wPropertyElt(wtype, s.getPredicate(), s, s.getObject());
			}
		} finally {
			ss.close();
		}
	}

	private void wLiEltStar(Iterator ss) {
		while (ss.hasNext()) {
			Statement s = (Statement) ss.next();
			wPropertyElt(wdesc, LI, s, s.getObject());
		}
	}

	/*
	[6.5] idAboutAttr    ::= idAttr | aboutAttr | aboutEachAttr
	we use
	[6.5a] idAboutAttr    ::= idAttr | aboutAttr
	 */
	private Set idDone = new HashSet();

	private boolean wIdAboutAttrOpt(Resource r) {
		return wIdAttrOpt(r) || wNodeIDAttr(r) || wAboutAttr(r);
	}

	/**
	 * Returns false if the resource is not genuinely anonymous and cannot
	 * be referred to using an ID.
	 * [6.6] idAttr         ::= ' ID="' IDsymbol '"'
	 */
	private boolean wIdAttrOpt(Resource r) {

		if (isGenuineAnon(r))
			return true; // We have output resource (with nothing).
		if (prettyWriter.sIdAttr)
			return false;
		if (r.isAnon())
			return false;
		if (isLocalReference(r)) {
			// Try and use the reification rules if they apply.
			// Issue: aren't we just about to list those statements explicitly.
			if (wantReification(r))
				return false;
			// Can be an ID if not already output.
			if (idDone.contains(r)) {
				return false; // We have already output this one.
			} else {
				idDone.add(r);
				print(" ");
				printRdfAt("ID");
				print("=");
				print(quote(getLocalName(r)));
				return true;
			}
		} else {
			return false;
		}
	}

	/*
	[6.7] aboutAttr      ::= ' about="' URI-reference '"'
	 */
	private boolean wAboutAttr(Resource r) {
		print(" ");
		printRdfAt("about");
		print("=");
		wURIreference(r);
		return true;
	}

	private void wURIreference(String s) {
		print(quote(prettyWriter.relativize(s)));
	}

	private void wURIreference(Resource r) {
		wURIreference(r.getURI());
	}

	/*
	[6.16] idRefAttr      ::= idAttr | resourceAttr
	 */
	private void wIdRefAttrOpt(Statement s, Resource r) {
		wIdAttrReified(s);
		if (!isGenuineAnon(r)) {
			wResourceNodeIDAttr(r);
		}
	}

	/*
	[6.6] idAttr         ::= ' ID="' IDsymbol '"'
	 */
	private void wIdAttrReified(Statement s) {
		if (wantReification(s)) {
			/*
			if ( prettyWriter.sReification )
			  System.err.println("???");
			else
			  System.err.println("!!!");
			*/
			Statement reify[] = reification(s);
			Resource res = (Resource) statement2res.get(s);
			idDone.add(res);
			int i;
			for (i = 0; i < reify.length; i++)
				done(reify[i]);
			print(" ");
			printRdfAt("ID");
			print("=");
			print(quote(getLocalName(res)));
			haveReified.add(res);
		}
	}

	/*
	[6.18] resourceAttr   ::= ' resource="' URI-reference '"'
	 */
	private boolean wResourceNodeIDAttr(Resource r) {
		return wNodeIDAttr(r) || wResourceAttr(r);
	}

	/*
	 nodeIDAttr   ::= ' rdf:nodeID="' URI-reference '"'
	 */
	private boolean wNodeIDAttr(Resource r) {
		if (!r.isAnon())
			return false;
		print(" ");
		printRdfAt("nodeID");
		print("=");
		print(q(prettyWriter.anonId(r)));

		return true;
	}

	/*
	[6.18] resourceAttr   ::= ' resource="' URI-reference '"'
	 */
	private boolean wResourceAttr(Resource r) {
		if (r.isAnon())
			return false;
		print(" ");
		printRdfAt("resource");
		print("=");
		wURIreference(r);
		return true;
	}

	int codeCoverage[] = new int[8];

	/*
	 [6.19] Qname          ::= [ NSprefix ':' ] name
	 
	private void wQnameStart(String ns, String local) {
		print(prettyWriter.startElementTag(ns, local));
	}

	private void wQnameEnd(String ns, String local) {
		print(prettyWriter.endElementTag(ns, local));
	}
*/
	private void wQNameAttr(Property p) {
		print(prettyWriter.attributeTag(p.getURI()));
	}

	private void printRdfAt(String s) {
		print(prettyWriter.rdfAt(s));
	}

	/*
	[6.10] propAttr       ::= typeAttr
	                       | propName '="' string '"' (with embedded quotes escaped)
	[6.11] typeAttr       ::= ' type="' URI-reference '"'
	 */
	private void wPropAttr(Property p, RDFNode n) {
		tab();
		if (p.equals(RDF.type))
			wTypeAttr((Resource) n);
		else
			wPropAttrString(p, (Literal) n);
	}

	private void wTypeAttr(Resource r) {
		print(" ");
		printRdfAt("type");
		print("=");
		wURIreference(r);
		//print(quote(r.getURI()));
	}

	private void wPropAttrString(Property p, Literal l) {
		print(" ");
		wQNameAttr(p);
		print("=" + quote(l.getString()));
	}

	/*
	[daml.2] parseDamlCollection ::= ' parseType="daml:collection"'
	 */
	private void wParseDamlCollection() {
		print(" ");
		printRdfAt("parseType");
		print("=" + q("daml:collection"));
	}

	/*
	[List.2] parseCollection ::= ' parseType="Collection"'
	 */
	private void wParseCollection() {
		print(" ");
		printRdfAt("parseType");
		print("=" + q("Collection"));
	}

	/*
	[6.32] parseLiteral   ::= ' parseType="Literal"'
	 */
	private void wParseLiteral() {
		print(" ");
		printRdfAt("parseType");
		print("=" + q("Literal"));
	}

	private void wDatatype(String dtURI) {
		print(" ");
		printRdfAt("datatype");
		print("=");
		maybeNewline();
		wURIreference(dtURI);
	}
	/*
	[6.33] parseResource  ::= ' parseType="Resource"'
	 */
	private void wParseResource() {
		print(" ");
		printRdfAt("parseType");
		print("=" + q("Resource"));
	}

	private void printNameSpaceDefn() {
		print(prettyWriter.xmlnsDecl());
	}

	/****************************************************************************
	 *  Utility routines ...
	 *
	 ***************************************************************************/

	/***
	 * Output and indentation.
	 ***/
	private int indentLevel = 0;

	private int currentColumn = 0;

	static private String filler(int lgth) {
		char rslt[] = new char[lgth];
		Arrays.fill(rslt, ' ');
		return new String(rslt);
	}

	private void tab() {
		int desiredColumn = prettyWriter.tab * indentLevel;
		if (desiredColumn > prettyWriter.width) {
			desiredColumn = 4 + (desiredColumn - 4) % prettyWriter.width;
		}
		if ((desiredColumn == 0 && currentColumn == 0)
			|| desiredColumn > currentColumn) {
			String spaces = filler(desiredColumn - currentColumn);
			out.print(spaces);
		} else {
			out.println();
			out.print(filler(desiredColumn));
		}
		currentColumn = desiredColumn;
	}

	private void maybeNewline() {
		if (currentColumn > prettyWriter.width) {
			tab();
		}
	}

	/**
	 *  Quote str with either ' or " quotes to be in attribute position
	 *  in XML.
	    The real rules are found at http://www.w3.org/TR/REC-xml#AVNormalize
	 */
	private String quote(String str) {
		return prettyWriter.qq(str);
	}
	private String q(String str) {
		return prettyWriter.q(str);
	}

	/**
	 *  Indentation screws up if there is a tab character in s.
	 *  We do not check this.
	 */
	private void print(String s) {
		out.print(s);
		int ix = s.lastIndexOf('\n');
		if (ix == -1)
			currentColumn += s.length();
		else
			currentColumn = s.length() - ix - 1;
	}

	private void indentPlus() {
		indentLevel++;
	}

	private void indentMinus() {
		indentLevel--;
	}

	/* Unexpected error.
	 */
	private void error(String msg) {
		JenaException e =
			new BrokenException("Internal error in Unparser: " + msg);
		this.prettyWriter.fatalError(e);
		throw e; // Just in case.
	}
	/**
	 * Name space stuff.
	 **/
	private void addTypeNameSpaces() {
		NodeIterator nn = model.listObjectsOfProperty(RDF.type);
		try {
			while (nn.hasNext()) {
				RDFNode obj = nn.nextNode();
				int split = isOKType(obj);
				if (split != -1)
					prettyWriter.addNameSpace(
						((Resource) obj).getURI().substring(0, split));
			}
		} finally {
			nn.close();
		}
	}

	private String getNameSpace(Resource r) {
		if (r.isAnon()) {
			logger.error("Internal error - Unparser.getNameSpace; giving up");
			throw new BrokenException("Internal error: getNameSpace(bNode)");
		} 
			String uri = r.getURI();
			int split = Util.splitNamespace(uri);
			return uri.substring(0, split);
		
	}

	/**
	 *  Local and/or anonymous resources.
	 **/
	private boolean isGenuineAnon(Resource r) {
		if (!r.isAnon())
			return false;
		Integer v = (Integer) objectTable.get(r);
		return v == null
			|| ((!prettyWriter.sResourcePropertyElt)
				&& v.intValue() <= 1
				&& (!haveReified.contains(r)));
	}

	private boolean isLocalReference(Resource r) {
		return (!r.isAnon())
			&& getNameSpace(r).equals(localName + "#")
			&& XMLChar.isValidNCName(getLocalName(r));
	}
	/*
	 * Utility for turning an integer into an alphabetic string.
	 
	private static String getSuffix(int suffixId) {
		if (suffixId == 0)
			return "";
		else {
			suffixId--;
			int more = (suffixId / 26);

			return getSuffix(more)
				+ new Character((char) ('a' + suffixId % 26));
		}
	}
	*/

	private String getLocalName(Resource r) {
		if (r.isAnon()) {
			logger.error("Internal error - giving up - Unparser.getLocalName");
			throw new BrokenException("Internal error: getLocalName(bNode)");
		} else {
			String uri = r.getURI();
			int split = Util.splitNamespace(uri);
			return uri.substring(split);
		}
	}
	/**
	 *   objectTable initialization.
	 */

	private void increaseObjectCount(Resource r) {
		if (!r.isAnon())
			return;
		Integer cnt = (Integer) objectTable.get(r);
		if (cnt == null) {
			cnt = one;
		} else {
			cnt = new Integer(cnt.intValue() + 1);
		}
		objectTable.put(r, cnt);
	}

	/***
	 *  Reification support.
	 ****/
	/*
	  Is the use of ID in rule [6.12] to create a reification helpful or not?
	 */
	private boolean wantReification(Statement s) {
		return wantReification(s, (Resource) statement2res.get(s));
	}

	private boolean wantReification(Resource res) {
		return wantReification((Statement) res2statement.get(res), res);
	}

	private boolean wantReification(Statement s, Resource ref) {
		if (s == null
			|| ref == null
			|| ref.isAnon()
			|| prettyWriter.sReification)
			return false;
		if (!(isLocalReference(ref)))
			return false;
		Statement reify[] = reification(s);
		int i;
		for (i = 0; i < reify.length; i++)
			if (doneSet.contains(reify[i]) || (!model.contains(reify[i])))
				return false; // Some of reification already done.
		return true; // Reification rule helps.
	}

	private Statement[] reification(Statement s) {
		Model m = s.getModel();
		Resource r = (Resource) statement2res.get(s);
		return new Statement[] {
			m.createStatement(r, RDF.type, RDF.Statement),
			m.createStatement(r, RDF.subject, s.getSubject()),
			m.createStatement(r, RDF.predicate, s.getPredicate()),
			m.createStatement(r, RDF.object, s.getObject())};
	}

	private boolean hasProperties(Resource r) {
		ExtendedIterator ss = listProperties(r);
		if (avoidExplicitReification
			&& //  ( r instanceof Statement ) &&
		 (!r.isAnon())
			&& isLocalReference(r)
			&& res2statement.containsKey(r)) {
			ss = new MapFilterIterator(new MapFilter() {
				public Object accept(Object o) {
					Statement s = (Statement) o;
					Property p = s.getPredicate();
					String local = p.getLocalName();
					return (
						(!p.getNameSpace().equals(rdfns))
							|| !((RDF.type.equals(p)
								&& s.getObject().equals(RDF.Statement))
								|| RDF.object.equals(p)
								|| RDF.predicate.equals(p)
								|| RDF.subject.equals(p)))
						? o
						: null;
				}
			}, ss);
		}
		try {
			return ss.hasNext();
		} finally {
			ss.close();
		}
	}
	private ExtendedIterator listProperties(Resource r) {
		return new MapFilterIterator(new MapFilter() {
			public Object accept(Object o) {
				return doneSet.contains(o) ? null : o;
			}
		}, r.listProperties());
	}
	// Good type statement, or simple string valued statement with no langID
	// See http://www.w3.org/TR/REC-xml#AVNormalize
	private boolean canBeAttribute(Statement s, Set seen) {
		Property p = s.getPredicate();
		// Check seen first.
		if (prettyWriter.sPropertyAttr
			|| seen.contains(p)) // We can't use the same attribute
			// twice in one rule.
			return false;
		seen.add(p);

		if (p.equals(RDF.type)) {
			// If we have a model in which a type is given
			// as a string, then we avoid the attribute rule 6.10 which is
			// ambiguous with 6.11.
			RDFNode n = s.getObject();
			return (n instanceof Resource) && !((Resource) n).isAnon();
		}

		if (s.getObject() instanceof Literal) {
			Literal l = s.getLiteral();
			if (l.getDatatypeURI() != null)
				return false;

			if (l.getLanguage().equals("")) {
				// j.cook.up bug fix
				if (prettyWriter.isDefaultNamespace(getNameSpace(p)))
					return false;

				String str = l.getString();
				if (str.length() < 40) {
					char buf[] = str.toCharArray();
					for (int i = 0; i < buf.length; i++) {
						// See http://www.w3.org/TR/REC-xml#AVNormalize
						if (buf[i] <= ' ')
							return false;
					}
					return !wantReification(s);
				}
			}
		}
		return false;
	}

	private boolean allPropsAreAttr(Resource r) {
		ClosableIterator ss = listProperties(r);
		Set seen = new HashSet();
		try {
			while (ss.hasNext()) {
				Statement s = (Statement) ss.next();
				if (!canBeAttribute(s, seen))
					return false;
			}
		} finally {
			ss.close();
		}
		return true;
	}
	private void done(Statement s) {
		doneSet.add(s);
		// return false;
	}
	/**  If r represent a daml:collection return a 2D array of its statements.
	 *  For each member there are three statements the first gives the DAML.first
	 *  statement, the second the DAML.rest statement and the third the RDF.type
	 *  statement.
	 *  @return null on failure or the elements of the collection.
	 *
	 */
	private Statement[][] getDamlList(RDFNode r) {
		return prettyWriter.sDamlCollection
			? null
			: getList(
				r,
				DAML_OIL.List,
				DAML_OIL.first,
				DAML_OIL.rest,
				DAML_OIL.nil);
	}
	private Statement[][] getRDFList(RDFNode r) {
		return prettyWriter.sParseTypeCollectionPropertyElt
			? null
			: getList(r, null, RDF.first, RDF.rest, RDF.nil);
	}

	private Statement[][] getList(
		RDFNode r,
		Resource list,
		Property first,
		Property rest,
		Resource nil) {
		boolean lookingGood = false;

		Vector rslt = new Vector();
		Set seen = new HashSet();
		RDFNode next = r;
		// We walk down the list and check each member.
		try {

			while (!next.equals(nil)) {
				Statement elt[] = new Statement[list == null ? 2 : 3];
				if (next instanceof Literal)
					return null;
				Resource res = (Resource) next;
				// We cannot label the nodes in the daml:collection construction.
				if (!isGenuineAnon(res))
					return null;
				// The occurs check - cyclic loop rather than a list.
				if (seen.contains(next))
					return null;
				seen.add(next);

				// We must have exactly three properties.
				StmtIterator ss = res.listProperties();
				try {
					while (ss.hasNext()) {
						Statement s = ss.nextStatement();
						Property p = s.getPredicate();
						int ix;
						RDFNode obj = s.getObject();
						if (doneSet.contains(s))
							return null;
						if (!(obj instanceof Resource)) {
							return null;
						}
						if (p.equals(RDF.type)) {
							ix = 2;
							if (!obj.equals(list))
								return null;
						} else if (p.equals(first)) {
							ix = 0;
							lookingGood = true;
						} else if (p.equals(rest)) {
							ix = 1;
							next = obj;
						} else {
							return null;
						}
						if (elt[ix] != null)
							return null;
						elt[ix] = s;
					}
				} finally {
					ss.close();
				}
				for (int i = 0; i < elt.length; i++)
					if (elt[i] == null)
						// didn't have the three required elements.
						return null;
				rslt.add(elt);
			}
			if (rslt.size() == 0)
				return null;
			lookingGood = false; // suppress bug report request
		} finally {
			/*
			if (lookingGood) {
				// try and get decent bug report
				logger.warn("The RDF/XML-ABBREV writer detected a partially well-formed list structure, but rejected it. "+
				"There have been hard to reproduce bugs concerning this. "+
				"Please take a copy of your output and mail it to jena-dev@yahoogroups.com"+
				" Even better, would be a copy of your code that produced it.") ;
			}
			*/
		}
		Statement array[][] = new Statement[rslt.size()][];
		rslt.copyInto(array);
		return array;

	}
	/**
	 * @return A statement that is suitable for a typed node construction or null.
	 */
	private Statement getType(Resource r) {
		Statement rslt;
		try {
			if (r instanceof Statement) {
				rslt = ((Statement) r).getStatementProperty(RDF.type);
				if (rslt == null || (!rslt.getObject().equals(RDF.Statement)))
					error("Statement type problem");
			} else {
				rslt = r.getRequiredProperty(RDF.type);
			}
		} catch (PropertyNotFoundException rdfe) {
			if (r instanceof Statement)
				error("Statement type problem");
			rslt = null;
		}
		if (rslt == null || isOKType(rslt.getObject()) == -1)
			return null;

		return rslt;
	}

	/** @param n The value of some rdf:type (precondition).
	 * @return The split point or -1.
	 */

	private int isOKType(RDFNode n) {

		if (!(n instanceof Resource))
			return -1;
		if (((Resource) n).isAnon())
			return -1;
		// Only allow resources with namespace and fragment ID
		String uri = ((Resource) n).getURI();

		int split = Util.splitNamespace(uri);
		if (split == 0 || split == uri.length())
			return -1;

		return split;
	}

	/**
	 *  The order of outputting the resources.
	 *  This all supports wObjStar.
	 **/
	private Set infinite;
	private void findInfiniteCycles() {
		// find all statements that haven't been done.
		StmtIterator ss = model.listStatements();
		Relation relation = new Relation();
		try {
			while (ss.hasNext()) {
				Statement s = ss.nextStatement();
				if (!doneSet.contains(s)) {
					RDFNode rn = s.getObject();
					if (rn instanceof Resource) {
						relation.set(s.getSubject(), rn);
					}
				}
			}
		} finally {
			ss.close();
		}
		relation.transitiveClosure();
		infinite = relation.getDiagonal();
	}
	/**
	 * This class is an iterator over the set infinite, but
	 * we wait until it is used before instantiating the
	 * underlying iterator.
	 */
	private Iterator allInfiniteLeft() {
		return new LateBindingIterator() {
			public Iterator create() {
				return infinite.iterator();
			}
		};
	}

	private Iterator pleasingTypeIterator() {
		if (pleasingTypes == null)
			return new NullIterator();
		Map buckets = new HashMap();
		Set bucketArray[] = new Set[pleasingTypes.length];
		// Set up buckets and bucketArray. Each is a collection
		// of the same buckets, one ordered, the other hashed.
		for (int i = 0; i < pleasingTypes.length; i++) {
			bucketArray[i] = new HashSet();
			buckets.put(pleasingTypes[i], bucketArray[i]);
		}

		ResIterator rs = model.listSubjects();
		try {
			while (rs.hasNext()) {
				Resource r = rs.nextResource();
				Statement s = getType(r);
				if (s != null) {
					Set bucket = (Set) buckets.get(s.getObject());
					if (bucket != null) {
						if (isGenuineAnon(r)) {
							Integer v = (Integer) objectTable.get(r);
							if (v != null && v.intValue() == 1)
								continue;
						}
						bucket.add(r);
					}
				}
			}
		} finally {
			rs.close();
		}

		// Now all the pleasing resources are in the buckets.
		// Add all their iterators togethor:

		return new IteratorIterator(new Map1Iterator(new Map1() {
			public Object map1(Object bkt) {
				return ((Set) bkt).iterator();
			}
		}, new ArrayIterator(bucketArray)));

	}
	/**
	 *  listSubjects  - generates a list of subjects for the wObjStar rule.
	 *  We wish to order these elegantly.
	 *  The current implementation goes for:
	 * <ul>
	 * <li> The current file - mainly intended for good DAML.
	 * <li> Subjects that are not objects of anything, excluding reifications
	 * <li> At these stage we evaluate a dependency graph of the remaining resources.
	 * <li>non-anonymous resources that are the object of more than one
	 * rule that are in infinite cycles.
	 * <li> any non genuinely anonymous resources that are in infinite cycles
	 * <li>any other resource in an infinite cyle
	 * <li>any other resource.
	 * <li>reifications
	 *</ul>
	 *
	 *
	 * At the end, we need to close any underlying ResIterators from the model,
	 * however to avoid complications in much of this code we use general
	 * java.util.Iterator-s. We hence use a wrapper around a ResIterator to
	 * allow us to manage the closing issue.
	 */
	private Iterator listSubjects() {
		//  The current file - mainly intended for good DAML.
		Iterator currentFile =
			//			new ArrayIterator(
		//				new Resource[] { model.createResource(this.localName)});
	new SingletonIterator(model.createResource(this.localName));
		// The pleasing types
		Iterator pleasing = pleasingTypeIterator();

		Iterator fakeStopPleasing = new NullIterator() {
			public boolean hasNext() {
				pleasingTypeSet = new HashSet();
				return false;
			}
		};

		// Subjects that are not objects of anything.
		Iterator nonObjects = new FilterIterator(new Filter() {
			public boolean accept(Object o) {
				return (!objectTable.containsKey(o))
					&& (!wantReification((Resource) o));
			}
		}, modelListSubjects());
		// At these stage we evaluate a dependency graph of the remaining resources.
		// This is stuck in the master iterator so that it's hasNext is called
		// at an appropriate time (after the earlier stages, before the later stages).
		// We use this to trigger the dependency graph evalaution.
		Iterator fakeLazyEvaluator = new NullIterator() {
			public boolean hasNext() {
					// Evalaute dependency graph.
	findInfiniteCycles();
				return false;
			}
		};
		// non-anonymous resources that are the object of more than one
		// triple that are in infinite cycles.
		Iterator firstChoiceCyclic = new FilterIterator(new Filter() {
			public boolean accept(Object o) {
				Resource r = (Resource) o;
				codeCoverage[4]++;
				if (r.isAnon())
					return false;
				Integer cnt = (Integer) objectTable.get(r);
				if (cnt == null || cnt.intValue() <= 1)
					return false;
				return true;
			}
		}, this.allInfiniteLeft());
		// any non genuinely anonymous resources that are in infinite cycles
		Iterator nonAnonInfinite = new FilterIterator(new Filter() {
			public boolean accept(Object o) {
				codeCoverage[5]++;
				Resource r = (Resource) o;
				return !isGenuineAnon(r);
			}
		}, allInfiniteLeft());
		// any other resource in an infinite cyle
		Iterator infinite = allInfiniteLeft();
		Iterator anotherFake = new NullIterator() {
			public boolean hasNext() {
				avoidExplicitReification = false;
				return false;
			}
		};
		Iterator reifications = new FilterIterator(new Filter() {
			public boolean accept(Object o) {
				codeCoverage[6]++;
				return res2statement.containsKey(o);
			}
		}, allInfiniteLeft());
		// any other resource.
		Iterator backStop = modelListSubjects();

		Iterator all[] =
			new Iterator[] {
				currentFile,
				pleasing,
				fakeStopPleasing,
				nonObjects,
				fakeLazyEvaluator,
				firstChoiceCyclic,
				nonAnonInfinite,
				infinite,
				anotherFake,
				reifications,
				new NullIterator() {
					public boolean hasNext() { if (
						modelListSubjects()
						.hasNext())
						codeCoverage[7]++;
					return false;
				}
			}, backStop };
		Iterator allAsOne = new IteratorIterator(new ArrayIterator(all));

		// Filter for those that still have something to list.
		return new FilterIterator(new Filter() {
			public boolean accept(Object o) {
				return hasProperties((Resource) o);
			}
		}, allAsOne);
	}

	private Set openResIterators = new HashSet();

	private synchronized void close(ResIterator resIt) {
		resIt.close();
		openResIterators.remove(resIt);
	}

	private synchronized void closeAllResIterators() {
		Iterator members = openResIterators.iterator();
		while (members.hasNext()) {
			((ResIterator) members.next()).close();
		}
		openResIterators = new HashSet();
	}

	private Iterator modelListSubjects() {
		ResIterator resIt = model.listSubjects();
		openResIterators.add(resIt);
		return resIt;

	}

}

/*
    (c) Copyright 2000, 2001, 2002, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
