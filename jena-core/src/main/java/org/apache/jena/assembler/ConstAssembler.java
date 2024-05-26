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

package org.apache.jena.assembler;

import org.apache.jena.assembler.assemblers.* ;

/*
   Assembler initialization - so it works when inside class initialization processes.
   Assembler is an interface and constants are statics (implicitly or explicitly final)
   which makes controlling order tricky.

   Solution: initialization code (ie. inside jenaSystem.init) must use teh methods here,
   and not Assembler.constant.

   Use ConstAssembler.general(), not Assembler.general.
*/
public class ConstAssembler
{
    private static Assembler defaultModel = null ;
	public static Assembler defaultModel() {
		if ( defaultModel == null )
			defaultModel = new DefaultModelAssembler();
		return defaultModel ;
	}

    private static Assembler memoryModel = null ;
	public static Assembler memoryModel() {
		if ( memoryModel == null )
			memoryModel = new MemoryModelAssembler();
		return memoryModel ;
	}

    private static Assembler infModel = null ;
	public static Assembler infModel() {
		if ( infModel == null )
			infModel = new InfModelAssembler();
		return infModel ;
	}

    private static Assembler ontModel = null ;
	public static Assembler ontModel() {
		if ( ontModel == null )
			ontModel = new OntModelAssembler();
		return ontModel ;
	}

    private static Assembler reasonerFactory = null ;
	public static Assembler reasonerFactory() {
		if ( reasonerFactory == null )
			reasonerFactory = new ReasonerFactoryAssembler();
		return reasonerFactory ;
	}

    private static Assembler content = null ;
	public static Assembler content() {
		if ( content == null )
			content = new ContentAssembler();
		return content ;
	}

    private static Assembler prefixMapping = null ;
	public static Assembler prefixMapping() {
		if ( prefixMapping == null )
			prefixMapping = new PrefixMappingAssembler();
		return prefixMapping ;
	}

    private static Assembler unionModel = null ;
	public static Assembler unionModel() {
		if ( unionModel == null )
			unionModel = new UnionModelAssembler();
		return unionModel ;
	}

    private static Assembler ontModelSpec = null ;
	public static Assembler ontModelSpec() {
		if ( ontModelSpec == null )
			ontModelSpec = new OntModelSpecAssembler();
		return ontModelSpec ;
	}

    private static Assembler ruleSet = null ;
	public static Assembler ruleSet() {
		if ( ruleSet == null )
			ruleSet = new RuleSetAssembler();
		return ruleSet ;
	}

    private static Assembler documentManager = null ;
	public static Assembler documentManager() {
		if ( documentManager == null )
			documentManager = new DocumentManagerAssembler();
		return documentManager ;
	}

    private static AssemblerGroup general = null ;
    public  static AssemblerGroup general() {
        if ( general == null ) {
            general =  AssemblerGroup.create()
                .implementWith( JA.DefaultModel, defaultModel() )
                .implementWith( JA.MemoryModel, memoryModel() )
                .implementWith( JA.InfModel, infModel() )
                .implementWith( JA.ReasonerFactory, reasonerFactory() )
                .implementWith( JA.Content, content() )
                .implementWith( JA.ContentItem, content() )
                .implementWith( JA.UnionModel, unionModel() )
                .implementWith( JA.PrefixMapping, prefixMapping() )
                .implementWith( JA.SinglePrefixMapping, prefixMapping() )
                .implementWith( JA.OntModel, ontModel() )
                .implementWith( JA.OntModelSpec, ontModelSpec() )
                .implementWith( JA.RuleSet, ruleSet() )
                .implementWith( JA.DocumentManager, documentManager() )
                ;
        }
        return general ;
    }
}
