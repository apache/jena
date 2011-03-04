/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
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
/*
 * (c) Copyright 2010 Talis Systems Ltd.
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