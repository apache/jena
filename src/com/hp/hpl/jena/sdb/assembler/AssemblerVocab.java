/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.assembler;

import static com.hp.hpl.jena.sdb.util.Vocab.property;
import static com.hp.hpl.jena.sdb.util.Vocab.type;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class AssemblerVocab
{
    // Vocabulary later
    private static final String NS = "http://jena.hpl.hp.com/2006/01/acmd#" ;
    public static String getURI() { return NS ; } 
    
    public static final Resource QueryAssemblerType             = type(NS, "Query") ;
    public static final Resource SDBConnectionAssemblerType     = type(NS, "SDBConnection") ;
    public static final Resource StoreAssemblerType             = type(NS, "Store") ;
    
    // ---- Commands
    public static final Property pSteps         = property(NS, "steps") ;
    
    public static final Property pMain          = property(NS, "main") ;
    public static final Property pClassname     = property(NS, "classname") ;

    public static final Property pArgs          = property(NS, "args") ;
    public static final Property pArgName       = property(NS, "name") ;
    public static final Property pArgValue      = property(NS, "value") ;

    // Other
    public static final Property pQuery              = property(NS, "query") ;
    public static final Property pQueryFile          = property(NS, "queryFile") ;
    public static final Property pQueryString        = property(NS, "queryString") ;

    public static final Property pDataset            = property(NS, "dataset") ;
    public static final Property pGraphData          = property(NS, "graph") ;
    public static final Property pNamedGraphData     = property(NS, "namedGraph") ;
    
    public static final Property pOutputFormat       = property(NS, "outputFormat") ;
    public static final Property pCustomizerClass    = property(NS, "customizer") ;

    // renaming: sdbFoo => foo ?/
    
    // ---- Connection
    public static final Property pDriver             = property(NS, "driver") ;
    public static final Property pSDBtype            = property(NS, "sdbType") ;
    public static final Property pSDBhost            = property(NS, "sdbHost") ;
    public static final Property pSDBargStr          = property(NS, "sdbArgStr") ;
    public static final Property pSDBname            = property(NS, "sdbName") ;
    public static final Property pSDBschema          = property(NS, "sdbSchema") ;
    // ModeRDB graph name - on the connection
    public static final Property pRDBtype           = property(NS, "rdbType") ;
    
    // The JDBC URL written out long form.  Overrides the above.
    public static final Property pJDBC               = property(NS, "jdbcURL") ;
    public static final Property pSDBuser            = property(NS, "sdbUser") ;
    public static final Property pSDBpassword        = property(NS, "sdbPassword") ;
    
    // ---- Store (uses pSDBtype) 
    public static final Property pLayout            = property(NS, "layout") ;
    public static final Property pConnection        = property(NS, "connection") ;
    
    // MySQL engine type
    public static final Property pMySQLEngine       = property(NS, "engine") ;
    // ModeRDB graph name - on the layout
    public static final Property pModelRDBname      = property(NS, "rdbModelName") ;
    
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
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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