package com.hp.hpl.jena.xmloutput;

import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.vocabulary.*;

/**
 * 
 * This interface only adds documentation to {@link RDFWriter}.
 * The documentation identifies the properties that can be
 * set on RDF/XML and RDF/XML-ABBREV writers.
 * @author jjc
 *
 */
public interface RDFXMLWriterI extends RDFWriter {
    /** Suppress a compiler warning. */
   Object _NotInteresting = RDFSyntax.parseCollection;
    

/** Sets properties on this writer.

 <TABLE BORDER="1" CELLPADDING="3" CELLSPACING="0">
 <TR BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 <TD COLSPAN=4><FONT SIZE="+2">
 <B>Properties to Control RDF/XML Output</B></FONT></TD>
 </TR>
 <tr BGCOLOR="#EEEEFF" CLASS="TableSubHeadingColor">
 <th>Property Name</th>
 <th>Description</th>
 <th>Value class</th>
 <th>Legal Values</th>
 </tr>
 <tr BGCOLOR="white" CLASS="TableRowColor">
<td>xmlbase</td>
<td>The value for xml:base in the
 file as a string.</td><td>String</td>
<td>a URI string, or null (default)</td></tr>
 <tr BGCOLOR="white" CLASS="TableRowColor">
 <td>longId</td>
 <td> Whether to use long or short id's for anon
 resources. Short id's are easier to read and are the default, but can run
 out of memory on very large models.</td>
<td>String or Boolean</td>
<td>
"true",  "false" (default)
 </td>
</tr>
 <tr BGCOLOR="white" CLASS="TableRowColor">
 <td>allowBadURIs</td>
 <td>URIs in the graph are, by default, checked prior to
serialization.</td>
<td>String or Boolean</td>
<td>
"true",  "false" (default)
 </td>
</tr>
 <tr BGCOLOR="white" CLASS="TableRowColor">
 <td>relativeURIs</td>
<td>
What sort of relative URIs should be used.
A comma separate list of options:
    <dl>
    <dt>same-document
    <dd>same-document references (e.g. "" or "#foo")
    <dt>network
    <dd>network paths e.g. "//example.org/foo" omitting the URI scheme
    <dt>absolute
    <dd>absolute paths e.g. "/foo" omitting the scheme and authority
    <dt>relative
    <dd>relative path not begining in "../"
    <dt>parent
    <dd>relative path begining in "../"
    <dt>grandparent
    <dd>relative path begining in "../../"
    </dl>
The default value is "same-document, absolute, relative, parent".
To switch off relative URIs use the value "".
    Relative    URIs of any of these types are output where possible if
 and only if the option has been specified.
</td>
<td>String</td>
<td></td>
</tr>
  <tr BGCOLOR="white" CLASS="TableRowColor">
 <td>showXmlDeclaration</td>
 <dd>can be true, false or "default" (null)
 <td>If true, an XML Declaration is included in the output, if false
  no XML declaration is included.
  The default behaviour only gives an XML Declaration when
  asked to write to an OutputStreamWriter that uses some
  encoding other than UTF-8 or UTF-16. 
In this case the encoding is shown
  in the XML declaration.
To ensure that the encoding attribute is shown in the XML declaration
either use the <code><b>write(Model,Writer,String)</b></code>
variant with an appropriate OutputStreamWriter or set this option
to false write the declaration to an OutputStream before calling
<code><b>write(Model,OutputStream,String)</b></code>.
</td>
<td>true, "true", false, "false" or "default"</td>
</tr>
  <tr BGCOLOR="white" CLASS="TableRowColor">
 <td>tab</td>
 <td>The number of spaces with which to indent XML child elements.</td>
<td>String or Integer</td>
<td>positive integer "2" is the default</td>
</tr>
  <tr BGCOLOR="white" CLASS="TableRowColor">
 <td>width</td>
 <td>A guide to the num of cols before inserting an arbitrary newline.</td>
<td>String or Integer</td>
<td>positive integer "60" is the default</td>
</tr>
  <tr BGCOLOR="white" CLASS="TableRowColor">
 <td>attributeQuoteChar</td>
<td>How to write XML attributes.</td>
<td>String</td>
<td>"\"" or "'"</td>
</tr>
  <tr BGCOLOR="white" CLASS="TableRowColor">
 <td>blockRules</td>
 <td>
 A list of Resource or a String being a comma separated list
 of fragment
 IDs from
 <a href="http://www.w3.org/TR/rdf-syntax-grammar">
 http://www.w3.org/TR/rdf-syntax-grammar</a> indicating
 grammar rules that will not be used.
 Rules that can be avoided are:
 <ul>
 <li><a href="http://www.w3.org/TR/rdf-syntax-grammar#section-Reification"
 >section-Reification</a>  ({@link RDFSyntax#sectionReification})</li>
 <li><a href="http://www.w3.org/TR/rdf-syntax-grammar#section-List-Expand"
 >section-List-Expand</a> ({@link RDFSyntax#sectionListExpand})</li>
 <li><a href="http://www.w3.org/TR/rdf-syntax-grammar#parseTypeLiteralPropertyElt">parseTypeLiteralPropertyElt</a>
 ({@link RDFSyntax#parseTypeLiteralPropertyElt})</li>
 <li><a href="http://www.w3.org/TR/rdf-syntax-grammar#parseTypeResourcePropertyElt">parseTypeResourcePropertyElt</a>
 ({@link RDFSyntax#parseTypeLiteralPropertyElt})</li>
 <li><a href="http://www.w3.org/TR/rdf-syntax-grammar#parseTypeCollectionPropertyElt">parseTypeCollectionPropertyElt</a>
 ({@link RDFSyntax#parseTypeCollectionPropertyElt})</li>
 <li><a href="http://www.w3.org/TR/rdf-syntax-grammar#idAttr">idAttr</a>
 ({@link RDFSyntax#idAttr})</li>
 <li><a href="http://www.w3.org/TR/rdf-syntax-grammar#propertyAttr">propertyAttr</a>
 ({@link RDFSyntax#propertyAttr})</li>
 </ul>
 In addition "daml:collection" ({@link DAML_OIL#collection})
 can be blocked. Blocking <a href=
 "http://www.w3.org/TR/rdf-syntax-grammar#idAttr">idAttr</a>  also blocks
 <a href="http://www.w3.org/TR/rdf-syntax-grammar#section-Reification"
 >section-Reification</a>.
 For the basic writer (RDF/XML) only
 <a href="http://www.w3.org/TR/rdf-syntax-grammar#parseTypeLiteralPropertyElt">parseTypeLiteralPropertyElt</a>
 has any affect, since none of the other rules are implemented by that writer.
</td><td>Resource[] or String</td><td></td>
 
 <tr BGCOLOR="white" CLASS="TableRowColor">
 <td>prettyTypes</td>
 <td>Only for the RDF/XML-ABBREV writer.
This a list of
 the types of the principal objects in the model.  The writer
  will tend to create RDF/XML with resources of these types at the
  top level.
 <br />
 Example usage showing the default value:
  <pre>
 w.setProperty("prettyTypes",
      new Resource[]{
			DAML_OIL.Ontology,
			OWL.Ontology,
			DAML_OIL.Datatype,
			OWL.Datatype,
			RDFS.Datatype,
			DAML_OIL.Class,
			RDFS.Class,
			OWL.Class,
			DAML_OIL.Property,
			OWL.ObjectProperty,
			RDF.Property,
			DAML_OIL.ObjectProperty,
			OWL.DatatypeProperty,
			DAML_OIL.DatatypeProperty,
			OWL.TransitiveProperty,
			OWL.SymmetricProperty,
			OWL.FunctionalProperty,
			OWL.InverseFunctionalProperty,
			DAML_OIL.TransitiveProperty,
			DAML_OIL.UnambiguousProperty,
			DAML_OIL.UniqueProperty,
          });
  </pre>
  </td><td>Resource[]</td><td></td>
</tr>
</table>
 * @param propName One of "xmlBase", "LongId", "allowBadURIs",
 * "relativeURIs","showXMLDeclaration", "tab", "attributeQuoteChar",
 * "blockRules", "prettyTypes"
 * @param propValue A String, Boolean, Integer, Resource[] as appropriate.
 * @return the old value for this property, or <code>null</code>
 * if no value was set.
 */
 Object setProperty(
    String propName,
    Object propValue);

}
