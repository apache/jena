/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import org.openjena.riot.system.IRIResolver ;
import arq.cmd.CmdException ;

import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.util.FileManager ;

public class ModLangParse implements ArgModuleGeneral
{
    private ArgDecl argCheck    = new ArgDecl(ArgDecl.HasValue, "check") ;
    private ArgDecl argNoCheck  = new ArgDecl(ArgDecl.NoValue, "nocheck", "noCheck") ;
    private ArgDecl argSink     = new ArgDecl(ArgDecl.NoValue, "sink", "null") ;

    private ArgDecl argStrict   = new ArgDecl(ArgDecl.NoValue, "strict") ;
    private ArgDecl argValidate = new ArgDecl(ArgDecl.NoValue, "validate") ;
    
    private ArgDecl argSkip     = new ArgDecl(ArgDecl.NoValue, "skip") ;
    private ArgDecl argNoSkip   = new ArgDecl(ArgDecl.NoValue, "noSkip") ;
    private ArgDecl argStop     = new ArgDecl(ArgDecl.NoValue, "stopOnError", "stoponerror", "stop") ;
    
    private ArgDecl argBase     = new ArgDecl(ArgDecl.HasValue, "base") ;
    
    private ArgDecl argRDFS     = new ArgDecl(ArgDecl.HasValue, "rdfs") ;

    private  String rdfsVocabFilename   = null ;
    private  Model  rdfsVocab           = null ;
    private  String baseIRI             = null ;
    private boolean explicitCheck       = false ;
    private boolean explicitNoCheck     = false ;
    private boolean skipOnBadTerm       = false ;
    private boolean stopOnBadTerm       = false ;
    private boolean bitbucket           = false ; 
    private boolean strict              = false ;
    private boolean validate            = false ;
    
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("Parser control") ;
        cmdLine.add(argSink,    "--sink",           "Parse but throw away output") ;
        cmdLine.add(argBase,    "--base=URI",       "Set the base URI (does not apply to N-triples and N-Quads)") ;
        cmdLine.add(argCheck,   "--check=boolean",  "Addition checking of RDF terms") ;// (default: off for N-triples, N-Quads, on for Turtle and TriG)") ;
        cmdLine.add(argStrict,  "--strict",         "Run with in strict mode") ;
        cmdLine.add(argValidate,"--validate",       "Same as --sink --check=true --strict") ;
//        cmdLine.add(argRDFS,    "--rdfs=file",      "Apply some RDFS inference using the vocabulary in the file") ;
        
        cmdLine.add(argNoCheck, "--nocheck",        "Turn off checking of RDF terms") ;
//        cmdLine.add(argSkip,    "--skip",           "Skip (do not output) triples failing the RDF term tests") ;
//        cmdLine.add(argNoSkip,  "--noSkip",         "Include triples failing the RDF term tests (not recommended)") ;
//        cmdLine.add(argStop,    "--stop",           "Stop parsing on encountering a bad RDF term") ;
    }

    public void processArgs(CmdArgModule cmdLine)
    {
        if ( cmdLine.contains(argValidate) )
        {
            validate = true ;
            strict = true ;
            explicitCheck = true ;
            bitbucket = true ;
        }
        
        if ( cmdLine.contains(argCheck) )
        {
            boolean b = ! cmdLine.getArg(argCheck).getValue().equalsIgnoreCase("false") ;
            explicitCheck = b ;
            explicitNoCheck = !b ;
        }
        
        if ( cmdLine.contains(argNoCheck) )
            explicitNoCheck = true ;
        
        if ( cmdLine.contains(argStrict) )
            strict = true ;

        if ( cmdLine.contains(argSkip) )
            skipOnBadTerm = true ; 
        if ( cmdLine.contains(argNoSkip) )
            skipOnBadTerm = false ;
        
        if ( cmdLine.contains(argBase) )
        {
            baseIRI = cmdLine.getValue(argBase) ;
            IRI iri = IRIResolver.get().resolve(baseIRI) ;
            if ( iri.hasViolation(false) )
                throw new CmdException("Bad base IRI: "+baseIRI) ;
            if ( ! iri.isAbsolute() )
                throw new CmdException("Base IRI must be an absolute IRI: "+baseIRI) ;
        }
        
        if ( cmdLine.contains(argStop) )
            stopOnBadTerm = true ;
        
        if ( cmdLine.contains(argSink) )
            bitbucket = true ;
        
        if ( cmdLine.contains(argRDFS) )
        {
            rdfsVocabFilename = cmdLine.getArg(argRDFS).getValue() ;
            rdfsVocab = FileManager.get().loadModel(rdfsVocabFilename) ;
        }
    }

    public boolean explicitChecking()
    {
        return explicitCheck ;
    }

    public boolean explicitNoChecking()
    {
        return explicitNoCheck ;
    }

    public boolean strictMode()
    {
        return strict ;
    }

    public boolean validate()
    {
        return validate ;
    }

    public boolean skipOnBadTerm()
    {
        return skipOnBadTerm ;
    }

    public boolean stopOnBadTerm()
    {
        return stopOnBadTerm ;
    }

    public boolean toBitBucket()
    {
        return bitbucket ;
    }

    public String getBaseIRI()
    {
        return baseIRI ;
    }

    public Model getRDFSVocab()     { return rdfsVocab ; } 
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