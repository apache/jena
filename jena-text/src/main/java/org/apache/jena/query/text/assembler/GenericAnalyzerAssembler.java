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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.text.TextIndexException;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.lucene.analysis.Analyzer;

/**
 * Creates generic analyzers given a fully qualified Class name and a list
 * of parameters for a constructor of the Class.
 * <p>
 * The parameters may be of the following types:
 * <pre>
 *     text:TypeString    String
 *     text:TypeSet       org.apache.lucene.analysis.util.CharArraySet
 *     text:TypeFile      java.io.FileReader
 *     text:TypeInt       int
 *     text:TypeBoolean   boolean
 *     text:TypeAnalyzer  org.apache.lucene.analysis.Analyzer
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
 * <code>Set&lt;String&gt;</code>. So a simple wrapper can extract the values
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
    text:map (
         [ text:field "text" ; 
           text:predicate rdfs:label;
           text:analyzer [
               a text:GenericAnalyzer ;
               text:class "org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper" ;
               text:params (
                    [ text:paramName "defaultAnalyzer" ;
                      text:paramType text:TypeAnalyzer ;
                      text:paramValue [ a text:SimpleAnalyzer ] ]
                    [ text:paramName "maxShingleSize" ;
                      text:paramType text:TypeInt ;
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
                      text:paramType text:TypeSet ;
                      text:paramValue ("the" "a" "an") ]
                    [ text:paramName "stemExclusionSet" ;
                      text:paramType text:TypeSet ;
                      text:paramValue ("ing" "ed") ]
                    )
           ] .
     */

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

                List<Params.ParamSpec> specs = Params.getParamSpecs((Resource) node);

                // split the param specs into classes and values for constructor lookup
                final Class<?> paramClasses[] = new Class<?>[specs.size()];
                final Object paramValues[] = new Object[specs.size()];
                for (int i = 0; i < specs.size(); i++) {
                    Params.ParamSpec spec = specs.get(i);
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
}
