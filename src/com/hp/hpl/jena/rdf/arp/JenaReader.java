/*
    (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
    [See end of file]
*/

package com.hp.hpl.jena.rdf.arp;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.datatypes.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.xml.sax.InputSource;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;

/** Interface between Jena and ARP.
 *
 * @author jjc
 */
public class JenaReader implements RDFReader, ARPErrorNumbers {

  private final class JRStatementHandler implements StatementHandler {
		static private final int BULK_UPDATE_SIZE = 1000;
		private final BulkUpdateHandler bulk;
		final Triple triples[];
		int ix = 0;
		private JRStatementHandler( BulkUpdateHandler bulk) {
			super();
			this.bulk = bulk;
			triples = new Triple[BULK_UPDATE_SIZE];
		}
		public void statement(
			AResource subj,
			AResource pred,
			AResource obj) {
			try {
				triples[ix++]=convert(subj,pred,obj);
			} catch (JenaException e) {
				errorHandler.error(e);
			}
			if (ix==BULK_UPDATE_SIZE)
			  bulkUpdate();
		}
		public void statement(
			AResource subj,
			AResource pred,
			ALiteral lit) {
				try {
					triples[ix++]=convert(subj,pred,lit);
				} catch (JenaException e) {
					errorHandler.error(e);
				}
				if (ix==BULK_UPDATE_SIZE)
				  bulkUpdate();
		}
		private void bulkUpdate() {
			try {
				if (ix == BULK_UPDATE_SIZE)
				  bulk.add(triples);
				else 
				  bulk.add(Arrays.asList(triples).subList(0,ix));
				ix = 0;
			}
			catch (JenaException e) {
			errorHandler.error(e);
		  }
		}
	}

	static private final int BULK_UPDATE_SIZE = 1000;
  
	/** Sets the reader for the languages RDF/XML and RDF/XML-ABBREV to be JenaReader.
	 * @param m The Model on which to set the reader properties.
	 */
	static public void useMe(Model m) {
		m.setReaderClassName("RDF/XML", JenaReader.class.getName());
		m.setReaderClassName("RDF/XML-ABBREV", JenaReader.class.getName());
	}

	static private final String saxFeaturesURL = "http://xml.org/sax/features/";
	static private final String saxPropertiesURL =
		"http://xml.org/sax/properties/";
	static private final String apacheFeaturesURL =
		"http://apache.org/xml/features/";
	static private final String apachePropertiesURL =
		"http://apache.org/xml/properties/";
	static private final String arpPropertiesURL =
		"http://jena.hpl.hp.com/arp/properties/";
	static private final int arpPropertiesURLLength = arpPropertiesURL.length();

	/** Creates new JenaReader
	 */
	public JenaReader() {
		arpf = ARPFilter.create();
	}
	private ARPFilter arpf;
	private Model model;
	
	public void read(Model model, String url) throws JenaException {

		try {
			URLConnection conn = new URL(url).openConnection();
			String encoding = conn.getContentEncoding();
			if (encoding == null)
				read(model, conn.getInputStream(), url);
			else
				read(
					model,
					new InputStreamReader(conn.getInputStream(), encoding),
					url);
		} catch (FileNotFoundException e) {
		    throw new DoesNotExistException( url );
		} catch (IOException e) {
			throw new JenaException(e);
		}
	}


	/** Converts an ARP literal into a Jena Literal.
	 * @param lit The ARP literal.
	 * @return The Jena Literal.
	 * @deprecated Should never have been public.
	 */
	static public Literal translate(ALiteral lit) {
		return new LiteralImpl(
			lit.toString(),
			lit.getLang(),
			lit.isWellFormedXML(),
			null);
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

	static Node convert(AResource r){
		if (r.isAnonymous()) {
			String id = r.getAnonymousID();
			Node rr = (Node) r.getUserData();
			if (rr == null) {
				rr = Node.createAnon();
				r.setUserData(rr);
			}
			return rr;
		} else {
			return Node.createURI(r.getURI());
		}
	}
	static Triple convert(AResource s,AResource p, AResource o){
	    return Triple.create(convert(s),convert(p),convert(o));
	}
	static Triple convert(AResource s,AResource p, ALiteral o){
		return Triple.create(convert(s),convert(p),convert(o));
	}
	/** Converts an ARP resource into a Jena property.
	 * @param r The ARP resource.
	 * @throws JenaException If r is anonymous, or similarly ill-formed.
	 * @return The Jena property.
	 * @deprecated Should never have been public.
	 */
	static public Property translatePred(AResource r) throws JenaException {
		return new PropertyImpl(r.getURI());
	}


	/**
	 *  Reads from reader, using base URI xmlbase, adding triples to model.
	 * If xmlbase is "" then relative URIs may be added to model.
	 * @param model A model to add triples to.
	 * @param reader The RDF/XML document.
	 * @param xmlBase The base URI of the document or "".
	 */
	private void read(Model m, InputSource inputS, String xmlBase)
		throws JenaException {
			model = m;
			if (xmlBase != null && !xmlBase.equals("")) {
				try {
					URI uri = new URI(xmlBase);
				} catch (MalformedURIException e) {
					errorHandler.error(e);
				}
			}
			arpf.setNamespaceHandler(new NamespaceHandler() {

				public void startPrefixMapping(String prefix, String uri) {
			  	if (PrefixMappingImpl.isNiceURI(uri))
			  	   model.setNsPrefix(prefix, uri);
				}

				public void endPrefixMapping(String prefix) {
				}
			});
read(model.getGraph(), inputS, xmlBase);
	}

	synchronized private void read(final Graph g, InputSource inputS, String xmlBase) {
		
			try {
			final BulkUpdateHandler bulk = g.getBulkUpdateHandler();
			inputS.setSystemId(xmlBase);
			JRStatementHandler handler =new JRStatementHandler(bulk); 
			arpf.setStatementHandler(handler);
		
			arpf.setErrorHandler(new ARPSaxErrorHandler(errorHandler));
			arpf.parse(inputS, xmlBase);
			handler.bulkUpdate();
		} catch (IOException e) {
			throw new JenaException(e);
		} catch (SAXException e) {
			throw new JenaException(e);
		}
	}

	/**
	 *  Reads from reader, using base URI xmlbase, adding triples to model.
	 * If xmlbase is "" then relative URIs may be added to model.
	 * @param model A model to add triples to.
	 * @param reader The RDF/XML document.
	 * @param xmlBase The base URI of the document or "".
	 */
	public void read(final Model model, Reader reader, String xmlBase)
		throws JenaException {
		read(model, new InputSource(reader), xmlBase);
	}
	/**
	 *  Reads from reader, using base URI xmlbase, adding triples to graph.
	 * If xmlbase is "" then relative URIs may be added to graph.
	 * @param g A graph to add triples to.
	 * @param reader The RDF/XML document.
	 * @param xmlBase The base URI of the document or "".
	 */
	public void read(Graph g, Reader reader, String xmlBase)
		throws JenaException {
		read(g, new InputSource(reader), xmlBase);
	}
	/**
	 *  Reads from inputStream, using base URI xmlbase, adding triples to model.
	 * If xmlbase is "" then relative URIs may be added to model.
	 * @param model A model to add triples to.
	 * @param in The RDF/XML document stream.
	 * @param xmlBase The base URI of the document or "".
	 */
	public void read(final Model model, InputStream in, String xmlBase)
		throws JenaException {
		read(model, new InputSource(in), xmlBase);
	}
	/**
		 *  Reads from inputStream, using base URI xmlbase, adding triples to graph.
		 * If xmlbase is "" then relative URIs may be added to graph.
		 * @param g A graph to add triples to.
		 * @param in The RDF/XML document stream.
		 * @param xmlBase The base URI of the document or "".
		 */
		public void read(Graph g, InputStream in, String xmlBase)
			 {
			read(g, new InputSource(in), xmlBase);
		}
	RDFErrorHandler errorHandler = new RDFDefaultErrorHandler();

	/**
	   Change the error handler.
	 *<p>
	 * Note that errors of class {@link ParseException}
	 * can be promoted using the {@link ParseException#promote}
	 * method.
	 * See ARP documentation for {@link org.xml.sax.ErrorHandler} for the
	 * details of error promotion.
	 * @param errHandler The new error handler.
	 * @return The old error handler.
	 */
	public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler) {
		RDFErrorHandler old = this.errorHandler;
		this.errorHandler = errHandler;
		return old;
	}

	/**
	 *
	 * Change a property of the RDF or XML parser.
	 * <p>
	 * This method is untested.
	 * <p>
	 * I do not believe that many of the XML features or properties are in fact
	 * useful for ARP users.
	 * The ARP properties allow fine-grained control over error reporting.
	 * <p>
	 * This interface can be used to set and get:
	 * <dl>
	 * <dt>
	 * SAX2 features</dt>
	 * <dd>
	 * See <a href="http://xml.apache.org/xerces-j/features.html">Xerces features</a>.
	 * Value should be given as a String "true" or "false" or a Boolean.
	 * </dd>
	 * <dt>
	 * SAX2 properties
	 * </dt>
	 * <dd>
	 * See <a href="http://xml.apache.org/xerces-j/properties.html">Xerces properties</a>.
	 * </dd>
	 * <dt>
	 * Xerces features
	 * </dt>
	 * <dd>
	 * See <a href="http://xml.apache.org/xerces-j/features.html">Xerces features</a>.
	 * Value should be given as a String "true" or "false" or a Boolean.
	 * </dd>
	 * <dt>
	 * Xerces properties
	 * </dt>
	 * <dd>
	 * See <a href="http://xml.apache.org/xerces-j/properties.html">Xerces properties</a>.
	 * </dd>
	 * <dt>
	 * ARP properties
	 * </dt>
	 * <dd>
	 * These are referred to either by their property name, (see below) or by
	 * an absolute URL of the form <code>http://jena.hpl.hp.com/arp/properties/&lt;PropertyName&gt;</code>.
	 * The value should be a String, an Integer or a Boolean depending on the property.
	 * <br>
	 * ARP property names and string values are case insensitive.
	 * <br>
	 * <TABLE BORDER="1" CELLPADDING="3" CELLSPACING="0">
	 * <TR BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
	 * <TD COLSPAN=4><FONT SIZE="+2">
	 * <B>ARP Properties</B></FONT></TD>
	 * </TR>
	 * <tr BGCOLOR="#EEEEFF" CLASS="TableSubHeadingColor">
	 * <th>Property Name</th>
	 * <th>Description</th>
	 * <th>Value class</th>
	 * <th>Legal Values</th>
	 * </tr>
	 * <tr BGCOLOR="white" CLASS="TableRowColor">
	 * <td><CODE>error-mode</CODE></td>
	 * <td>
	 * {@link ARP#setDefaultErrorMode}<br>
	 * {@link ARP#setLaxErrorMode}<br>
	 * {@link ARP#setStrictErrorMode}<br>
	 * {@link ARP#setStrictErrorMode(int)}<br>
	 * </td>
	 * <td>String</td>
	 * <td><CODE>default</CODE><br>
	 * <CODE>lax</CODE><br>
	 * <CODE>strict</CODE><br>
	 * <CODE>strict-ignore</CODE><br>
	 * <CODE>strict-warning</CODE><br>
	 * <CODE>strict-error</CODE><br>
	 * <CODE>strict-fatal</CODE><br></td>
	 * </tr>
	 * <tr BGCOLOR="white" CLASS="TableRowColor">
	 * <td><CODE>embedding</CODE></td>
	 * <td>
	 * {@link ARP#setEmbedding}
	 * </td>
	 * <td>String or Boolean</td>
	 * <td><CODE>true</CODE> or <CODE>false</CODE></td>
	 * </tr>
	 * <tr BGCOLOR="white" CLASS="TableRowColor">
	 * <td>
	 * <code>ERR_&lt;XXX&gt;</code><br>
	 * <code>WARN_&lt;XXX&gt;</code><br>
	 * <code>IGN_&lt;XXX&gt;</code></td>
	 * <td>
	 * {@link ARPErrorNumbers}<br>
	 * Any of the error condition numbers listed.<br>
	 * {@link ARP#setErrorMode(int, int)}
	 * </td>
	 * <td>String or Integer</td>
	 * <td>{@link ARPErrorNumbers#EM_IGNORE EM_IGNORE}<br>
	 * {@link ARPErrorNumbers#EM_WARNING EM_WARNING}<br>
	 * {@link ARPErrorNumbers#EM_ERROR EM_ERROR}<br>
	 * {@link ARPErrorNumbers#EM_FATAL EM_FATAL}<br>
	 * </td>
	 * </tr>
	 * </table>
	 * </dd>
	 * </dl>
	 *
	 * @param str The property to set.
	 * @param value The new value; values of class String will be converted into appropriate classes. Values of
	 * class Boolean or Integer will be used for appropriate properties.
	 * @throws JenaException For bad values.
	 * @return The old value, or null if none, or old value is inaccesible.
	 */
	public Object setProperty(String str, Object value) throws JenaException {
		Object obj = value;
		if (str.startsWith("http:")) {
			if (str.startsWith(arpPropertiesURL)) {
				return setArpProperty(
					str.substring(arpPropertiesURLLength),
					obj);
			}
			if (str.startsWith(saxPropertiesURL)
				|| str.startsWith(apachePropertiesURL)) {
				Object old;
				try {
					old = arpf.getProperty(str);
				} catch (SAXNotSupportedException ns) {
					old = null;
				} catch (SAXNotRecognizedException nr) {
					errorHandler.error(new UnknownPropertyException(str));
					return null;
				}
				try {
					arpf.setProperty(str, obj);
				} catch (SAXNotSupportedException ns) {
					errorHandler.error(new JenaException(ns));
				} catch (SAXNotRecognizedException nr) {
					errorHandler.error(new UnknownPropertyException(str));
					return null;
				}
				return old;
			}

			if (str.startsWith(saxFeaturesURL)
				|| str.startsWith(apacheFeaturesURL)) {
				Boolean old;
				try {
					old = new Boolean(arpf.getFeature(str));
				} catch (SAXNotSupportedException ns) {
					old = null;
				} catch (SAXNotRecognizedException nr) {
					errorHandler.error(new UnknownPropertyException(str));
					return null;
				}
				try {
					arpf.setFeature(str, ((Boolean) obj).booleanValue());
				} catch (SAXNotSupportedException ns) {
					errorHandler.error(new JenaException(ns));
				} catch (SAXNotRecognizedException nr) {
					errorHandler.error(new UnknownPropertyException(str));
					return null;
				} catch (ClassCastException cc) {
					errorHandler.error(
						new JenaException(
							new SAXNotSupportedException(
								"Feature: '"
									+ str
									+ "' can only have a boolean value.")));
				}
				return old;
			}
		}
		return setArpProperty(str, obj);
	}

	static public int errorCode(String upper) {
		Class c = ARPErrorNumbers.class;
		try {
			java.lang.reflect.Field fld = c.getField(upper);
			return fld.getInt(null);
		} catch (Exception e) {
			return -1;
		}
	}

	static public String errorCodeName(int errNo) {
		Class c = ARPErrorNumbers.class;
		java.lang.reflect.Field flds[] = c.getDeclaredFields();
		for (int i = 0; i < flds.length; i++) {
			try {
				if (flds[i].getInt(null) == errNo)
					return flds[i].getName();
			} catch (Exception e) {
			}
		}
		return null;
	}

	/**Supported proprties:
	 * error-mode  (String)        default, lax, strict, strict-ignore, strict-warning, strict-error, strict-fatal
	 * embedding  (String/Boolean) true, false
	 * ERR_*      (String/Integer) em_warning, em_fatal, em_ignore, em_error
	 * IGN_*      ditto
	 * WARN_*     ditto
	 */
	private Object setArpProperty(String str, Object v) {
		str = str.toUpperCase();
		if (v == null)
			v = "";
		if (v instanceof String) {
			v = ((String) v).toUpperCase();
		}
		if (str.equals("ERROR-MODE")) {
			if (v instanceof String) {
				String val = (String) v;
				if (val.equals("LAX")) {
					arpf.setLaxErrorMode();
					return null;
				}
				if (val.equals("DEFAULT")) {
					arpf.setDefaultErrorMode();
					return null;
				}
				if (val.equals("STRICT")) {
					arpf.setStrictErrorMode();
					return null;
				}
				if (val.equals("STRICT-WARNING")) {
					arpf.setStrictErrorMode(EM_WARNING);
					return null;
				}
				if (val.equals("STRICT-FATAL")) {
					arpf.setStrictErrorMode(EM_FATAL);
					return null;
				}
				if (val.equals("STRICT-IGNORE")) {
					arpf.setStrictErrorMode(EM_IGNORE);
					return null;
				}
				if (val.equals("STRICT-ERROR")) {
					arpf.setStrictErrorMode(EM_ERROR);
					return null;
				}
			}
			errorHandler.error(
				new IllegalArgumentException(
					"Property \"ERROR-MODE\" takes the following values: "
						+ "\"default\", \"lax\", \"strict\", \"strict-ignore\", \"strict-warning\", \"strict-error\", \"strict-fatal\"."));
			return null;
		}
		if (str.equals("EMBEDDING")) {
			if (v instanceof String) {
				v = Boolean.valueOf((String) v);
			}
			if (!(v instanceof Boolean)) {
				// Illegal value.
				errorHandler.error(
					new IllegalArgumentException("Property \"EMBEDDING\" requires a boolean value."));
				boolean old = arpf.setEmbedding(false);
				arpf.setEmbedding(old);
				return new Boolean(old);
			} else {
				return new Boolean(
					arpf.setEmbedding(((Boolean) v).booleanValue()));
			}
		}
		if (str.startsWith("ERR_")
			|| str.startsWith("IGN_")
			|| str.startsWith("WARN_")) {
			int cond = errorCode(str);
			if (cond == -1) {
				// error, see end of function.
			} else {
				if (v instanceof String) {
					if (!((String) v).startsWith("EM_")) {
						// error, see below.
					} else {
						int val = errorCode((String) v);
						if (val == -1) {
							// error, see below.
						} else {
							int rslt = arpf.setErrorMode(cond, val);
							return new Integer(rslt);
						}
					}
				} else if (v instanceof Integer) {
					int val = ((Integer) v).intValue();
					switch (val) {
						case EM_IGNORE :
						case EM_WARNING :
						case EM_ERROR :
						case EM_FATAL :
							int rslt = arpf.setErrorMode(cond, val);
							return new Integer(rslt);
						default :
							// error, see below.
					}
				}
				// Illegal value.
				errorHandler.error(
					new IllegalArgumentException(
						"Property \""
							+ str
							+ "\" cannot have value: "
							+ v.toString()));
				int old = arpf.setErrorMode(cond, EM_ERROR);
				arpf.setErrorMode(cond, old);
				return new Integer(old);
			}
		}
		errorHandler.error(new UnknownPropertyException(str));
		return null;
	}

	/** Create a instance of ModelMem() and set it to use JenaReader as its default reader.
	 * @deprecated This Reader is now the default.
	 * @return A new in-memory Jena model.
	 */
	static public Model memModel() {
		Model rslt = ModelFactory.createDefaultModel();
		useMe(rslt);
		return rslt;
	}

}

/*
 *  (c) Copyright 2001 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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

 * * $Id: JenaReader.java,v 1.22 2004-06-18 14:18:43 chris-dollin Exp $

   AUTHOR:  Jeremy J. Carroll
 */