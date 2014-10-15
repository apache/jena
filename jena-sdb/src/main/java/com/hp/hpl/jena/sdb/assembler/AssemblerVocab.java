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

package com.hp.hpl.jena.sdb.assembler;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.assemblers.AssemblerGroup;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.util.Vocab;

public class AssemblerVocab
{
    private static final String NS = SDB.namespace ;
    
    public static String getURI() { return NS ; } 

    // Types
    public static final Resource SDBConnectionAssemblerType     = Vocab.type(NS, "SDBConnection") ;
    public static final Resource StoreAssemblerType             = Vocab.type(NS, "Store") ;
    public static final Resource DatasetAssemblerType           = Vocab.type(NS, "DatasetStore") ;
    public static final Resource ModelType                      = Vocab.type(NS, "Model") ;
    public static final Resource GraphType                      = Vocab.type(NS, "Graph") ;

    public static final Property pStore              = Vocab.property(NS, "store") ;
    public static final Property pDataset            = Vocab.property(NS, "dataset") ;
    public static final Property pGraphData          = Vocab.property(NS, "graph") ;
    public static final Property pNamedGraph1        = Vocab.property(NS, "graphName") ;
    public static final Property pNamedGraph2        = Vocab.property(NS, "namedGraph") ;

    // ---- Experimental
    
    // ---- Commands
    public static final Property pSteps         = Vocab.property(NS, "steps") ;
    
    public static final Property pMain          = Vocab.property(NS, "main") ;
    public static final Property pClassname     = Vocab.property(NS, "classname") ;

    public static final Property pArgs          = Vocab.property(NS, "args") ;
    public static final Property pArgName       = Vocab.property(NS, "name") ;
    public static final Property pArgValue      = Vocab.property(NS, "value") ;

    // Other
    public static final Resource QueryAssemblerType             = Vocab.type(NS, "Query") ;

    public static final Property pQuery              = Vocab.property(NS, "query") ;
    public static final Property pQueryFile          = Vocab.property(NS, "queryFile") ;
    public static final Property pQueryString        = Vocab.property(NS, "queryString") ;

    public static final Property pOutputFormat       = Vocab.property(NS, "outputFormat") ;
    //public static final Property pCustomizerClass    = Vocab.property(NS, "customizer") ;

    // renaming: sdbFoo => foo ?/
    
    // ---- Connection
    public static final Property pDriver             = Vocab.property(NS, "driver") ;
    public static final Property pSDBtype            = Vocab.property(NS, "sdbType") ;
    public static final Property pSDBhost            = Vocab.property(NS, "sdbHost") ;
    public static final Property pSDBname            = Vocab.property(NS, "sdbName") ;
    //public static final Property pSDBschema          = Vocab.property(NS, "sdbSchema") ;
    
    // The JDBC URL written out long form.  Overrides the above.
    public static final Property pJDBC               = Vocab.property(NS, "jdbcURL") ;
    public static final Property pPoolSize           = Vocab.property(NS, "poolSize") ;
    public static final Property pSDBuser            = Vocab.property(NS, "sdbUser") ;
    public static final Property pSDBpassword        = Vocab.property(NS, "sdbPassword") ;
    
    // ---- Store
    public static final Property pLayout             = Vocab.property(NS, "layout") ;
    public static final Property pConnection         = Vocab.property(NS, "connection") ;
    
    public final static Property featureProperty      = Vocab.property(NS, "feature") ;
    public final static Property featureNameProperty  = Vocab.property(NS, "name") ;
    public final static Property featureValueProperty = Vocab.property(NS, "value") ;

    
    // MySQL engine type
    public static final Property pMySQLEngine       = Vocab.property(NS, "engine") ;
    
    // SAP storage type
    public static final Property pStorageType       = Vocab.property(NS, "storage") ;
    
    private static boolean initialized = false ; 
    
    static { init() ; }
    
    static public void init()
    {
        if ( initialized )
            return ;
        register(Assembler.general) ;
        initialized = true ;
    }
    
    static public void register(AssemblerGroup g)
    {
        // Wire in the extension assemblers (extensions relative to the Jena assembler framework)
        //assemblerClass(CommandAssemblerType,          new CommandAssembler()) ;
        assemblerClass(g, QueryAssemblerType,            new QueryAssembler()) ;
        assemblerClass(g, SDBConnectionAssemblerType,    new SDBConnectionDescAssembler()) ;
        assemblerClass(g, StoreAssemblerType,            new StoreDescAssembler()) ;
        assemblerClass(g, DatasetAssemblerType,          new DatasetStoreAssembler()) ;
        assemblerClass(g, ModelType,                     new SDBModelAssembler()) ;
        assemblerClass(g, GraphType,                     new SDBModelAssembler()) ;
    }
    
    private static void assemblerClass(AssemblerGroup g, Resource r, Assembler a)
    {
        if ( g == null )
            g = Assembler.general ;
        g.implementWith(r, a) ;
    }
}
