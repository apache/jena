/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.rdfxml.xmloutput.impl;

import java.io.OutputStream ;
import java.io.OutputStreamWriter ;
import java.io.PrintWriter ;
import java.io.Writer ;
import java.util.* ;
import java.util.Map.Entry ;
import java.util.regex.Pattern ;

import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIFactory ;
import org.apache.xerces.util.XMLChar ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.JenaRuntime ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler ;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl ;
import com.hp.hpl.jena.rdf.model.impl.Util ;
import com.hp.hpl.jena.rdfxml.xmloutput.RDFXMLWriterI ;
import com.hp.hpl.jena.shared.* ;
import com.hp.hpl.jena.util.CharEncoding ;
import com.hp.hpl.jena.util.FileUtils ;
import com.hp.hpl.jena.vocabulary.* ;

/** 
 * This is not part of the public API.
 * Base class for XML serializers.
 * All methods with side-effects should be synchronized in this class and its
 * subclasses. (i. e. XMLWriters assume that the world is not changing around
 * them while they are writing).
 * 
 * Functionality:
 * 
 * <ul>
 * <li>setProperty etc
 * <li>namespace prefixes
 * <li>xmlbase
 * <li>relative URIs
 * <li>encoding issues
 * <li>anonymous node presentational
 * <li>errorHandler
 * </ul>
*/
abstract public class BaseXMLWriter implements RDFXMLWriterI {
    
    private static final String newline = 
        JenaRuntime.getSystemProperty( "line.separator" );
    static private final String DEFAULT_NS_ENTITY_NAME = "this";
    static private final String DEFAULT_NS_ENTITY_NAME_ALT = "here";
    private String defaultNSEntityName = "UNSET" ;
    
    public BaseXMLWriter() {
        setupMaps();
    }
    
	private static Logger xlogger = LoggerFactory.getLogger( BaseXMLWriter.class );
    
    protected static SimpleLogger logger = new SimpleLogger() {
      	@Override
        public void warn(String s) {
      		xlogger.warn(s);
      	}
      	@Override
        public void warn(String s, Exception e) {
      		xlogger.warn(s,e);
      	}
  };
  
  public static SimpleLogger setLogger(SimpleLogger lg) {
  	SimpleLogger old = logger;
  	logger= lg;
  	return old;
  }
  
    abstract protected void unblockAll();
    
    abstract protected void blockRule(Resource r);

    abstract protected void writeBody
        ( Model mdl, PrintWriter pw, String baseUri, boolean inclXMLBase );

	static private Set<String> badRDF = new HashSet<>();
    
    /**
        Counter used for allocating Jena transient namespace declarations.
    */
	private int jenaPrefixCount;
    
	static String RDFNS = RDF.getURI();
    
    
	static private Pattern jenaNamespace;
    
	static {
	    jenaNamespace =
				Pattern.compile("j\\.([1-9][0-9]*|cook\\.up)");
		
		badRDF.add("RDF");
		badRDF.add("Description");
		badRDF.add("li");
		badRDF.add("about");
		badRDF.add("aboutEach");
		badRDF.add("aboutEachPrefix");
		badRDF.add("ID");
		badRDF.add("nodeID");
		badRDF.add("parseType");
		badRDF.add("datatype");
		badRDF.add("bagID");
		badRDF.add("resource");
	}

	String xmlBase = null;

    private IRI baseURI;
        
	boolean longId = false;
    
    private boolean demandGoodURIs = true;
    
	int tabSize = 2;
    
	int width = 60;

	HashMap<AnonId, String> anonMap = new HashMap<>();
    
	int anonCount = 0;
    
	static private RDFDefaultErrorHandler defaultErrorHandler =
		new RDFDefaultErrorHandler();
        
	RDFErrorHandler errorHandler = defaultErrorHandler;

	Boolean showXmlDeclaration = null;
    
    protected Boolean showDoctypeDeclaration = Boolean.FALSE;

	/*
	 * There are two sorts of id's for anonymous resources.  Short id's are the
	 * default, but require a mapping table.  The mapping table means that
	 * serializing a large model could run out of memory.  Long id's require no
	 * mapping table, but are less readable.
	 */

	String anonId(Resource r)  {
		return longId ? longAnonId( r ) : shortAnonId( r );
	}

	/*
	 * A shortAnonId is computed by maintaining a mapping table from the internal
	 * id's of anon resources.  The short id is the index into the table of the
	 * internal id.
	 */
	private String shortAnonId(Resource r)  {
		String result = anonMap.get(r.getId());
		if (result == null) {
			result = "A" + Integer.toString(anonCount++);
			anonMap.put(r.getId(), result);
		}
		return result;
	}

	/*
	 * A longAnonId is the internal id of the anon resource expressed as a
	 * character string.
	 *
	 * This code makes no assumptions about the characters used in the
	 * implementation of an anon id.  It checks if they are valid namechar
	 * characters and escapes the id if not.
	 */

	private String longAnonId(Resource r)  {
		String rid = r.getId().toString();
        return XMLChar.isValidNCName( rid ) ? rid : escapedId( rid );
	}

    /**
        true means all namespaces defined in the model prefixes will be noted in xmlns
        declarations; false means only "required" ones will be noted. Hook for configuration.
    */
    private boolean writingAllModelPrefixNamespaces = true;
        
    private Relation<String> nameSpaces = new Relation<>();
    
    private Map<String, String> ns;
    
    private PrefixMapping modelPrefixMapping;
        
	private Set<String> namespacesNeeded;
    
	void addNameSpace(String uri) {
		namespacesNeeded.add(uri);
	}

    boolean isDefaultNamespace( String uri ) {
        return "".equals( ns.get( uri ) );
    }
        
    private void addNameSpaces( Model model )  {
        NsIterator nsIter = model.listNameSpaces();
        while (nsIter.hasNext()) this.addNameSpace( nsIter.nextNs() );
    }
    
    private void primeNamespace( Model model )
    {
        Map<String, String> m = model.getNsPrefixMap();
        for ( Entry<String, String> e : m.entrySet() )
        {
            String value = e.getValue();
            String already = this.getPrefixFor( value );
            if ( already == null )
            {
                this.setNsPrefix( model.getNsURIPrefix( value ), value );
                if ( writingAllModelPrefixNamespaces )
                {
                    this.addNameSpace( value );
                }
            }
        }
        
        if ( usesPrefix(model, "") )
        {
            // Doing "" prefix.  Ensure it is a non-clashing, non-empty entity name.
            String entityForEmptyPrefix = DEFAULT_NS_ENTITY_NAME ;
            if ( usesPrefix(model, entityForEmptyPrefix) ) 
                entityForEmptyPrefix = DEFAULT_NS_ENTITY_NAME_ALT ;
            int i = 0 ;
            while ( usesPrefix(model,entityForEmptyPrefix) )
            {
                entityForEmptyPrefix = DEFAULT_NS_ENTITY_NAME_ALT+"."+i ;
                i++ ;
            }
            defaultNSEntityName = entityForEmptyPrefix ;
        }
    }

    void setupMaps() {
        nameSpaces.set11(RDF.getURI(), "rdf");
        nameSpaces.set11(RDFS.getURI(), "rdfs");
        nameSpaces.set11(DC.getURI(), "dc");
        nameSpaces.set11(RSS.getURI(), "rss");
        nameSpaces.set11("http://www.daml.org/2001/03/daml+oil.daml#", "daml");
        nameSpaces.set11(VCARD.getURI(), "vcard");
        nameSpaces.set11("http://www.w3.org/2002/07/owl#", "owl");
    }
                
	void workOutNamespaces() {
		if (ns == null) {
    		ns = new HashMap<>();
    		Set<String> prefixesUsed = new HashSet<>();
			setFromWriterSystemProperties( ns, prefixesUsed );
            setFromGivenNamespaces( ns, prefixesUsed );
        }
	}

    private void setFromWriterSystemProperties( Map<String, String> ns, Set<String> prefixesUsed ) {
        for ( String uri : namespacesNeeded )
        {
            String val = JenaRuntime.getSystemProperty( RDFWriter.NSPREFIXPROPBASE + uri );
            if ( val != null && checkLegalPrefix( val ) && !prefixesUsed.contains( val ) )
            {
                ns.put( uri, val );
                prefixesUsed.add( val );
            }
        }
    }

    private void setFromGivenNamespaces( Map<String, String> ns, Set<String> prefixesUsed ) {
        for ( String uri : namespacesNeeded )
        {
            if ( ns.containsKey( uri ) )
            {
                continue;
            }
            String val = null;
            Set<String> s = nameSpaces.forward( uri );
            if ( s != null )
            {
                Iterator<String> it2 = s.iterator();
                if ( it2.hasNext() )
                {
                    val = it2.next();
                }
                if ( prefixesUsed.contains( val ) )
                {
                    val = null;
                }
            }
            if ( val == null )
            {
                // just in case the prefix has already been used, look for a free one.
                // (the usual source of such prefixes is reading in a model we wrote out earlier)
                do
                {
                    val = "j." + ( jenaPrefixCount++ );
                }
                while ( prefixesUsed.contains( val ) );
            }
            ns.put( uri, val );
            prefixesUsed.add( val );
        }
	}

	final synchronized public void setNsPrefix(String prefix, String ns) {
        if (checkLegalPrefix(prefix)) {
            nameSpaces.set11(ns, prefix);
        }
    }
    
    final public String getPrefixFor( String uri )
        {
            // xml and xmlns namespaces are pre-bound
            if ("http://www.w3.org/XML/1998/namespace".equals(uri)) return "xml";
            if ("http://www.w3.org/2000/xmlns/".equals(uri)) return "xmlns";
        Set<String> s = nameSpaces.backward( uri );
        if (s != null && s.size() == 1) return s.iterator().next();
        return null; 
        }

	String xmlnsDecl() {
		workOutNamespaces();
		StringBuilder result = new StringBuilder();
		Iterator<Entry<String, String>> it = ns.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> ent = it.next();
			String prefix = ent.getValue();
			String uri = ent.getKey();
            result.append( newline ).append( "    xmlns" );
			if (prefix.length() > 0) result.append( ':' ).append( prefix );
			result.append( '=' ).append( substitutedAttribute( checkURI( uri ) ) );
		}
		return result.toString();
	}

	static final private int FAST = 1;
	static final private int START = 2;
	static final private int END = 3;
	static final private int ATTR = 4;
	static final private int FASTATTR = 5;
    
	String rdfEl(String local) {
		return tag(RDFNS, local, FAST, true);
	}
    
	String startElementTag(String uri, String local) {
		return tag(uri, local, START, false);
	}
    
	protected String startElementTag(String uriref) {
		return splitTag(uriref, START);
	}
    
	String attributeTag(String uriref) {
		return splitTag(uriref, ATTR);
	}
    
	String attributeTag(String uri, String local) {
		return tag(uri, local, ATTR, false);
	}
    
	String rdfAt(String local) {
		return tag(RDFNS, local, FASTATTR, true);
	}
    
	String endElementTag(String uri, String local) {
		return tag(uri, local, END, false);
	}
    
	protected String endElementTag(String uriref) {
		return splitTag(uriref, END);
	}
    
	String splitTag(String uriref, int type) {
		int split = Util.splitNamespace( uriref );
		if (split == uriref.length()) throw new InvalidPropertyURIException( uriref );
		return tag( uriref.substring( 0, split ), uriref.substring( split ), type, true );
    }
    
	static public boolean dbg = false;
    
	String tag( String namespace, String local, int type, boolean localIsQname)  {
		if (dbg)
			System.err.println(namespace + " - " + local);
		String prefix = ns.get( namespace );
		if (type != FAST && type != FASTATTR) {
			if ((!localIsQname) && !XMLChar.isValidNCName(local))
				return splitTag(namespace + local, type);
			if (namespace.equals(RDFNS)) {
				// Description, ID, nodeID, about, aboutEach, aboutEachPrefix, li
				// bagID parseType resource datatype RDF
				if (badRDF.contains(local)) {
					logger.warn(	"The URI rdf:" + local + " cannot be serialized in RDF/XML." );
					throw new InvalidPropertyURIException( "rdf:" + local );
				}
			}
		}
		boolean cookUp = false;
		if (prefix == null) {
            checkURI( namespace );
			logger.warn(
				"Internal error: unexpected QName URI: <"
					+ namespace
					+ ">.  Fixing up with j.cook.up code.",
				new BrokenException( "unexpected QName URI " + namespace ));
			cookUp = true;
		} else if (prefix.length() == 0) {
			if (type == ATTR || type == FASTATTR)
				cookUp = true;
			else
				return local;
		}
		if (cookUp) return cookUpAttribution( type, namespace, local );
		return prefix + ":" + local;
	}
    
    private String cookUpAttribution( int type, String namespace, String local )
        {
        String prefix = "j.cook.up";
        switch (type) {
            case FASTATTR :
            case ATTR :
                return "xmlns:" + prefix + "=" + substitutedAttribute( namespace ) + " " + prefix + ":" + local;
            case START :
                return prefix  + ":" + local + " xmlns:" + prefix+ "=" + substitutedAttribute( namespace );
            default:
            case END :
                return prefix + ":" + local;
            case FAST :
              //  logger.error("Unreachable code - reached.");
                throw new BrokenException( "cookup reached final FAST" );
            }
        }

	/** Write out an XML serialization of a model.
	 * @param model the model to be serialized
	 * @param out the OutputStream to receive the serialization
	 * @param base The URL at which the file will be placed.
	 */
	@Override
    final public void write(Model model, OutputStream out, String base)
		 { write( model, FileUtils.asUTF8(out), base ); }

	/** Serialize Model <code>model</code> to Writer <code>out</out>.
     * @param model The model to be written.
	 * @param out The Writer to which the serialization should be sent.
	 * @param base the base URI for relative URI calculations.  <code>null</code> means use only absolute URI's.
	 */
	@Override
    synchronized public void write(Model model, Writer out, String base)
		 {        
		setupNamespaces( model );
		PrintWriter pw = out instanceof PrintWriter ? (PrintWriter) out : new PrintWriter( out );
		if (!Boolean.FALSE.equals(showXmlDeclaration)) writeXMLDeclaration( out, pw );
		writeXMLBody( model, pw, base );
		pw.flush();
	}

    /**
     	@param baseModel
     	@param model
    */
    private void setupNamespaces( Model model )
        {
        this.namespacesNeeded = new HashSet<>();
        this.ns = null;
        this.modelPrefixMapping = model;
        primeNamespace( model );
        addNameSpace( RDF.getURI() );
        addNameSpaces(model);
        jenaPrefixCount = 0;
        }
    
    static IRIFactory factory = IRIFactory.jenaImplementation();

   
	private void writeXMLBody( Model model, PrintWriter pw, String base ) {
        if (showDoctypeDeclaration.booleanValue()) generateDoctypeDeclaration( model, pw );
//		try {
        // errors?
			if (xmlBase == null) {
				baseURI = (base == null || base.length() == 0) ? null : factory.create(base);
				writeBody(model, pw, base, false);
			} else {
				baseURI = xmlBase.length() == 0 ? null : factory.create(xmlBase);
				writeBody(model, pw, xmlBase, true);
			}
//		} catch (MalformedURIException e) {
//			throw new BadURIException( e.getMessage(), e);
//		}
	}

	protected static final Pattern predefinedEntityNames = Pattern.compile( "amp|lt|gt|apos|quot" );
    
    public boolean isPredefinedEntityName( String name )
        { return predefinedEntityNames.matcher( name ).matches(); }
                
    private String attributeQuoteChar ="\"";
    
    protected String attributeQuoted( String s ) 
        { return attributeQuoteChar + s + attributeQuoteChar; }
    
    protected String substitutedAttribute( String s ) 
        {
        String substituted = Util.substituteStandardEntities( s );
        if (!showDoctypeDeclaration.booleanValue()) 
            return attributeQuoted( substituted );
        else
            {
            int split = Util.splitNamespace( substituted );
            String namespace = substituted.substring(  0, split );
            String prefix = modelPrefixMapping.getNsURIPrefix( namespace );
            return prefix == null || isPredefinedEntityName( prefix )
                ? attributeQuoted( substituted )
                : attributeQuoted( "&" + strForPrefix(prefix) + ";" + substituted.substring( split ) )
                ;
            }
        }
    
    private void generateDoctypeDeclaration( Model model, PrintWriter pw )
        {
        String rdfns = RDF.getURI();
		String rdfRDF = model.qnameFor( rdfns + "RDF" );
        if ( rdfRDF == null ) {
        	model.setNsPrefix("rdf",rdfns);
        	rdfRDF = "rdf:RDF";
        }
        Map<String, String> prefixes = model.getNsPrefixMap();
        pw.print( "<!DOCTYPE " + rdfRDF +" [" );
            for ( String prefix : prefixes.keySet() )
            {
                if ( isPredefinedEntityName( prefix ) )
                {
                    continue;
                }
                pw.print(
                    newline + "  <!ENTITY " + strForPrefix( prefix ) + " '" + Util.substituteEntitiesInEntityValue(
                        prefixes.get( prefix ) ) + "'>" );
            }
        pw.print( "]>" + newline );
        }

	private String strForPrefix(String prefix)
    {
        if ( prefix.length() == 0 )
            return defaultNSEntityName ;
        return prefix ;
    }
	
	private static boolean usesPrefix(Model model, String prefix)
	{
	    return model.getNsPrefixURI(prefix) != null ;
    }

    private void writeXMLDeclaration(Writer out, PrintWriter pw) {
		String decl = null;
		if (out instanceof OutputStreamWriter) {
			String javaEnc = ((OutputStreamWriter) out).getEncoding();
			// System.err.println(javaEnc);
			if (!(javaEnc.equals("UTF8") || javaEnc.equals("UTF-16"))) {
			    CharEncoding encodingInfo = CharEncoding.create(javaEnc);
		        
				String ianaEnc = encodingInfo.name();
				decl = "<?xml version="+attributeQuoted("1.0")+" encoding=" + attributeQuoted(ianaEnc) + "?>";
				if (!encodingInfo.isIANA())
			     logger.warn(encodingInfo.warningMessage()+"\n"+
				            "   It is better to use a FileOutputStream, in place of a FileWriter.");
				       
			}
		}
		if (decl == null && showXmlDeclaration != null)
			decl = "<?xml version="+attributeQuoted("1.0")+"?>";
		if (decl != null) {
			pw.println(decl);
		}
	}

	/** Set an error handler.
	 * @param errHandler The new error handler to be used, or null for the default handler.
	 * @return the old error handler
	 */
	@Override
    synchronized public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler) {
		// null means no user defined error handler.
		// We implement this using defaultErrorHandler,
		// but hide this fact from the user.
		RDFErrorHandler rslt = errorHandler;
		if (rslt == defaultErrorHandler) rslt = null;
		errorHandler = errHandler == null ? defaultErrorHandler : errHandler;
		return rslt;
	}

	static private final char ESCAPE = 'X';
    
	static private String escapedId(String id) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < id.length(); i++) {
			char ch = id.charAt(i);
			if (ch != ESCAPE
				&& (i == 0 ? XMLChar.isNCNameStart(ch) : XMLChar.isNCName(ch))) {
				result.append( ch );
			} else {
				escape( result, ch );
			}
		}
		return result.toString();
	}

    static final char [] hexchar = "0123456789abcdef".toCharArray();
                
	static private void escape( StringBuffer sb, char ch) {
		sb.append( ESCAPE );
		int charcode = ch;
		do {
			sb.append( hexchar[charcode & 15] );
			charcode = charcode >> 4;
		} while (charcode != 0);
		sb.append( ESCAPE );
	}
    
    /**
        Set the writer property propName to the value obtained from propValue. Return an
        Object representation of the original value.
         
     	@see com.hp.hpl.jena.rdf.model.RDFWriter#setProperty(java.lang.String, java.lang.Object)
     */
	@Override
    final synchronized public Object setProperty( String propName, Object propValue ) {
		if (propName.equalsIgnoreCase("showXmlDeclaration")) {
			return setShowXmlDeclaration(propValue);
        } else if (propName.equalsIgnoreCase( "showDoctypeDeclaration" )) {
            return setShowDoctypeDeclaration( propValue );
        } else if (propName.equalsIgnoreCase( "minimalPrefixes" )) {
            try { return new Boolean( !writingAllModelPrefixNamespaces ); }
            finally { writingAllModelPrefixNamespaces = !getBoolean( propValue ); }
		} else if (propName.equalsIgnoreCase("xmlbase")) {
			String result = xmlBase;
			xmlBase = (String) propValue;
			return result;
		} else if (propName.equalsIgnoreCase("tab")) {
			return setTab( propValue );
		} else if (propName.equalsIgnoreCase("width")) {
			return setWidth(propValue);
		} else if (propName.equalsIgnoreCase("longid")) {
			Boolean result = new Boolean(longId);
			longId = getBoolean(propValue);
			return result;
		} else if (propName.equalsIgnoreCase("attributeQuoteChar")) {
			return setAttributeQuoteChar(propValue);
		} else if (propName.equalsIgnoreCase( "allowBadURIs" )) {
			Boolean result = new Boolean( !demandGoodURIs );
            demandGoodURIs = !getBoolean(propValue);
			return result;
		} else if (propName.equalsIgnoreCase("prettyTypes")) {
			return setTypes((Resource[]) propValue);
		} else if (propName.equalsIgnoreCase("relativeURIs")) {
			int old = relativeFlags;
			relativeFlags = str2flags((String) propValue);
			return flags2str(old);
		} else if (propName.equalsIgnoreCase("blockRules")) {
			return setBlockRules(propValue);
		} else {
			logger.warn("Unsupported property: " + propName);
			return null;
		}
	}
    
	private String setAttributeQuoteChar(Object propValue) {
		String oldValue = attributeQuoteChar;
		if ( "\"".equals(propValue) || "'".equals(propValue) )
		  attributeQuoteChar = (String)propValue;
		else 
		  logger.warn("attributeQutpeChar must be either \"\\\"\" or \', not \""+propValue+"\"" );
		return oldValue;
	}

	private Integer setWidth(Object propValue) {
		Integer oldValue = new Integer(width);
		if (propValue instanceof Integer) {
			width = ((Integer) propValue).intValue();
		} else {
			try {
				width = Integer.parseInt((String) propValue);
			} catch (Exception e) {
				logger.warn(	"Bad value for width: '" + propValue + "' [" + e.getMessage() + "]" );
			}
		}
		return oldValue;
	}

	private Integer setTab(Object propValue) {
		Integer result = new Integer(tabSize);
		if (propValue instanceof Integer) {
			tabSize = ((Integer) propValue).intValue();
		} else {
			try {
				tabSize = Integer.parseInt((String) propValue);
			} catch (Exception e) {
				logger.warn(	"Bad value for tab: '" + propValue + "' [" + e.getMessage() + "]" );
			}
		}
		return result;
	}
    
    private String setShowDoctypeDeclaration( Object propValue )
        {
        String oldValue = showDoctypeDeclaration.toString();
        showDoctypeDeclaration = getBooleanValue( propValue, Boolean.FALSE );
        return oldValue;
        }

    private String setShowXmlDeclaration( Object propValue ) 
        {
        String oldValue = showXmlDeclaration == null ? null : showXmlDeclaration.toString();
        showXmlDeclaration = getBooleanValue( propValue, null );
        return oldValue;
        }

    /**
        Answer the boolean value corresponding to o, which must either be a Boolean,
        or a String parsable as a Boolean.
    */
    static private boolean getBoolean( Object o ) 
        { return getBooleanValue( o, Boolean.FALSE ).booleanValue(); }
    
    private static Boolean getBooleanValue( Object propValue, Boolean theDefault )
        {
        if (propValue == null)
            return theDefault;
        else if (propValue instanceof Boolean)
            return (Boolean) propValue;
        else if (propValue instanceof String)
            return stringToBoolean( (String) propValue, theDefault );
        else
            throw new JenaException( "cannot treat as boolean: " + propValue );
        }

	private static Boolean stringToBoolean( String b, Boolean theDefault )
        {
        if (b.equals( "default" )) return theDefault;
        if (b.equalsIgnoreCase( "true" )) return Boolean.TRUE;
        if (b.equalsIgnoreCase( "false" )) return Boolean.FALSE;
        throw new BadBooleanException( b );
        }
    
	Resource[] setTypes( Resource x[] ) {
		logger.warn( "prettyTypes is not a property on the Basic RDF/XML writer." );
		return null;
	}
    
	private Resource blockedRules[] = new Resource[]{RDFSyntax.propertyAttr};
    
	Resource[] setBlockRules(Object o) {
		Resource rslt[] = blockedRules;
		unblockAll();
		if (o instanceof Resource[]) {
			blockedRules = (Resource[]) o;
		} else {
			StringTokenizer tkn = new StringTokenizer((String) o, ", ");
			Vector<Resource> v = new Vector<>();
			while (tkn.hasMoreElements()) {
				String frag = tkn.nextToken();
				//  System.err.println("Blocking " + frag);
				v.add(new ResourceImpl(RDFSyntax.getURI() + frag));
			}

			blockedRules = new Resource[v.size()];
			v.copyInto(blockedRules);
		}
        for ( Resource blockedRule : blockedRules )
        {
            blockRule( blockedRule );
        }
		return rslt;
	}
	/*
	private boolean sameDocument = true;
	private boolean network = false;
	private boolean absolute = true;
	private boolean relative = true;
	private boolean parent = true;
	private boolean grandparent = false;
	*/
	private int relativeFlags =
		IRI.SAMEDOCUMENT | IRI.ABSOLUTE | IRI.CHILD | IRI.PARENT;

    /**
        Answer the form of the URI after relativisation according to the relativeFlags set
        by properties. If the flags are 0 or the base URI is null, answer the original URI.
        Throw an exception if the URI is "bad" and we demandGoodURIs.
    */
    protected String relativize( String uri ) { 
        return relativeFlags != 0 && baseURI != null
            ? relativize( baseURI, uri )
            : checkURI( uri );
    }
    
    /**
        Answer the relative form of the URI against the base, according to the relativeFlags.
    */
    private String relativize( IRI base, String uri )  {
        // errors?
        return base.relativize( uri, relativeFlags).toString();
    }

    /**
        Answer the argument URI, but if we demandGoodURIs and it isn't good, throw
        a JenaException that encapsulates a MalformedURIException. There doesn't
        appear to be a convenient URI.checkGood() kind of method, alas.
     */
    private String checkURI( String uri ) {
        if (demandGoodURIs) {
            IRI iri = factory.create( uri );
            
            if (iri.hasViolation(false) ) 
            throw new BadURIException( "Only well-formed absolute URIrefs can be included in RDF/XML output: "
                     + (iri.violations(false).next()).getShortMessage()); 
        }
             
            
        return uri;
    }
    
    /**
        Answer true iff prefix is a "legal" prefix to use, ie, is empty [for the default namespace]
        or an NCName that does not start with "xml" and does not match the reserved-to-Jena
        pattern.
    */
    private boolean checkLegalPrefix( String prefix ) {
        if (prefix.equals(""))
            return true;
        if (prefix.toLowerCase().startsWith( "xml" ))
            logger.warn( "Namespace prefix '" + prefix + "' is reserved by XML." );
        else if (!XMLChar.isValidNCName(prefix))
            logger.warn( "'" + prefix + "' is not a legal namespace prefix." );
        else if (jenaNamespace.matcher(prefix).matches())
            logger.warn( "Namespace prefix '" + prefix + "' is reserved by Jena." );
        else
            return true;
        return false;
    }

    static private String flags2str(int f) {
	StringBuffer oldValue = new StringBuffer(64);
	if ( (f&IRI.SAMEDOCUMENT)!=0 )
	   oldValue.append( "same-document, " );
	if ( (f&IRI.NETWORK)!=0 )
	   oldValue.append( "network, ");
	if ( (f&IRI.ABSOLUTE)!=0 )
	   oldValue.append("absolute, ");
	if ( (f&IRI.CHILD)!=0 )
	   oldValue.append("relative, ");
	if ((f&IRI.PARENT)!=0)
	   oldValue.append("parent, ");
	if ((f&IRI.GRANDPARENT)!=0)
	   oldValue.append("grandparent, ");
	if (oldValue.length() > 0)
	   oldValue.setLength(oldValue.length()-2);
	   return oldValue.toString();
	}

	public static int str2flags(String pv){
	StringTokenizer tkn = new StringTokenizer(pv,", ");
	int rslt = 0;
	while ( tkn.hasMoreElements() ) {
	    String flag = tkn.nextToken();
	    if ( flag.equals("same-document") )
	       rslt |= IRI.SAMEDOCUMENT;
	    else if ( flag.equals("network") )
	       rslt |= IRI.NETWORK;
	    else if ( flag.equals("absolute") )
	       rslt |= IRI.ABSOLUTE;
	    else if ( flag.equals("relative") )
	       rslt |= IRI.CHILD;
	    else if ( flag.equals("parent") )
	       rslt |= IRI.PARENT;
	    else if ( flag.equals("grandparent") )
	       rslt |= IRI.GRANDPARENT;
	    else
	
	    logger.warn(
	        "Incorrect property value for relativeURIs: " + flag
	        );
	}
	return rslt;
	}
    
}
