/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

// To do:
//   Split into different writers for fast and pretty with common superclass
//     Superclass has common operations (formatting), output stream, prefix map
//   Options
//     Make some of the config variable depend on system properties
//     Document the environment variables including I/O howto.
//   Better layout:
//     Better deciding when to use current line
//       need to look at next items before deciding on a newline or not.
//     Deciding on one line or several for:
//       RDF lists
//       Object lists (currently swicthed off)
//       Property lists
//     Clustering : rdf:type to front.
//     Clustering : same namespace together


package com.hp.hpl.jena.n3;

//import org.apache.log4j.Logger;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelCom ;
import com.hp.hpl.jena.vocabulary.OWL ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.util.JenaException ;

import java.util.* ;
import java.io.* ;

/** An N3 pretty printer.
 *  Tries to make N3 data look readable - works better on regular data.
 *
 * @author		Andy Seaborne
 * @version 	$Id: N3JenaWriter.java,v 1.12 2003-04-28 14:22:25 andy_seaborne Exp $
 */



public class N3JenaWriter implements RDFWriter
{
    //static Logger logger = Logger.getLogger("com.hp.hpl.jena.n3.N3JenaWriter") ;
    
	// This N3 writer proceeds in 2 stages.  First, it analysises the model to be
	// written to extract information that is going to be specially formatted
	// (RDF lists, small anon nodes) and to calculate the prefixes that will be used.

    static final private boolean doObjectListsAsLists = false ;
    
    // Write the N3 out using the base URI to reduce certain things: 
    // - Write URIs that match the base name as <>
    // - Write prefix declarations for <> and/or <#> in short form.
    
    static final private boolean doAbbreviatedBaseURIref = true ;
	static public boolean DEBUG = false ;

	RDFErrorHandler errorHandler = null;
	Map writerPropertyMap = new HashMap() ;

	public static final String propWriteSimple = "com.hp.hpl.jena.n3.N3JenaWriter.writeSimple" ;

	int bNodeCounter = 0 ;

	static final String NS_W3_log = "http://www.w3.org/2000/10/swap/log#" ;

	// Data structures used in controlling the formatting

	Set rdfLists      	= null ; 		// Heads of daml lists
	Set rdfListsAll   	= null ;		// Any resources in a daml lists
	Set rdfListsDone  	= null ;		// RDF lists written
	Set roots          	= null ;		// Things to put at the top level
	Set oneRefObjects 	= null ;		// Bnodes referred to once as an object - can inline
	Set oneRefDone   	= null ;		// Things done - so we can check for missed items
	Map prefixMap 	   	= new HashMap() ;	// Prefixes to actually use
	Map	bNodesMap       = null ;		// BNodes seen.

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
	String indent = pad(6) ;
	int minGap = 1 ;

    boolean doingPrettyWrite = true ;

	public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler)
	{
		RDFErrorHandler old = errorHandler;
		errorHandler = errHandler;
		return old;
	}

	public void setNsPrefix(String prefix, String ns)
	{
		if ( prefix.endsWith(":") )
			prefix = prefix.substring(0,prefix.length()-1) ;
		if ( prefix.indexOf('.') != -1 )
            throw new JenaException("N3JenaWriter.setNsPrefix: N3 prefixes can't contain a '.':: "+prefix) ;
		prefixMap.put(prefix, ns) ;
	}

    public String getPrefixFor( String uri )
        { return (String)prefixMap.get(uri); }

	public Object setProperty(String propName, Object propValue) throws RDFException
	{
		Object obj = writerPropertyMap.get(propName) ;
		writerPropertyMap.put(propName, propValue) ;
		return obj ;
	}

	/** Write the model out in N3.  The writer should be one suitable for UTF-8 which
	 * excludes a PrintWriter or a FileWriter which use default character set.
	 *
	 * Examples:
	 * <pre>
	 * try {
	 *		Writer w =  new BufferedWriter(new OutputStreamWriter(output, "UTF-8")) ;
	 *		model.write(w, base) ;
	 *		try { w.flush() ; } catch (IOException ioEx) {}
	 *	} catch (java.io.UnsupportedEncodingException ex) {} //UTF-8 is required so can't happen
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
	 */

    public void write(Model baseModel, Writer _out, String base) throws RDFException
    {
        Model model = ModelCom.withHiddenStatements( baseModel );

        Object obj = writerPropertyMap.get(propWriteSimple);

        try {
            if (obj == null)
                obj = System.getProperty(propWriteSimple);
        } catch (SecurityException secEx) {}

        if (obj != null && obj instanceof String)
        {
            if (!"false".equals((String) obj))
                doingPrettyWrite = false;
        }

        if (!doingPrettyWrite)
        {
            writeSimple(model, _out, base);
            return;
        }

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
        
		// Allocate datastructures - allows reuse of a writer
		startWriting() ;

		prepare(model) ;

		// Phase 2:
		// Do the output.
		writeModel(model) ;

		// Release intermediate memory - allows reuse of a writer
		finishWriting() ;
	}


	// Not the Jena writer interface

	/** Write the model out in N3, encoded in in UTF-8
	 * @see #write(Model,Writer,String)
	 */

	public synchronized void write(Model model, OutputStream output, String base) throws RDFException
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


	private void prepare(Model model) throws RDFException
	{
		preparePrefixes(model) ;
		prepareLists(model) ;
		prepareOneRefBNodes(model) ;
	}


	private void preparePrefixes(Model model) throws RDFException
	{
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
	}

	// Find well-formed RDF lists - does not find empty lists (this is intentional)
	// Works by finding all tails, and work backwards to the head.
    // RDF lists may, or may not, have a type element.

	private void prepareLists(Model model) throws RDFException
	{
		Set thisListAll = new HashSet();

		StmtIterator listTailsIter = model.listStatements(null, RDF.rest, RDF.nil);

		// For every tail of a list
		tailLoop:
		for ( ; listTailsIter.hasNext() ; )
		{
			// The resource for the current element being considered.
			Resource listElement  = listTailsIter.nextStatement().getSubject() ;
            // The resource pointing to the link we have just looked at.
            Resource validListHead = null ;

			// Chase to head of list
			for ( ; ; )
			{
				boolean isOK = checkListElement(listElement) ;
				if ( ! isOK )
					break ;

				// At this point the element is exactly a DAML list element.
				if ( DEBUG ) out.println("# RDF list all: "+formatResource(listElement)) ;
				validListHead = listElement ;
				thisListAll.add(listElement) ;

				// Find the previous node.
				StmtIterator sPrev = model.listStatements(null, RDF.rest, listElement) ;

				if ( ! sPrev.hasNext() )
					// No daml:rest link
					break ;

				// Valid pretty-able list.  Might be longer.
				listElement = sPrev.nextStatement().getSubject() ;
				if ( sPrev.hasNext() )
				{
					if ( DEBUG ) out.println("# RDF shared tail from "+formatResource(listElement)) ;
					break ;
				}
			}
			// At head of a pretty-able list - add its elements and its head.
			if ( DEBUG ) out.println("# DAML list head: "+formatResource(validListHead)) ;
			rdfListsAll.addAll(thisListAll) ;
			if ( validListHead != null )
				rdfLists.add(validListHead) ;
		}
		listTailsIter.close() ;
	}

	// Validate one list element.
	private boolean checkListElement(Resource listElement) throws RDFException
	{
		if (!listElement.hasProperty(RDF.rest)
			|| !listElement.hasProperty(RDF.first))
		{
			if (DEBUG)
				out.println(
					"# RDF list element does not have required properties: "
						+ formatResource(listElement));
			return false;
		}

        // Must be exactly two properties (the ones we just tested for)
        // or three including the RDF.type RDF.List statement.
        int numProp = countProperties(listElement);

        if ( numProp == 2)
            // Must have exactly the properties we just tested for.
            return true ;


        if (numProp == 3)
        {
            if (listElement.hasProperty(RDF.type, RDF.List))
                return true;
            if (DEBUG)
                out.println(
                    "# RDF list element: 3 properties but no rdf:type rdf:List"
                        + formatResource(listElement));
            return false;
        }

        if (DEBUG)
            out.println(
                "# RDF list element does not right number of properties: "
                    + formatResource(listElement));
        return false;
	}

	// Find bnodes that are objects of only one statement (and hence can be inlined)
	// which are not RDF lists.

	private void prepareOneRefBNodes(Model model) throws RDFException
	{

		NodeIterator objIter = model.listObjects() ;
		for ( ; objIter.hasNext() ; )
		{
			RDFNode n = objIter.nextNode() ;
			if ( ! ( n instanceof Resource ) )
				continue ;

			Resource obj = (Resource)n ;

			if ( obj.getURI() != null && ! obj.getURI().equals("") )
				// Not a bNode.
				continue ;

			if ( rdfListsAll.contains(obj) )
				// RDF list (head or element)
				continue ;

			StmtIterator pointsToIter = model.listStatements(null, null, obj) ;
			if ( ! pointsToIter.hasNext() )
				// Corrupt graph!
				throw new RuntimeException(this.getClass().getName()+": found object with no arcs!") ;

			Statement s = pointsToIter.nextStatement() ;
			if ( ! pointsToIter.hasNext() )
			{
				if ( DEBUG )
					out.println("# OneRef: "+formatResource(obj)) ;
				oneRefObjects.add(obj) ;
			}
		}
		objIter.close() ;

		// Debug
		if ( DEBUG )
		{
			out.println("# RDF Lists      = "+rdfLists.size()) ;
			out.println("# RDF ListsAll   = "+rdfListsAll.size()) ;
			out.println("# oneRefObjects  = "+oneRefObjects.size()) ;
		}
	}

	// Work function for doing the writing.

	private void writeModel(Model model) throws RDFException
	{
        if ( baseURIref != null )
            out.println("# Base: "+baseURIref) ;
        
		for ( Iterator pIter = prefixMap.keySet().iterator() ; pIter.hasNext() ; )
		{
			String p = (String)pIter.next() ;
			String u = (String)prefixMap.get(p) ;

            // Special cases: N3 handling of base names.
            if ( doAbbreviatedBaseURIref && p.equals("") )
            {
                if ( u.equals(baseURIrefHash) )
                    u = "#" ;
                if ( u.equals(baseURIref) )
                    u = "" ;
            }
            
			String tmp = "@prefix "+p+": " ;
			out.print(tmp) ;
			out.print(pad(16-tmp.length())) ;
			out.println("<"+u+"> .") ;
		}

        if ( prefixMap.size() != 0 )
            out.println() ;

		boolean doingFirst = true ;
		ResIterator rIter = model.listSubjects() ;
		for ( ; rIter.hasNext() ; )
		{
			// Subject:
			// First - it is something we will write out as a structure in an object field?
			// That is, a RDF list or the object of exactly one statement.
			Resource subj = rIter.nextResource() ;
			if ( rdfListsAll.contains(subj)   ||
				 oneRefObjects.contains(subj)  )
			{
				if ( DEBUG )
					out.println( "# Skipping: "+formatResource(subj)) ;
				continue ;
			}

			// We really are going to print something via writeTriples
			if ( doingFirst )
				doingFirst = false ;
			else
				out.println() ;

			// New top level item.
			writeSubject(subj, true) ;
		}
		rIter.close() ;

		// Should be no "one ref" objects: either they were found at the top level
		// or they were written embedded.  However loops of "one ref" can occur :-)

		oneRefObjects.removeAll(oneRefDone) ;

		for ( Iterator leftOverIter = oneRefObjects.iterator() ; leftOverIter.hasNext(); )
		{
			out.println() ;
			if ( DEBUG )
				out.println("# One ref") ;
			writeSubject((Resource)leftOverIter.next() , false) ;
		}


		// Are there any unattached RDF lists?
		// We missed these earlier (assumed all DAML lists are values of some statement)
		for ( Iterator leftOverIter = rdfLists.iterator() ; leftOverIter.hasNext(); )
		{
			Resource r = (Resource)leftOverIter.next() ;
			if ( rdfListsDone.contains(r) )
				continue ;
			out.println() ;
			if ( DEBUG )
				out.println("# RDF List") ;
			if ( countArcsTo(r) > 0 )
			{
				// Name it.
				out.print(formatResource(r)) ;
				out.print(" :- ") ;
			}
			writeList(r) ;
			out.println( " .") ;
		}

		//out.println() ;
		//writeModelSimple(model,  bNodesMap, base) ;

		out.flush() ;
	}


	private void writeSubject(Resource resource, boolean allowDeep)
		throws RDFException
	{
		String tmp = formatResource(resource);
		out.print(tmp);
		// Currently at end of subject
		if (tmp.length() + minGap < indent.length())
			out.print( pad(indent.length() - tmp.length()));
		else
		{
			// Does not fit this line.
			out.println();
			out.print(indent);
		}
		out.incIndent(indent.length()) ;
		writePropertyList(resource, allowDeep) ;
		out.decIndent(indent.length()) ;
		out.println( " .");
		//out.setIndent(0) ;
	}

	// Finish at end of line.
	private void writePropertyList(Resource resource, boolean allowDeep)
		throws RDFException
	{
		// Properties to do.
		Set properties = new HashSet() ;
		StmtIterator sIter = resource.listProperties();
		for ( ; sIter.hasNext() ; )
		{
			properties.add(sIter.nextStatement().getPredicate()) ;
		}
		sIter.close() ;

        // Should write certain well know properties in standard order
        // e.g. rdf:type, rdfs:subClassOf, rdfs:subPropertyOf

	topLevelLoop:
		// For each property.
		for (Iterator iter = properties.iterator() ; iter.hasNext();)
		{
			Property property = (Property)iter.next() ;

			// Object list
			writeObjectList(resource, property, allowDeep) ;

			if (iter.hasNext())
				out.println( " ;");
		}
	}


	// Need to decide between one line or many.
    // Very hard to do a pretty thing here because the objects may be large or small or a mix.

    private void writeObjectList(Resource resource, Property property, boolean allowDeep)
        throws RDFException
    {

        String propStr = null;

        if (wellKnownPropsMap.containsKey(property.getURI()))
            propStr = (String) wellKnownPropsMap.get(property.getURI());
        else
            propStr = formatResource(property);

        if (doObjectListsAsLists)
        {
            // Witre object lists as "property obj, obj, obj ;"
            // Often does a bad job when objs are large or structured

            out.print(propStr);

            // Currently at end of property
            if (propStr.length() + minGap < indent.length())
                out.print(pad(indent.length() - propStr.length()));
            else
            {
                // Does not fit this line.
                out.println();
                out.print(indent);
            }
            out.incIndent(indent.length());

            // Do all the statements with the same property.
            StmtIterator sIter = resource.listProperties(property);
            for (; sIter.hasNext();)
            {
                Statement stmt = sIter.nextStatement();
                writeObject(stmt.getObject(), allowDeep);

                // As an object list
                if (sIter.hasNext())
                    out.print(" , ");
            }
            sIter.close();
            out.decIndent(indent.length());
            return;

        }

        // Write with object lists as clsuters of statements with the same property
        // Looks more like a machine did it but fewer bad cases.

        StmtIterator sIter = resource.listProperties(property);
        for (; sIter.hasNext();)
        {
            Statement stmt = sIter.nextStatement() ;
            out.print(propStr);

            // Currently at end of property
            if (propStr.length() + minGap < indent.length())
                out.print(pad(indent.length() - propStr.length()));
            else
            {
                // Does not fit this line.
                out.println();
                out.print(indent);
            }
            out.incIndent(indent.length());
            // Write one object
            writeObject(stmt.getObject(), allowDeep) ;
            out.decIndent(indent.length());

            if ( sIter.hasNext() )
            {
                out.println(" ;") ;
            }
        }
        sIter.close() ;

	}

	private void writeObject(RDFNode node, boolean allowDeep) throws RDFException
	{
		if (node instanceof Literal)
		{
			writeLiteral((Literal) node);
			return;
		}

		Resource rObj = (Resource) node;
		if (allowDeep && oneRefObjects.contains(rObj))
		{
			oneRefDone.add(rObj);
			int oldIndent = out.getIndent();
			out.setIndent(out.getCol());

			//out.incIndent(4);
			//out.println();
			out.print("[ ");
			out.incIndent(2);
			writePropertyList(rObj, allowDeep);
			out.decIndent(2);
			out.print(" ]");
			//out.decIndent(4);

			out.setIndent(oldIndent);
			return;
		}

		if (rdfLists.contains(rObj))
			if (countArcsTo(rObj) <= 1)
			{
				writeList(rObj);
				return;
			}

		out.print(formatResource(rObj));
	}



	// Need to out.print in short (all on one line) and long forms (multiple lines)
	// That needs starts point depth tracking.
	private void writeList(Resource resource)
		throws RDFException
	{
		out.print( "(");
		out.incIndent(2) ;
		boolean listFirst = true;
		for (Iterator iter = rdfListIterator(resource); iter.hasNext();)
		{
			if (!listFirst)
				out.print( " ");
			listFirst = false;
			RDFNode n = (RDFNode) iter.next();
			writeObject(n, true) ;
		}
		out.print( ")");
		out.decIndent(2) ;
		rdfListsDone.add(resource);

	}

	private String formatResource(Resource r)
	{
		if ( r.isAnon() )
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

			if ( ! bNodesMap.containsKey(r) )
				bNodesMap.put(r, "_:b"+(++bNodeCounter)) ;
			return (String)bNodesMap.get(r) ;

		}

		// It has a URI.
		if ( r.equals(RDF.nil) )
			return "()" ;

        return formatURI(r.getURI()) ;
    }
    
    private String formatURI(String uriStr)
    {
		String matchURI = "" ;
		String matchPrefix = null ;

        if ( doAbbreviatedBaseURIref && uriStr.equals(baseURIref) )
            return "<>" ;

//		// Because we always use "@prefix : <#> ."
//		if ( doingBaseHash && uriStr.startsWith(baseName+"#") )
//		{
//			String localname = uriStr.substring((baseName+"#").length()) ;
//			if ( localname.indexOf('.') == -1 )
//				return ":"+localname ;
//		}
//		else
		{
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
				String localname = uriStr.substring(matchURI.length()) ;
				if ( localname.indexOf('.') == -1 )
					return matchPrefix+":"+localname ;
			}
		}
		// Not as a qname - write as a quoted URIref
		// URIref
		return "<"+uriStr+">" ;
	}


	private void writeLiteral(Literal literal)
	{
        String datatype = literal.getDatatypeURI() ;
        String lang = literal.getLanguage() ;
		String s = literal.toString() ;

		int j = 0 ;
		int i = -1 ;

		out.print("\"");
		for(;;)
		{
			i = s.indexOf('"',j) ;

			if (i == -1)
			{
				out.print(s.substring(j));
				out.print("\"");
				break;
			}
			out.print(s.substring(j, i));
			out.print("\\\"");
			j = i + 1;
		}

        if ( lang != null && lang.length()>0)
        {
            out.print("@") ;
            out.print(lang) ;
        }
        if ( datatype != null )
        {
            out.print("^^") ;
            out.print(formatURI(datatype)) ;
        }
	}


	private String pad(int cols)
	{
		StringBuffer sb = new StringBuffer() ;
		for ( int i = 0 ; i < cols ; i++ )
			sb.append(' ') ;
		return sb.toString() ;
	}

	// Called before each writing run.
	protected void startWriting()
	{
		rdfLists 		= new HashSet() ;
		rdfListsAll 	= new HashSet() ;
		rdfListsDone 	= new HashSet() ;
		oneRefObjects 	= new HashSet() ;
		oneRefDone 		= new HashSet() ;
		//prefixMap - retained across runs
		bNodesMap		= new HashMap() ;
	}

	// Especially release large intermediate memory objects
	protected void finishWriting()
	{
		rdfLists 		= null ;
		rdfListsAll 	= null ;
		rdfListsDone 	= null ;
		oneRefObjects 	= null ;
		oneRefDone 		= null ;
		bNodesMap		= null ;
	}

	// Utilities

	private int countProperties(Resource r) throws RDFException
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

	private int countProperties(Resource r, Property p) throws RDFException
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


	private int countArcsTo(Resource resource) throws RDFException
	{
		return countArcsTo(null, resource) ;
	}

	private int countArcsTo(Property prop, Resource resource) throws RDFException
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


	private Iterator rdfListIterator(Resource r)
		throws RDFException
	{
		List list = new ArrayList() ;

		for ( ; ! r.equals(RDF.nil); )
		{
			StmtIterator sIter = r.getModel().listStatements(r, RDF.first, (RDFNode)null) ;
			list.add(sIter.nextStatement().getObject()) ;
			if ( sIter.hasNext() )
				// @@ need to cope with this (unusual) case
				throw new RuntimeException("Multi valued list item") ;
			sIter = r.getModel().listStatements(r, RDF.rest, (RDFNode)null) ;
			r = (Resource)sIter.nextStatement().getObject() ;
			if ( sIter.hasNext() )
				throw new RuntimeException("List has two tails") ;
		}
		return list.iterator() ;
	}



	// Writes the model, no fancy formatting.
	public synchronized void writeSimple(Model model, Writer _out, String base) throws RDFException
	{
        doingPrettyWrite = false ;

		if ( ! ( _out instanceof BufferedWriter ) )
			_out = new BufferedWriter(_out) ;
		out = new IndentedWriter(_out) ;

		baseURIref = base ;
		startWriting() ;
		writeModelSimple(model) ;
		finishWriting() ;
	}


	private void writeModelSimple(Model model) throws RDFException
	{
		for ( Iterator pIter = prefixMap.keySet().iterator() ; pIter.hasNext() ; )
		{
			String p = (String)pIter.next() ;
			String u = (String)prefixMap.get(p) ;
			out.println( "@prefix "+p+": <"+u+"> .") ;
		}

		if ( !prefixMap.containsKey("") )
		{
			//doingBaseHash = true ;
			out.println( "@prefix : <#> .") ;
		}

//		if ( doingBaseHash || prefixMap.size() != 0 )
//			out.println() ;

        if ( prefixMap.size() != 0 )
            out.println() ;

		// Works by running the same code but with empty control structures.
		ResIterator rIter = model.listSubjects() ;
		for ( ; rIter.hasNext() ; )
		{
			writeSubject(rIter.nextResource(), false) ;
			if ( rIter.hasNext() )
				out.println() ;
		}
		rIter.close() ;
		out.flush() ;
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
