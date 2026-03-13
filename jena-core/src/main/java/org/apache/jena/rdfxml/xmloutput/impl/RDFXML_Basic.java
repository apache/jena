/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.rdfxml.xmloutput.impl;

import java.io.PrintWriter;

import org.apache.jena.datatypes.xsd.impl.XMLLiteralType;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.rdf.model.impl.Util ;
import org.apache.jena.shared.JenaException;
import org.apache.jena.vocabulary.ITS;
import org.apache.jena.vocabulary.RDFSyntax ;

/** Writes out an XML serialization of a model.
 */
public class RDFXML_Basic extends BaseXMLWriter {

    /**
     * Do not create directly.
     * @deprecated The RDFWriter may be replaced.
     */
    @Deprecated
    public RDFXML_Basic() {}

    private String space;

    // Move to BaseXMLWriter?
    private String itsPrefix = null;
    // Is there an ITS namespace declaration in the model prefixes?
    private boolean itsModelNamespace = false;

    @Override
    protected void writeBody(Model model, PrintWriter pw, String base, boolean inclXMLBase) {
        setSpaceFromTabCount();
        setupITS(model);
        writeRDFHeader(model, pw);
        writeRDFStatements(model, pw);
        writeRDFTrailer(pw, base);
        pw.flush();
    }

    private void setupITS(Model model) {
        itsPrefix = model.getNsURIPrefix(ITS.uri);
        // If its is in the namespaces, we use that else print on each literal with a base direction
        // This enables streaming.
        itsModelNamespace = (itsPrefix != null);
    }

    private void setSpaceFromTabCount() {
        space = "";
        for ( int i = 0 ; i < tabSize ; i += 1 )
            space += " ";
    }

    protected void writeSpace(PrintWriter writer) {
        writer.print(space);
    }

    private void writeRDFHeader(Model model, PrintWriter writer) {
        String xmlns = xmlnsDecl();
        writer.print("<" + rdfEl("RDF") + xmlns);
        if ( null != xmlBase && xmlBase.length() > 0 )
            writer.print("\n    xml:base=" + substitutedAttribute(xmlBase));
        if ( itsModelNamespace )
            writer.printf("\n    %s:version=%s", itsPrefix, attributeQuoted("2.0"));
        writer.println(" > ");
    }

    protected void writeRDFStatements(Model model, PrintWriter writer) {
        ResIterator rIter = model.listSubjects();
        while (rIter.hasNext())
            writeRDFStatements(model, rIter.nextResource(), writer);
    }

    protected void writeRDFTrailer(PrintWriter writer, String base) {
        writer.println("</" + rdfEl("RDF") + ">");
    }

    protected void writeRDFStatements(Model model, Resource subject, PrintWriter writer) {
        StmtIterator sIter = model.listStatements(subject, null, (RDFNode)null);
        writeDescriptionHeader(subject, writer);
        while (sIter.hasNext())
            writePredicate(sIter.nextStatement(), writer);
        writeDescriptionTrailer(subject, writer);
    }

    protected void writeDescriptionHeader(Resource subject, PrintWriter writer) {
        writer.print(space + "<" + rdfEl("Description") + " ");
        writeResourceId(subject, writer);
        writer.println(">");
    }

    protected void writePredicate(Statement stmt, final PrintWriter writer) {
        final Property predicate = stmt.getPredicate();
        final RDFNode object = stmt.getObject();

        writer.print(space+space+
                     "<"
                     + startElementTag(
                                       SplitRDFXML.namespace(predicate),
                                       SplitRDFXML.localname(predicate)));

        switch(object) {
            case Resource resource ->{
                writer.print(" ");
                writeResourceReference(resource, writer);
                writer.println("/>");
            }
            case Literal literal ->{
                writeLiteral(literal, writer);
                writer.println("</"+ endElementTag(SplitRDFXML.namespace(predicate), SplitRDFXML.localname(predicate)) + ">");
            }
            case StatementTerm triple ->{
                writeTripleTerm(triple, writer);
                writer.print(space);
                writer.print(space);
                writer.println("</"+ endElementTag(SplitRDFXML.namespace(predicate), SplitRDFXML.localname(predicate)) + ">");
            }
            default->{
                throw new JenaException("Bad object: "+object);
            }
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


    protected void writeResourceId( Resource r, PrintWriter writer ) {
        if (r.isAnon()) {
            writer.print(rdfAt("nodeID") + "=" + attributeQuoted(anonId(r)));
        } else {
            writer.print(
                         rdfAt("about")
                         + "="
                         + substitutedAttribute(relativize(r.getURI())));
        }
    }

    protected void writeResourceReference( Resource r, PrintWriter writer ) {
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
            writer.printf(" xml:lang=%s", attributeQuoted( lang ));
        } else if ( Util.isDirLangString(literal)) {
            if ( itsPrefix == null )
                itsPrefix = syntheticNamespaceForITS(literal.getModel());;
                if ( ! itsModelNamespace ) {
                    writer.printf(" xmlns:%s=%s",itsPrefix, attributeQuoted(ITS.uri));
                    writer.printf(" %s:version=%s", itsPrefix, attributeQuoted("2.0"));
                }
                writer.printf(" xml:lang=%s", attributeQuoted( lang ));
                writer.printf(" %s:dir=%s", itsPrefix, attributeQuoted(literal.getBaseDirection()));
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

    // Determine a prefix for the ITS
    private static String syntheticNamespaceForITS(Model model) {
        int count = 0;
        String nsPrefix = "its";
        // Find an unused prefix.
        for(;;) {
            if ( model.getNsPrefixURI(nsPrefix) == null)
                break;
            nsPrefix = "its." + (count++);
            if ( count > 10_000 )
                // Safety
                throw new JenaException("Can't determine an XML namepsace prefix for ITS");
        }
        return nsPrefix;
    }


    protected void writeTripleTerm(StatementTerm triple, PrintWriter writer) {
        writer.print(" " + rdfAt("parseType") + "=" + attributeQuoted( "Triple" )+">");
        writer.println();
        Statement stmt = triple.getStatement();
        Resource subject = stmt.getSubject();
        writer.print(space);
        writer.print(space);
        writeDescriptionHeader( subject, writer );

        writer.print(space);
        writer.print(space);
        writePredicate( stmt, writer );

        writer.print(space);
        writer.print(space);
        writeDescriptionTrailer( subject, writer );
    }
}
