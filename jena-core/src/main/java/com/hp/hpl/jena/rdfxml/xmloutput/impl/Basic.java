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
 */
public class Basic extends BaseXMLWriter 
    {
	public Basic() 
        {}
    
    private String space;
	
    @Override protected void writeBody
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
    
    @Override protected void unblockAll() 
        { blockLiterals = false; }
    
    private boolean blockLiterals = false;
    
    @Override protected void blockRule( Resource r ) {
        if (r.equals( RDFSyntax.parseTypeLiteralPropertyElt )) {
     //       System.err.println("Blocking");
            blockLiterals = true;
        } else
           logger.warn("Cannot block rule <"+r.getURI()+">");
    }

	protected void writeDescriptionTrailer( Resource subject, PrintWriter writer ) 
        { writer.println( space + "</" + rdfEl( "Description" ) + ">" ); }
    
    
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
