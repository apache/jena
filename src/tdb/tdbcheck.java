/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import arq.cmd.CmdUtils;
import arq.cmdline.CmdARQ;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.Violation;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.RDFReaderFImpl;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.sparql.util.graph.GraphSink;
import com.hp.hpl.jena.tdb.base.loader.NTriplesReader2;
import com.hp.hpl.jena.tdb.base.loader.NodeTupleReader;
import com.hp.hpl.jena.util.FileManager;

/** Check an N-Triples file (or any other syntax). */
public class tdbcheck extends CmdARQ
{
    static public void main(String... argv)
    { 
        CmdUtils.setLog4j() ;
        // Override N-TRIPLES
        String bulkLoaderClass = NTriplesReader2.class.getName() ;
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLES", bulkLoaderClass) ;
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLE", bulkLoaderClass) ;
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
        @SuppressWarnings("unchecked")
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

    private void execOne(String f)
    {
        Graph g = new GraphSinkCheck() ;
        Model model = ModelFactory.createModelForGraph(g) ;
        
        NodeTupleReader.CheckingIRIs = true ;
        
        if ( f != null )
        {
            if ( isVerbose() )
                System.out.println("File: "+f) ;
            FileManager.get().readModel(model, f) ;
        }
        else
        {
            //model.read(System.in, null, "N-TRIPLES") ;
            // MUST BE N-TRIPLES
            NodeTupleReader.CheckingNTriples = true ;
            NodeTupleReader.CheckingIRIs = true ;
            boolean forceISO8859 = false ;
            if ( forceISO8859 )
                forceISO8859() ;

            NodeTupleReader.read(new NodeTupleReader.NullSink(), System.in, null) ;
        }
    }

    private void forceISO8859()
    {
        // Force to ISO-8859-1
        Reader r = null ;
        try
        {
            //java.io canonical name: ISO8859_1 
            //java.nio canonical name: ISO-8859-1 
            r = new InputStreamReader(System.in, "ISO-8859-1") ;
        } catch (UnsupportedEncodingException ex)
        {
            ex.printStackTrace();
            return ;
        }

        NodeTupleReader.read(new NodeTupleReader.NullSink(), r, null) ;
    }

    @Override
    protected String getCommandName()
    {
        return Utils.className(this) ;
    }

    // Inherit from this to get performAdd checks 
    static class GraphSinkCheck extends GraphSink
    {
        @Override
        public void performAdd( Triple t )
        {
            check(t.getSubject()) ;
            check(t.getPredicate()) ;
            check(t.getObject()) ;
        }
        static IRIFactory iriFactory = IRIFactory.semanticWebImplementation();

        final private void check(Node node)
        {
            if ( node.isURI() ) checkURI(node) ;
            else if ( node.isBlank() ) checkBlank(node) ;
            else if ( node.isLiteral() ) checkLiteral(node) ;
            else if ( node.isVariable() ) checkVar(node) ;
        }
        
        final private void checkVar(Node node)
        {}

        final private void checkLiteral(Node node)
        {
            LiteralLabel lit = node.getLiteral() ;
            
            // Datatype check (and plain literals are always well formed)
            if ( lit.getDatatype() != null && ! lit.isWellFormed() )
                throw new JenaException("Lexical not valid for datatype: "+node) ;
            
//            // Not well formed.
//            if ( lit.getDatatype() != null )
//            {
//                if ( ! lit.getDatatype().isValid(lit.getLexicalForm()) )
//                    throw new JenaException("Lexical not valid for datatype: "+node) ;
//            }
            
            if (lit.language() != null )
            {
                // Not a pefect test.
                String lang = lit.language() ;
                if ( lang.length() > 0 && ! lang.matches("[a-z]{1,8}(-[a-z]{1,8})*") )
                    throw new JenaException("Language not valid: "+node) ;
            }
        }

        final private void checkBlank(Node node)
        {
            String x =  node.getBlankNodeLabel() ;
            if ( x.indexOf(' ') >= 0 )
                throw new JenaException("Illegal blank node label (contains a space): "+node) ;
        }


        final private void checkURI(Node node)
        {
            boolean includeWarnings = true ;
            IRI iri = iriFactory.create(node.getURI()); // always works
            if (iri.hasViolation(includeWarnings))
            {
                @SuppressWarnings("unchecked")
                Iterator<Violation> it = iri.violations(includeWarnings);
                // Deemphasise some wanrings.
                Violation vError = null ;
                Violation vWarning = null ;
                Violation vSub = null ;
                while (it.hasNext()) {
                    Violation v2 = it.next();
                    int code = v2.getViolationCode() ;
                    if ( v2.isError() )
                    {
                        vError = v2 ;
                        continue ;
                    }
                    
                    // Supress the importance of these.
                    if ( code == Violation.LOWERCASE_PREFERRED ||
                         code == Violation.PERCENT_ENCODING_SHOULD_BE_UPPERCASE )
                    {
                        if ( vSub == null )
                            vSub = v2 ;
                        continue ;
                    }
                    vWarning = v2 ;
                    break ;
                }
                if ( vError != null )
                    throw new JenaException(vError.getShortMessage()) ;
                if ( vWarning != null )
                    throw new JenaException(vWarning.getShortMessage()) ;
                if ( vSub != null )
                    throw new JenaException(vSub.getShortMessage()) ;
                }
            }
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