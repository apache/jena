/**
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

package org.apache.jena.query.text.assembler;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.query.text.TextIndexException;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;

/**
 * Parses assembler parameter definitions for <code>GenericAnalyzer</code>, 
 * <code>GenericFilter</code>, and <code>GenericTokenizer</code>.
 * <p>
 * The parameters may be of the following types:
 * <pre>
 *     text:TypeString        String
 *     text:TypeSet           org.apache.lucene.analysis.util.CharArraySet
 *     text:TypeFile          java.io.FileReader
 *     text:TypeInt           int
 *     text:TypeBoolean       boolean
 *     text:TypeAnalyzer      org.apache.lucene.analysis.Analyzer
 *     text:TypeTokenStream   org.apache.lucene.analysis.TokenStream
 * </pre>
 * 
 * Although the list of types is not exhaustive it is a simple matter
 * to create a wrapper Analyzer, Filter, Tokenizer that reads a file with information 
 * that can be used to initialize any sort of parameters that may be needed. 
 * The provided types cover the vast majority of cases.
 * <p>
 * For example, <code>org.apache.lucene.analysis.ja.JapaneseAnalyzer</code>
 * has a constructor with 4 parameters: a <code>UserDict</code>,
 * a <code>CharArraySet</code>, a <code>JapaneseTokenizer.Mode</code>, and a 
 * <code>Set&lt;String></code>. So a simple wrapper can extract the values
 * needed for the various parameters with types not available in this
 * extension, construct the required instances, and instantiate the
 * <code>JapaneseAnalyzer</code>.
 * <p>
 * Adding custom Analyzers, etc., such as the above wrapper analyzer is a simple
 * matter of adding the Analyzer class and any associated filters and tokenizer
 * and so on to the classpath for Jena - usually in a jar. Of course, all of 
 * the Analyzers, Filters, and Tokenizers that are included in the Lucene distribution 
 * bundled with Jena are available as generics as well.
 * <p>
 * Each parameter object is specified with:
 * <ul>
 * <li>an optional <code>text:paramName</code> that may be used to document which 
 * parameter is represented</li>
 * <li>a <code>text:paramType</code> which is one of: <code>text:TypeString</code>, 
 * <code>text:TypeSet</code>, <code>text:TypeFile</code>, <code>text:TypeInt</code>, 
 * <code>text:TypeBoolean</code>, <code>text:TypeAnalyzer</code>.</li>
 * <li>a text:paramValue which is an xsd:string, xsd:boolean or xsd:int or resource.</li>
 * </ul>
 * <p>
 * A parameter of type <code>text:TypeSet</code> <i>must have</i> a list of zero or 
 * more <code>String</code>s.
 * <p>
 * A parameter of type <code>text:TypeString</code>, <code>text:TypeFile</code>, 
 * <code>text:TypeBoolean</code>, <code>text:TypeInt</code> or <code>text:TypeAnalyzer</code> 
 * <i>must have</i> a single <code>text:paramValue</code> of the appropriate type.
 * <p>
 * A parameter of type <code>text:TypeTokenStream</code> does not have <code>text:paramValue</code>.
 * It is used to mark the occurence of the <code>TokenStream</code> parameter for a <code>Filter</code>.
 * <p>
 * Examples:
 * <pre>
    text:map (
         [ text:field "text" ; 
           text:predicate rdfs:label;
           text:analyzer [
               a text:GenericAnalyzer ;
               text:class "org.apache.lucene.analysis.en.EnglishAnalyzer" ;
               text:params (
                    [ text:paramName "stopwords" ;
                      text:paramType text:TypeSet ;
                      text:paramValue ("the" "a" "an") ]
                    [ text:paramName "stemExclusionSet" ;
                      text:paramType text:TypeSet ;
                      text:paramValue ("ing" "ed") ]
                    )
           ] .
 * </pre>
 * <pre>
    [] a text:TextIndexLucene ;
       text:defineFilters (
           text:filter [
               a text:GenericFilter ;
               text:class "fi.finto.FoldingFilter" ;
               text:params (
                    [ text:paramName "source" ;
                      text:paramType text:TypeTokenStream ]
                    [ text:paramName "whitelisted" ;
                      text:paramType text:TypeSet ;
                      text:paramValue ("ç") ]
                    )
           ]
        )
 * </pre>
 */
public class Params {
    /*
    text:map (
         [ text:field "text" ; 
           text:predicate rdfs:label;
           text:analyzer [
               a text:GenericAnalyzer ;
               text:class "org.apache.lucene.analysis.en.EnglishAnalyzer" ;
               text:params (
                    [ text:paramName "stopwords" ;
                      text:paramType text:TypeSet ;
                      text:paramValue ("the" "a" "an") ]
                    [ text:paramName "stemExclusionSet" ;
                      text:paramType text:TypeSet ;
                      text:paramValue ("ing" "ed") ]
                    )
           ] .
     */

    public static final String TYPE_ANALYZER    = "TypeAnalyzer";
    public static final String TYPE_BOOL        = "TypeBoolean";
    public static final String TYPE_FILE        = "TypeFile";
    public static final String TYPE_INT         = "TypeInt";
    public static final String TYPE_SET         = "TypeSet";
    public static final String TYPE_STRING      = "TypeString";
    public static final String TYPE_TOKENSTREAM = "TypeTokenStream";

    protected static List<ParamSpec> getParamSpecs(Resource list) {
        List<ParamSpec> result = new ArrayList<>();
        Resource current = list;

        while (current != null && ! current.equals(RDF.nil)){
            Statement firstStmt = current.getProperty(RDF.first);
            if (firstStmt == null) {
                throw new TextIndexException("parameter list not well formed: " + current);
            }

            RDFNode first = firstStmt.getObject();
            if (! first.isResource()) {
                throw new TextIndexException("parameter specification must be an anon resource : " + first);
            }

            result.add(getParamSpec((Resource) first));

            Statement restStmt = current.getProperty(RDF.rest);
            if (restStmt == null) {
                throw new TextIndexException("parameter list not terminated by rdf:nil");
            }

            RDFNode rest = restStmt.getObject();
            if (! rest.isResource()) {
                throw new TextIndexException("parameter list node is not a resource : " + rest);
            }

            current = (Resource) rest;
        }

        return result;
    }

    protected static ParamSpec getParamSpec(Resource node) {
        Statement nameStmt = node.getProperty(TextVocab.pParamName);
        Statement valueStmt = node.getProperty(TextVocab.pParamValue);
        
        String name = getStringValue(nameStmt);
        String type = getType(node);
        String value = getStringValue(valueStmt);

        switch (type) {

        // String
        case TYPE_STRING: {
            if (value == null) {
                throw new TextIndexException("Value for string param: " + name + " must not be empty!");
            }

            return new ParamSpec(name, value, String.class);
        }

        // java.io.FileReader
        case TYPE_FILE: {

            if (value == null) {
                throw new TextIndexException("Value for file param must exist and must contain a file name.");
            }

            try {
                // The analyzer is responsible for closing the file
                Reader fileReader = new java.io.FileReader(value);
                return new ParamSpec(name, fileReader, Reader.class);

            } catch (java.io.FileNotFoundException ex) {
                throw new TextIndexException("File " + value + " for param " + name + " not found!");
            }
        }

        // org.apache.lucene.analysis.util.CharArraySet
        case TYPE_SET: {
            if (valueStmt == null) {
                throw new TextIndexException("A set param spec must have a text:paramValue:" + node);
            }

            RDFNode valueNode = valueStmt.getObject();
            if (!valueNode.isResource()) {
                throw new TextIndexException("A set param spec text:paramValue must be a list of strings: " + valueNode);
            }

            List<String> values = toStrings((Resource) valueNode);

            return new ParamSpec(name, new CharArraySet(values, false), CharArraySet.class);
        }

        // int
        case TYPE_INT:
            if (value == null) {
                throw new TextIndexException("Value for int param: " + name + " must not be empty!");
            }

            int n = ((Literal) valueStmt.getObject()).getInt();
            return new ParamSpec(name, n, int.class);

            // boolean
        case TYPE_BOOL:
            if (value == null) {
                throw new TextIndexException("Value for boolean param: " + name + " must not be empty!");
            }

            boolean b = ((Literal) valueStmt.getObject()).getBoolean();
            return new ParamSpec(name, b, boolean.class);

            // org.apache.lucene.analysis.Analyzer
        case TYPE_ANALYZER:
            if (valueStmt == null) {
                throw new TextIndexException("Analyzer param spec must have a text:paramValue:" + node);
            }

            RDFNode valueNode = valueStmt.getObject();
            if (!valueNode.isResource()) {
                throw new TextIndexException("Analyzer param spec text:paramValue must be an analyzer spec resource: " + valueNode);
            }

            Analyzer analyzer = (Analyzer) Assembler.general.open((Resource) valueNode);
            return new ParamSpec(name, analyzer, Analyzer.class);

        default:
            // there was no match
            Log.error("org.apache.jena.query.text.assembler.Params", "Unknown parameter type: " + type + " for param: " + name + " with value: " + value);
            break;
        }

        return null;
    }
    
    private static String getType(Resource node) {
        Statement typeStmt = node.getProperty(TextVocab.pParamType);
        Statement valueStmt = node.getProperty(TextVocab.pParamValue);
        String type = null;
        
        if (typeStmt == null) {

            if (valueStmt == null) {
                throw new TextIndexException("Parameter specification must have a text:paramValue: " + node);
            }
            
            RDFNode obj = valueStmt != null ? valueStmt.getObject() : null;
            Literal lit = obj.asLiteral();
            RDFDatatype rdfType = lit.getDatatype();
            Class<?> clazz = rdfType.getJavaClass();

            if (clazz == java.lang.Boolean.class) {
                type = TYPE_BOOL;
            } else if (clazz == java.math.BigInteger.class) {
                type = TYPE_INT;
            } else if (clazz == java.lang.String.class) {
                type = TYPE_STRING;
            }
        } else {
            Resource typeRes = typeStmt.getResource();
            type = typeRes.getLocalName();
        }
        
        return type;
    }

    private static String getStringValue(Statement stmt) {
        if (stmt == null) {
            return null;
        } else {
            RDFNode node = stmt.getObject();
            if (node.isLiteral()) {
                return ((Literal) node).getLexicalForm();
            } else {
                return null;
            }
        }
    }

    protected static List<String> toStrings(Resource list) {
        List<String> result = new ArrayList<>();
        Resource current = list;

        while (current != null && ! current.equals(RDF.nil)){
            Statement firstStmt = current.getProperty(RDF.first);
            if (firstStmt == null) {
                throw new TextIndexException("param spec of type set not well formed");
            }

            RDFNode first = firstStmt.getObject();
            if (! first.isLiteral()) {
                throw new TextIndexException("param spec of type set item is not a literal: " + first);
            }

            result.add(((Literal)first).getLexicalForm());

            Statement restStmt = current.getProperty(RDF.rest);
            if (restStmt == null) {
                throw new TextIndexException("param spec of type set not terminated by rdf:nil");
            }

            RDFNode rest = restStmt.getObject();
            if (! rest.isResource()) {
                throw new TextIndexException("param spec of type set rest is not a resource: " + rest);
            }

            current = (Resource) rest;
        }

        return result;
    }

    /**
     * <code>ParamSpec</code> contains the <code>name</code>, <code>Class</code>, and 
     * <code>value</code> of a parameter for a constructor (or really any method in general)
     */
    protected static final class ParamSpec {

        private final String name;
        private final Object value;
        private final Class<?> clazz;

        public ParamSpec(String key, Object value) {
            this(key, value, value.getClass());
        }

        public ParamSpec(String key, Object value, Class<?> clazz) {
            this.name = key;
            this.value = value;
            this.clazz = clazz;
        }

        public String getKey() {
            return name;
        }

        public Object getValue() {
            return value;
        }

        public Class<?> getValueClass() {
            return clazz;
        }
    }
}
