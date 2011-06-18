/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package riotcmd;

import java.io.InputStream ;
import java.util.List ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.IRILib ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.out.SinkQuadOutput ;
import org.openjena.riot.pipeline.inf.InfFactory ;
import org.openjena.riot.system.SinkExtendTriplesToQuads ;
import arq.cmd.CmdException ;
import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdGeneral ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.util.FileManager ;

/*
 * TDB Infer
 *   RDFS
 *   owl:sameAs (in T-Box, not A-Box)
 *   owl:equivalentClass, owl:equivalentProperty
 *   owl:TransitiveProperty, owl:SymmetricProperty
 *
 * OWLprime - Oracle
- rdfs:domain
- rdfs:range
- rdfs:subClassOf
- rdfs:subPropertyOf
- owl:equivalentClass
- owl:equivalentProperty
- owl:sameAs
- owl:inverseOf
- owl:TransitiveProperty
- owl:SymmetricProperty
- owl:FunctionalProperty
- owl:InverseFunctionalProperty

 JimH: RDFS3:
 #
    * equivalentClass
    * equivalentProperty
    * sameAs
    * differentFrom (and allDifferent) 

# Property Characteristics:

    * inverseOf
    * TransitiveProperty
    * SymmetricProperty
    * FunctionalProperty
    * InverseFunctionalProperty
    * ObjectProperty
    * DatatypeProperty
    * disjointWith 

AllegroGraph RDFS++
    * rdf:type
    * rdfs:subClassOf
    * rdfs:domain and rdfs:range
    * rdfs:subPropertyOf
    * owl:sameAs
    * owl:inverseOf
    * owl:TransitiveProperty
 */
public class infer extends CmdGeneral
{
    static final ArgDecl argRDFS = new ArgDecl(ArgDecl.HasValue, "rdfs") ;
    private Model vocab ;
    
    public static void main(String... argv)
    {
        new infer(argv).mainRun() ;
    }        

    protected infer(String[] argv)
    {
        super(argv) ;
        super.add(argRDFS) ;
    }

//    public static void expand(String filename, Model vocab)
//    {
//        Sink<Triple> sink = new SinkTripleOutput(System.out) ;
//        sink = new InferenceExpanderRDFS(sink, vocab) ;
//        RiotReader.parseTriples(filename, sink) ;
//        IO.flush(System.out); 
//    }

    @Override
    protected String getSummary()
    {
        return "infer --rdfs=vocab FILE ..." ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        if ( ! contains(argRDFS) )
            throw new CmdException("Required argument missing: --"+argRDFS.getKeyName()) ;
        String fn = getValue(argRDFS) ;
        vocab = FileManager.get().loadModel(fn) ;
    }

    @Override
    protected void exec()
    {
        Sink<Quad> sink = new SinkQuadOutput(System.out) ;
        sink = InfFactory.infQuads(sink, vocab) ;
        
        List<String> files = getPositionalOrStdin() ;
        if ( files.isEmpty() )
            files.add("-") ;
            
        for ( String fn : files )
            processFile(fn, sink) ;
        IO.flush(System.out); 
    }

    private void processFile(String filename, Sink<Quad> sink)
    {
        Lang lang = filename.equals("-") ? Lang.NQUADS : Lang.guess(filename, Lang.NQUADS) ;
        String baseURI = IRILib.filenameToIRI(filename) ;
        
        if ( lang.isTriples() )
        {
            InputStream in = IO.openFile(filename) ;
            Sink<Triple> sink2 = new SinkExtendTriplesToQuads(sink) ;
            LangRIOT parser = RiotReader.createParserTriples(in, lang, baseURI, sink2) ;
            parser.parse() ;
            return ;
        }
        else
        {
            InputStream in = IO.openFile(filename) ;
            LangRIOT parser = RiotReader.createParserQuads(in, lang, baseURI, sink) ; 
            parser.parse() ;
        }        
    }

    @Override
    protected String getCommandName()
    {
        return "infer" ;
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
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