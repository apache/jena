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

package org.apache.jena.query.text.analyzer ;

import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.List ;
import java.lang.reflect.InvocationTargetException;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.text.TextIndexException;
import org.apache.jena.query.text.assembler.GenericFilterAssembler.FilterSpec;
import org.apache.jena.query.text.assembler.GenericTokenizerAssembler.TokenizerSpec;
import org.apache.jena.query.text.assembler.TextVocab;
import org.apache.lucene.analysis.Analyzer ;
import org.apache.lucene.analysis.TokenFilter ;
import org.apache.lucene.analysis.Tokenizer ;
import org.apache.lucene.analysis.TokenStream ;
import org.apache.lucene.analysis.core.KeywordTokenizer ;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter ;
import org.apache.lucene.analysis.core.WhitespaceTokenizer ;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/** 
 * Lucene Analyzer implementation that can be configured with different
 * Tokenizer and (optionally) TokenFilter implementations.
 */

public class ConfigurableAnalyzer extends Analyzer {
        private final String tokenizer;
        private final List<String> filters;
        
        private static Hashtable<String, FilterSpec>    filterSpecs = new Hashtable<>();
        private static Hashtable<String, TokenizerSpec> tokenizerSpecs = new Hashtable<>();
        
        static{
            Class<?>[] paramClasses = new Class<?>[0];
            Object[] paramValues = new Object[0];
            
            tokenizerSpecs.put(TextVocab.NS+"KeywordTokenizer", new TokenizerSpec(KeywordTokenizer.class, paramClasses, paramValues));
            tokenizerSpecs.put(TextVocab.NS+"LetterTokenizer", new TokenizerSpec(LetterTokenizer.class, paramClasses, paramValues));
            tokenizerSpecs.put(TextVocab.NS+"StandardTokenizer", new TokenizerSpec(StandardTokenizer.class, paramClasses, paramValues));
            tokenizerSpecs.put(TextVocab.NS+"WhitespaceTokenizer", new TokenizerSpec(WhitespaceTokenizer.class, paramClasses, paramValues));
            
            paramClasses = new Class<?>[] {TokenStream.class};
            paramValues = new Object[]{ null };
            
            filterSpecs.put(TextVocab.NS+"ASCIIFoldingFilter", new FilterSpec(ASCIIFoldingFilter.class, paramClasses, paramValues));
            filterSpecs.put(TextVocab.NS+"LowerCaseFilter", new FilterSpec(LowerCaseFilter.class, paramClasses, paramValues));
            filterSpecs.put(TextVocab.NS+"StandardFilter", new FilterSpec(StandardFilter.class, paramClasses, paramValues));
        }
        
        public static void defineFilter(String id, FilterSpec spec) {
            filterSpecs.put(id, spec);
        }
        
        public static void defineTokenizer(String id, TokenizerSpec spec) {
            tokenizerSpecs.put(id, spec);
        }
        
        /**
         * Create instance of a Lucene Tokenizer, <code>class</code>, with provided parameters
         *
         * @param clazz The analyzer class
         * @param paramClasses The parameter classes
         * @param paramValues The parameter values
         * @return The lucene analyzer
         */
        private Tokenizer newTokenizer(Class<?> clazz, Class<?>[] paramClasses, Object[] paramValues) {
  
            String className = clazz.getName();
  
            try {
                final Constructor<?> cstr = clazz.getDeclaredConstructor(paramClasses);
  
                return (Tokenizer) cstr.newInstance(paramValues);
  
            } catch (IllegalArgumentException | IllegalAccessException | InstantiationException | InvocationTargetException | SecurityException e) {
                Log.error(this, "Exception while instantiating tokenizer class " + className + ". " + e.getMessage(), e);
            } catch (NoSuchMethodException ex) {
                Log.error(this, "Could not find matching tokenizer class constructor for " + className + " " + ex.getMessage(), ex);
            }
  
            return null;
        }

        /**
         * Create instance of the Lucene Analyzer, <code>class</code>, with provided parameters
         *
         * @param clazz The analyzer class
         * @param paramClasses The parameter classes
         * @param paramValues The parameter values
         * @return The lucene analyzer
         */
        private TokenFilter newFilter(Class<?> clazz, Class<?>[] paramClasses, Object[] paramValues) {
  
            String className = clazz.getName();
  
            try {
                final Constructor<?> cstr = clazz.getDeclaredConstructor(paramClasses);
  
                return (TokenFilter) cstr.newInstance(paramValues);
  
            } catch (IllegalArgumentException | IllegalAccessException | InstantiationException | InvocationTargetException | SecurityException e) {
                Log.error(this, "Exception while instantiating analyzer class " + className + ". " + e.getMessage(), e);
            } catch (NoSuchMethodException ex) {
                Log.error(this, "Could not find matching analyzer class constructor for " + className + " " + ex.getMessage(), ex);
            }
  
            return null;
        }
        
        private Tokenizer getTokenizer(String tokenizerName) {
            TokenizerSpec spec = tokenizerSpecs.get(tokenizerName);
            if (spec == null) {
                throw new TextIndexException("Unknown tokenizer : " + tokenizerName);
            }

            Class<?> clazz = spec.clazz;
            Class<?>[] paramClasses = spec.paramClasses;
            Object[] paramValues = spec.paramValues;
            
            return newTokenizer(clazz, paramClasses, paramValues);
        }
        
        private TokenFilter getTokenFilter(String filterName, TokenStream source) {
            FilterSpec spec = filterSpecs.get(filterName);
            
            if (spec == null) {
                throw new TextIndexException("Unknown filter : " + filterName);
            }

            Class<?> clazz = spec.clazz;
            Class<?>[] paramClasses = spec.paramClasses;
            Object[] paramValues = spec.paramValues;
            
            // the source should always be the first parameter
            paramValues[0] = source;
            
            return newFilter(clazz, paramClasses, paramValues);
        }
        
        public ConfigurableAnalyzer(String tokenizer, List<String> filters) {
                this.tokenizer = tokenizer;
                this.filters = filters;
        }

        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
                Tokenizer source = getTokenizer(this.tokenizer);
                TokenStream stream = source;
                for (String filter : this.filters) {
                        stream = getTokenFilter(filter, stream);
                }
                return new TokenStreamComponents(source, stream);
        }

        @Override
        protected TokenStream normalize(String fieldName, TokenStream in) {
                TokenStream stream = in;
                for (String filter : this.filters) {
                        stream = getTokenFilter(filter, stream);
                }
                return stream;
        }

}
