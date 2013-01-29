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

import com.hp.hpl.jena.reasoner.ReasonerException;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * Exception used to indicate syntactic errors the LP version of the
 * backward chaining rule engine.
 */
public class LPRuleSyntaxException extends ReasonerException {

    
    /**
     * Constructor.
     * @param msg a free-text message describing the problem
     */
    public LPRuleSyntaxException(String msg, Rule rule) {
        super("Syntax error in backward rule: " + rule.toShortString() 
                +"\n" + msg);
    }
    
    /**
     * Constructor.
     * @param msg a free-text message describing the problem
     * @param cause a nested exception which prompted this error
     */
    public LPRuleSyntaxException(String msg, Rule rule, Throwable cause) {
        super("Syntax error in backward rule: " + rule.toShortString() 
                +"\n" + msg, cause);
    }
}
