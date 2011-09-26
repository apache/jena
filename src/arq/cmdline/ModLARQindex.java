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

package arq.cmdline;

import org.apache.lucene.analysis.standard.StandardAnalyzer ;
import org.apache.lucene.index.IndexReader ;
import org.apache.lucene.index.IndexWriter ;
import org.apache.lucene.store.FSDirectory ;
import arq.cmd.CmdException ;

import com.hp.hpl.jena.query.larq.ARQLuceneException ;
import com.hp.hpl.jena.query.larq.IndexLARQ ;

public class ModLARQindex implements ArgModuleGeneral
{
    ArgDecl argIndex = new ArgDecl(ArgDecl.HasValue, "larq", "lucene", "index")  ;
    String luceneDir ;
    

    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("LARQ") ;
        cmdLine.add(argIndex, "--larq=DIR", "Index directory") ;
    }

    public void processArgs(CmdArgModule cmdLine)
    {
        if ( ! cmdLine.contains(argIndex) )
            throw new CmdException("No index") ;
        luceneDir = cmdLine.getValue(argIndex) ; 
    }
    
    public IndexLARQ getIndexLARQ()
    { 
        try {
            FSDirectory dir = FSDirectory.getDirectory(luceneDir);
            IndexReader indexReader = IndexReader.open(dir) ;
            return new IndexLARQ(indexReader) ;
        } catch (Exception ex)
        { throw new ARQLuceneException("LARQ", ex) ; }
    }
    
    public IndexWriter getIndexWriter()
    {
        try {
            FSDirectory dir = FSDirectory.getDirectory(luceneDir);
            IndexWriter indexWriter = new IndexWriter(dir, new StandardAnalyzer()) ;
            return indexWriter ;
        } catch (Exception ex)
        { throw new ARQLuceneException("LARQ", ex) ; }
    }
}
