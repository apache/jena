/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.assembler;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class AssemblerVocab
{
    // Vocabulary later
    static final String NS = "http://jena.hpl.hp.com/2006/01/acmd#" ;
    // Types 
    public static final Resource CommandLineType                = type(NS, "Cmd") ;
    public static final Resource ScriptType                     = type(NS, "Script") ;
    
    //public static final Resource CommandAssemblerType           = type(NS, "Command") ;
    public static final Resource QueryAssemblerType             = type(NS, "Query") ;
    public static final Resource DatasetAssemblerType           = type(NS, "Dataset") ;
    public static final Resource SDBConnectionAssemblerType     = type(NS, "SDBConnection") ;
    public static final Resource StoreAssemblerType             = type(NS, "Store") ;
    
    // ---- Commands
    public static final Property pSteps         = ResourceFactory.createProperty(NS, "steps") ;
    
    public static final Property pMain          = ResourceFactory.createProperty(NS, "main") ;
    public static final Property pClassname     = ResourceFactory.createProperty(NS, "classname") ;

    public static final Property pArgs          = ResourceFactory.createProperty(NS, "args") ;
    public static final Property pArgName       = ResourceFactory.createProperty(NS, "name") ;
    public static final Property pArgValue      = ResourceFactory.createProperty(NS, "value") ;

    // Other
    public static final Property pQuery              = ResourceFactory.createProperty(NS, "query") ;
    public static final Property pQueryFile          = ResourceFactory.createProperty(NS, "queryFile") ;
    public static final Property pQueryString        = ResourceFactory.createProperty(NS, "queryString") ;

    public static final Property pDataset            = ResourceFactory.createProperty(NS, "dataset") ;
    public static final Property pGraphData          = ResourceFactory.createProperty(NS, "graph") ;
    public static final Property pNamedGraphData     = ResourceFactory.createProperty(NS, "namedGraph") ;
    
    public static final Property pOutputFormat       = ResourceFactory.createProperty(NS, "outputFormat") ;
    public static final Property pCustomizerClass    = ResourceFactory.createProperty(NS, "customizer") ;

    // renaming: sdbFoo => foo ?/
    
    // ---- Connection
    public static final Property pDriver             = ResourceFactory.createProperty(NS, "driver") ;
    public static final Property pSDBtype            = ResourceFactory.createProperty(NS, "sdbType") ;
    public static final Property pSDBhost            = ResourceFactory.createProperty(NS, "sdbHost") ;
    public static final Property pSDBargStr          = ResourceFactory.createProperty(NS, "sdbArgStr") ;
    public static final Property pSDBname            = ResourceFactory.createProperty(NS, "sdbName") ;
    public static final Property pSDBschema          = ResourceFactory.createProperty(NS, "sdbSchema") ;
    // ModeRDB graph name - on the connection
    public static final Property pRDBtype           = ResourceFactory.createProperty(NS, "rdbType") ;
    
    // The JDBC URL written out long form.  Overrides the above.
    public static final Property pJDBC               = ResourceFactory.createProperty(NS, "jdbcURL") ;
    public static final Property pSDBuser            = ResourceFactory.createProperty(NS, "sdbUser") ;
    public static final Property pSDBpassword        = ResourceFactory.createProperty(NS, "sdbPassword") ;
    
    // ---- Store (uses pSDBtype) 
    public static final Property pLayout            = ResourceFactory.createProperty(NS, "layout") ;
    public static final Property pConnection        = ResourceFactory.createProperty(NS, "connection") ;
    
    // MySQL engine type
    public static final Property pMySQLEngine       = ResourceFactory.createProperty(NS, "engine") ;
    // ModeRDB graph name - on the layout
    public static final Property pModelRDBname      = ResourceFactory.createProperty(NS, "rdbModelName") ;
    
    private static boolean initialized = false ; 
    
    static { init() ; } 
    
    static public void init()
    {
        if ( initialized )
            return ;
        // Wire in the extension assemblers (extensions relative to the Jena assembler framework)
        assemblerClass(CommandLineType,               new CmdDescAssembler()) ;
        assemblerClass(ScriptType,                    new ScriptAssembler()) ;
        //assemblerClass(CommandAssemblerType,          new CommandAssembler()) ;
        assemblerClass(QueryAssemblerType,            new QueryAssembler()) ;
        assemblerClass(DatasetAssemblerType,          new DatasetAssembler()) ;
        assemblerClass(SDBConnectionAssemblerType,    new SDBConnectionDescAssembler()) ;
        assemblerClass(StoreAssemblerType,            new StoreDescAssembler()) ;
        initialized = true ;
    }
    
    private static void assemblerClass(Resource r, Assembler a)
    {
        Assembler.general.implementWith(r, a) ;
        //**assemblerAssertions.add(r, RDFS.subClassOf, JA.Object) ;
    }
    
    private static Resource type(String namespace, String localName)
    { return ResourceFactory.createResource(namespace+localName) ; }

    private static Property property(String namespace, String localName)
    { return ResourceFactory.createProperty(namespace+localName) ; }
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