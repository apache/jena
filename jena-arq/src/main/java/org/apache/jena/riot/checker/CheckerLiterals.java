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

package org.apache.jena.riot.checker ;

import java.util.Objects ;
import java.util.regex.Pattern ;

import org.apache.jena.JenaRuntime ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.sparql.graph.NodeConst ;
import org.apache.jena.util.SplitIRI;

public class CheckerLiterals implements NodeChecker {
    // A flag to enable the test suite to read bad data.
    public static boolean WarnOnBadLiterals = true ;

    private ErrorHandler handler ;

    public CheckerLiterals(ErrorHandler handler) {
        this.handler = handler ;
    }

    @Override
    public boolean check(Node node, long line, long col) {
        return node.isLiteral() && checkLiteral(node, handler, line, col) ;
    }

    final static private Pattern langPattern = Pattern.compile("[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*") ;

    public static boolean checkLiteral(Node node, ErrorHandler handler, long line, long col) {
        if ( !node.isLiteral() ) {
            handler.error("Not a literal: " + node, line, col) ;
            return false ;
        }

        return checkLiteral(node.getLiteralLexicalForm(), node.getLiteralLanguage(), node.getLiteralDatatype(), handler, line, col) ;
    }

    public static boolean checkLiteral(String lexicalForm, RDFDatatype datatype, ErrorHandler handler, long line, long col) {
        return checkLiteral(lexicalForm, null, datatype, handler, line, col) ;
    }

    public static boolean checkLiteral(String lexicalForm, String lang, ErrorHandler handler, long line, long col) {
        return checkLiteral(lexicalForm, lang, null, handler, line, col) ;
    }

    public static boolean checkLiteral(String lexicalForm, String lang, RDFDatatype datatype, ErrorHandler handler,
                                       long line, long col) {
        if ( !WarnOnBadLiterals )
            return true ;

        boolean hasLang = lang != null && !lang.equals("") ;
        if ( !hasLang ) {
            // Datatype check (and RDF 1.0 simple literals are always well formed)
            if ( datatype != null )
                return validateByDatatype(lexicalForm, datatype, handler, line, col) ;
            return true ;
        }

        // Has a language.
        if ( JenaRuntime.isRDF11 ) {
            if ( datatype != null && !Objects.equals(datatype.getURI(), NodeConst.rdfLangString.getURI()) ) {
                handler.error("Literal has language but wrong datatype", line, col) ;
                return false ;
            }
        } else {
            if ( datatype != null ) {
                handler.error("Literal has datatype and language", line, col) ;
                return false ;
            }
        }

        // Test language tag format -- not a perfect test.
        if ( !lang.isEmpty() && !langPattern.matcher(lang).matches() ) {
            handler.warning("Language not valid: " + lang, line, col) ;
            return false ;
        }
        return true ;
    }

    // Whitespace.
    // XSD allows whitespace before and after the lexical forms of a literal but not insiode.
    // Jena handles this correctly.

    protected static boolean validateByDatatype(String lexicalForm, RDFDatatype datatype, ErrorHandler handler, long line, long col) {
//        if ( SysRIOT.StrictXSDLexicialForms )
//            checkWhitespace(lexicalForm, datatype, handler, line, col);
        return validateByDatatypeJena(lexicalForm, datatype, handler, line, col) ;
    }

    protected static boolean validateByDatatypeJena(String lexicalForm, RDFDatatype datatype, ErrorHandler handler, long line, long col) {
        if ( datatype.isValid(lexicalForm) )
            return true ;
        handler.warning("Lexical form '" + lexicalForm + "' not valid for datatype " + xsdDatatypeName(datatype), line, col) ;
        return false ;
    }

    protected static boolean checkWhitespace(String lexicalForm, RDFDatatype datatype, ErrorHandler handler, long line, long col) {
        if ( lexicalForm.contains(" ") ) {
            handler.warning("Whitespace in "+xsdDatatypeName(datatype)+" literal: '" + lexicalForm + "'", line, col) ;
            return false ;
        }
        if ( lexicalForm.contains("\n") ) {
            handler.warning("Newline in "+xsdDatatypeName(datatype)+" literal: '" + lexicalForm + "'", line, col) ;
            return false ;
        }
        if ( lexicalForm.contains("\r") ) {
            handler.warning("Newline in "+xsdDatatypeName(datatype)+" literal: '" + lexicalForm + "'", line, col) ;
            return false ;
        }
        return true ;
    }

    private static String xsdDatatypeName(RDFDatatype datatype) {
        return "XSD "+SplitIRI.localname(datatype.getURI());
    }
}
