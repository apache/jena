/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package arq;

import java.util.Iterator ;

import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdLARQ ;
import arq.cmdline.ModDatasetGeneralAssembler ;
import arq.cmdline.ModLARQindex ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.larq.IndexBuilderModel ;
import com.hp.hpl.jena.query.larq.IndexBuilderString ;
import com.hp.hpl.jena.query.larq.IndexBuilderSubject ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.StmtIterator ;

public class larqbuilder extends CmdLARQ
{
    ModDatasetGeneralAssembler modDataset = new ModDatasetGeneralAssembler() ;
    ModLARQindex modIndex = new ModLARQindex() ;
    
    ArgDecl argNodes = new ArgDecl(ArgDecl.NoValue, "nodes", "subjects")  ;
    private boolean indexSubjects ;
    
    // --larq filename 
    public static void main(String... argv)
    {
        new larqbuilder(argv).mainRun() ;
    }
    
    protected larqbuilder(String[] argv)
    {
        super(argv) ;
        super.addModule(modDataset) ;
        super.addModule(modIndex) ;
        super.add(argNodes, "--subjects", "Index literals to subject nodes") ;
    }

    @Override
    protected String getSummary()
    {
        return "larqbuilder --larq DIR [--subjects] --data RDF" ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        indexSubjects = super.contains(argNodes) ;
    }
    
    @Override
    protected void exec()
    {
        // ---- Read and index all literal strings.
        IndexBuilderModel larqBuilder = 
            indexSubjects ? new IndexBuilderSubject(modIndex.getIndexWriter()) :
                            new IndexBuilderString(modIndex.getIndexWriter()) ; 
        Dataset ds = modDataset.getDataset() ;
        index(larqBuilder, ds.getDefaultModel()) ;
        for ( Iterator<String> iter = ds.listNames() ; iter.hasNext() ; )
        {
            String g = iter.next() ;
            index(larqBuilder, ds.getNamedModel(g)) ;
        }
        
        larqBuilder.closeWriter() ;
    }

    private void index(IndexBuilderModel larqBuilder, Model model)
    {
        StmtIterator sIter = model.listStatements() ;
        larqBuilder.indexStatements(sIter) ;
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