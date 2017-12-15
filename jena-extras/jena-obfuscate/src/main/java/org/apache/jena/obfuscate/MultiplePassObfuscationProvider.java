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
package org.apache.jena.obfuscate;

import org.apache.jena.graph.Node;

/**
 * A decorator around another obfuscator that applies it multiple times for
 * additional obfuscation
 */
public class MultiplePassObfuscationProvider implements ObfuscationProvider {

    private final ObfuscationProvider obfuscator;
    private final int passes;

    public MultiplePassObfuscationProvider(ObfuscationProvider provider, int passes) {
        if (provider == null)
            throw new NullPointerException("Obfuscation Provider cannot be null");
        if (passes < 2)
            throw new IllegalArgumentException("Must specify passes >= 2");
        this.obfuscator = provider;
        this.passes = passes;
    }

    @Override
    public Node obfuscateNode(Node n) {
        Node obfuscated = n;
        for (int i = 1; i <= this.passes; i++) {
            obfuscated = this.obfuscator.obfuscateNode(obfuscated);
        }
        return obfuscated;
    }

}
