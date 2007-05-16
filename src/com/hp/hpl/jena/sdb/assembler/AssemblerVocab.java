/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.assembler;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.util.Vocab;

public class AssemblerVocab
{
    // Vocabulary later
    // Was: "http://jena.hpl.hp.com/2006/01/acmd#"
    private static final String NS = SDB.namespace ;
    
    public static String getURI() { return NS ; } 
    
    public static final Resource QueryAssemblerType             = Vocab.type(NS, "Query") ;
    public static final Resource SDBConnectionAssemblerType     = Vocab.type(NS, "SDBConnection") ;
    public static final Resource StoreAssemblerType             = Vocab.type(NS, "Store") ;
    
    // ---- Commands
    public static final Property pSteps         = Vocab.property(NS, "steps") ;
    
    public static final Property pMain          = Vocab.property(NS, "main") ;
    public static final Property pClassname     = Vocab.property(NS, "classname") ;

    public static final Property pArgs          = Vocab.property(NS, "args") ;
    public static final Property pArgName       = Vocab.property(NS, "name") ;
    public static final Property pArgValue      = Vocab.property(NS, "value") ;

    // Other
    public static final Property pQuery              = Vocab.property(NS, "query") ;
    public static final Property pQueryFile          = Vocab.property(NS, "queryFile") ;
    public static final Property pQueryString        = Vocab.property(NS, "queryString") ;

    public static final Property pDataset            = Vocab.property(NS, "dataset") ;
    public static final Property pGraphData          = Vocab.property(NS, "graph") ;
    public static final Property pNamedGraphData     = Vocab.property(NS, "namedGraph") ;
    
    public static final Property pOutputFormat       = Vocab.property(NS, "outputFormat") ;
    //public static final Property pCustomizerClass    = Vocab.property(NS, "customizer") ;

    // renaming: sdbFoo => foo ?/
    
    // ---- Connection
    public static final Property pDriver             = Vocab.property(NS, "driver") ;
    public static final Property pSDBtype            = Vocab.property(NS, "sdbType") ;
    public static final Property pSDBhost            = Vocab.property(NS, "sdbHost") ;
    public static final Property pSDBargStr          = Vocab.property(NS, "sdbArgStr") ;
    public static final Property pSDBname            = Vocab.property(NS, "sdbName") ;
    public static final Property pSDBschema          = Vocab.property(NS, "sdbSchema") ;
    // ModeRDB graph name - on the connection
    public static final Property pRDBtype           = Vocab.property(NS, "rdbType") ;
    
    // The JDBC URL written out long form.  Overrides the above.
    public static final Property pJDBC               = Vocab.property(NS, "jdbcURL") ;
    public static final Property pSDBuser            = Vocab.property(NS, "sdbUser") ;
    public static final Property pSDBpassword        = Vocab.property(NS, "sdbPassword") ;
    
    // ---- Store (uses pSDBtype) 
    public static final Property pLayout             = Vocab.property(NS, "layout") ;
    public static final Property pConnection         = Vocab.property(NS, "connection") ;
    
    public final static Property featureProperty      = Vocab.property(NS, "feature") ;
    public final static Property featureNameProperty  = Vocab.property(NS, "name") ;
    public final static Property featureValueProperty = Vocab.property(NS, "value") ;

    
    // MySQL engine type
    public static final Property pMySQLEngine       = Vocab.property(NS, "engine") ;
    // ModeRDB graph name - on the layout
    public static final Property pModelRDBname      = Vocab.property(NS, "rdbModelName") ;
    
    private static boolean initialized = false ; 
    
    static { init() ; } 
    
    static public void init()
    {
        if ( initialized )
            return ;
        // Wire in the extension assemblers (extensions relative to the Jena assembler framework)
        //assemblerClass(CommandAssemblerType,          new CommandAssembler()) ;
        assemblerClass(QueryAssemblerType,            new QueryAssembler()) ;
        assemblerClass(SDBConnectionAssemblerType,    new SDBConnectionDescAssembler()) ;
        assemblerClass(StoreAssemblerType,            new StoreDescAssembler()) ;
        initialized = true ;
    }
    
    private static void assemblerClass(Resource r, Assembler a)
    {
        Assembler.general.implementWith(r, a) ;
        //**assemblerAssertions.add(r, RDFS.subClassOf, JA.Object) ;
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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