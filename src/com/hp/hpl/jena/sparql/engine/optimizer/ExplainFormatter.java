/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * The class takes care of pretty formatting of the ARQo explain method
 * 
 * @author Markus Stocker
 * @version $Id$
 */


public class ExplainFormatter 
{
	public static void printTable(PrintStream out, String[] header, List rows, String left, String sep)
	{
		int[] colWidths = colWidths(header, rows) ;
		
		printLine(out, colWidths, left, sep) ;
		printHeader(out, header, colWidths, left, sep) ;
		printLine(out, colWidths, left, sep) ;
		
		for (Iterator iter = rows.iterator(); iter.hasNext(); )
		{
			List row = (List)iter.next() ;
			StringBuffer bf = new StringBuffer(120) ;
			
			bf.append(left) ;
			
			for (int i = 0; i < row.size(); i++)
			{
				String s = (String)row.get(i) ;
				
				bf.append(s) ;
				
				for (int j = 0; j < colWidths[i] - s.length(); j++)
					bf.append(' ') ;
				
				bf.append(sep) ;
			}
			
			out.println(bf) ;
		}
		
		printLine(out, colWidths, left, sep) ;
	}
	
	public static List formatCols(PrefixMapping prefix, Triple triple1, Triple triple2, double cost)
	{
		List cols = new ArrayList() ;
		
		String subject1 = formatNode(prefix, triple1.getSubject()) ; 
		String predicate1 = formatNode(prefix, triple1.getPredicate()) ;
		String object1 = formatNode(prefix, triple1.getObject()) ;
		
		String subject2 = formatNode(prefix, triple2.getSubject()) ;
		String predicate2 = formatNode(prefix, triple2.getPredicate()) ;
		String object2 = formatNode(prefix, triple2.getObject()) ;
		
		cols.add(subject1 + " " + predicate1 + " " + object1) ;
		cols.add(subject2 + " " + predicate2 + " " + object2) ;		
		cols.add(formatCost(cost)) ;
		
		return cols ;
	}
	
	public static List formatCols(PrefixMapping prefix, Triple triple, double cost)
	{
		List cols = new ArrayList() ;
		
		cols.add(formatNode(prefix, triple.getSubject())) ;
		cols.add(formatNode(prefix, triple.getPredicate())) ;
		cols.add(formatNode(prefix, triple.getObject())) ;
		cols.add(formatCost(cost)) ;
	
		return cols ;   
	}
	
	private static void printHeader(PrintStream out, String[] header, int[] colWidths, String left, String sep)
	{
		StringBuffer bf = new StringBuffer(120) ;
		
		// Left mark
		bf.append(left) ;
		
		for (int i = 0; i < header.length; i++ )
		{
			String s = header[i] ;
			bf.append(s) ;
			
			for (int j = 0; j < colWidths[i] - s.length(); j++)
	            bf.append(' ') ;
			
			bf.append(sep) ;
		}
		
		out.println(bf) ;
	}
	
	private static void printLine(PrintStream out, int[] colWidths, String left, String sep)
	{
		StringBuffer bf = new StringBuffer(120) ;
		int tableLength = left.length() ;
		
		for (int i = 0; i < colWidths.length; i++ )
		{
			tableLength += colWidths[i] + sep.length() ;
		}
		
		// -1 correction for the last sep of length 3, ' | '
		for (int i = 0; i < tableLength - 1; i++)
			bf.append('-') ;
		
		out.println(bf) ;
	}
	
	private static int[] colWidths(String[] header, List rows)
	{
		 int[] colWidths = new int[header.length] ;
	
		 for (Iterator iter = rows.iterator(); iter.hasNext(); )
		 {
			 List row = (ArrayList)iter.next() ;
				 
			 for (int i = 0; i < row.size(); i++ )
			 {
				 String s = (String)row.get(i) ;
				 
				 if (s.length() > colWidths[i])
					 colWidths[i] = s.length() ;
			 }
		 }
		 
		 return colWidths ;
	}
	
	private static String formatNode(PrefixMapping prefixMapping, Node node)
	{
		if (node.isVariable())
			return "?" + node.getName() ;
		else if (node.isBlank())
			return node.getBlankNodeLabel() ;
 		else if (node.isURI())
 		{
 			// If the node is a URI without a PREFIX (e.g. ?s rdf:type <http://myOntologyClass>) return the <URI>
 			String prefix = prefixMapping.getNsURIPrefix(node.getNameSpace()) ;
 			
 			if (prefix == null)
 				return "<" + node.getURI() + ">" ;
 			
			return prefix + ":" + node.getLocalName() ;
 		}
		else if (node.isLiteral())
			return '"' + node.getLiteralLexicalForm() + '"';

		
		return null ;
	}
	
	private static String formatCost(double cost)
	{
		if (cost <= 1d)
			return new Double(cost).toString() ;
		
		return "NaN" ;
	}
}


/*
 *  (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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