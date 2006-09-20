/*
 *  (c)     Copyright 2000, 2001, 2002, 2002, 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
 *   All rights reserved.
  [See end of file]
  $Id: Basic.java,v 1.17 2006-09-20 13:56:23 chris-dollin Exp $
*/

package com.hp.hpl.jena.xmloutput.impl;

import java.io.PrintWriter;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.vocabulary.RDFSyntax;

/** Writes out an XML serialization of a model.
 *
 * @author  bwm
 * @version   Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.17 $' Date='$Date: 2006-09-20 13:56:23 $'
 */
public class Basic extends BaseXMLWriter 
    {
	public Basic() 
        {}
    
    private String space;
	
    protected void writeBody
        ( Model model, PrintWriter pw, String base, boolean inclXMLBase ) 
        {
        setSpaceFromTabCount();
		writeRDFHeader( model, pw );
		writeRDFStatements( model, pw );
		writeRDFTrailer( pw, base );
		pw.flush();
        }

    private void setSpaceFromTabCount()
        {
        space = "";
        for (int i=0; i < tabSize; i += 1) space += " ";
        }
    
    protected void writeSpace( PrintWriter writer )
        { writer.print( space ); }

	private void writeRDFHeader(Model model, PrintWriter writer) 
        {
		String xmlns = xmlnsDecl();
		writer.print( "<" + rdfEl( "RDF" ) + xmlns );
		if (null != xmlBase && xmlBase.length() > 0)
            writer.print( "\n  xml:base=" + substitutedAttribute( xmlBase ) );
		writer.println( " > " );
        }

    protected void writeRDFStatements( Model model, PrintWriter writer )
        {
		ResIterator rIter = model.listSubjects();
		while (rIter.hasNext()) writeRDFStatements( model, rIter.nextResource(), writer );
		}

	protected void writeRDFTrailer( PrintWriter writer, String base ) 
        { writer.println( "</" + rdfEl( "RDF" ) + ">" ); }

	protected void writeRDFStatements
        ( Model model, Resource subject, PrintWriter writer )
	    {
		StmtIterator sIter = model.listStatements( subject, null, (RDFNode) null );
		writeDescriptionHeader( subject, writer );
		while (sIter.hasNext()) writePredicate( sIter.nextStatement(), writer );
		writeDescriptionTrailer( subject, writer );
	    }

	protected void writeDescriptionHeader( Resource subject, PrintWriter writer)
        {
        writer.print( space + "<" + rdfEl( "Description" ) + " " );
		writeResourceId( subject, writer );
		writer.println( ">" );
        }

	protected void writePredicate(Statement stmt, final PrintWriter writer)
		 {
		final Property predicate = stmt.getPredicate();
		final RDFNode object = stmt.getObject();

		writer.print(space+space+
			"<"
				+ startElementTag(
					predicate.getNameSpace(),
					predicate.getLocalName()));
                           
		if (object instanceof Resource) {
			writer.print(" ");
			writeResourceReference(((Resource) object), writer);
			writer.println("/>");
		} else {
			writeLiteral((Literal) object, writer);
			writer.println(
				"</"
					+ endElementTag(
						predicate.getNameSpace(),
						predicate.getLocalName())
					+ ">");
		}
	}
    
    protected void unblockAll() 
        { blockLiterals = false; }
    
    private boolean blockLiterals = false;
    
    protected void blockRule( Resource r ) {
        if (r.equals( RDFSyntax.parseTypeLiteralPropertyElt )) {
     //       System.err.println("Blocking");
            blockLiterals = true;
        } else
           logger.warn("Cannot block rule <"+r.getURI()+">");
    }

	protected void writeDescriptionTrailer( Resource subject, PrintWriter writer ) 
        { writer.println( space + "</" + rdfEl( "Description" ) + ">" ); }
    
    /**
        @deprecated - use writeDescriptionTrailer( Resource subject, PrintWriter writer )
        @param writer
    */
    protected void writeDescriptionTrailer( PrintWriter writer )
        { writeDescriptionTrailer( null, writer ); }
    
    protected void writeResourceId( Resource r, PrintWriter writer )
        {
		if (r.isAnon()) {
			writer.print(rdfAt("nodeID") + "=" + attributeQuoted(anonId(r)));
		} else {
			writer.print(
				rdfAt("about")
					+ "="
					+ substitutedAttribute(relativize(r.getURI())));
		}
	}

	protected void writeResourceReference( Resource r, PrintWriter writer )
		 {
		if (r.isAnon()) {
			writer.print(rdfAt("nodeID") + "=" + attributeQuoted(anonId(r)));
		} else {
			writer.print(
				rdfAt("resource")
					+ "="
					+ substitutedAttribute(relativize(r.getURI())));
		}
	}

	protected void writeLiteral( Literal l, PrintWriter writer ) {
		String lang = l.getLanguage();
        String form = l.getLexicalForm();
		if (!lang.equals("")) {
			writer.print(" xml:lang=" + attributeQuoted( lang ));
		}
		if (l.isWellFormedXML() && !blockLiterals) {
			writer.print(" " + rdfAt("parseType") + "=" + attributeQuoted( "Literal" )+">");
			writer.print( form );
		} else {
			String dt = l.getDatatypeURI();
			if (dt != null) writer.print( " " + rdfAt( "datatype" ) + "=" + substitutedAttribute( dt ) );
            writer.print(">");
            writer.print( Util.substituteEntitiesInElementContent( form ) );
		}
	}

}

/*
	(c) Copyright 2000, 2001, 2002, 2002, 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
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