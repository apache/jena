/*
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.n3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.util.iterator.* ;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.vocabulary.OWL ;
import com.hp.hpl.jena.vocabulary.XSD ;
import com.hp.hpl.jena.vocabulary.RDF ;

import java.util.* ;
import java.io.* ;
import java.text.* ;

/** Common framework for implementing N3 writers.
 *
 * @author		Andy Seaborne
 * @version 	$Id: N3JenaWriterCommon.java,v 1.27 2004-11-04 17:20:14 andy_seaborne Exp $
 */

public class N3JenaWriterCommon implements RDFWriter
{
    static Log logger = LogFactory.getLog(N3JenaWriterCommon.class) ;
    
	// N3 writing proceeds in 2 stages.
    // First, it analysis the model to be written to extract information
    // that is going to be specially formatted (RDF lists, one ref anon nodes)
    // Second do the writing walk.
    
    // The simple N3 writer does nothing during preparation.
    
    Map writerPropertyMap = null ;

    final boolean doAbbreviatedBaseURIref = getBooleanValue("abbrevBaseURI", true) ; 
    boolean alwaysAllocateBNodeLabel = false ;
    
    // Common variables
	RDFErrorHandler errorHandler = null;

	static final String NS_W3_log = "http://www.w3.org/2000/10/swap/log#" ;

	Map prefixMap 	   	= new HashMap() ;	// Prefixes to actually use
	Map	bNodesMap       = null ;		    // BNodes seen.
    int bNodeCounter    = 0 ;

    // Specific properties that have a short form.
	static Map wellKnownPropsMap = new HashMap() ;
	static {
		wellKnownPropsMap.put(NS_W3_log+"implies",		"=>" ) ;
		wellKnownPropsMap.put(OWL.sameAs.getURI(),	    "="  ) ;
		wellKnownPropsMap.put(RDF.type.getURI(),		"a"  ) ;
	}

	// Work variables controlling the output
	IndentedWriter out = null ;
	String baseURIref = null ;
    String baseURIrefHash = null ;

    // Min spacing of items    
    int minGap = getIntValue("minGap", 1) ;
    String minGapStr = pad(minGap) ;

    // Gap from subject to property
	int indentProperty = getIntValue("indentProperty", 6) ;
    
    // Width of property before wrapping.
    // This is not necessarily a control of total width
    // e.g. the pretty writer may be writing properties inside indented one ref bNodes 
    int widePropertyLen = getIntValue("widePropertyLen", 20) ;
    
    // Column for property when an object follows a property on the same line
    int propertyCol = getIntValue("propertyColumn", 8) ;
    
    // Minimum gap from property to object when object on a new line.
    int indentObject = propertyCol ;
    
    // If a subject is shorter than this, the first property may go on same line.
    int subjectColumn = getIntValue("subjectColumn", indentProperty) ; 
    // Require shortSubject < subjectCol (strict less than)
    int shortSubject = subjectColumn-minGap;
    
    boolean useWellKnownPropertySymbols = getBooleanValue("usePropertySymbols", true) ;
    
    boolean allowTripleQuotedStrings = getBooleanValue("useTripleQuotedStrings", true) ;
    boolean allowDoubles = getBooleanValue("useDoubles", true) ;
    
    // ----------------------------------------------------
    // Jena RDFWriter interface

	public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler)
	{
		RDFErrorHandler old = errorHandler;
		errorHandler = errHandler;
		return old;
	}

    public Object setProperty(String propName, Object propValue) 
    {
        // Store localized property names
        propName = absolutePropName(propName) ;
        if ( writerPropertyMap == null )
            writerPropertyMap = new HashMap() ;
        Object oldValue = writerPropertyMap.get(propName);
        writerPropertyMap.put(propName, propValue);
        return oldValue;
   }

    /** Write the model out in N3.  The writer should be one suitable for UTF-8 which
    * excludes a PrintWriter or a FileWriter which use default character set.
    *
    * Examples:
    * <pre>
    * try {
    *      Writer w =  new BufferedWriter(new OutputStreamWriter(output, "UTF-8")) ;
    *      model.write(w, base) ;
    *      try { w.flush() ; } catch (IOException ioEx) {}
    *  } catch (java.io.UnsupportedEncodingException ex) {} //UTF-8 is required so can't happen
    * </pre>
    * or
    * <pre>
    * try {
    *     OutputStream out = new FileOutputStream(file) ;
    *     Writer w =  new BufferedWriter(new OutputStreamWriter(out, "UTF-8")) ;
    *     model.write(w, base) ;
    * }
    * catch (java.io.UnsupportedEncodingException ex) {}
    * catch (java.io.FileNotFoundException noFileEx) { ... }
    * </pre>
    * @see #write(Model,Writer,String)
    */

    public void write(Model baseModel, Writer _out, String base) 
    {
        if (!(_out instanceof BufferedWriter))
            _out = new BufferedWriter(_out);
        out = new IndentedWriter(_out);

        // Base is (should be) a URI, not a URI ref.
        
        if ( base != null )
        {
            baseURIref = base ;
            if ( !base.endsWith("#"))
                baseURIrefHash = baseURIref+"#" ;
        }
        
        processModel(baseModel) ;
    }
    
	/** Write the model out in N3, encoded in in UTF-8
	 * @see #write(Model,Writer,String)
	 */

	public synchronized void write(Model model, OutputStream output, String base) 
	{
		try {
			Writer w =  new BufferedWriter(new OutputStreamWriter(output, "UTF-8")) ;
			write(model, w, base) ;
			try { w.flush() ; } catch (IOException ioEx) {}
		} catch (java.io.UnsupportedEncodingException ex)
		{
			System.err.println("Failed to create UTF-8 writer") ;
		}
	}

    // ----------------------------------------------------
    // The assumed processing model is:
    // Writing N3 involves ordering the graph into:
    // -- Subjects
    // -- Property lists within subjects
    // -- Object lists with in properties
    
    //  A derived class may choose to intercept and implement at any of these levels. 
     
    // Standard layout is:
    // subject
    //    property1 value1 ;
    //    property2 value2 ;
    //    property3 value3 .
    
    // Normal hook points for subclasses.

    protected void startWriting() {}
    protected void finishWriting() {}
    protected void prepare(Model model) {}
    
    protected void processModel(Model baseModel)
    {
        prefixMap = baseModel.getNsPrefixMap() ;
        Model model = ModelFactory.withHiddenStatements( baseModel );
        bNodesMap = new HashMap() ;

        // If no base defined for the model, but one given to writer,
        // then use this.
        String base2 = (String)prefixMap.get("") ;
        
        if ( base2 == null && baseURIrefHash != null )
            prefixMap.put("", baseURIrefHash) ;

        for ( Iterator iter = prefixMap.keySet().iterator() ; iter.hasNext() ; )
        {
            String prefix = (String)iter.next() ;
            if ( prefix.indexOf('.') != -1 )
                iter.remove() ;
        }
        
        startWriting() ;
        prepare(model) ;

        writeHeader(model) ;
        writePrefixes(model) ;
        
        if (prefixMap.size() != 0)
            out.println();

        // Do the output.
        writeModel(model) ;

        // Release intermediate memory - allows reuse of a writer
        finishWriting() ;
        bNodesMap = null ;
    }

    protected void writeModel(Model model)
    {
       // Needed only for no prefixes, no blank first line. 
        boolean doingFirst = true;
        ResIterator rIter = model.listSubjects();
        for (; rIter.hasNext();)
        {
            // Subject:
            // First - it is something we will write out as a structure in an object field?
            // That is, a RDF list or the object of exactly one statement.
            Resource subject = rIter.nextResource();
            if ( skipThisSubject(subject) )
            {
                if (N3JenaWriter.DEBUG)
                    out.println("# Skipping: " + formatResource(subject));
                continue;
            }

            // We really are going to print something via writeTriples
            if (doingFirst)
                doingFirst = false;
            else
                out.println();

            writeOneGraphNode(subject) ;
            
            
        }
        rIter.close();
    } 

    protected void writeOneGraphNode(Resource subject)
    {
        // New top level item.
        // Does not take effect until newline.
        out.incIndent(indentProperty) ;
        writeSubject(subject);
        writePropertiesForSubject(subject) ;
        out.decIndent(indentProperty) ; 
        out.println(" .");
    }

    protected void writePropertiesForSubject(Resource subj)
    {
        ClosableIterator iter = preparePropertiesForSubject(subj);
        // For each property.
        for (; iter.hasNext();)
        {
            Property property = (Property) iter.next();

            // Object list
            writeObjectList(subj, property);

            if (iter.hasNext())
                out.println(" ;");
        }
        iter.close();
    }

    // Hook called on every resource.
    // Since there is spacing bewteen resource frames, need to know
    // whether an item will cause any output.   
    protected boolean skipThisSubject(Resource r) { return false ; }


    // This is the hook called within writeModel.  
    // NB May not be at the top level (indent = 0)
    
    protected void writeSubject(Resource subject)
    {
        String subjStr = formatResource(subject);
        out.print(subjStr);
        // May be very short : if so, stay on this line.
        
        // Currently at end of subject.
        // NB shortSubject is (subjectColumn-minGap) so there is a gap.

        if (subjStr.length() < shortSubject )
        {
            out.print(pad(subjectColumn - subjStr.length()) );
        }
        else
            // Does not fit this line.
            out.println();
    }
    
    protected void writeHeader(Model model)
    {
        if (baseURIref != null && !baseURIref.equals("") )
            out.println("# Base: " + baseURIref);
    }
    
    protected void writePrefixes(Model model)
    {
        for (Iterator pIter = prefixMap.keySet().iterator(); pIter.hasNext();)
        {
            String p = (String) pIter.next();
            String u = (String) prefixMap.get(p);

            // Special cases: N3 handling of base names.
            if (doAbbreviatedBaseURIref && p.equals(""))
            {
                if (baseURIrefHash != null && u.equals(baseURIrefHash))
                    u = "#";
                if (baseURIref != null && u.equals(baseURIref))
                    u = "";
            }

            String tmp = "@prefix " + p + ": ";
            out.print(tmp);
            out.print(pad(16 - tmp.length()));
            // NB Starts with a space to ensure a gap.
            out.println(" <" + u + "> .");
        }

    }
    
    protected void writeObjectList(Resource resource, Property property)
    {
        String propStr = formatProperty(property) ;

//        if (wellKnownPropsMap.containsKey(property.getURI()))
//            propStr = (String) wellKnownPropsMap.get(property.getURI());
//        else
//            propStr = formatResource(property);

        // Write with object lists as clusters of statements with the same property
        // Looks more like a machine did it but fewer bad cases.

        StmtIterator sIter = resource.listProperties(property);
        for (; sIter.hasNext();)
        {
            Statement stmt = sIter.nextStatement() ;
            String objStr = formatNode(stmt.getObject()) ;
            
            out.print(propStr);
            out.incIndent(indentObject);

            if ( (propStr.length()+minGap) <= widePropertyLen )
            {
                // Property col allows for min gap but widePropertyLen > propertyCol 
                // (which looses alignment - this is intentional.
                // Ensure there is at least min gap.
                
                int padding = calcPropertyPadding(propStr) ;
                out.print(pad(padding)) ;
                
//                if ( propStr.length() < propertyWidth ) 
//                    out.print( pad(propertyCol-minGap-propStr.length()) ) ;
//                out.print(minGapStr) ;
            }
            else
                // Does not fit this line.
                out.println();

            // Write one object - simple writing.
            
            out.print(objStr) ;
            out.decIndent(indentObject);

            if ( sIter.hasNext() )
            {
                out.println(" ;") ;
            }
        }
        sIter.close() ;

    }

    protected String formatNode(RDFNode node)
    {
        if (node instanceof Literal)
            return formatLiteral((Literal) node);
        else
            return formatResource((Resource)node) ;
    }

    protected void writeObject(RDFNode node)
    {
        if (node instanceof Literal)
        {
            writeLiteral((Literal) node);
            return;
        }

        Resource rObj = (Resource) node;

        out.print(formatResource(rObj));
    }
    
    protected void writeLiteral(Literal literal) 
    {
        out.print(formatLiteral(literal)) ;
    }
    
    protected ClosableIterator preparePropertiesForSubject(Resource r)
    {
        // Properties to do.
        Set properties = new HashSet() ;

        StmtIterator sIter = r.listProperties();
        for ( ; sIter.hasNext() ; )
            properties.add(sIter.nextStatement().getPredicate()) ;
        sIter.close() ;
        return WrappedIterator.create(properties.iterator()) ;
    }
    
    
    // Utility operations
    protected String formatResource(Resource r)
	{
		if ( r.isAnon() )
		{
            if ( ! alwaysAllocateBNodeLabel )
            {
                // Does anything point to it?
                StmtIterator sIter = r.getModel().listStatements(null, null, r) ;
    
                if ( ! sIter.hasNext() )
                {
                    sIter.close() ;
                    // This bNode is not referenced so don't need the bNode Id.
                    // Must be a subject - indent better be zero!
                    // This only happens for subjects because object bNodes
                    // referred to once (the other case for [] syntax)
                    // are handled elsewhere (by oneRef set)
    
                    // Later: use [ prop value ] for this.
                    return "[]" ;
                }
                sIter.close() ;
            }
			if ( ! bNodesMap.containsKey(r) )
				bNodesMap.put(r, "_:b"+(++bNodeCounter)) ;
			return (String)bNodesMap.get(r) ;

		}

		// It has a URI.
        
		if ( r.equals(RDF.nil) )
			return "()" ;

        return formatURI(r.getURI()) ;
    }

    protected String formatLiteral(Literal literal)
    {
        String datatype = literal.getDatatypeURI() ;
        String lang = literal.getLanguage() ;
    	String s = literal.getLexicalForm() ;
    
        if ( datatype != null )
        {
            // Special form we know how to handle?
            // Assume valid text
            if ( datatype.equals(XSD.integer.getURI()) )
            {
                try {
                    new java.math.BigInteger(s) ;
                    return s ;
                } catch (NumberFormatException nfe) {}
                // No luck.  Continue.
                // Continuing is always safe.
            }
                
            if ( this.allowDoubles && datatype.equals(XSD.xdouble.getURI()) )
            {
                // Must have an '.' or 'e' or 'E'
                if ( s.indexOf('.') >= 0 || 
                     s.indexOf('e') >= 0 ||
                     s.indexOf('E') >= 0 )
                {
                    try {
                        Double.parseDouble(s) ;
                        return s ;
                    } catch (NumberFormatException nfe) {}
                    // No luck.  Continue.
                }
            }
        }
        // Format the text - with escaping.
        StringBuffer sbuff = new StringBuffer() ;
        boolean singleQuoteLiteral = true ;
        
        String quoteMarks = "\"" ;
        
        // Things that force the use of """ strings
        if ( this.allowTripleQuotedStrings &&
             ( s.indexOf("\n") != -1 ||
               s.indexOf("\r") != -1 ||
               s.indexOf("\f") != -1 ) )
        {
            quoteMarks = "\"\"\"" ;
            singleQuoteLiteral = false ;
        }
        
        sbuff.append(quoteMarks);
        string(sbuff, s, singleQuoteLiteral) ;
        sbuff.append(quoteMarks);
    
        // Format the language tag 
        if ( lang != null && lang.length()>0)
        {
            sbuff.append("@") ;
            sbuff.append(lang) ;
        }
        
        // Format the datatype
        if ( datatype != null )
        {
            sbuff.append("^^") ;
            sbuff.append(formatURI(datatype)) ;
        }
        return sbuff.toString() ;
    }
    
    protected String formatProperty(Property p)
    {
        String prop = p.getURI() ;
        if ( this.useWellKnownPropertySymbols && wellKnownPropsMap.containsKey(prop) )
            return (String)wellKnownPropsMap.get(prop);

        return formatURI(prop) ;
    }
    
    protected String formatURI(String uriStr)
    {
		String matchURI = "" ;
		String matchPrefix = null ;

        if ( doAbbreviatedBaseURIref && uriStr.equals(baseURIref) )
            return "<>" ;

		// Try for a prefix and write as qname.  Find the longest if several.
        // Possible optimization: split URI and have URI=> ns: map.
        // Ordering prefixes by length, then first hit is better.
        // 
        // Also: could just assume that the split is on / or #
        // Means we need to find a prefix just once. 
		for ( Iterator pIter = prefixMap.keySet().iterator() ; pIter.hasNext() ; )
		{
			String p = (String)pIter.next() ;
			String u = (String)prefixMap.get(p) ;
			if ( uriStr.startsWith(u) )
				if ( matchURI.length() < u.length() )
				{
					matchPrefix = p ;
					matchURI = u ;
				}
		}
		if ( matchPrefix != null )
		{
			String localname = uriStr.substring(matchURI.length()) ;
            
            if ( checkQName(matchPrefix, localname) )
                return matchPrefix+":"+localname ;

            // Continue and return quoted URIref
		}

		// Not as a qname - write as a quoted URIref
		// Should we unicode escape here?
        // It should be right - the writer should be UTF-8 on output.
		return "<"+uriStr+">" ;
	}

    // Qnames in N3 aren't really qnames
    //   No dots; digit can be first
    // These tests must agree, or be more restrictive, than the parser. 
    static boolean checkQName(String ns, String local)
    {
        return checkQNameNamespace(ns) && checkQNameLocalname(local) ;
    }
    
    static boolean checkQNameNamespace(String s)
    {
        return checkQNamePart(s) ;
    }
    static boolean checkQNameLocalname(String s)
    {
        return checkQNamePart(s) ;
    }

    
    static boolean checkQNamePart(String s)
    {
        boolean isOK = true ;
        CharacterIterator cIter = new StringCharacterIterator(s) ;
        
        for ( char ch = cIter.first() ;
              ch != java.text.CharacterIterator.DONE ;
              ch = cIter.next() )
        {
            if ( Character.isLetterOrDigit(ch) )
                continue ;
            switch (ch)
            {
                case '_': case '-':
                    continue ;
            }
            // Not an acceptable characters
            isOK = false ;
            break ;
        }
        return isOK ; 
    }
    
    final static String WS = "\n\r\t" ;

	protected static void string(StringBuffer sbuff, String s, boolean singleQuoteLiteral)
    {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            // Escape escapes and quotes
            if (c == '\\' || c == '"' )
            {
                sbuff.append('\\') ;
                sbuff.append(c) ;
                continue ;
            }
            
            // Characters to literally output.
            // This would generate 7-bit safe files 
//            if (c >= 32 && c < 127)
//            {
//                sbuff.append(c) ;
//                continue;
//            }    

            // Whitespace
            if ( singleQuoteLiteral && ( c == '\n' || c == '\r' || c == '\f' ) )
            {
                if (c == '\n') sbuff.append("\\n");
                if (c == '\t') sbuff.append("\\t");
                if (c == '\r') sbuff.append("\\r");
                if (c == '\f') sbuff.append("\\f");
                continue ;
            }
            
            // Output as is (subject to UTF-8 encoding on output that is)
            sbuff.append(c) ;
            
//            // Unicode escapes
//            // c < 32, c >= 127, not whitespace or other specials
//            String hexstr = Integer.toHexString(c).toUpperCase();
//            int pad = 4 - hexstr.length();
//            sbuff.append("\\u");
//            for (; pad > 0; pad--)
//                sbuff.append("0");
//            sbuff.append(hexstr);
        }
    }
    
    protected int calcPropertyPadding(String propStr)
    {
        int padding = propertyCol - propStr.length();
        if (padding < minGap)
            padding = minGap;
        return padding ;
    }
     
	protected static String pad(int cols)
	{
		StringBuffer sb = new StringBuffer() ;
		for ( int i = 0 ; i < cols ; i++ )
			sb.append(' ') ;
		return sb.toString() ;
	}

	// Utilities

    protected int countProperties(Resource r) 
	{
		int numProp = 0 ;
		StmtIterator sIter = r.listProperties() ;
		for ( ; sIter.hasNext() ; )
		{
			sIter.nextStatement() ;
			numProp++ ;
		}
		sIter.close() ;
		return numProp ;
	}

    protected int countProperties(Resource r, Property p) 
	{
		int numProp = 0 ;
		StmtIterator sIter = r.listProperties(p) ;
		for ( ; sIter.hasNext() ; )
		{
			sIter.nextStatement() ;
			numProp++ ;
		}
		sIter.close() ;
		return numProp ;
	}


    protected int countArcsTo(Resource resource) 
	{
		return countArcsTo(null, resource) ;
	}

    protected int countArcsTo(Property prop, Resource resource) 
	{
		int numArcs = 0 ;
		StmtIterator sIter = resource.getModel().listStatements(null, prop, resource) ;
		for ( ; sIter.hasNext() ; )
		{
			sIter.nextStatement() ;
			numArcs++ ;
		}
		sIter.close() ;
		return numArcs ;
	}


    protected Iterator rdfListIterator(Resource r)
	{
		List list = new ArrayList() ;

		for ( ; ! r.equals(RDF.nil); )
		{
			StmtIterator sIter = r.getModel().listStatements(r, RDF.first, (RDFNode)null) ;
			list.add(sIter.nextStatement().getObject()) ;
			if ( sIter.hasNext() )
				// @@ need to cope with this (unusual) case
				throw new JenaException("N3: Multi valued list item") ;
			sIter = r.getModel().listStatements(r, RDF.rest, (RDFNode)null) ;
			r = (Resource)sIter.nextStatement().getObject() ;
			if ( sIter.hasNext() )
				throw new JenaException("N3: List has two tails") ;
		}
		return list.iterator() ;
	}
    
    // Convenience operations for accessing system properties.
    
    protected String getStringValue(String prop, String defaultValue)
    {
        String p = getPropValue(prop) ;
        
        if ( p == null )
            return defaultValue ;
        return p ;
    }
     
    protected boolean getBooleanValue(String prop, boolean defaultValue)
    {
        String p = getPropValue(prop) ;
        
        if ( p == null )
            return defaultValue ;
            
        if ( p.equalsIgnoreCase("true") )
            return true ;
        
        if ( p.equals("1") )
            return true ;
            
        return false ;
    }        

    protected int getIntValue(String prop, int defaultValue)
    {
        String p = getPropValue(prop) ;
        if ( p == null )
            return defaultValue ;
        try {
            return Integer.parseInt(p) ;
        } catch (NumberFormatException ex)
        {
            logger.warn("Format error for property: "+prop) ;
            return defaultValue ;
        }
    }
    
    // May be the absolute or local form of the property name
    
    protected String getPropValue(String prop)
    {
        prop = absolutePropName(prop) ;
        if ( writerPropertyMap != null && writerPropertyMap.containsKey(prop) )
            return (String)writerPropertyMap.get(prop) ;
        String s = System.getProperty(prop) ;
        if ( s == null )
            s = System.getProperty(localPropName(prop)) ;
        return s ;
    }
    
    protected String absolutePropName(String propName)
    {
        if ( propName.indexOf(':') == -1 )
            return N3JenaWriter.propBase + propName ;
        return propName ;
    }
    
    protected String localPropName(String propName)
    {
        if ( propName.startsWith(N3JenaWriter.propBase) )
            propName = propName.substring(N3JenaWriter.propBase.length()) ;
        return propName ;
    }
    
    
}

/*
 *  (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
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
