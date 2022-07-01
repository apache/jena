/*
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

package riotcmd;

import java.util.List ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdfs.RDFSFactory;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;

/*
 * Infer
 *   RDFS
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
    private Graph vocab ;

    public static void main(String... argv)
    {
        new infer(argv).mainRun() ;
    }

    protected infer(String[] argv)
    {
        super(argv) ;
        super.add(argRDFS) ;
    }

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
        vocab = RDFDataMgr.loadGraph(fn) ;
    }

    @Override
    protected void exec()
    {
        StreamRDF sink = StreamRDFLib.writer(System.out) ;
        sink = RDFSFactory.streamRDFS(sink, vocab) ;

        List<String> files = getPositionalOrStdin() ;
        if ( files.isEmpty() )
            files.add("-") ;

        for ( String fn : files )
            processFile(fn, sink) ;
        IO.flush(System.out);
    }

    private void processFile(String filename, StreamRDF sink)
    {
        Lang lang = filename.equals("-") ? RDFLanguages.NQUADS : RDFLanguages.filenameToLang(filename, RDFLanguages.NQUADS) ;

        if ( filename.equals("-") )
            RDFParser.source(System.in).lang(RDFLanguages.NQUADS).parse(sink);
        else
            RDFParser.source(filename).parse(sink);
    }

    @Override
    protected String getCommandName()
    {
        return "infer" ;
    }
}
