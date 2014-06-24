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

package com.hp.hpl.jena.assembler;

import java.util.*;

import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
    A RuleSet wraps a list of rules.
 */
public class RuleSet
    {
    private static final List<Rule> emptyRules = Collections.emptyList();
    
    public static final RuleSet empty = create( emptyRules );
    
    public static RuleSet create( List<Rule> rules )
        { return new RuleSet( rules ); }

    public static RuleSet create( String ruleString )
        { return create( Rule.parseRules( ruleString ) ); }
    
    private final List<Rule> rules;
    
    protected RuleSet( List<Rule> rules )
        { this.rules = new ArrayList<>( rules ); }

    public List<Rule> getRules()
        { return rules; }
    
    @Override public int hashCode()
        { return rules.hashCode(); }
    
    @Override public boolean equals( Object other )
        { return other instanceof RuleSet && rules.equals( ((RuleSet) other).rules ); }

    }
