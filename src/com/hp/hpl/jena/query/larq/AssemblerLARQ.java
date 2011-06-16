/*
 * (c) Copyright 2010 Talis Information Ltd
 * (c) Copyright 2010, 2011 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.larq;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.assembler.exceptions.AssemblerException;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.assembler.DatasetAssemblerVocab;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

public class AssemblerLARQ extends AssemblerBase implements Assembler
{
    /** Vocabulary
     *     ja:textIndex ....
     */

    static { LARQ.init(); }
    
    @Override
    public Object open(Assembler a, Resource root, Mode mode)
    {
        LARQ.init();

        if ( ! GraphUtils.exactlyOneProperty(root, DatasetAssemblerVocab.pIndex) )
            throw new AssemblerException(root, "Required: exactly one index property" ) ;

        try
        {
            String indexPath = GraphUtils.getAsStringValue(root, DatasetAssemblerVocab.pIndex) ;
            return make(null, indexPath) ;
        } catch (Exception ex)
        {
            throw new ARQLuceneException("Failed to assemble Lucene index", ex) ;
        }
    }
    
    public static IndexLARQ make (Dataset dataset, String indexPath) throws CorruptIndexException, IOException 
    {
        Directory directory = FSDirectory.getDirectory(new File(indexPath)) ;
        IndexReader indexReader = null;
        if ( dataset != null ) {
            IndexWriter indexWriter = new IndexWriter(directory, new StandardAnalyzer());
            IndexBuilderModel larqBuilder = new IndexBuilderString(indexWriter) ; 
            dataset.getDefaultModel().register(larqBuilder);
            for ( Iterator<String> iter = dataset.listNames() ; iter.hasNext() ; ) {
                String g = iter.next() ;
                dataset.getNamedModel(g).register(larqBuilder) ;
            }
            indexReader = IndexReader.open(directory) ;
        } else {
            indexReader = IndexReader.open(directory) ;
        }
        IndexLARQ indexLARQ = new IndexLARQ(indexReader) ;
        LARQ.setDefaultIndex(indexLARQ) ;
        return indexLARQ ;
    }

}

/*
 * (c) Copyright 2010 Talis Information Ltd
 * (c) Copyright 2010, 2011 Talis Systems Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */