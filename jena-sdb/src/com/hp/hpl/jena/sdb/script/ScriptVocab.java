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

package com.hp.hpl.jena.sdb.script;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sdb.assembler.AssemblerVocab;

public class ScriptVocab
{
    // A command is ...
    // a dataset + a query + an output format
    static final String NS = AssemblerVocab.getURI() ;
    // Types 
    public static final Resource CommandLineType                = type(NS, "Cmd") ;
    public static final Resource ScriptType                     = type(NS, "Script") ;
    public static final Resource DatasetAssemblerType           = type(NS, "Dataset") ;

    //public static final Resource CommandAssemblerType           = type(NS, "Command") ;

    private static boolean initialized = false ; 
    static { init() ; }
    
    public static void init()
    {
        if ( initialized )
            return ;
        assemblerClass(CommandLineType,               new CmdDescAssembler()) ;
        assemblerClass(ScriptType,                    new ScriptAssembler()) ;
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
