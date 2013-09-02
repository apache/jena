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

import org.apache.jena.query.spatial.SpatialIndex;
import org.apache.jena.query.spatial.SpatialIndexException;
import org.apache.jena.query.spatial.SpatialIndexLucene;
import org.apache.jena.query.spatial.SpatialQuery;
import org.apache.jena.query.spatial.assembler.SpatialVocab;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmd.CmdException;
import arq.cmdline.ArgDecl;
import arq.cmdline.CmdARQ;

import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils;
import com.hp.hpl.jena.sparql.util.Utils;

/**
 * Text index development tool - dump the index.
 */
public class spatialindexdump extends CmdARQ {

    private static Logger      log          = LoggerFactory.getLogger(spatialindexdump.class) ;

    public static final ArgDecl assemblerDescDecl = new ArgDecl(ArgDecl.HasValue, "desc", "dataset") ;
    protected SpatialIndex        spatialIndex    = null ;

    static public void main(String... argv) {
        SpatialQuery.init() ;
        new spatialindexdump(argv).mainRun() ;
    }

    protected spatialindexdump(String[] argv) {
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
        spatialIndex = (SpatialIndex)AssemblerUtils.build(file, SpatialVocab.spatialIndex) ;
    }        

    @Override
    protected String getSummary() {
        return getCommandName() + " assemblerFile" ;
    }

    @Override
    protected void exec() {
        
        if ( spatialIndex instanceof SpatialIndexLucene )
            dump((SpatialIndexLucene)spatialIndex) ;
//        else if ( spatialIndex instanceof SpatialIndexSolr )
//            dump((SpatialIndexSolr)spatialIndex) ;
        else
            System.err.println("Unsupported index type : "+Utils.className(spatialIndex)) ;
        }

//    private static void dump(SpatialIndexSolr spatialIndex) { System.err.println("Not implemented : dump Solr index") ; }

    private static void dump(SpatialIndexLucene spatialIndex) {
        try {
            Directory directory = spatialIndex.getDirectory() ;
            Analyzer analyzer = spatialIndex.getAnalyzer() ;
            IndexReader indexReader = DirectoryReader.open(directory) ;
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            QueryParser queryParser = new QueryParser(SpatialIndexLucene.VER, spatialIndex.getDocDef().getEntityField(), analyzer);
            Query query = queryParser.parse("*:*");
            ScoreDoc[] sDocs = indexSearcher.search(query, 1000).scoreDocs ;
            for ( ScoreDoc sd : sDocs ) {
                System.out.println("Doc: "+sd.doc) ;
                Document doc = indexSearcher.doc(sd.doc) ;
                //System.out.println(doc) ;
                for ( IndexableField f : doc ) {
                    //System.out.println("  "+f) ;
                    System.out.println("  "+f.name()+" = "+f.stringValue()) ;
                }
                
            }

        } catch (Exception ex) { throw new SpatialIndexException(ex) ; }
        
    }
}
