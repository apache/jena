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

package org.apache.jena.rdf.model.impl;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.TextDirection;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.shared.CannotEncodeCharacterException;
import org.apache.jena.util.SplitIRI;

/** Some utility functions.
 */
public class Util extends Object {

    protected static Pattern standardEntities =
        Pattern.compile( "&|<|>|\t|\n|\r|\'|\"" );

    /**
     * Given an absolute URI, determine the split point between the namespace
     * part and the localname part. See {@link SplitIRI#splitXML} for details.
     */
    public static int splitNamespaceXML(String uri) {
        // Legacy. Call the moved code.
        return SplitIRI.splitXML(uri);
    }

    public static String substituteStandardEntities( String s ) {
        if (standardEntities.matcher( s ).find()) {
            return substituteEntitiesInElementContent( s )
                    .replaceAll( "'", "&apos;" )
                    .replaceAll( "\t","&#9;" )
                    .replaceAll( "\n", "&#xA;" )
                    .replaceAll( "\r", "&#xD;" )
                    .replaceAll( "\"", "&quot;" )
                    ;
        }
        else
            return s;
    }

    protected static Pattern entityValueEntities =
        Pattern.compile( "&|%|\'|\"" );

   public static String substituteEntitiesInEntityValue( String s ) {
       if (entityValueEntities.matcher( s ).find()) {
           return s
                   .replaceAll( "&","&amp;" )
                   .replaceAll( "'", "&apos;" )
                   .replaceAll( "%", "&#37;" )
                   .replaceAll( "\"", "&quot;" )
                   ;
       }
       else
           return s;
   }

    protected static Pattern elementContentEntities = Pattern.compile( "<|>|&|[\0-\37&&[^\n\t]]|\uFFFF|\uFFFE" );
    /**
        Answer <code>s</code> modified to replace &lt;, &gt;, and &amp; by
        their corresponding entity references.

    <p>
        Implementation note: as a (possibly misguided) performance hack,
        the obvious cascade of replaceAll calls is replaced by an explicit
        loop that looks for all three special characters at once.
    */
    public static String substituteEntitiesInElementContent(String s) {
        Matcher m = elementContentEntities.matcher(s);
        if ( !m.find() )
            return s;
        else {
            int start = 0;
            StringBuilder result = new StringBuilder();
            do {
                result.append(s.substring(start, m.start()));
                char ch = s.charAt(m.start());
                switch (ch) {
                    case '\r' :
                        result.append("&#xD;");
                        break;
                    case '<' :
                        result.append("&lt;");
                        break;
                    case '&' :
                        result.append("&amp;");
                        break;
                    case '>' :
                        result.append("&gt;");
                        break;
                    default :
                        throw new CannotEncodeCharacterException(ch, "XML");
                }
                start = m.end();
            } while (m.find(start));
            result.append(s.substring(start));
            return result.toString();
        }
    }

    public static String replace(String s, String oldString, String newString) {
        return s.replace(oldString, newString);
    }

    /**
     * A Node is a simple string if:
     * <ul>
     * <li>(RDF 1.0) No datatype and no language tag.
     * <li>(RDF 1.1) xsd:string
     * </ul>
     */
    public static boolean isSimpleString(Node n) {
        Objects.requireNonNull(n);
        if ( ! n.isLiteral() )
            return false;
        RDFDatatype dt = n.getLiteralDatatype();
        if ( dt == null )
            return !hasLangTest(n);
        return dt.equals(XSDDatatype.XSDstring);
    }

    /**
     * A Node is a well-formed language string if it has a language tag
     * and it does not have a base direction.
     * This excludes {@code "abc"^^rdf:langString} which is not well-formed.
     */
    public static boolean isLangString(Node n) {
        Objects.requireNonNull(n);
        if ( ! n.isLiteral() )
            return false;
        return hasLangTest(n) && !hasDirectionText(n);
    }

    /**
     * A Node is a well-formed directional language string if it has a language tag
     * and it has an base direction.
     */
    public static boolean isDirLangString(Node n) {
        Objects.requireNonNull(n);
        if ( ! n.isLiteral() )
            return false;
        return hasDirectionText(n) && hasLangTest(n);
    }

    /** Test whether this node has a language (rdf:langString or rdf:dirLangString) */
    public static boolean hasLang(Node n) {
        if ( ! n.isLiteral() )
            return false;
        String lang = n.getLiteralLanguage();
        return hasLangTest(n);
    }

    /** Test whether this node has an initial text language (rdf:dirLangString) */
    public static boolean hasDirection(Node n) {
        if ( ! n.isLiteral() )
            return false;
        return hasDirectionText(n);
    }

    private static boolean hasDirectionText(Node n) {
        TextDirection textDir = n.getLiteralBaseDirection();
        return textDir != null;
    }

    private static boolean hasLangTest(Node n) {
        String lang = n.getLiteralLanguage();
        return ! Lib.isEmpty(lang);
    }

    /** Return true if the literal is a simple string.
     *  <p>RDF 1.0 {@literal =>} it is a plain literal, with no language tag
     *  <p>RDF 1.1 {@literal =>} it has datatype xsd:string
     */
    public static boolean isSimpleString(Literal lit) {
        Objects.requireNonNull(lit);
        RDFDatatype dt = lit.getDatatype();
        if (  dt == null )
            return ! isLangString(lit);
        return dt.equals(XSDDatatype.XSDstring);
    }

    /** Return true if the literal has a language tag. */
    public static boolean isLangString(Literal lit) {
        Objects.requireNonNull(lit);
        String lang = lit.getLanguage();
        if ( lang == null )
            return false;
        return ! lang.equals("");
    }

    /** Return true if the literal is well-formed, has a language tag and a base direction. */
    public static boolean isDirLangString(Literal lit) {
        Objects.requireNonNull(lit);
        String lang = lit.getLanguage();
        if ( lang == null )
            return false;
        String textDir = lit.getBaseDirection();
        if ( textDir == null )
            return false;
        return true;
    }
}
