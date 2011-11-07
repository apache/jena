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

package com.hp.hpl.jena.assembler.assemblers;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;

public class PrefixMappingAssembler extends AssemblerBase implements Assembler
    {
    @Override
    public Object open( Assembler a, Resource root, Mode irrelevant )
        {
        checkType( root, JA.PrefixMapping );
        return getPrefixes( a, root, PrefixMapping.Factory.create() ); 
        }

    public static PrefixMapping getPrefixes( Assembler a, Resource root, PrefixMapping result )
        {
        setSimplePrefixes( root, result );
        includePrefixesFor( a, root, result, JA.includes );
        includePrefixesFor( a, root, result, JA.prefixMapping );
        return result;
        }

    private static void includePrefixesFor( Assembler a, Resource root, PrefixMapping result, Property includeUsing )
        {
        for (StmtIterator it = root.listProperties( includeUsing ); it.hasNext();)
            {
            Statement s = it.nextStatement();
            PrefixMapping sub = (PrefixMapping) a.open( getResource( s ) );
            result.setNsPrefixes( sub );
            }
        }

    private static void setSimplePrefixes( Resource root, PrefixMapping result )
        {
        if (root.hasProperty( JA.prefix ))
            {
            Literal prefix = getUniqueLiteral( root, JA.prefix );
            Literal namespace = getUniqueLiteral( root, JA.namespace );
            result.setNsPrefix( prefix.getLexicalForm(), namespace.getLexicalForm() );
            }
        }
    }
