/*
 * (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP All rights
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
 * $Id: ARPFilter.java,v 1.20 2004-01-27 17:34:08 jeremy_carroll Exp $
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
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.apache.xerces.parsers.*;
import org.apache.xerces.xni.parser.*;
import org.apache.xerces.xni.*;

import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.ext.LexicalHandler;
import java.io.*;

import org.apache.xerces.util.EncodingMap;

/**
 * updates by kers to handle exporting namespace prefix maps.
 * 
 * @author jjc
 */
class ARPFilter
	extends XMLFilterImpl
	implements RDFParserConstants, ARPErrorNumbers, LexicalHandler {
	static {
		//    org.apache.xerces.utils.XMLCharacterProperties.initCharFlags();
		CharacterModel.isFullyNormalizedConstruct(
			"make the linkage error happen early");
		ARP.initEncoding();
	}
	private XMLPullParserConfiguration pullParser;
	private SAXParser saxParser;
	private ARPFilter(SAXParser rdr, XMLPullParserConfiguration config) {
		super(rdr);
		pullParser = config;
		saxParser = rdr;
		rdr.setEntityResolver(this);
		rdr.setDTDHandler(this);
		rdr.setContentHandler(this);
		rdr.setErrorHandler(this);
		setErrorHandler(new DefaultErrorHandler());
	}

	/**
	 * we store all the prefix mappings that are seen during the parse. set* of
	 * all its bindings. In the nice case, prefixes that are present will be
	 * bound to singleton sets.
	 */
//	private Map prefixMap = new HashMap();

	/**
	 * over-ridden from XMLFilterImpl: catch a namespace prefix mapping as it
	 * goes past.
	 * 
	 * @param prefix
	 *            the name of the prefix (ie the X in xmlns:X=U)
	 * @param uri
	 *            the uri string (ie the U)
	 */
	public void startPrefixMapping(String prefix, String uri)
		throws SAXException {
		super.startPrefixMapping(prefix, uri);
		nameHandler.startPrefixMapping(prefix,uri);
		/*
		Set uris = (Set) prefixMap.get(prefix);
		if (uris == null) {
			uris = new HashSet();
			prefixMap.put(prefix, uris);
		}
		uris.add(uri);
		*/
	}
	public void endPrefixMapping(String prefix)
		throws SAXException {
		super.endPrefixMapping(prefix);
		nameHandler.endPrefixMapping(prefix);
	}

	/**
	 * add the prefixes we have remembered to a supplied map x. This way we
	 * don't expose our internal map to updates.
	 * 
	 * @param x
	 *            the map to be updated
	 * @return the updated map
	 * /
	public Map getPrefixes(Map x) {
		x.putAll(prefixMap);
		return x;
	}
	*/

	void userWarning(ParseException e) throws SAXException {
		getErrorHandler().warning(e.rootCause());
	}
	void userError(ParseException e) throws SAXException {
		if (e.getFatal())
			getErrorHandler().fatalError(e.rootCause());
		else
			getErrorHandler().error(e.rootCause());
	}
	/*
	 * void userFatalError(SAXParseException e) throws SAXException {
	 * getErrorHandler().fatalError(e); }
	 */
	static private class MySAXParser extends SAXParser {
		MySAXParser(StandardParserConfiguration c) {
			super(c);
			try {
			setFeature("http://xml.org/sax/features/string-interning",false);
			}
			catch (SAXException e){
				// Not supported - aggh
				// TODO ask on xerces list why not?
			//	e.printStackTrace();
			}
		}
		ARPFilter a;
		public void xmlDecl(
			String version,
			String encoding,
			String standalone,
			Augmentations augs) {
			a.setEncoding(encoding == null ? "UTF" : encoding);
			super.xmlDecl(version, encoding, standalone, augs);
		}
		/*
		 * public void startDocument(XMLLocator locator, java.lang.String
		 * encoding, NamespaceContext namespaceContext, Augmentations augs) {
		 * a.setEncoding(encoding);
		 * super.startDocument(locator,encoding,namespaceContext,augs); }
		 *  
		 */
	}
	static ARPFilter create() {
		StandardParserConfiguration c = new StandardParserConfiguration();
		MySAXParser msp = new MySAXParser(c);
		ARPFilter a = new ARPFilter(msp, c);
		msp.a = a;
		return a;
	}

	private Map nodeIdUserData;

	private boolean embedding = false;
	boolean setEmbedding(boolean x) {
		boolean old = embedding;
		embedding = x;
		return old;
	}

	XMLInputSource convert(InputSource in) {
		Reader rdr = in.getCharacterStream();
		InputStream str = in.getByteStream();
		String publicID = in.getPublicId();
		String systemID = in.getSystemId();
		readerXMLEncoding = null;
		encodingProblems = false;
		if (rdr == null && str == null) {
			return new XMLInputSource(publicID, systemID, systemID);
		} else if (rdr == null) {
			return new XMLInputSource(publicID, systemID, systemID, str, null);
		} else if (str == null) {
			if (rdr instanceof InputStreamReader) {
				String enc = ((InputStreamReader) rdr).getEncoding();
				readerXMLEncoding = EncodingMap.getJava2IANAMapping(enc);
				if (readerXMLEncoding == null)
					readerXMLEncoding = enc;
				//     System.err.println("readerXMLEncoding = " +
				// readerXMLEncoding);
			}
			return new XMLInputSource(publicID, systemID, systemID, rdr, null);
		}
		return null;
	}
	boolean parseSome() {
		try {
			return pullParser.parse(false);
		} catch (UTFDataFormatException e) {
			generalError(ERR_UTF_ENCODING, e);
			return false;
		} catch (IOException e) {
			generalError(ERR_GENERIC_IO, e);
			return false;
		} catch (DontDieYetException e) {
			return false;
		}
	}
	private String readerXMLEncoding = null;
	// Might be "UTF" to indicate we don't know if it is UTF-8 or UTF-16
	private String xmlEncoding = null;
	boolean encodingProblems = false;
	public void setEncoding(String e) {
		e = e.toUpperCase();
		//  System.err.println("xmlEncoding = " + e);
		if (e != null && xmlEncoding == null) {
			// special case UTF-8 or UTF-16?
			if (e.equals("UTF")
				&& readerXMLEncoding != null
				&& readerXMLEncoding.startsWith("UTF")) {
				xmlEncoding = readerXMLEncoding;
				return;
			}
			xmlEncoding = e;
			if (readerXMLEncoding != null && !readerXMLEncoding.equals(e)) {
				this.putWarning(
					WARN_ENCODING_MISMATCH,
					new Location(locator),
					"Encoding on InputStreamReader or FileReader does not match that of XML document. Use FileInputStream. ["
						+ readerXMLEncoding
						+ " != "
						+ e
						+ "]");
				encodingProblems = true;
				/*
				 * if ((readerXMLEncoding.indexOf("IBM") != -1) !=
				 * (xmlEncoding.indexOf("IBM") != -1)) { this.putWarning(
				 * ERR_ENCODING_MISMATCH, new Location(locator), "IBM encodings
				 * may be wholly incompatible with non-IBM encodings."); }
				 */
			}
		}
	}
	Locator getLocator() {
		return pipe == null ? null : pipe.getLocator();
	}
	synchronized public void parse(InputSource input)
		throws IOException, SAXException {
		parse(input, input.getSystemId());
	}
	synchronized public void parse(InputSource input, String base)
		throws IOException, SAXException {
		// Make sure we have a sane state for
		// Namespace processing.
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
		// Start the RDFParser
		pipe = new TokenPipe(this);
		pullParser.setInputSource(convert(input));
		saxParser.setFeature("http://xml.org/sax/features/namespaces", true);
		saxParser.setFeature(
			"http://xml.org/sax/features/namespace-prefixes",
			true);
		saxParser.setProperty(
			"http://xml.org/sax/properties/lexical-handler",
			this);
		saxParser.reset();

		// initEncodingChecks();
		try {
			try {
				RDFParser p = new RDFParser(pipe, ARPFilter.this);
				if (embedding)
					p.embeddedFile(documentContext);
				else
					p.rdfFile(documentContext);
			} catch (WrappedException wrapped) {
				wrapped.throwMe();
			} catch (ParseException parse) {
				throw parse.rootCause();
			}
		} finally {
			if ( scopeHandler != nullScopeHandler ) {
				Iterator it = nodeIdUserData.keySet().iterator();
				while (it.hasNext()) {
					String nodeId = (String)it.next();
					ARPResource bn = new ARPResource(this);
					bn.setNodeId(nodeId);
					scopeHandler.endBNodeScope(bn);
				}
			}
		}

	}
	private NamespaceHandler nameHandler = new NamespaceHandler() {

		public void startPrefixMapping(String prefix, String uri) {
			
		}

		public void endPrefixMapping(String prefix) {
			
		}
	};
	// Add scope handler
	NamespaceHandler setNamespaceHandler(NamespaceHandler sh) {
		NamespaceHandler old = nameHandler;
		nameHandler = sh;
		return old;
	}
	// Add scope handler
	ExtendedHandler setExtendedHandler(ExtendedHandler sh) {
		ExtendedHandler old = scopeHandler;
		scopeHandler = sh;
		return old;
	}
	static ExtendedHandler nullScopeHandler = new ExtendedHandler() {

		public void endBNodeScope(AResource bnode) {
		}

		public void startRDF() {
		}

		public void endRDF() {
		}

		public boolean discardNodesWithNodeID() {
			return true;
		}
	};
	ExtendedHandler scopeHandler = nullScopeHandler;

	StatementHandler setStatementHandler(StatementHandler sh) {
		StatementHandler old = statementHandler;
		statementHandler = sh;
		return old;
	}
	StatementHandler statementHandler = new StatementHandler() {
		public void statement(AResource s, AResource p, AResource o) {
		}
		public void statement(AResource s, AResource p, ALiteral o) {
		}
	};
	// accessed in ARPQname.
	XMLContext documentContext;
	//String documentURI;
	private TokenPipe pipe;
	private Locator locator;
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

	private void warning(int id, String s) {
		try {
			switch (errorMode[id]) {
				case EM_IGNORE :
					break;
				case EM_WARNING :
					getErrorHandler().warning(new ParseException(id, s));
					break;
				case EM_ERROR :
					getErrorHandler().error(new ParseException(id, s));
					break;
				case EM_FATAL :
					getErrorHandler().fatalError(new ParseException(id, s));
					break;
			}

		} catch (SAXException e) {
			throw new WrappedException(e);
		}
	}
	// a bit excessive in length!
	static private int defaultErrorMode[] = new int[400];
	static {
		for (int i = 0; i < defaultErrorMode.length; i++)
			defaultErrorMode[i] = i / 100;
	}
	private int errorMode[] = (int[]) defaultErrorMode.clone();
	void setDefaultErrorMode() {
		errorMode = (int[]) defaultErrorMode.clone();
	}
	void setLaxErrorMode() {
		setDefaultErrorMode();
		for (int i = 100; i < 200; i++)
			setErrorMode(i, EM_IGNORE);
		setErrorMode(WARN_MINOR_INTERNAL_ERROR, EM_WARNING);
	}
	void setStrictErrorMode() {
		setStrictErrorMode(EM_IGNORE);
	}

	void setStrictErrorMode(int nonErrorMode) {
		setDefaultErrorMode();
		for (int i = 1; i < 100; i++)
			setErrorMode(i, nonErrorMode);
		int warning = EM_WARNING;
		int error = EM_ERROR;
		switch (nonErrorMode) {
			case EM_ERROR :
				warning = EM_ERROR;
				break;
			case EM_FATAL :
				warning = error = EM_FATAL;
				break;
		}
		for (int i = 100; i < 200; i++)
			setErrorMode(i, error);
		// setErrorMode(IGN_XMLBASE_USED,warning);
		// setErrorMode(IGN_XMLBASE_SIGNIFICANT,error);
		setErrorMode(WARN_MINOR_INTERNAL_ERROR, warning);
		setErrorMode(WARN_MINOR_INTERNAL_ERROR, warning);
		setErrorMode(WARN_DEPRECATED_XMLLANG, warning);
		setErrorMode(WARN_STRING_NOT_NORMAL_FORM_C, warning);
		//       setErrorMode(WARN_EMPTY_ABOUT_EACH,nonErrorMode);
		setErrorMode(WARN_UNKNOWN_PARSETYPE, warning);
		//     setErrorMode(WARN_BAD_XML, nonErrorMode);
		setErrorMode(WARN_PROCESSING_INSTRUCTION_IN_RDF, nonErrorMode);
		setErrorMode(WARN_LEGAL_REUSE_OF_ID, nonErrorMode);
		setErrorMode(WARN_RDF_NN_AS_TYPE, nonErrorMode);
		setErrorMode(WARN_UNKNOWN_RDF_ELEMENT, warning);
		setErrorMode(WARN_UNKNOWN_RDF_ATTRIBUTE, warning);
		setErrorMode(WARN_UNQUALIFIED_RDF_ATTRIBUTE, warning);
		setErrorMode(WARN_UNKNOWN_XML_ATTRIBUTE, nonErrorMode);
		// setErrorMode(WARN_QNAME_AS_ID, error);
		//      setErrorMode(WARN_BAD_XML, error);
		setErrorMode(WARN_SAX_WARNING, warning);
		setErrorMode(IGN_DAML_COLLECTION, error);
	}
	int setErrorMode(int errno, int mode) {
		int old = errorMode[errno];
		switch (mode) {
			case EM_WARNING :
			case EM_IGNORE :
				if (errno >= 100 * EM_ERROR && errno != ERR_NOT_WHITESPACE)
					break;
			case EM_ERROR :
			case EM_FATAL :
				switch (errno) {
					case ERR_UNABLE_TO_RECOVER :
						break;
					default :
						errorMode[errno] = mode;
				}
		}
		return old;
	}
	void parseWarning(int id, Location where, String s) throws ParseException {
		parseWarning(id, where, s, null);
	}
	void parseWarning(int id, Location where, String s, SAXParseException saxe)
		throws ParseException {
		int mode = errorMode[id];
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
	private void putWarning(int no, Location where, String msg) {
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
		super.setDocumentLocator(locator);
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
					&& errorMode[WARN_IN_STRICT_MODE] != EM_ERROR) {
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

	public void endEntity(java.lang.String str) {
	}

	public void endDTD() {
	}

	public void startDTD(String str, String str1, String str2) {
	}

	public void endCDATA() {
	}

	public void startCDATA() {
	}

	public void comment(char[] ch, int start, int length) {
		Location where = new Location(locator);
		pipe.putNextToken(
			new StrToken(COMMENT, where, new String(ch, start, length)));
	}

	public void startEntity(java.lang.String str) {
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

	public void error(SAXParseException e) {
		saxError(ERR_SAX_ERROR, e);
	}
	public void warning(SAXParseException e) {
		saxError(WARN_SAX_WARNING, e);
	}
	public void fatalError(SAXParseException e) {
		saxError(ERR_SAX_FATAL_ERROR, e);
		throw new DontDieYetException();
	}
	private void generalError(int i, Exception e) {
		Location where = new Location(locator);
		//   System.err.println(e.getMessage());
		pipe.putNextToken(new ExceptionToken(i, where, e));

	}
	private void saxError(int i, SAXParseException e) {
		Location where =
			new Location(
				e.getSystemId(),
				e.getLineNumber(),
				e.getColumnNumber());

		pipe.putNextToken(new ExceptionToken(i, where, e));

	}

	private static class DontDieYetException extends RuntimeException {
	}
	/**
	 * @param v
	 */
	public void endLocalScope(Object v) {
		
		if (scopeHandler != nullScopeHandler 
		  && v != null
		  && v instanceof ARPResource) {
			ARPResource bn = (ARPResource) v;
			if (!bn.isAnonymous())
				return;
		  if (!bn.getHasBeenUsed())
		    return;
			if (bn.hasNodeID()) {
				// save for later end scope
				if ( scopeHandler.discardNodesWithNodeID())
				  return;
				  
				String bnodeID = bn.nodeID;
				if (!nodeIdUserData.containsKey(bnodeID))
					nodeIdUserData.put(bnodeID, null);
			} else {
				scopeHandler.endBNodeScope(bn);
				
			}
		}
	}

	/**
	 * 
	 */
	public void endRDF() {
		scopeHandler.endRDF();
	}
	/**
	 * 
	 */
	public void startRDF() {
		scopeHandler.startRDF();
	}

	public boolean ignoring(int eCode) {
		return errorMode[eCode]==EM_IGNORE;
	}

}
