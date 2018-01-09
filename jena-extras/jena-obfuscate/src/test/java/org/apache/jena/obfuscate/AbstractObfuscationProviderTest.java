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

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.obfuscate.config.ValuesOnlyConfig;
import org.junit.Assert;
import org.junit.Test;

/**
 * Abstract tests for obfuscation providers
 */
public abstract class AbstractObfuscationProviderTest {

    /**
     * Creates an obfuscation provider to test
     * 
     * @return Provider
     */
    protected abstract ObfuscationProvider createInstance();

    @Test
    public void determinstic_01() {
        Node input = NodeFactory.createURI("http://example.org");
        ObfuscationProvider provider = createInstance();

        Node output1 = provider.obfuscateNode(input);
        Node output2 = provider.obfuscateNode(input);

        Assert.assertNotEquals(input, output1);
        Assert.assertNotEquals(input, output2);

        Assert.assertEquals(output1, output2);
    }

    @Test
    public void repetition_01() {
        Node input = NodeFactory.createURI("http://example.org");
        ObfuscationProvider provider = createInstance();

        // Applying obfuscation multiple times should obfuscate further
        Node output1 = provider.obfuscateNode(input);
        Node output2 = provider.obfuscateNode(output1);

        Assert.assertNotEquals(input, output1);
        Assert.assertNotEquals(input, output2);

        Assert.assertNotEquals(output1, output2);
    }

    @Test
    public void repetition_02() {
        Node input = NodeFactory.createURI("http://example.org");
        ObfuscationProvider provider = createInstance();

        // Applying obfuscation multiple times should obfuscate further
        // providing unique output each time
        Set<Node> outputs = new HashSet<>();
        for (int i = 2; i <= 10; i++) {
            ObfuscationProvider multiPass = new MultiplePassObfuscationProvider(provider, i);
            Node output = multiPass.obfuscateNode(input);

            Assert.assertTrue(outputs.add(output));
        }
        
        Assert.assertEquals(9, outputs.size());
    }
    
    @Test
    public void defaults_01() {
        Node input = NodeFactory.createLiteral("example", "en-gb");
        ObfuscationProvider provider = createInstance();
        
        Node output = provider.obfuscateNode(input);
        Assert.assertNotEquals(input, output);
        Assert.assertNotEquals(input.getLiteralLanguage(), output.getLiteralLanguage());
    }
    
    @Test
    public void defaults_02() {
        Node input = NodeFactory.createLiteral("example", TypeMapper.getInstance().getSafeTypeByName("http://example.org/type"));
        ObfuscationProvider provider = createInstance();
        
        Node output = provider.obfuscateNode(input);
        Assert.assertNotEquals(input, output);
        Assert.assertNotEquals(input.getLiteralDatatypeURI(), output.getLiteralDatatypeURI());
    }
    
    @Test
    public void value_only_01() {
        Node input = NodeFactory.createURI("http://example.org");
        ObfuscationProvider provider = createInstance();
        provider.setConfiguration(new ValuesOnlyConfig());
        
        Node output = provider.obfuscateNode(input);
        Assert.assertNotEquals(input, output);
    }
    
    @Test
    public void value_only_02() {
        Node input = NodeFactory.createLiteral("example");
        ObfuscationProvider provider = createInstance();
        provider.setConfiguration(new ValuesOnlyConfig());
        
        Node output = provider.obfuscateNode(input);
        Assert.assertNotEquals(input, output);
    }
    
    @Test
    public void value_only_03() {
        Node input = NodeFactory.createLiteral("example", "en-gb");
        ObfuscationProvider provider = createInstance();
        provider.setConfiguration(new ValuesOnlyConfig());
        
        Node output = provider.obfuscateNode(input);
        Assert.assertNotEquals(input, output);
        Assert.assertEquals(input.getLiteralLanguage(), output.getLiteralLanguage());
    }
    
    @Test
    public void value_only_04() {
        Node input = NodeFactory.createLiteral("example", TypeMapper.getInstance().getSafeTypeByName("http://example.org/type"));
        ObfuscationProvider provider = createInstance();
        provider.setConfiguration(new ValuesOnlyConfig());
        
        Node output = provider.obfuscateNode(input);
        Assert.assertNotEquals(input, output);
        Assert.assertEquals(input.getLiteralDatatypeURI(), output.getLiteralDatatypeURI());
    }
}
