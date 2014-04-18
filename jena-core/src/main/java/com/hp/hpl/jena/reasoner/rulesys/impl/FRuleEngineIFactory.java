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

package com.hp.hpl.jena.reasoner.rulesys.impl;

import java.util.List;

import com.hp.hpl.jena.reasoner.rulesys.ForwardRuleInfGraphI;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * Factory class for creating {@link FRuleEngineI}. This class is a singleton pattern, the single global 
 * instance could be replaced to provide a custom implementation. 
 * <pre>
 * {@code
 * MyFRuleEngineIFactory anotherFactory  = new MyFRuleEngineIFactory();
 * FRuleEngineIFactory.setInstance(anotherFactory);
* }
* </pre>
 */
public class FRuleEngineIFactory {
    private static FRuleEngineIFactory instance = new FRuleEngineIFactory();

    /**
     * Return the single global instance of this factory
     */
    public static FRuleEngineIFactory getInstance() { return instance; }
    
    /**
     * Replaces the custom global instance.
     * @param instance the new factory instance
     */
    public static void setInstance(FRuleEngineIFactory instance) { FRuleEngineIFactory.instance = instance; }
    
    /**
     * Creates a {@link ForwardRuleInfGraphI} instance. 
     * 
     * @param parent the F or FB infGraph that it using the engine, the parent graph
     * holds the deductions graph and source data.
     * @param rules the rule set to be processed
     * @param useRETE if <code>true</code> force this factory to create a {@link RETEEngine} otherwise a 
     * {@link FRuleEngine} is created.
     * @return the created engine.
     */
    public FRuleEngineI createFRuleEngineI(ForwardRuleInfGraphI parent, List<Rule> rules, boolean useRETE) {
        FRuleEngineI engine;
        if (rules != null) {
            if (useRETE) {
                engine = new RETEEngine(parent, rules);
            } else {
                engine = new FRuleEngine(parent, rules);
            }
        } else {
            if (useRETE) {
                engine = new RETEEngine(parent);
            } else {
                engine = new FRuleEngine(parent);
            }
        }
        return engine;
    }
}
