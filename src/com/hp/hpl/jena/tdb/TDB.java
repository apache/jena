/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP}
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb;

import static lib.Log.log;

import com.hp.hpl.jena.rdf.model.impl.RDFReaderFImpl;
import com.hp.hpl.jena.tdb.pgraph.assembler.PGraphAssemblerVocab;
import com.hp.hpl.jena.tdb.solver.StageGeneratorPGraphBGP;
import com.hp.hpl.jena.tdb.solver.StageGeneratorPGraphSimple;

import com.hp.hpl.jena.sparql.engine.main.StageGenerator;

import com.hp.hpl.jena.query.ARQ;


public class TDB
{
    public static String namespace = "http://jena.hpl.hp.com/2008/tdb#" ;
    
//    public static void panic(Class<?> clazz, String string)
//    {
//        org.slf4j.LoggerFactory.getLogger(clazz).error(string) ;
//        throw new TDBException(string) ;
//    }
    
    // Called on assembler loading.
    public static void init() { }
    
    static { initWorker() ; }
    
    private static boolean initialized = false ;
    private static synchronized void initWorker()
    {
        if ( initialized )
            return ;
        initialized = true ;
        
        PGraphAssemblerVocab.init();
        
        // Globally change the stage generator
        if ( true )
        {
            StageGenerator orig = (StageGenerator)ARQ.getContext().get(ARQ.stageGenerator) ;
            StageGenerator stageGenerator = null ;
            if ( false )
            {
                log(TDB.class).warn("Using find-based solver") ;
                stageGenerator = new StageGeneratorPGraphSimple(orig) ;
                ARQ.getContext().set(ARQ.stageGenerator, stageGenerator) ;
            }
            else
                stageGenerator = new StageGeneratorPGraphBGP(orig) ;
            
            ARQ.getContext().set(ARQ.stageGenerator, stageGenerator) ;
        }
        
        // Override N-TRIPLES
        String bulkLoaderClass = "com.hp.hpl.jena.tdb.base.loader.BulkReader" ;
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLES", bulkLoaderClass) ;
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLE", bulkLoaderClass) ;
    }
    
    /** The root package name for SDB */   
    public static final String PATH = "com.hp.hpl.jena.tdb";
   
    /** The product name */   
    public static final String NAME = "TDB";
   
    /** The TDB web site : see also http://jena.sourceforge.net*/   
    public static final String WEBSITE = "http://jena.hpl.hp.com/wiki/TDB";
   
    /** The full name of the current ARQ version */   
    public static final String VERSION = "@version@";
   
    /** The major version number for this release of SDB (ie '2' for SDB 2.0) */
    public static final String MAJOR_VERSION = "@version-major@";
   
    /** The minor version number for this release of SDB (ie '0' for SDB 2.0) */
    public static final String MINOR_VERSION = "@version-minor@";
   
    /** The version status for this release of SDB (eg '-beta1' or the empty string) */
    public static final String VERSION_STATUS = "@version-status@";
   
    /** The date and time at which this release was built */   
    public static final String BUILD_DATE = "@build-time@";
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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