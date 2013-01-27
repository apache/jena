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

import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.graph.*;

/**
 * Variant of the normal TriplePattern object which can be updated in place
 * to avoid store turn over. This is specific to the LP system.
 */
public class MutableTriplePattern extends TriplePattern {

    /**
     * Constructor.
     */
    public MutableTriplePattern() {
        super(null, null, null);
    }
    
    /**
     * Set the subject,predicate, object components of the pattern.
     */
    public void setPattern(Node subject, Node predicate, Node object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }
    
}
