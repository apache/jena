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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.text.TextIndexException;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;

/**
 * Creates generic analyzers given a fully qualified Class name and a list
 * of parameters for a constructor of the Class.
 * <p>
 * The parameters may be of the following types:
 * <pre>
 *     string    String
 *     set       org.apache.lucene.analysis.util.CharArraySet
 *     file      java.io.FileReader
 *     int       int
 *     boolean   boolean
 *     analyzer  org.apache.lucene.analysis.Analyzer
 * </pre>
 * 
 * Although the list of types is not exhaustive it is a simple matter
 * to create a wrapper Analyzer that reads a file with information that can
 * be used to initialize any sort of parameters that may be needed for
 * a given Analyzer. The provided types cover the vast majority of cases.
 * <p>
 * For example, <code>org.apache.lucene.analysis.ja.JapaneseAnalyzer</code>
 * has a constructor with 4 parameters: a <code>UserDict</code>,
 * a <code>CharArraySet</code>, a <code>JapaneseTokenizer.Mode</code>, and a 
 * <code>Set&lt;String></code>. So a simple wrapper can extract the values
 * needed for the various parameters with types not available in this
 * extension, construct the required instances, and instantiate the
 * <code>JapaneseAnalyzer</code>.
 * <p>
 * Adding custom Analyzers such as the above wrapper analyzer is a simple
 * matter of adding the Analyzer class and any associated filters and tokenizer
 * and so on to the classpath for Jena - usually in a jar. Of course, all of 
 * the Analyzers that are included in the Lucene distribution bundled with Jena
 * are available as generic Analyzers as well.
 * <p>
 * Each parameter object is specified with:
 * <ul>
 * <li>an optional <code>text:paramName</code> that may be used to document which 
 * parameter is represented</li>
 * <li>a <code>text:paramType</code> which is one of: <code>string</code>, 
 * <code>set</code>, <code>file</code>, <code>int</code>, <code>boolean</code>.</li>
 * <li>a text:paramValue which is an xsd:string, xsd:boolean or xsd:int.</li>
 * </ul>
 * <p>
 * A parameter of type <code>set</code> <i>must have</i> a list of zero or more <code>String</code>s.
 * <p>
 * A parameter of type <code>string</code>, <code>file</code>, <code>boolean</code>, or 
 * <code>int</code> <i>must have</i> a single <code>text:paramValue</code> of the appropriate type.
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
                      text:paramType "set" ;
                      text:paramValue ("the" "a" "an") ]
                    [ text:paramName "stemExclusionSet" ;
                      text:paramType "set" ;
                      text:paramValue ("ing" "ed") ]
                    )
           ] .
 * </pre>
 * <pre>
    text:map (
         [ text:field "text" ; 
           text:predicate rdfs:label;
           text:analyzer [
               a text:GenericAnalyzer ;
               text:class "org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper" ;
               text:params (
                    [ text:paramName "defaultAnalyzer" ;
                      text:paramType "analyzer" ;
                      text:paramValue [ a text:SimpleAnalyzer ] ]
                    [ text:paramName "maxShingleSize" ;
                      text:paramType "int" ;
                      text:paramValue 3 ]
                    )
           ] .
 * </pre>
 */
public class GenericAnalyzerAssembler extends AssemblerBase {
    /*
    text:map (
         [ text:field "text" ; 
           text:predicate rdfs:label;
           text:analyzer [
               a text:GenericAnalyzer ;
               text:class "org.apache.lucene.analysis.en.EnglishAnalyzer" ;
               text:params (
                    [ text:paramName "stopwords" ;
                      text:paramType "set" ;
                      text:paramValue ("the" "a" "an") ]
                    [ text:paramName "stemExclusionSet" ;
                      text:paramType "set" ;
                      text:paramValue ("ing" "ed") ]
                    )
           ] .
     */

    public static final String TYPE_ANALYZER = "analyzer";
    public static final String TYPE_BOOL = "boolean";
    public static final String TYPE_FILE = "file";
    public static final String TYPE_INT = "int";
    public static final String TYPE_SET = "set";
    public static final String TYPE_STRING = "string";

    @Override
	public Analyzer open(Assembler a, Resource root, Mode mode) {
	    if (root.hasProperty(TextVocab.pClass)) {
	        // text:class is expected to be a string literal
	        String className = root.getProperty(TextVocab.pClass).getString();

	        // is the class accessible?
	        Class<?> clazz = null;
	        try {
	            clazz = Class.forName(className);
	        } catch (ClassNotFoundException e) {
	            Log.error(this, "Analyzer class " + className + " not found. " + e.getMessage(), e);
	            return null;
	        }

	        // Is the class an Analyzer?
	        if (!Analyzer.class.isAssignableFrom(clazz)) {
	            Log.error(this, clazz.getName() + " has to be a subclass of " + Analyzer.class.getName());
	            return null;
	        }
	        
	        if (root.hasProperty(TextVocab.pParams)) {
	            RDFNode node = root.getProperty(TextVocab.pParams).getObject();
	            if (! node.isResource()) {
	                throw new TextIndexException("text:params must be a list of parameter resources: " + node);
	            }

	            List<ParamSpec> specs = getParamSpecs((Resource) node);

	            // split the param specs into classes and values for constructor lookup
	            final Class<?> paramClasses[] = new Class<?>[specs.size()];
	            final Object paramValues[] = new Object[specs.size()];
	            for (int i = 0; i < specs.size(); i++) {
	                ParamSpec spec = specs.get(i);
	                paramClasses[i] = spec.getValueClass();
	                paramValues[i] = spec.getValue();
	            }

	            // Create new analyzer
	            return newAnalyzer(clazz, paramClasses, paramValues);

	        } else {
	            // use the nullary Analyzer constructor
	            return newAnalyzer(clazz, new Class<?>[0], new Object[0]);
	        }
	    } else {
	        throw new TextIndexException("text:class property is required by GenericAnalyzer: " + root);
	    }
	}

    /**
     * Create instance of the Lucene Analyzer, <code>class</code>, with provided parameters
     *
     * @param clazz The analyzer class
     * @param paramClasses The parameter classes
     * @param paramValues The parameter values
     * @return The lucene analyzer
     */
    private Analyzer newAnalyzer(Class<?> clazz, Class<?>[] paramClasses, Object[] paramValues) {

        String className = clazz.getName();

        try {
            final Constructor<?> cstr = clazz.getDeclaredConstructor(paramClasses);

            return (Analyzer) cstr.newInstance(paramValues);

        } catch (IllegalArgumentException | IllegalAccessException | InstantiationException | InvocationTargetException | SecurityException e) {
            Log.error(this, "Exception while instantiating analyzer class " + className + ". " + e.getMessage(), e);
        } catch (NoSuchMethodException ex) {
            Log.error(this, "Could not find matching analyzer class constructor for " + className + " " + ex.getMessage(), ex);
        }

        return null;
    }
    
    private List<ParamSpec> getParamSpecs(Resource list) {
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
    
    private ParamSpec getParamSpec(Resource node) {
        Statement nameStmt = node.getProperty(TextVocab.pParamName);
        Statement typeStmt = node.getProperty(TextVocab.pParamType);
        Statement valueStmt = node.getProperty(TextVocab.pParamValue);
        
        String name = getStringValue(nameStmt);
        String type = getStringValue(typeStmt);
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
            Log.error(this, "Unknown parameter type: " + type + " for param: " + name + " with value: " + value);
            break;
        }

        return null;
    }
    
    private String getStringValue(Statement stmt) {
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

    private List<String> toStrings(Resource list) {
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
    private static final class ParamSpec {

        private final String name;
        private final Object value;
        private final Class<?> clazz;

        @SuppressWarnings("unused")
        public ParamSpec(String key, Object value) {
            this(key, value, value.getClass());
        }

        public ParamSpec(String key, Object value, Class<?> clazz) {
            this.name = key;
            this.value = value;
            this.clazz = clazz;
        }

        @SuppressWarnings("unused")
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
