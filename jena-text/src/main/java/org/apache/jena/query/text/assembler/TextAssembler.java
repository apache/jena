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

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.sparql.core.assembler.AssemblerUtils ;

public class TextAssembler
{
    public static void init()
    {
        AssemblerUtils.init() ;
        AssemblerUtils.registerDataset(TextVocab.textDataset,      new TextDatasetAssembler()) ;
        
        Assembler.general.implementWith(TextVocab.entityMap,        new EntityDefinitionAssembler()) ;
        Assembler.general.implementWith(TextVocab.textIndexLucene,  new TextIndexLuceneAssembler()) ;
        Assembler.general.implementWith(TextVocab.standardAnalyzer, new StandardAnalyzerAssembler()) ;
        Assembler.general.implementWith(TextVocab.simpleAnalyzer,   new SimpleAnalyzerAssembler()) ;
        Assembler.general.implementWith(TextVocab.keywordAnalyzer,  new KeywordAnalyzerAssembler()) ;
        Assembler.general.implementWith(TextVocab.lowerCaseKeywordAnalyzer, new LowerCaseKeywordAnalyzerAssembler()) ;
        Assembler.general.implementWith(TextVocab.localizedAnalyzer, new LocalizedAnalyzerAssembler()) ;
        Assembler.general.implementWith(TextVocab.configurableAnalyzer, new ConfigurableAnalyzerAssembler()) ;
        Assembler.general.implementWith(TextVocab.genericAnalyzer,  new GenericAnalyzerAssembler()) ;
        Assembler.general.implementWith(TextVocab.genericFilter,    new GenericFilterAssembler()) ;
        Assembler.general.implementWith(TextVocab.genericTokenizer,  new GenericTokenizerAssembler()) ;
        Assembler.general.implementWith(TextVocab.definedAnalyzer,  new DefinedAnalyzerAssembler()) ;

    }
}

