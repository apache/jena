/*
 *  (c)     Copyright Hewlett-Packard Company 2000, 2001, 2002
 *   All rights reserved.
  [See end of file]
  $Id: Basic.java,v 1.4 2003-02-11 15:17:11 chris-dollin Exp $
*/

package com.hp.hpl.jena.xmloutput;

import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

import java.io.PrintWriter;

/** Writes out an XML serialization of a model.
 *
 * @author  bwm
 * @version   Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.4 $' Date='$Date: 2003-02-11 15:17:11 $'
 */
public class Basic extends BaseXMLWriter {

	static String RDFNS = RDF.getURI();

	public Basic() {
	}

	void writeBody(
		Model model,
		PrintWriter pw,
		String base,
		boolean inclXMLBase) {
		// 	System.err.println(base +" - "+inclXMLBase + " + " + (model!=null));
		//	setupMaps();
		//pw = new PrintWriter(System.out);
		writeRDFHeader(model, pw);
		writeRDFStatements(model, pw);
		writeRDFTrailer(pw, base);
		pw.flush();
		/*
		} catch (Exception e) {
		
		System.err.println(base + " - " + inclXMLBase);
		errorHandler.error(e);
		}
		*/
	}

	private void writeRDFHeader(Model model, PrintWriter writer) {
		String xmlns = xmlnsDecl();
		NsIterator nsIter = model.listNameSpaces();
		String ns;

		writer.print("<" + rdfEl("RDF") + xmlns);

		if (null != xmlBase && xmlBase.length() > 0) {

			writer.print("\n  xml:base=\"" + xmlBase + "\"");
		}
		writer.println(" >");
	}

	protected void writeRDFStatements(Model model, PrintWriter writer)
		throws RDFException {
		ResIterator rIter = model.listSubjects();
		while (rIter.hasNext()) {
			writeRDFStatements(model, rIter.nextResource(), writer);
		}
	}

	protected void writeRDFTrailer(PrintWriter writer, String base) {
		writer.println("</" + rdfEl("RDF") + ">");
	}

	protected void writeRDFStatements(
		Model model,
		Resource subject,
		PrintWriter writer)
		throws RDFException {
		StmtIterator sIter =
			model.listStatements(
				new SimpleSelector(subject, null, (RDFNode) null));

		writeDescriptionHeader(subject, writer);
		if ((subject instanceof Statement)
			&& !model.contains((Statement) subject)) {
			// an unasserted reified statement
			writeReifiedProperties((Statement) subject, writer);
		}
		while (sIter.hasNext()) {
			writePredicate(sIter.nextStatement(), writer);
		}
		writeDescriptionTrailer(writer);

		// if the subject of subject is a reified statement not in the model
		// need to write it out too
		if (subject instanceof Statement) {
			Resource innerSubject = ((Statement) subject).getSubject();
			if (innerSubject instanceof Statement
				&& !model.contains((Statement) innerSubject)) {
				writeRDFStatements(model, innerSubject, writer);
			}
			RDFNode innerObject = ((Statement) subject).getObject();
			if (innerObject instanceof Statement
				&& !model.contains((Statement) innerObject)) {
				writeRDFStatements(model, (Resource) innerObject, writer);
			}
		}

	}

	protected void writeDescriptionHeader(Resource subject, PrintWriter writer)
		throws RDFException {
		writer.print("  <" + rdfEl("Description") + " ");
		writeResourceId(subject, writer);
		writer.println(">");
	}

	protected void writePredicate(Statement stmt, PrintWriter writer)
		throws RDFException {

		Property predicate = stmt.getPredicate();
		RDFNode object = stmt.getObject();

		writer.print(
			"    <"
				+ startElementTag(
					predicate.getNameSpace(),
					predicate.getLocalName()));

		//        if (stmt.isReified()) {
		//            writer.print(" " + nsPrefix(RDFNS) + ":ID='" + anonId(stmt) + "'");
		//        }

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

	protected void writeDescriptionTrailer(PrintWriter writer) {
		writer.println("  </" + rdfEl("Description") + ">");
	}

	protected void writeReifiedProperties(Statement stmt, PrintWriter writer)
		throws RDFException {
		writer.println(
			"    <"
				+ rdfEl("type")
				+ rdfAt("resource")
				+ "='"
				+ RDFNS
				+ "Statement'/>");
		writer.print("    <" + rdfEl("subject") + " ");
		writeResourceReference(stmt.getSubject(), writer);
		writer.println("/>");
		writer.println(
			"    <"
				+ rdfEl("predicate")
				+ rdfAt("resource")
				+ "='"
				+ Util.substituteStandardEntities(stmt.getPredicate().getURI())
				+ "'/>");
		writer.print("    <" + rdfEl("object") + " ");

		RDFNode object = stmt.getObject();
		if (object instanceof Resource) {
			writeResourceReference((Resource) stmt.getObject(), writer);
			writer.println("/>");
		} else {
			writeLiteral((Literal) object, writer);
			writer.println("</" + rdfEl("object") + ">");
		}
	}

	protected void writeResourceId(Resource r, PrintWriter writer)
		throws RDFException {
		if (r.isAnon()) {
			writer.print(rdfAt("nodeID") + "='" + anonId(r));
		} else {
			writer.print(
				rdfAt("about")
					+ "='"
					+ Util.substituteStandardEntities(r.getURI()));
		}
		writer.print("'");
	}

	protected void writeResourceReference(Resource r, PrintWriter writer)
		throws RDFException {
		if (r.isAnon()) {
			writer.print(rdfAt("nodeID") + "='" + anonId(r));
		} else {
			writer.print(
				rdfAt("resource")
					+ "='"
					+ Util.substituteStandardEntities(r.getURI()));
		}
		writer.print("'");
	}

	protected void writeLiteral(Literal l, PrintWriter writer) {
		String lang = l.getLanguage();
		if (!lang.equals("")) {
			writer.print(" xml:lang=\'" + lang + "'");
		}
		if (l.getWellFormed()) {
			writer.print(" " + rdfAt("parseType") + "='Literal'>");
			writer.print(l.toString());
		} else {
			String dt = l.getDatatypeURI();
			if (dt != null) {
                writer.print(" " + rdfAt("datatype") + "='" +
                Util.substituteStandardEntities(dt)+"'>");
                writer.print(l.getLexicalForm());
			} else {
				writer.print(">");
				writer.print(Util.substituteStandardEntities(l.toString()));
			}
		}
	}

}

/*
	(c) Copyright Hewlett-Packard Company 2002
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