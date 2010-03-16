/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import java.io.InputStream ;
import java.io.InputStreamReader ;
import java.io.Reader ;
import java.io.UnsupportedEncodingException ;
import java.util.List ;

import org.openjena.atlas.logging.Log ;
import arq.cmdline.CmdARQ ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.riot.Checker ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.sparql.util.graph.GraphSink ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.util.FileManager ;

/** Check an N-Triples file (or any other syntax). */
public class tdbcheck extends CmdARQ
{
    static public void main(String... argv)
    { 
        TDB.init(); // Includes reorganising the Jena readers.
        Log.setLog4j() ;
        // Checking done in graph.
        new tdbcheck(argv).mainRun() ;
    }

    protected tdbcheck(String[] argv)
    {
        super(argv) ;
    }
    
    @Override
    protected String getSummary()
    {
        return getCommandName()+" FILE ..." ;
    }

    @Override
    protected void exec()
    {
        List<String> files = getPositional() ;
        if ( files.size() == 0 )
        {
            //throw new CmdException("No files - nothing to do") ;
            execOne(null) ;
            return ;
        }
        
        for ( String f : files )
            execOne(f) ;
    }

    class GraphCheckingSink extends GraphSink
    {
        Checker checker = new Checker() ;
        
            @Override
            public void performAdd( Triple triple )
            {
                checker.check(triple, -1, -1) ;
            }
    }
    
    private void execOne(String f)
    {
        // Black hole for triples - with checking.
        // Turtle already does this checking.
        Graph g = new GraphCheckingSink() ;
        Model model = ModelFactory.createModelForGraph(g) ;
        
        if ( f != null )
        {
            if ( isVerbose() )
                System.out.println("File: "+f) ;
            FileManager.get().readModel(model, f) ;
        }
        else
        {
            // MUST BE N-TRIPLES
            if ( true )
            {
                // RIOT will make this UTF-8
                model.read(System.in, null, "N-TRIPLES") ;    
            }
            else
            {
                // ASCII or ISO-8859-1
                boolean strictASCII = false ;
                Reader r = strictASCII ? readerASCII(System.in) : readerISO8859(System.in) ;
                model.read(r, null, "N-TRIPLES") ;    
            }
            
        }
    }

    private Reader readerISO8859(InputStream inputStream)
    {
        try
        {
            //java.io canonical name: ISO8859_1 
            //java.nio canonical name: ISO-8859-1 
            return new InputStreamReader(inputStream, "ISO-8859-1") ;
        } catch (UnsupportedEncodingException ex)
        {
            ex.printStackTrace();
            return null ;
        }
    }
    
    private Reader readerASCII(InputStream inputStream)
    {
        try
        {
            return new InputStreamReader(inputStream, "ASCII") ;
        } catch (UnsupportedEncodingException ex)
        {
            ex.printStackTrace();
            return null ;
        }
    }
    
    @Override
    protected String getCommandName()
    {
        return Utils.className(this) ;
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