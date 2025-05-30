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

package org.apache.jena.rdfxml.xmloutput.impl;

import java.io.PrintWriter;

import org.apache.jena.datatypes.xsd.impl.XMLLiteralType;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.rdf.model.impl.Util ;
import org.apache.jena.shared.JenaException;
import org.apache.jena.vocabulary.RDFSyntax ;

/** Writes out an XML serialization of a model.
 */
public class RDFXML_Basic extends BaseXMLWriter
    {
	public RDFXML_Basic()
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
					SplitRDFXML.namespace(predicate),
					SplitRDFXML.localname(predicate)));

		if (object instanceof Resource) {
			writer.print(" ");
			writeResourceReference(((Resource) object), writer);
			writer.println("/>");
		} else {
			writeLiteral((Literal) object, writer);
			writer.println(
				"</"
					+ endElementTag(SplitRDFXML.namespace(predicate), SplitRDFXML.localname(predicate))
					+ ">");
		}
	}

    @Override protected void unblockAll()
        { blockLiterals = false; }

    private boolean blockLiterals = false;

    @Override protected void blockRule( Resource r ) {
        if (r.equals( RDFSyntax.parseTypeLiteralPropertyElt )) {
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
		    if ( r.isStatementTerm() )
		        throw new JenaException("Triple terms not supported in RDF/XML");

			writer.print(
				rdfAt("resource")
					+ "="
					+ substitutedAttribute(relativize(r.getURI())));
		}
	}

    protected void writeLiteral( Literal literal, PrintWriter writer ) {
		String lang = literal.getLanguage();
        String form = literal.getLexicalForm();
        boolean isXML = XMLLiteralType.isXMLLiteral(literal.getDatatype());
		if (Util.isLangString(literal)) {
			writer.print(" xml:lang=" + attributeQuoted( lang ));
		} else if ( isXML && !blockLiterals) {
		    // RDF XML Literals inline.
			writer.print(" " + rdfAt("parseType") + "=" + attributeQuoted( "Literal" )+">");
			writer.print( form );
			return ;
		} else {
	        // Datatype (if not xsd:string and RDF 1.1)
	        String dt = literal.getDatatypeURI();
	        if ( ! Util.isSimpleString(literal) )
	            writer.print( " " + rdfAt( "datatype" ) + "=" + substitutedAttribute( dt ) );
		}
		// Content.
		writer.print(">");
		writer.print( Util.substituteEntitiesInElementContent( form ) );
	}

}
