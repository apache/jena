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

package arq;

import arq.cmdline.CmdARQ ;
import arq.cmdline.ModEngine ;
import org.apache.jena.arq.junit.TextTestRunner;
import org.apache.jena.arq.junit.sparql.SparqlTests;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.TerminationException;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.rdf.model.Literal ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.junit.EarlReport ;
import org.apache.jena.sparql.util.NodeFactoryExtra ;
import org.apache.jena.sparql.vocabulary.DOAP ;
import org.apache.jena.sparql.vocabulary.FOAF ;
import org.apache.jena.sparql.vocabulary.TestManifest ;
import org.apache.jena.vocabulary.DC ;
import org.apache.jena.vocabulary.DCTerms ;
import org.apache.jena.vocabulary.RDF ;
import org.apache.jena.vocabulary.XSD ;

/** A program to execute query test suites
 *
 * <pre>
 * Usage:
 *   [--all]
 *   [--dawg]
 *   <i>testManifest</i>
 *   [ --query <i>query</i> --data <i>data</i> --result <i>result</i> ] -- run one test
 * </pre>
 */

public class qtest extends CmdARQ
{
    protected ArgDecl earlDecl =   new ArgDecl(ArgDecl.NoValue, "earl") ;

    protected ModEngine modEngine = null ;

    protected String testfile = null;
    protected boolean createEarlReport = false;

    public static void main(String... argv)
    {
        try {
            new qtest(argv).mainRun() ;
        }
        catch (TerminationException ex) { System.exit(ex.getCode()) ; }
    }

    public qtest(String[] argv)
    {
        super(argv) ;

        modEngine = setModEngine() ;
        addModule(modEngine) ;
        getUsage().addUsage("<manifest>", "run the tests specified in the given manifest") ;
        add(earlDecl, "--earl", "create EARL report") ;
    }

    protected ModEngine setModEngine()
    {
        return new ModEngine() ;
    }

    @Override
    protected String getCommandName() { return Lib.className(this) ; }

    @Override
    protected String getSummary() { return getCommandName()+" [--earl] <manifest>" ; }

    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        if ( ! hasPositional() )
            throw new CmdException("No manifest file") ;
        createEarlReport = super.contains(earlDecl);
        testfile = getPositionalArg(0) ;
    }

    @Override
    protected void exec()
    {
        if ( cmdStrictMode )
            ARQ.setStrictMode() ;

        for ( String manifest : super.getPositional() ) {
            if ( createEarlReport )
                oneManifestEarl(testfile) ;
            else
                oneManifest(testfile) ;
        }
    }

    static void oneManifest(String testManifest)
    {
        TextTestRunner.runOne(testManifest, SparqlTests::makeSPARQLTest) ;
    }

    static void oneManifestEarl(String testManifest)
    {
        String name =  "ARQ" ;
        String releaseName =  "ARQ" ;
        String version = "3.16.0" ;
        String homepage = "http://jena.apache.org/" ;
        String systemURI = "http://jena.apache.org/#arq" ;  // Null for bNode.

        // Include information later.
        EarlReport report = new EarlReport(systemURI, name, version, homepage) ;
        
        TextTestRunner.runOne(report, testManifest, SparqlTests::makeSPARQLTest);

        Model model = report.getModel() ;
        model.setNsPrefix("dawg", TestManifest.getURI()) ;

        // Update the EARL report.
        Resource jena = model.createResource()
                    .addProperty(FOAF.homepage, model.createResource("http://jena.apache.org/")) ;

        // ARQ is part fo Jena.
        Resource arq = report.getSystem()
                        .addProperty(DCTerms.isPartOf, jena) ;

        // Andy wrote the test software (updates the thing being tested as well as they are the same).
        Resource who = model.createResource(FOAF.Person)
                                .addProperty(FOAF.name, "Andy Seaborne")
                                .addProperty(FOAF.homepage,
                                             model.createResource("http://people.apache.org/~andy")) ;

        Resource reporter = report.getReporter() ;
        reporter.addProperty(DC.creator, who) ;

        model.setNsPrefix("doap", DOAP.getURI()) ;
        model.setNsPrefix("xsd", XSD.getURI()) ;

        // DAWG specific stuff.
        Resource system = report.getSystem() ;
        system.addProperty(RDF.type, DOAP.Project) ;
        system.addProperty(DOAP.name, name) ;
        system.addProperty(DOAP.homepage, homepage) ;
        system.addProperty(DOAP.maintainer, who) ;

        Resource release = model.createResource(DOAP.Version) ;
        system.addProperty(DOAP.release, release) ;

        Node today_node = NodeFactoryExtra.todayAsDate() ;
        Literal today = model.createTypedLiteral(today_node.getLiteralLexicalForm(), today_node.getLiteralDatatype()) ;
        release.addProperty(DOAP.created, today) ;
        release.addProperty(DOAP.name, releaseName) ;      // Again

        RDFDataMgr.write(System.out, model, Lang.TTL);
    }
 }
