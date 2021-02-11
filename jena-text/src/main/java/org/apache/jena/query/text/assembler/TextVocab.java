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
    public static final Resource textIndexLucene    = Vocab.resource(NS, "TextIndexLucene") ;
    public static final Property pLanguage          = Vocab.property(NS, "language") ;
    public static final Property pDirectory         = Vocab.property(NS, "directory") ;         // Lucene
    public static final Property pMultilingualSupport   = Vocab.property(NS, "multilingualSupport") ;
    public static final Property pMaxBasicQueries   = Vocab.property(NS, "maxBasicQueries") ;
    public static final Property pStoreValues       = Vocab.property(NS, "storeValues") ;
    public static final Property pIgnoreIndexErrors       = Vocab.property(NS, "ignoreIndexErrors") ;
    public static final Property pQueryAnalyzer     = Vocab.property(NS, "queryAnalyzer") ;
    public static final Property pQueryParser       = Vocab.property(NS, "queryParser") ;
    public static final Property pEntityMap         = Vocab.property(NS, "entityMap") ;
    public static final Property pTokenizer         = Vocab.property(NS, "tokenizer") ;
    public static final Property pFilter            = Vocab.property(NS, "filter") ;
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
    public static final Property pNoIndex           = Vocab.property(NS, "noIndex") ;
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
    /**
     * "Standard Filter" is a no-op in Lucene 7.x and removed in Lucene 8.x.
     *
     * @deprecated Do not use. To be removed.
     */
    @Deprecated
    public static final Resource standardFilter = Vocab.resource(NS, "StandardFilter");

    public static final Resource lowerCaseFilter    = Vocab.resource(NS, "LowerCaseFilter");
    public static final Resource asciiFoldingFilter = Vocab.resource(NS, "ASCIIFoldingFilter");

    public static final Resource definedAnalyzer    = Vocab.resource(NS, "DefinedAnalyzer");
    public static final Resource genericAnalyzer    = Vocab.resource(NS, "GenericAnalyzer");
    public static final Resource genericFilter      = Vocab.resource(NS, "GenericFilter");
    public static final Resource genericTokenizer   = Vocab.resource(NS, "GenericTokenizer");
    public static final Resource typeAnalyzer       = Vocab.resource(NS, Params.TYPE_ANALYZER);
    public static final Resource typeBoolean        = Vocab.resource(NS, Params.TYPE_BOOL);
    public static final Resource typeFile           = Vocab.resource(NS, Params.TYPE_FILE);
    public static final Resource typeInt            = Vocab.resource(NS, Params.TYPE_INT);
    public static final Resource typeSet            = Vocab.resource(NS, Params.TYPE_SET);
    public static final Resource typeString         = Vocab.resource(NS, Params.TYPE_STRING);
    public static final Property pClass             = Vocab.property(NS, "class");
    public static final Property pParams            = Vocab.property(NS, "params");
    public static final Property pParamName         = Vocab.property(NS, "paramName");
    public static final Property pParamType         = Vocab.property(NS, "paramType");
    public static final Property pParamValue        = Vocab.property(NS, "paramValue");
    public static final Property pDefAnalyzers      = Vocab.property(NS, "defineAnalyzers");
    public static final Property pDefAnalyzer       = Vocab.property(NS, "defineAnalyzer");
    public static final Property pDefFilter         = Vocab.property(NS, "defineFilter");
    public static final Property pDefTokenizer      = Vocab.property(NS, "defineTokenizer");
    public static final Property pAddLang           = Vocab.property(NS, "addLang");
    public static final Property pUseAnalyzer       = Vocab.property(NS, "useAnalyzer");
    public static final Property pSearchFor         = Vocab.property(NS, "searchFor");
    public static final Property pAuxIndex          = Vocab.property(NS, "auxIndex");
    public static final Property pIndexAnalyzer     = Vocab.property(NS, "indexAnalyzer");

    // Query Cache
    public static final Property pCacheQueries      = Vocab.property(NS, "cacheQueries");

    // Property Lists
    public static final Property pPropLists         = Vocab.property(NS, "propLists");
    public static final Property pPropListProp      = Vocab.property(NS, "propListProp");
    public static final Property pProps             = Vocab.property(NS, "props");
}

