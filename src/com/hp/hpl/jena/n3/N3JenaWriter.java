/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

// To do:
//   Choosing prefixes
//   Printing only in use prefixes.
//   Options
//   Deciding on one line or several for:
//     DAML lists
//     Object lists
//     Property lists

package com.hp.hpl.jena.n3;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.util.Log;
import com.hp.hpl.jena.vocabulary.*;

import java.util.* ;
import java.io.* ;

/** An N3 pretty printer.
 *  Tries to make N3 data look readable - works better on regular data.
 * 
 * @author		Andy Seaborne
 * @version 	$Id: N3JenaWriter.java,v 1.4 2003-02-11 15:17:15 chris-dollin Exp $
 */



public class N3JenaWriter implements RDFWriter
{
	// This N3 writer proceeds in 2 stages.  First, it analysises the model to be
	// written to extract information that is going to be specially formatted 
	// (DAML lists, small anon nodes) and to calculate the prefixes that will be used.
	
	static public boolean DEBUG = false ;
	
	RDFErrorHandler errorHandler = null;
	Map writerPropertyMap = new HashMap() ;
	public static final String propWriteSimple = "com.hp.hpl.jena.n3.N3JenaWriter.writeSimple" ;
	
	int bNodeCounter = 0 ;
	
	static final DAMLVocabulary damlVocabulary = DAML_OIL.getInstance() ;
	//static final DAMLVocabulary damlVocabulary = DAML_OIL_2000_12.getInstance() ;
	
	static final String NS_W3_log = "http://www.w3.org/2000/10/swap/log#" ;
	
	// Data structures used in controlling the formatting
	
	Set damlLists      	= null ; 		// Heads of daml lists
	Set damlListsAll   	= null ;		// Any resources in a daml lists
	Set damlListsDone  	= null ;		// DAML lists written
	Set roots          	= null ;		// Things to put at the top level
	Set oneRefObjects 	= null ;		// Bnodes refered to one as an object - can inline
	Set oneRefDone   	= null ;		// Things done - so we can check for missed items
	Set prefixesUsed   	= null ;		// Prefixes seen
	Map prefixMap 	   	= new HashMap() ;	// Prefixes to actually use
	Map	bNodesMap       = null ;		// BNodes seen.
	
	static Map wellKnownPropsMap = new HashMap() ;
	static {
		wellKnownPropsMap.put(NS_W3_log+"implies",				     	"=>" ) ;
		wellKnownPropsMap.put(damlVocabulary.equivalentTo().getURI(),	"="  ) ;
		wellKnownPropsMap.put(RDF.type.getURI(),				     	"a"  ) ;
	}
	
	// Work variables controlling the output
	IndentedWriter out = null ;
	String baseName = null ;
	String indent = pad(6) ;
	int minGap = 1 ;
	
	boolean doingBaseHash = false ;
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
		{
			Log.warning("N3 names prefix can't contain a '.'", "N3JenaWriter", "setNsPrefix") ;
			return ;
		}
		prefixMap.put(prefix, ns) ;
	}

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
	
    public void write(Model model, Writer _out, String base) throws RDFException
    {
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

        baseName = base;
		
		// Allocate datastructures - allows reuse of a writer
		startWriting() ;

		prepare(model, base) ;
		
		// Phase 2:
		// Do the output.
		writeModel(model) ;
		
		// Release intermediate memory - allows reuse of a writer
		finishWriting() ;
	}
	
	
	// Not the Jena writer interface
	
	/** Write the model out in N3, encoded in in UTF-8
	 * @see write(Model,Writer,String)
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

	
	private void prepare(Model model, String base) throws RDFException
	{
		preparePrefixes(model) ;
		prepareDAMLLists(model) ;
		prepareOneRefBNodes(model) ;
	}
	

	// Needs to be better
	private void preparePrefixes(Model model) throws RDFException
	{
		if ( !prefixMap.containsValue(RDF.getURI()) && !prefixMap.containsKey("rdf") )
			setNsPrefix("rdf", RDF.getURI()) ;
			
		if ( !prefixMap.containsValue(RDFS.getURI()) && !prefixMap.containsKey("rdfs") )
			setNsPrefix("rdfs", RDFS.getURI()) ;
			
		if ( !prefixMap.containsValue(damlVocabulary.NAMESPACE_DAML().getURI())
			  && !prefixMap.containsKey("daml") )
			setNsPrefix("daml", damlVocabulary.NAMESPACE_DAML().getURI()) ;
		
		if ( !prefixMap.containsValue(NS_W3_log) && !prefixMap.containsKey("log") )
			setNsPrefix("log", NS_W3_log) ;
		
		for ( Iterator iter = prefixMap.keySet().iterator() ; iter.hasNext() ; )
		{
			String prefix = (String)iter.next() ;
			if ( prefix.indexOf('.') != -1 )
				iter.remove() ;
		}

	}
	
	// Find DAML lists - does not find empty lists (this is intentional)
	// Works by finding all tails, and work backwards to the head.

	private void prepareDAMLLists(Model model) throws RDFException
	{
		Set thisListAll = new HashSet();

		StmtIterator listTailsIter = listStatements(model, null, damlVocabulary.rest(), damlVocabulary.nil());
		
		// For every tail of a list
		tailLoop:
		for ( ; listTailsIter.hasNext() ; )
		{
			// The resource pointing to the link we have just looked at.
			Resource validListHead = null ;
			// The resource for the current element being considered.
			Resource listElement  = listTailsIter.nextStatement().getSubject() ;
			
			// Chase to head of list
			for ( ; ; )
			{
				boolean isOK = checkDAMLListElement(listElement) ;
				if ( ! isOK )
					break ;
				
				// At this point the element is exactly a DAML list element.
				if ( DEBUG ) out.println("# DAML list all: "+formatResource(listElement)) ;
				validListHead = listElement ;
				thisListAll.add(listElement) ;

				// Find the previous node.
				StmtIterator sPrev = listStatements(model, null, damlVocabulary.rest(), listElement) ;
				
				if ( ! sPrev.hasNext() )
					// No daml:rest link
					break ;
				
				// Valid pretty-able list.  Might be longer.
				listElement = sPrev.nextStatement().getSubject() ;
				if ( sPrev.hasNext() )
				{
					if ( DEBUG ) out.println("# DAML shared tail from "+formatResource(listElement)) ;
					break ;
				}
			}
			// At head of a pretty-able list - add its elements and its head.
			if ( DEBUG ) out.println("# DAML list head: "+formatResource(validListHead)) ;
			damlListsAll.addAll(thisListAll) ;
			if ( validListHead != null )
				damlLists.add(validListHead) ;
		}
		listTailsIter.close() ;
	}
	
	// Validate one list element.
	private boolean checkDAMLListElement(Resource listElement) throws RDFException
	{
		if (!listElement.hasProperty(damlVocabulary.rest())
			|| !listElement.hasProperty(damlVocabulary.first()))
		{
			if (DEBUG)
				out.println(
					"# DAML list element does not have required properties: "
						+ formatResource(listElement));
			return false;
		}

		// Must be exactly two properties (the ones we just tested for).
		int numProp = countProperties(listElement);
		if (numProp != 2)
		{
			if (DEBUG)
			out.println(
					"# DAML list element does not right number of properties: "+formatResource(listElement));
			return false ;
		}
		return true ;
	}
	
	// Find bnodes that are objects of only one statement (and hence can be inlined)
	// which are not DAML lists.

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
			
			if ( damlListsAll.contains(obj) )
				// DAML list (head or element)
				continue ;
				
			StmtIterator pointsToIter = listStatements(model, null, null, obj) ;
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
			out.println("# damlLists      = "+damlLists.size()) ;
			out.println("# damlListsAll   = "+damlListsAll.size()) ;
			out.println("# oneRefObjects  = "+oneRefObjects.size()) ;
		}
	}
	
	// Work function for doing the writing.
	
	private void writeModel(Model model) throws RDFException
	{
		for ( Iterator pIter = prefixMap.keySet().iterator() ; pIter.hasNext() ; )
		{
			String p = (String)pIter.next() ;
			String u = (String)prefixMap.get(p) ;
			String tmp = "@prefix "+p+": " ;
			out.print(tmp) ;
			out.print(pad(16-tmp.length())) ;
			out.println("<"+u+"> .") ;
		}
		
		if ( !prefixMap.containsKey("") )
		{
			doingBaseHash = true ;
			String tmp = "@prefix : " ;
			out.print(tmp) ;
			out.print(pad(16-tmp.length())) ;
			out.println("<#> .") ;
		}
		
		if ( doingBaseHash || prefixMap.size() != 0 )
			out.println() ;
		
		boolean doingFirst = true ;
		ResIterator rIter = model.listSubjects() ;
		for ( ; rIter.hasNext() ; )
		{
			// Subject:
			// First - it is something we will write out as a structure in an object field?
			// That is, a DAML list or the object of exactly one statement.
			Resource subj = rIter.nextResource() ;
			if ( damlListsAll.contains(subj)   ||
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
			writeTriples(subj, true) ;
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
			writeTriples((Resource)leftOverIter.next() , false) ;
		}	
			
		
		// Are there any unattached DAML lists?
		// We missed these earlier (assumed all DAML lists are values of some statement)
		for ( Iterator leftOverIter = damlLists.iterator() ; leftOverIter.hasNext(); )
		{
			Resource r = (Resource)leftOverIter.next() ;
			if ( damlListsDone.contains(r) )
				continue ;
			out.println() ;
			if ( DEBUG )
				out.println("# DAML List") ;
			if ( countArcsTo(r) > 0 )
			{
				// Name it.
				out.print(formatResource(r)) ;
				out.print(" :- ") ;
			}
			writeDamlList(r) ;
			out.println( " .") ;
		}
		
		//out.println() ;
		//writeModelSimple(model,  bNodesMap, base) ;
			
		out.flush() ;
	}
	
	
	private void writeTriples(Resource resource, boolean allowDeep)
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
		// Ones we have done.
		Set properties = new HashSet() ;
		StmtIterator sIter = resource.listProperties();
		for ( ; sIter.hasNext() ; )
		{
			properties.add(sIter.nextStatement().getPredicate()) ;	
		}
		sIter.close() ;
				
	topLevelLoop: 
		// For each property.
		for (Iterator iter = properties.iterator() ; iter.hasNext();)
		{
			Property property = (Property)iter.next() ;

			String propStr = null ;

			if (wellKnownPropsMap.containsKey(property.getURI()))
				propStr = (String) wellKnownPropsMap.get(property.getURI());
			else
				propStr = formatResource(property) ;
				
			out.print(propStr) ;
			
			// Need to do the same line or next trick as in writeTriples
			//out.print("  ") ;
			
			// Currently at end of property
			if (propStr.length() + minGap < indent.length())
				out.print( pad(indent.length() - propStr.length()));
			else
			{
				// Does not fit this line.
				out.println();
				out.print(indent);
			}
			out.incIndent(indent.length()) ;

			// Object list
			writeObjectList(resource, property, allowDeep) ;
			out.decIndent(indent.length()) ;
			
			if (iter.hasNext())
				out.println( " ;");
		}
	}
	

	// Need to decide between one line or many.
	private void writeObjectList(Resource resource, Property property, boolean allowDeep)
		throws RDFException
	{
		StmtIterator sIter = resource.listProperties(property) ;
		for ( ; sIter.hasNext() ; )
		{
			Statement stmt = sIter.nextStatement() ;
			writeObject(stmt.getObject(), allowDeep) ;
			if (sIter.hasNext())
				out.print( " , ");
		}
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

		if (damlLists.contains(rObj))
			if (countArcsTo(rObj) <= 1)
			{
				writeDamlList(rObj);
				return;
			}

		out.print(formatResource(rObj));
	}



	// Need to out.print in short (all on one line) and long forms (multiple lines)
	// That needs starts point depth tracking.
	private void writeDamlList(Resource resource)
		throws RDFException
	{
		out.print( "(");
		out.incIndent(2) ;
		boolean listFirst = true;
		for (Iterator iter = damlListIterator(resource); iter.hasNext();)
		{
			if (!listFirst)
				out.print( " ");
			listFirst = false;
			RDFNode n = (RDFNode) iter.next();
			writeObject(n, true) ;
		}
		out.print( ")");
		out.decIndent(2) ;
		damlListsDone.add(resource);

	}
	
	private String formatResource(Resource r)
	{
		if ( r.isAnon() )
		{
			if ( ! bNodesMap.containsKey(r) )
				bNodesMap.put(r, "_:b"+(++bNodeCounter)) ;
			return (String)bNodesMap.get(r) ;

		}

		// It has a URI.
		if ( r.equals(damlVocabulary.nil()) )
			return "()" ;
		
		String uriStr = r.getURI() ;
		
		if ( uriStr.equals(baseName) )
			return "<>" ;

		String matchURI = "" ;
		String matchPrefix = null ;


		// Because we always use "@prefix : <#>."
		if ( doingBaseHash && uriStr.startsWith(baseName+"#") )
		{
			String localname = uriStr.substring((baseName+"#").length()) ;
			if ( localname.indexOf('.') == -1 )
				return ":"+localname ;
		}
		else
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
		// Not as a qname - write a quoted URIref
		// URIref
		return "<"+r.getURI()+">" ;
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
            out.print("^^<") ;
            out.print(datatype) ;
            out.print(">") ;
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
		damlLists 		= new HashSet() ;
		damlListsAll 	= new HashSet() ;
		damlListsDone 	= new HashSet() ;
		oneRefObjects 	= new HashSet() ;
		oneRefDone 		= new HashSet() ;
		prefixesUsed 	= new HashSet();
		//prefixMap - retained across runs
		bNodesMap		= new HashMap() ;
	}
	
	// Especially release large intermediate memory objects 
	protected void finishWriting()
	{
		damlLists 		= null ;
		damlListsAll 	= null ;
		damlListsDone 	= null ;
		oneRefObjects 	= null ;
		oneRefDone 		= null ;
		prefixesUsed 	= null ;
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
		StmtIterator sIter = listStatements(resource.getModel(), null, prop, resource) ;
		for ( ; sIter.hasNext() ; )
		{
			sIter.nextStatement() ;
			numArcs++ ;
		}
		sIter.close() ;
		return numArcs ;
	}
	
	
	private StmtIterator listStatements(Model model, Resource subj, Property prop, RDFNode obj)
		throws RDFException
	{
		return model.listStatements(new SimpleSelector(subj, prop, obj)) ;
	}



	private Iterator damlListIterator(Resource r)
		throws RDFException
	{
		List list = new ArrayList() ;
		
		for ( ; ! r.equals(damlVocabulary.nil()); )
		{
			StmtIterator sIter = listStatements(r.getModel(), r, damlVocabulary.first(), null) ;
			list.add(sIter.nextStatement().getObject()) ;
			if ( sIter.hasNext() )
				// @@ need to cope with this (unusual) case
				throw new RuntimeException("Multi valued list item") ;
			sIter = listStatements(r.getModel(), r, damlVocabulary.rest(), null) ;
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

		baseName = base ;
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
			doingBaseHash = true ;
			out.println( "@prefix : <#> .") ;
		}
		
		if ( doingBaseHash || prefixMap.size() != 0 )
			out.println() ;

		// Works by running the same code but with empty control structures.
		ResIterator rIter = model.listSubjects() ;
		for ( ; rIter.hasNext() ; )	
		{
			writeTriples(rIter.nextResource(), false) ;
			if ( rIter.hasNext() )
				out.println() ;
		}
		rIter.close() ;
		out.flush() ;
	}



}

/*
 *  (c) Copyright Hewlett-Packard Company 2002
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
