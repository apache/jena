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

package jena ;

import org.apache.jena.query.text.* ;
import org.apache.jena.query.text.assembler.TextVocab ;
import org.apache.lucene.analysis.Analyzer ;
import org.apache.lucene.document.Document ;
import org.apache.lucene.index.DirectoryReader ;
import org.apache.lucene.index.IndexReader ;
import org.apache.lucene.index.IndexableField ;
import org.apache.lucene.queryparser.classic.QueryParser ;
import org.apache.lucene.search.IndexSearcher ;
import org.apache.lucene.search.Query ;
import org.apache.lucene.search.ScoreDoc ;
import org.apache.lucene.store.Directory ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;
import arq.cmd.CmdException ;
import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdARQ ;

import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils ;
import com.hp.hpl.jena.sparql.util.Utils ;

/**
 * Text index development tool - dump the index.
 */
public class textindexdump extends CmdARQ {

    private static Logger      log          = LoggerFactory.getLogger(textindexdump.class) ;

    public static final ArgDecl assemblerDescDecl = new ArgDecl(ArgDecl.HasValue, "desc", "dataset") ;
    protected TextIndex        textIndex    = null ;

    static public void main(String... argv) {
        TextQuery.init() ;
        new textindexdump(argv).mainRun() ;
    }

    protected textindexdump(String[] argv) {
        super(argv) ;
        super.add(assemblerDescDecl, "--desc=", "Assembler description file") ;
    }

    @Override
    protected void processModulesAndArgs() {
        super.processModulesAndArgs() ;
        // Two forms : with and without arg.
        // Maximises similarity with other tools.
        String file ;
        if ( super.contains(assemblerDescDecl) ) {
            if ( getValues(assemblerDescDecl).size() != 1 )
                throw new CmdException("Multiple assembler descriptions given") ;
            if ( getPositional().size() != 0 )
                throw new CmdException("Additional assembler descriptions given") ; 
            file = getValue(assemblerDescDecl) ;
        } else {
            if ( getNumPositional() != 1 )
                throw new CmdException("Multiple assembler descriptions given") ;
            file = getPositionalArg(0) ;
        }
        textIndex = (TextIndex)AssemblerUtils.build(file, TextVocab.textIndex) ;
    }        

    @Override
    protected String getSummary() {
        return getCommandName() + " assemblerFile" ;
    }

    @Override
    protected void exec() {
        
        if ( textIndex instanceof TextIndexLucene )
            dump((TextIndexLucene)textIndex) ;
        else if ( textIndex instanceof TextIndexSolr )
            dump((TextIndexSolr)textIndex) ;
        else
            System.err.println("Unsupported index type : "+Utils.className(textIndex)) ;
        }

    private static void dump(TextIndexSolr textIndex) { System.err.println("Not implemented : dump Solr index") ; }

    private static void dump(TextIndexLucene textIndex) {
        try {
            Directory directory = textIndex.getDirectory() ;
            Analyzer analyzer = textIndex.getAnalyzer() ;
            IndexReader indexReader = DirectoryReader.open(directory) ;
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            QueryParser queryParser = new QueryParser(TextIndexLucene.VER, textIndex.getDocDef().getPrimaryField(), analyzer);
            Query query = queryParser.parse("*:*");
            ScoreDoc[] sDocs = indexSearcher.search(query, 1000).scoreDocs ;
            for ( ScoreDoc sd : sDocs ) {
                System.out.println("Doc: "+sd.doc) ;
                Document doc = indexSearcher.doc(sd.doc) ;
                // Don't forget that many fields aren't stored, just indexed.
                for ( IndexableField f : doc ) {
                    //System.out.println("  "+f) ;
                    System.out.println("  "+f.name()+" = "+f.stringValue()) ;
                }
                
            }

        } catch (Exception ex) { throw new TextIndexException(ex) ; }
        
    }
}
