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

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

public class RuleSetAssembler extends AssemblerBase implements Assembler
    {
    @Override public Object open( Assembler a, Resource root, Mode irrelevant )
        { 
        checkType( root, JA.RuleSet );
        return createRuleSet( a, root ); 
        }

    public static RuleSet createRuleSet( Assembler a, Resource root )
        { return RuleSet.create( addRules( new ArrayList<Rule>(), a, root ) ); }

    public static List<Rule> addRules( List<Rule> result, Assembler a, Resource root )
        {
        addLiteralRules( root, result );
        addIndirectRules( a, root, result );
        addExternalRules( root, result );
        return result;
        }

    static private void addIndirectRules( Assembler a, Resource root, List<Rule> result )
        {
        StmtIterator it = root.listProperties( JA.rules );
        while (it.hasNext()) 
            {
            Resource r = getResource( it.nextStatement() );
            result.addAll( ((RuleSet) a.open( r )).getRules() );
            }
        }

    static private void addExternalRules( Resource root, List<Rule> result )
        {
        StmtIterator it = root.listProperties( JA.rulesFrom );
        while (it.hasNext())
            {
            Resource s = getResource( it.nextStatement() );
            result.addAll( Rule.rulesFromURL( s.getURI() ) );
            }
        }

    static private void addLiteralRules( Resource root, List<Rule> result )
        {
        StmtIterator it = root.listProperties( JA.rule );
        while (it.hasNext())
            {
            String s = getString( it.nextStatement() );
            result.addAll( Rule.parseRules( s ) );
            }
        }
    }
