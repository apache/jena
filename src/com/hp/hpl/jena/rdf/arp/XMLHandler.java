/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * (c) Copyright 2003, Plugged In Software 
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 1.
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * $Id: XMLHandler.java,v 1.8 2005-02-21 12:09:29 andy_seaborne Exp $
 * 
 * AUTHOR: Jeremy J. Carroll
 */
/*
 * ARPFilter.java
 * 
 * Created on June 21, 2001, 10:01 PM
 */

package com.hp.hpl.jena.rdf.arp;

import java.util.*;

import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;



/**
 * This class converts SAX events into a stream
 * of encapsulated events suitable for the RDF parser.
 * In effect, this is the RDF lexer.
 * updates by kers to handle exporting namespace prefix maps.
 * 
 * @author jjc
 */
abstract class XMLHandler
	extends LexicalHandlerImpl 
	implements RDFParserConstants, ARPErrorNumbers {
	static {
		//    org.apache.xerces.utils.XMLCharacterProperties.initCharFlags();
		CharacterModel.isFullyNormalizedConstruct(
			"make the linkage error happen early");
		ARP.initEncoding();
	}

	boolean encodingProblems = false;


	public void startPrefixMapping(String prefix, String uri)
		 {
		handlers.getNamespaceHandler().startPrefixMapping(prefix,uri);
		
	}
	public void endPrefixMapping(String prefix)
		 {
		handlers.getNamespaceHandler().endPrefixMapping(prefix);
	}



	void userWarning(ParseException e) throws SAXException {
		//try {
		handlers.getErrorHandler().warning(e.rootCause());
		//}
		//catch (Exception ee){
	//		throw new WrappedException(ee);
		//}
	}
	void userError(ParseException e) throws SAXException {
		if (e.getFatal())
			handlers.getErrorHandler().fatalError(e.rootCause());
		else
			handlers.getErrorHandler().error(e.rootCause());
	}
	private Map nodeIdUserData;


	Locator getLocator() {
		return pipe == null ? null : pipe.getLocator();
	}
	StatementHandler getStatementHandler() {
		return handlers.getStatementHandler();
	}
	ARPHandlers getHandlers() {
		return handlers;
	}

	ARPOptions getOptions() {
		return options;
	}
	void setOptionsWith(ARPOptions newOpts) {
		options = newOpts.copy();
	}
	void setHandlersWith(ARPHandlers newHh){
		handlers = newHh.copy();
	}	// accessed in ARPQname.
	XMLContext documentContext;
	//String documentURI;
	TokenPipe pipe;
	Locator locator;
	static final String rdfns =
		"http://www.w3.org/1999/02/22-rdf-syntax-ns#".intern();
	static final String xmlns = "http://www.w3.org/XML/1998/namespace".intern();
	static final Map rdfnames = new HashMap();
	static {
		rdfnames.put("Description", new Integer(E_DESCRIPTION));
		rdfnames.put("RDF", new Integer(E_RDF));
		rdfnames.put("li", new Integer(E_LI));
	}
	static final Set knownRDFProperties = new HashSet();
	static final Set knownRDFTypes = knownRDFProperties;
	// WG decision makes this distinction spurious.
	//new HashSet();
	static {
		knownRDFTypes.add("Bag");
		knownRDFTypes.add("Seq");
		knownRDFTypes.add("Alt");
		knownRDFTypes.add("List");
		knownRDFTypes.add("XMLLiteral");
		knownRDFTypes.add("Property");
		knownRDFProperties.add("type");
		knownRDFTypes.add("Statement");
		knownRDFProperties.add("subject");
		knownRDFProperties.add("predicate");
		knownRDFProperties.add("object");
		knownRDFProperties.add("value");
		knownRDFProperties.add("first");
		knownRDFProperties.add("rest");
		// not strictly true.
		knownRDFProperties.add("nil");
	}
	static final Set knownBadRDFNames = new HashSet();
	static {
		knownBadRDFNames.add("ID");
		knownBadRDFNames.add("about");
		knownBadRDFNames.add("aboutEach");
		knownBadRDFNames.add("aboutEachPrefix");
		knownBadRDFNames.add("resource");
		knownBadRDFNames.add("bagID");
		knownBadRDFNames.add("parseType");
		knownBadRDFNames.add("datatype");
		knownBadRDFNames.add("li");
		knownBadRDFNames.add("type");
		knownBadRDFNames.add("Description");
		knownBadRDFNames.add("nodeID");
	}
	// The order of these must match their occurrence in grammar rules.
	static private String specialAtts[] =
		{ "base", "lang", "space", "ID", "about", "nodeID", "resource",
		//	"bagID",
		"parseType", "datatype", "type" };
	static private String specialNameSpaces[] =
		{ xmlns, xmlns, xmlns, rdfns, rdfns, rdfns,
		//	rdfns,
		rdfns, rdfns, rdfns, rdfns };
	//  static private int A_XMLSPACE = -1;
	static private int specialAttValues[] =
		{
			A_XMLBASE,
			A_XMLLANG,
			A_XMLSPACE,
			A_ID,
			A_ABOUT,
			A_NODEID,
			A_RESOURCE,
		//	A_BAGID,
		A_PARSETYPE, A_DATATYPE, A_TYPE, };

	void warning(int id, String s) {
		try {
			switch (options.getErrorMode()[id]) {
				case EM_IGNORE :
					break;
				case EM_WARNING :
					handlers.getErrorHandler().warning(new ParseException(id, s));
					break;
				case EM_ERROR :
					handlers.getErrorHandler().error(new ParseException(id, s));
					break;
				case EM_FATAL :
					handlers.getErrorHandler().fatalError(new ParseException(id, s));
					break;
			}

		} catch (SAXException e) {
			throw new WrappedException(e);
		}
	}
	
	private ARPOptions options = new ARPOptions();
	private ARPHandlers handlers = new ARPHandlers();
	
	void parseWarning(int id, Location where, String s) throws ParseException {
		parseWarning(id, where, s, null);
	}
	void parseWarning(int id, Location where, String s, SAXParseException saxe)
		throws ParseException {
		int mode = options.getErrorMode()[id];
		if (mode == EM_IGNORE)
			return;
		ParseException pe = new ParseException(id, where, s, saxe);
		if (mode == EM_FATAL) {
			pe.setFatal(true);
			mode = EM_ERROR;
		}
		if (mode == EM_ERROR)
			throw pe;

		try {
			userWarning(pe);
			return;
		} catch (ParseException rethrown) {
			if (rethrown == pe)
				throw rethrown;
			throw new WrappedException(pe);
		} catch (SAXException e) {
			throw new WrappedException(e);
		}
	}
	void parseWarning(Warn w) throws ParseException {
		parseWarning(w.number, w.location, w.msg);
	}
	void putWarning(int no, Location where, String msg) throws SAXParseException {
		pipe.putNextToken(new Warn(no, where, msg));
	}

	void setUserData(String nodeId, Object v) {
		nodeIdUserData.put(nodeId, v);
	}

	Object getUserData(String nodeId) {
		return nodeIdUserData.get(nodeId);
	}
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	private void doSpecialAtt(
		int ix,
		int attName,
		String ns,
		BitSet attsDone,
		Attributes atts,
		Location where)
		throws SAXException {

		attsDone.set(ix);

		if (attName == A_XMLSPACE)
			return;

		pipe.putNextToken(
			new ARPQname(attName, where, ns, null, atts.getQName(ix)));
		String val = atts.getValue(ix);

		if (attName == A_PARSETYPE) {
			if (val.equals("Resource")) {
				pipe.putNextToken(new StrToken(AV_RESOURCE, where, val));
			} else if (val.equals("Collection")) {
				pipe.putNextToken(new StrToken(AV_COLLECTION, where, val));
			} else if (
				val.equals("daml:collection")
					&& options.getErrorMode()[WARN_IN_STRICT_MODE] != EM_ERROR) {
				pipe.putNextToken(new StrToken(AV_DAMLCOLLECTION, where, val));
				putWarning(
					IGN_DAML_COLLECTION,
					where,
					"Illegal parseType: " + val);
			} else {
				pipe.putNextToken(new StrToken(AV_LITERAL, where, val));
				if (!val.equals("Literal")) {
					putWarning(
						WARN_UNKNOWN_PARSETYPE,
						where,
						"Unknown parseType: " + val);
				}
			}
		} else {
			pipe.putNextToken(new StrToken(AV_STRING, where, val));
		}
	}
	public void startElement(
		String uri,
		String localName,
		String rawName,
		Attributes atts)
		throws SAXException {
		Location where = new Location(locator);
		putElementQname(uri, localName, rawName, where);
		BitSet attsDone = new BitSet();

		for (int i = 0; i < atts.getLength(); i++) {
			String qn = atts.getQName(i);
			String prefix;
			if (qn.startsWith("xmlns")) {
				prefix = "";
				if (qn.equals("xmlns")) {
				} else if (qn.charAt(5) == ':') {
					prefix = qn.substring(6);
					//					atts.getLocalName(i);
				} else {
					continue;
				}

				attsDone.set(i);
				pipe.putNextToken(new StrToken(A_XMLNS, where, prefix));
				String nsuri = atts.getValue(i);
				pipe.putNextToken(new StrToken(AV_STRING, where, nsuri));
				// System.err.println(prefix + " => " + atts.getValue(i));
				if (nsuri.startsWith(rdfns) && !nsuri.equals(rdfns))
					putWarning(
						WARN_BAD_RDF_NAMESPACE_URI,
						where,
						"Namespace URI ref "
							+ nsuri
							+ " may not be used in RDF/XML.");
				if (nsuri.startsWith(xmlns) && !nsuri.equals(xmlns))
					putWarning(
						WARN_BAD_XML_NAMESPACE_URI,
						where,
						"Namespace URI ref "
							+ nsuri
							+ " may not be used in RDF/XML.");

			}
		}
		for (int i = 0; i < specialAtts.length; i++) {
			int ix = atts.getIndex(specialNameSpaces[i], specialAtts[i]);
			if (ix != -1) {
				doSpecialAtt(
					ix,
					specialAttValues[i],
					specialNameSpaces[i],
					attsDone,
					atts,
					where);
			}
			if (specialNameSpaces[i] == rdfns) {
				ix = atts.getIndex("", specialAtts[i]);
				if (ix != -1) {
					putWarning(
						WARN_UNQUALIFIED_RDF_ATTRIBUTE,
						where,
						"Unqualified use of rdf:"
							+ atts.getLocalName(ix)
							+ " has been deprecated.");
					doSpecialAtt(
						ix,
						specialAttValues[i],
						"",
						attsDone,
						atts,
						where);
				}
			}
		}
		for (int i = 0; i < atts.getLength(); i++) {
			if (!attsDone.get(i)) {
				String ns = atts.getURI(i);
				String qn = atts.getQName(i);
				if (qn.length() >= 3
					&& qn.substring(0, 3).toLowerCase().equals("xml")) {
					putWarning(
						WARN_UNKNOWN_XML_ATTRIBUTE,
						where,
						"XML attribute: "
							+ atts.getQName(i)
							+ " is not known and is being discarded.");
					continue;
				}
				if (ns.equals("")) {
					putWarning(
						WARN_UNQUALIFIED_ATTRIBUTE,
						where,
						"Attribute: "
							+ atts.getLocalName(i)
							+ ". Unqualified use is deprecated. Assuming namespace: "
							+ uri);
					ns = uri;
				}
				putAttributeQname(ns, atts.getLocalName(i), qn, where);
				pipe.putNextToken(
					new StrToken(AV_STRING, where, atts.getValue(i)));
			}
		}
	}

	public void endElement(String uri, String localName, String rawName)
		throws SAXException {
		Location loc = new Location(locator);
		pipe.putNextToken(new Token(E_END, loc));
	}
	public void characters(char ch[], int start, int length)
		throws SAXException {
		Location loc = new Location(locator);
		pipe.putNextToken(
			new StrToken(CD_STRING, loc, new String(ch, start, length)));
	}
	public void ignorableWhitespace(char ch[], int start, int length)
		throws SAXException { // Never called.
		characters(ch, start, length);
	}
	private boolean isMemberProperty(String name) {
		if (name.startsWith("_")) {
			String number = name.substring(1);
			if (number.startsWith("-") || number.startsWith("0"))
				return false;
			try {
				Integer.parseInt(number);
				return true;
			} catch (NumberFormatException e) {
				try {
					// It might be > Integer.MAX_VALUE
					new java.math.BigInteger(number);
					return true;
				} catch (NumberFormatException ee) {
					return false;
				}
			}
		}
		return false;
	}
	private boolean isKnownRDFProperty(String name) {
		return knownRDFProperties.contains(name);
	}
	private void putElementQname(
		String uri,
		String localName,
		String q,
		Location where)
		throws SAXException {
		Token warn = null;
		if (uri.equals(rdfns)) {
			Integer val = (Integer) rdfnames.get(localName);
			if (val == null) {
				if (isMemberProperty(localName)) {
					pipe.putNextToken(
						new ARPQname(E_RDF_N, where, uri, localName, q));
					return;
				} else if (
					!(knownRDFTypes.contains(localName)
						|| isKnownRDFProperty(localName))) {
					if (knownBadRDFNames.contains(localName))
						warn =
							new Warn(
								ERR_BAD_RDF_ELEMENT,
								where,
								"Creating statement(s) for syntactic RDF element: '<rdf:"
									+ localName
									+ "'.");
					else
						warn =
							new Warn(
								WARN_UNKNOWN_RDF_ELEMENT,
								where,
								"Creating statement(s) for unknown RDF element: '<rdf:"
									+ localName
									+ "'.");
				}
			} else {
				pipe.putNextToken(
					new ARPQname(val.intValue(), where, uri, localName, q));
				return;
			}
		}
		pipe.putNextToken(new ARPQname(E_OTHER, where, uri, localName, q));
		if (warn != null)
			pipe.putNextToken(warn);
	}

	private void putAttributeQname(
		String ns,
		String local,
		String q,
		Location where)
		throws SAXException {
		if (ns.equals(rdfns)) {
			if (isMemberProperty(local)) {
				pipe.putNextToken(new ARPQname(A_RDF_N, where, ns, local, q));
				return;
			} else if (!isKnownRDFProperty(local)) {
				if (knownBadRDFNames.contains(local))
					putWarning(
						ERR_BAD_RDF_ATTRIBUTE,
						where,
						"Inappropriate or removed RDF attribute: 'rdf:"
							+ local
							+ "'.");
				else
					putWarning(
						WARN_UNKNOWN_RDF_ATTRIBUTE,
						where,
						"Creating statement for unknown RDF property: 'rdf:"
							+ local
							+ "'.");
			}
		}
		pipe.putNextToken(new ARPQname(A_OTHER, where, ns, local, q));
	}




	public void comment(char[] ch, int start, int length) throws SAXParseException {
		Location where = new Location(locator);
		pipe.putNextToken(
			new StrToken(COMMENT, where, new String(ch, start, length)));
	}

	public void processingInstruction(String target, String data)
		throws SAXException {
		Location where = new Location(locator);
		pipe.putNextToken(
			new StrToken(
				PROCESSING_INSTRUCTION,
				where,
				(data == null ? target : target + " " + data)));
		//     pipe.putNextToken( new ARPQname(E_RDF_N,where,uri,localName, q) );
	}

	public void error(SAXParseException e) throws SAXParseException {
		saxError(ERR_SAX_ERROR, e);
	}
	public void warning(SAXParseException e) throws SAXParseException {
		saxError(WARN_SAX_WARNING, e);
	}
	public void fatalError(SAXParseException e) throws SAXException {
		saxError(ERR_SAX_FATAL_ERROR, e);
		throw new FatalParsingErrorException();
	}
	void generalError(int i, Exception e) throws SAXParseException {
		Location where = new Location(locator);
		//   System.err.println(e.getMessage());
		pipe.putNextToken(new ExceptionToken(i, where, e));

	}
	private void saxError(int i, SAXParseException e) throws SAXParseException {
		Location where =
			new Location(
				e.getSystemId(),
				e.getLineNumber(),
				e.getColumnNumber());

		pipe.putNextToken(new ExceptionToken(i, where, e));

	}

	/**
	 * @param v
	 */
	void endLocalScope(Object v) {
		
		if (handlers.getExtendedHandler() != ARPHandlers.nullScopeHandler 
		  && v != null
		  && v instanceof ARPResource) {
			ARPResource bn = (ARPResource) v;
			if (!bn.isAnonymous())
				return;
		  if (!bn.getHasBeenUsed())
		    return;
			if (bn.hasNodeID()) {
				// save for later end scope
				if ( handlers.getExtendedHandler().discardNodesWithNodeID())
				  return;
				  
				String bnodeID = bn.nodeID;
				if (!nodeIdUserData.containsKey(bnodeID))
					nodeIdUserData.put(bnodeID, null);
			} else {
				handlers.getExtendedHandler().endBNodeScope(bn);
				
			}
		}
	}

	void endRDF() {
		handlers.getExtendedHandler().endRDF();
	}
	void startRDF() {
		handlers.getExtendedHandler().startRDF();
	}

	boolean ignoring(int eCode) {
		return options.getErrorMode()[eCode]==EM_IGNORE;
	}
	protected void initParse(String base) throws MalformedURIException {
		nodeIdUserData = new HashMap();
		//String base = input.getSystemId();
		if (base == null) {
			warning(
				IGN_NO_BASE_URI_SPECIFIED,
				"Base URI not specified for input file; local URI references will be in error.");
			documentContext =
				new XMLNullContext(this, ERR_RESOLVING_URI_AGAINST_NULL_BASE);
	
		} else if (base.equals("")) {
			warning(
				IGN_NO_BASE_URI_SPECIFIED,
				"Base URI specified as \"\"; local URI references will not be resolved.");
			documentContext =
				new XMLNullContext(this, WARN_RESOLVING_URI_AGAINST_EMPTY_BASE);
		} else {
			base = ParserSupport.truncateXMLBase(base);
	
			documentContext = new XMLContext(base);
		}
	}
	void endBnodeScope() {
		if ( getHandlers().getExtendedHandler() != ARPHandlers.nullScopeHandler ) {
			Iterator it = nodeIdUserData.keySet().iterator();
			while (it.hasNext()) {
				String nodeId = (String)it.next();
				ARPResource bn = new ARPResource(this);
				bn.setNodeId(nodeId);
				getHandlers().getExtendedHandler().endBNodeScope(bn);
			}
		}
	}
	

}
