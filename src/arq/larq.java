/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package arq;

import java.util.Iterator ;

import arq.cmdline.CmdLARQ ;
import arq.cmdline.ModLARQindex ;

import com.hp.hpl.jena.query.larq.HitLARQ ;
import com.hp.hpl.jena.query.larq.IndexBuilderString ;
import com.hp.hpl.jena.query.larq.IndexLARQ ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.StmtIterator ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

public class larq extends CmdLARQ
{
    ModLARQindex modIndex = new ModLARQindex() ;
    
    public static void main(String... argv)
    {
        new larq(argv).mainRun() ;
    }
    
    protected larq(String[] argv)
    {
        super(argv) ;
        super.addModule(modIndex) ;
    }

    @Override
    protected String getSummary()
    {
        return "larqquery --larq DIR LuceneQueryString" ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
    }
    
    @Override
    protected void exec()
    {
        IndexLARQ index = modIndex.getIndexLARQ() ;
        for ( String s : super.getPositional() )
        {
            System.out.println("Search : "+s) ;
            Iterator<HitLARQ> hits = index.search(s) ;
            for ( ; hits.hasNext() ; )
            {
                HitLARQ h = hits.next() ;
                String str = FmtUtils.stringForNode(h.getNode()) ;
                if ( super.isVerbose() )
                    System.out.printf("  %-20s %.2f\n", str, h.getScore()) ;
                else
                    System.out.printf("  %-20s\n",str) ;
            }
        }
    }

    private void index(IndexBuilderString larqBuilder, Model model)
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