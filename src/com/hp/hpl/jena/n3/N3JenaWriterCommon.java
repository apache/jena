/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.n3;

import com.hp.hpl.jena.util.iterator.* ;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelCom ;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.vocabulary.OWL ;
import com.hp.hpl.jena.vocabulary.RDF ;

import java.util.* ;
import java.io.* ;

/** Common framework for implemening N3 writers.
 *
 * @author		Andy Seaborne
 * @version 	$Id: N3JenaWriterCommon.java,v 1.8 2003-07-29 14:37:54 chris-dollin Exp $
 */

public class N3JenaWriterCommon implements RDFWriter
{
    //static Logger logger = Logger.getLogger(N3JenaWriterCommon.class.getName()) ;
    
	// N3 writing proceeds in 2 stages.
    // First, it analysis the model to be written to extract information
    // that is going to be specially formatted (RDF lists, one ref anon nodes)
    // Second do the writing walk.
    
    // The simple N3 writer does nothing during preparation.
    
    static final boolean doAbbreviatedBaseURIref = true ; 
    boolean alwaysAllocateBNodeLabel = false ;
    
    // Common variables
	RDFErrorHandler errorHandler = null;
	Map writerPropertyMap = new HashMap() ;

	static final String NS_W3_log = "http://www.w3.org/2000/10/swap/log#" ;

	Map prefixMap 	   	= new HashMap() ;	// Prefixes to actually use
	Map	bNodesMap       = null ;		// BNodes seen.
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
    int minGap = 1 ;
    final String minGapStr = pad(minGap) ;

    // Gap from subject to property
	int indentProperty = 6 ;
    
    // Width of property before wrapping.
    // This is not necessarily a control of total width
    // e.g. the pretty writer may be writing properties inside indented one ref bNodes 
    int widePropertyLen = 20 ;
    
    // Column for property when an object follows a property on the same line
    int propertyCol = 8 ;
    
    // Max width of property to align to.
    // Property may be longer and still go on same line but the columnization is broken. 
    // Allow for min gap.
    // Require propertyWidth < propertyCol (strict less than)
    int propertyWidth = propertyCol-minGap ;

    //  Gap from property to object when object on a new line.
    int indentObject = propertyCol ;
    
    // If a subject is shorter than this, the first property may go on same line.
    int subjectCol = indentProperty ; 
    // Require shortSubject < subjectCol (strict less than)
    int shortSubject = indentProperty-minGap;

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
        Object obj = writerPropertyMap.get(propName);
        writerPropertyMap.put(propName, propValue);
        return obj;
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
        // NB shortSubject is (indentProperty-minGap) so there is a gap.
        if (subjStr.length() <= shortSubject )
        {
            out.print(pad(subjectCol - subjStr.length()) );
        }
        else
            // Does not fit this line.
            out.println();
    }
    
    protected void writeHeader(Model model)
    {
        if (baseURIref != null)
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
                if (u.equals(baseURIrefHash))
                    u = "#";
                if (u.equals(baseURIref))
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
        String propStr = null;

        if (wellKnownPropsMap.containsKey(property.getURI()))
            propStr = (String) wellKnownPropsMap.get(property.getURI());
        else
            propStr = formatResource(property);

        // Write with object lists as clusters of statements with the same property
        // Looks more like a machine did it but fewer bad cases.

        StmtIterator sIter = resource.listProperties(property);
        for (; sIter.hasNext();)
        {
            Statement stmt = sIter.nextStatement() ;
            String objStr = formatNode(stmt.getObject()) ;
            
            out.print(propStr);
            out.incIndent(indentObject);

            if ( propStr.length() < widePropertyLen )
            {
                // Property col allows for min gap but widePropertyLen > propertyCol 
                // (which looses alignment - this is intentional.
                // Ensure there is at least min gap.
                
                int padding = propertyCol-propStr.length() ;
                if ( padding < minGap )
                    padding = minGap ;
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
    
    protected String formatProperty(Property p)
    {
        String prop = p.getURI() ;
        if ( wellKnownPropsMap.containsKey(prop) )
            return (String)wellKnownPropsMap.get(prop);

        return formatURI(prop) ;
    }
    
    protected String formatURI(String uriStr)
    {
		String matchURI = "" ;
		String matchPrefix = null ;

        if ( doAbbreviatedBaseURIref && uriStr.equals(baseURIref) )
            return "<>" ;

		// Try for a prefix and write as qname
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
			// If there was a dot in the localname part of the qname,
			// then skip output a quoted URIref
			// (nsprefix should not have a dot in it - got thrown out
			// earlier).

            // Quick hack = fix properly by consolidating character
            // set handling with the reader 
			String localname = uriStr.substring(matchURI.length()) ;
            boolean isOK = true ;
            for ( int i = 0 ; i < localname.length() ; i++ )
            {
                char ch = localname.charAt(i) ;
                switch (ch)
                {
                    case '?': case '=': case ':': case '.':
                        isOK = false ;
                        break ;
                    default:
                        continue ;
                }
            }
            if ( isOK )
                return matchPrefix+":"+localname ;

            // Continue and return quoted URIref
		}

		// Not as a qname - write as a quoted URIref
		// URIref
		return "<"+uriStr+">" ;
	}


	protected String formatLiteral(Literal literal)
	{
        String datatype = literal.getDatatypeURI() ;
        String lang = literal.getLanguage() ;
		String s = literal.getLexicalForm() ;

		int j = 0 ;
		int i = -1 ;

        StringBuffer sbuff = new StringBuffer() ;
        sbuff.append("\"");
        
		for(;;)
		{
			i = s.indexOf('"',j) ;

			if (i == -1)
			{
                sbuff.append(s.substring(j));
                sbuff.append("\"");
				break;
			}
            sbuff.append(s.substring(j, i));
            sbuff.append("\\\"");
			j = i + 1;
		}

        if ( lang != null && lang.length()>0)
        {
            sbuff.append("@") ;
            sbuff.append(lang) ;
        }
        if ( datatype != null )
        {
            sbuff.append("^^") ;
            sbuff.append(formatURI(datatype)) ;
        }
        return sbuff.toString() ;
	}


	protected String pad(int cols)
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
    
}

/*
 *  (c) Copyright Hewlett-Packard Company 2001-2003
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
