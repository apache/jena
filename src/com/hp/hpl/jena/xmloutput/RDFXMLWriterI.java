package com.hp.hpl.jena.xmloutput;

import com.hp.hpl.jena.rdf.model.RDFWriter;

/**
 * 
 * This interface only adds documentation to {@link RDFWriter}.
 * The documentation identifies the properties that can be
 * set on RDF/XML and RDF/XML-ABBREV writers.
 * @author jjc
 *
 */
public interface RDFXMLWriterI extends RDFWriter {

/** Sets properties on this writer.
 *  The possible properties are:
 * <dl>
 * <dt>xmlbase
 * <dd>Allows the specification of the value for xml:base in the
 * file, as a string.
 * <dt>longId
 * <dd> (true or false) Whether to use long or short id's for anon
 * resources. Short id's are easier to read and are the default, but can run
 * out of memory on very large models.
 * <dt>allowBadURIs
 * <dd> (true or false) (default false) Whether to use long or short id's
 * for anon resources. Short id's are easier to read and are the default,
 * but can run out of memory on very large models.
 * <dt>relativeURIs
 * <dd>A comma separate list of options:
 *    <dl>
 *    <dt>same-document
 *    <dd>same-document references (e.g. "" or "#foo")
 *    <dt>network
 *    <dd>network paths e.g. "//example.org/foo" omitting the URI scheme
 *    <dt>absolute
 *    <dd>absolute paths e.g. "/foo" omitting the scheme and authority
 *    <dt>relative
 *    <dd>relative path not begining in "../"
 *    <dt>parent
 *    <dd>relative path begining in "../"
 *    <dt>grandparent
 *    <dd>relative path begining in "../../"
 *    </dl>
 *    The default value is "same-document, absolute, relative, parent".
 *    Relative    URIs of any of these types are output where possible if
 * and only if the option has been specified.
 * <dt>showXmlDeclaration
 * <dd>can be true, false or "default" (null)
 *  If true, an XML Declaration is included in the output, if false
 *  no XML declaration is included.
 *  The default behaviour only gives an XML Declaration when
 *  asked to write to an OutputStreamWriter that uses some
 *  encoding other than UTF-8. In this case the encoding is shown
 *  in the XML declaration.
 * <dt>tab</dt>
 * <dd>The number of spaces with which to indent XML child elements.</dd>
 * <dt>attributeQuoteChar</dt>
 * <dd>A one character string: "\"" or "'"</dd>
 * <dt>blockRules</dt>
 * <dd>
 * A list of Resource or a String being a comma separated list
 * of fragment
 * IDs from
 * <a href="http://www.w3.org/TR/rdf-syntax-grammar">
 * http://www.w3.org/TR/rdf-syntax-grammar</a> indicating
 * grammar rules that will not be used.
 * Rules that can be avoided are:
 * <ul>
 * <li>
 * <li><a href="http://www.w3.org/TR/rdf-syntax-grammar#section-Reification"
 * >section-Reification</a></li>
 * <li><a href="http://www.w3.org/TR/rdf-syntax-grammar#section-List-Expand"
 * >section-List-Expand</a></li>
<li><a href="http://www.w3.org/TR/rdf-syntax-grammar#parseTypeLiteralPropertyElt">parseTypeLiteralPropertyElt</a></li>
<li><a href="http://www.w3.org/TR/rdf-syntax-grammar#parseTypeResourcePropertyElt">parseTypeResourcePropertyElt</a></li>
<li><a href="http://www.w3.org/TR/rdf-syntax-grammar#parseTypeCollectionPropertyElt">parseTypeCollectionPropertyElt</a></li>
<li><a href="http://www.w3.org/TR/rdf-syntax-grammar#idAttr">idAttr</a></li>
<li><a href="http://www.w3.org/TR/rdf-syntax-grammar#propertyAttr">propertyAttr</a></li>

 * </ul>
 * In addition "daml:collection"
 * (or http://www.daml.org/2001/03/daml+oil#collection)
 * can be blocked. Blocking <a href=
 * "http://www.w3.org/TR/rdf-syntax-grammar#idAttr">idAttr</a>  also blocks
 * <a href="http://www.w3.org/TR/rdf-syntax-grammar#section-Reification"
 * >section-Reification</a>.
 * For the basic writer (RDF/XML) only
<a href="http://www.w3.org/TR/rdf-syntax-grammar#parseTypeLiteralPropertyElt">parseTypeLiteralPropertyElt</a>
has any affect, since none of the other rules are implemented by that writer.

 *
 *  * <dt>prettyTypes</dt>
 * <dd>
 * the types of the principal objects in the model.  Abbreviated
 *  will tend to create RDF/XML with resources of these types at the
 *  top level.
 * <br>
 * Example usage showing the default value:
 <pre>
* prettyWriter.setProperty("prettyTypes",new Resource[]{
*               DAML.Ontology,
*               DAML.Class,
*               DAML.Datatype,
*               DAML.Property,
*               DAML.ObjectProperty,
*               DAML.DatatypeProperty,
*               DAML.TransitiveProperty,
*               DAML.UnambigousProperty,
*               DAML.UniqueProperty,
*               });
 </pre>
 </dd>
 * @param propName Must be one of  "xmlbase", "showXmlDeclaration", "prettyTypes"
 * @param propValue Appropriate value for the property. i.e. For
 *                   <dd>
 *                   <dt>xmlbase</dt>
 *                   <dd>A string, representing a URI.</dd>
 *                   <dt>showXmlDeclaration</dt>
 *                   <dd>A Boolean, null, or the strings "true", "false", or "default"
 *                   <dt>longId
 *                   <dd>true or false
 *                   <dt>prettyTypes
 *                   <dd>
 *  An array of Resource's being types of objects to show
 *                  at the top level.
 *                   </dl>
 * @return the old value for this property, or <code>null</code>
 * if no value was set.
 */
 Object setProperty(
    String propName,
    Object propValue);

}
