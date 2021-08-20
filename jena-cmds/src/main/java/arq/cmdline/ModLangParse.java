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

package arq.cmdline;

import org.apache.jena.cmd.*;
import org.apache.jena.irix.IRIException;
import org.apache.jena.irix.IRIs;
import org.apache.jena.irix.IRIx;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;

public class ModLangParse extends ModBase
{
    private ArgDecl argCheck    = new ArgDecl(ArgDecl.NoValue, "check");
    private ArgDecl argNoCheck  = new ArgDecl(ArgDecl.NoValue, "nocheck", "noCheck");
    private ArgDecl argSink     = new ArgDecl(ArgDecl.NoValue, "sink", "null");
    private ArgDecl argCount    = new ArgDecl(ArgDecl.NoValue, "count");

    private ArgDecl argStrict   = new ArgDecl(ArgDecl.NoValue, "strict");
    private ArgDecl argValidate = new ArgDecl(ArgDecl.NoValue, "validate");

    private ArgDecl argStop     = new ArgDecl(ArgDecl.NoValue, "stopOnError", "stoponerror", "stop");
    private ArgDecl argStopWarn = new ArgDecl(ArgDecl.NoValue, "stopOnWarning", "stoponwarning", "stop-warnings");

    private ArgDecl argBase     = new ArgDecl(ArgDecl.HasValue, "base");

    private ArgDecl argRDFS     = new ArgDecl(ArgDecl.HasValue, "rdfs");

    private ArgDecl argSyntax   = new ArgDecl(ArgDecl.HasValue, "syntax");

    private String rdfsVocabFilename    = null;
    private Model  rdfsVocab            = null;
    private String baseIRI              = null;
    private boolean explicitCheck       = false;
    private boolean explicitNoCheck     = false;

    private boolean stopOnBadTerm       = false;   // Checking error
    private boolean stopOnWarnings      = false;   // Checking warning

    private boolean bitbucket           = false;
    private boolean strict              = false;
    private boolean validate            = false;
    private boolean outputCount         = false;
    private Lang lang                   = null;

    @Override
    public void registerWith(CmdGeneral cmdLine) {
        cmdLine.getUsage().startCategory("Parser control");
        cmdLine.add(argSink,    "--sink",           "Parse but throw away output");
        cmdLine.add(argSyntax,  "--syntax=NAME",    "Set syntax (otherwise syntax guessed from file extension)");
        cmdLine.add(argBase,    "--base=URI",       "Set the base URI (does not apply to N-triples and N-Quads)");
        cmdLine.add(argCheck,   "--check",          "Additional checking of RDF terms");
        cmdLine.add(argStrict,  "--strict",         "Run with in strict mode");
        cmdLine.add(argValidate,"--validate",       "Same as --sink --check --strict");
        cmdLine.add(argCount,   "--count",          "Count triples/quads parsed, not output them");
        cmdLine.add(argRDFS,    "--rdfs=file",      "Apply some RDFS inference using the vocabulary in the file");

        cmdLine.add(argNoCheck, "--nocheck",        "Turn off checking of RDF terms");

//        cmdLine.add(argStop,    "--stop",           "Stop parsing on encountering a bad RDF term");
//        cmdLine.add(argStopWarn,"--stop-warnings",  "Stop parsing on encountering a warning");
    }

    @Override
    public void processArgs(CmdArgModule cmdLine) {
        if ( cmdLine.contains(argValidate) ) {
            validate = true;
            strict = true;
            explicitCheck = true;
            bitbucket = true;
        }

        if ( cmdLine.contains(argSyntax) ) {
            String syntax = cmdLine.getValue(argSyntax);
            Lang lang$ = RDFLanguages.nameToLang(syntax);
            if ( lang$ == null )
                throw new CmdException("Can not detemine the syntax from '" + syntax + "'");
            this.lang = lang$;
        }

        if ( cmdLine.contains(argCheck) )
            explicitCheck = true;

        if ( cmdLine.contains(argNoCheck) )
            explicitNoCheck = true;

        if ( cmdLine.contains(argStrict) )
            strict = true;

        if ( cmdLine.contains(argBase) ) {
            baseIRI = cmdLine.getValue(argBase);
            try {
                IRIx iri = IRIs.reference(baseIRI);
                if ( !iri.isAbsolute() )
                    throw new CmdException("Base IRI not suitable for use as a base for RDF: " + baseIRI);
            } catch (IRIException ex) {
                throw new CmdException("Bad base IRI: " + baseIRI);
            }
        }

        if ( cmdLine.contains(argStop) )
            stopOnBadTerm = true;

        if ( cmdLine.contains(argStopWarn) )
            stopOnWarnings = true;

        if ( cmdLine.contains(argSink) )
            bitbucket = true;

        if ( cmdLine.contains(argCount) ) {
            bitbucket = true;
            outputCount = true;
        }

        if ( cmdLine.contains(argRDFS) ) {
            try {
                rdfsVocabFilename = cmdLine.getArg(argRDFS).getValue();
                rdfsVocab = RDFDataMgr.loadModel(rdfsVocabFilename);
            } catch (RiotException ex) {
                throw new CmdException("Error in RDFS vocabulary: " + rdfsVocabFilename);
            } catch (Exception ex) {
                throw new CmdException("Error: " + ex.getMessage());
            }
        }
    }

    public boolean explicitChecking() {
        return explicitCheck;
    }

    public boolean explicitNoChecking() {
        return explicitNoCheck;
    }

    public boolean strictMode() {
        return strict;
    }

    public boolean validate() {
        return validate;
    }

    public boolean outputCount() {
        return outputCount;
    }

    public boolean stopOnBadTerm() {
        return stopOnBadTerm;
    }

    public boolean stopOnWarnings() {
        return stopOnWarnings;
    }

    public boolean toBitBucket() {
        return bitbucket;
    }

    public String getBaseIRI() {
        return baseIRI;
    }

    public Model getRDFSVocab() {
        return rdfsVocab;
    }

    public Lang getLang() {
        return lang;
    }
}
