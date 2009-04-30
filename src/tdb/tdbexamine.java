/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import java.util.Iterator;

import tdb.cmdline.CmdTDB;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.solver.stats.StatsCollector;

import com.hp.hpl.jena.util.FileManager;


public class tdbexamine extends CmdTDB
{

    public static void main (String... argv)
    {
        new tdbexamine(argv).mainRun() ;
    }
    
    public tdbexamine(String[] argv)
    {
        super(argv) ;
        TDB.init() ;
    }

    
    @Override
    protected void exec()
    {
        Iterator<String> iter = super.getPositional().iterator() ;
        
        if ( ! iter.hasNext() )
        {
            System.err.println("No input files") ;
            System.exit(1) ;
        }
        
        for ( ; iter.hasNext() ; )
        {
            String s = iter.next();
            examineOne(s) ;
        }
    }

    private void examineOne(String s)
    {
        StatsCollector.StatsGraph graph = new StatsCollector.StatsGraph() ;  
        Model model = ModelFactory.createModelForGraph(graph) ;
        FileManager.get().readModel(model, s) ;
        graph.printStats() ;
    }

    @Override
    protected String getCommandName()
    {
        return Utils.className(this) ;
    }

    @Override
    protected String getSummary()
    {
        return getCommandName()+" FILE..." ;
    }


}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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