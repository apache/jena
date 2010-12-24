/*
 * (c) Copyright 2010 Talis Information Ltd
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.io.File ;

import org.apache.lucene.index.IndexReader ;
import org.apache.lucene.store.Directory ;
import org.apache.lucene.store.FSDirectory ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.assembler.Mode ;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase ;
import com.hp.hpl.jena.assembler.exceptions.AssemblerException ;
import com.hp.hpl.jena.query.larq.IndexLARQ ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils ;

public class AssemblerLARQ extends AssemblerBase implements Assembler
{
    /** Vocabulary
     *     ja:luceneIndex ....
     */
    
    @Override
    public Object open(Assembler a, Resource root, Mode mode)
    {
        try
        {
            if ( ! GraphUtils.exactlyOneProperty(root, LARQAssemblerVocab.pIndex) )
                throw new AssemblerException(root, "Required: exactly one index property" ) ;

            String index = GraphUtils.getAsStringValue(root, LARQAssemblerVocab.pIndex) ;
            
            
//            Directory dir = FSDirectory.open(new File(index)) ;
//            IndexReader indexReader = IndexReader.open(dir, false) ;
            IndexReader indexReader = IndexReader.open(index) ;
            
            IndexLARQ indexLARQ = new IndexLARQ(indexReader) ;
            return indexLARQ ;

        } catch (Exception ex)
        {
            throw new ARQException("Failed to assemble Lucene index", ex) ;
        }
    }
}



/*
 * (c) Copyright 2010 Talis Information Ltd
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