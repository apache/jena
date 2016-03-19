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

import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.tdb.assembler.Vocab ;

public class TextVocab
{
    public static final String NS                   =  "http://jena.apache.org/text#" ;
    
    public static final String pfQuery              =  NS+"query" ;

    public static final Resource textDataset        = Vocab.resource(NS, "TextDataset") ;
    public static final Property pDataset           = Vocab.property(NS, "dataset") ;
    public static final Property pIndex             = Vocab.property(NS, "index") ;
    public static final Property pTextDocProducer   = Vocab.property(NS, "textDocProducer") ;
    
    public static final Resource textIndex          = Vocab.resource(NS, "TextIndex") ;
    public static final Resource textIndexSolr      = Vocab.resource(NS, "TextIndexSolr") ;
    public static final Resource textIndexLucene    = Vocab.resource(NS, "TextIndexLucene") ;
    public static final Property pLanguage          = Vocab.property(NS, "language") ;
    public static final Property pServer            = Vocab.property(NS, "server") ;            // Solr
    public static final Property pDirectory         = Vocab.property(NS, "directory") ;         // Lucene
    public static final Property pMultilingualSupport   = Vocab.property(NS, "multilingualSupport") ;
    public static final Property pStoreValues       = Vocab.property(NS, "storeValues") ;
    public static final Property pQueryAnalyzer     = Vocab.property(NS, "queryAnalyzer") ;
    public static final Property pEntityMap         = Vocab.property(NS, "entityMap") ;
    public static final Property pTokenizer         = Vocab.property(NS, "tokenizer") ;
    public static final Property pFilters           = Vocab.property(NS, "filters") ;
    
    // Entity definition
    public static final Resource entityMap          = Vocab.resource(NS, "EntityMap") ;
    public static final Property pEntityField       = Vocab.property(NS, "entityField") ;
    public static final Property pDefaultField      = Vocab.property(NS, "defaultField") ;
    public static final Property pGraphField        = Vocab.property(NS, "graphField") ;
    public static final Property pLangField         = Vocab.property(NS, "langField") ;
    public static final Property pUidField          = Vocab.property(NS, "uidField") ;
    public static final Property pMap               = Vocab.property(NS, "map") ;
    public static final Property pField             = Vocab.property(NS, "field") ;
    public static final Property pPredicate         = Vocab.property(NS, "predicate") ;
    public static final Property pOptional          = Vocab.property(NS, "optional") ;
    
    // Analyzers
    public static final Property pAnalyzer          = Vocab.property(NS, "analyzer");
    public static final Resource standardAnalyzer   = Vocab.resource(NS, "StandardAnalyzer");
    public static final Property pStopWords         = Vocab.property(NS, "stopWords");
    public static final Resource simpleAnalyzer     = Vocab.resource(NS, "SimpleAnalyzer");
    public static final Resource keywordAnalyzer    = Vocab.resource(NS, "KeywordAnalyzer");
    public static final Resource lowerCaseKeywordAnalyzer    = Vocab.resource(NS, "LowerCaseKeywordAnalyzer");
    public static final Resource localizedAnalyzer    = Vocab.resource(NS, "LocalizedAnalyzer");
    public static final Resource configurableAnalyzer = Vocab.resource(NS, "ConfigurableAnalyzer");
    
    // Tokenizers
    public static final Resource standardTokenizer  = Vocab.resource(NS, "StandardTokenizer");
    public static final Resource letterTokenizer    = Vocab.resource(NS, "LetterTokenizer");
    public static final Resource keywordTokenizer   = Vocab.resource(NS, "KeywordTokenizer");
    public static final Resource whitespaceTokenizer = Vocab.resource(NS, "WhitespaceTokenizer");
    
    // Filters
    public static final Resource standardFilter     = Vocab.resource(NS, "StandardFilter");
    public static final Resource lowerCaseFilter    = Vocab.resource(NS, "LowerCaseFilter");
    public static final Resource asciiFoldingFilter = Vocab.resource(NS, "ASCIIFoldingFilter");

}

