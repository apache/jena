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

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.obfuscate.config.DefaultObfuscationConfig;
import org.apache.jena.obfuscate.config.ObfuscationConfiguration;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.util.NodeFactoryExtra;

/**
 * Simplistic abstract obfuscation provider that reduces all obfuscations to a
 * simple string obfuscation. For more advanced obfuscation approaches you
 * should implement the interface directly or override the
 * {@link #obfuscateNode(Node)} method.
 */
public abstract class AbstractObfuscationProvider implements ObfuscationProvider {

    private ObfuscationConfiguration config;

    @Override
    public ObfuscationConfiguration getConfiguration() {
        return config;
    }

    @Override
    public void setConfiguration(ObfuscationConfiguration config) {
        this.config = config;
    }

    @Override
    public Node obfuscateNode(Node n) {
        // This is a fairly simplistic implementation, it reduces everything to
        // a string and just has that passed to the abstract obfuscate(String)
        // method which derived implementations can provide their obfuscation
        // logic in

        // Grab the config, using the default if not specified
        ObfuscationConfiguration conf = this.config != null ? this.config : DefaultObfuscationConfig.INSTANCE;

        if (n.isBlank() && conf.shouldObscureBlankNodes()) {
            return NodeFactory.createBlankNode(new BlankNodeId(obfuscate(n.getBlankNodeLabel())));
        } else if (n.isVariable() && conf.shouldObscureVariables()) {
            return Var.alloc(obfuscate(n.getName()));
        } else if (n.isLiteral() && conf.shouldObscureLiterals()) {
            String value = conf.shouldObscureLiteralValue(n) ? obfuscate(n.getLiteralLexicalForm())
                    : n.getLiteralLexicalForm();

            if (StringUtils.isNotEmpty(n.getLiteralLanguage())) {
                return NodeFactory.createLiteral(value, conf.shouldObscureLiteralLanguages(n)
                        ? obfuscate(n.getLiteralLanguage()) : n.getLiteralLanguage());
            } else if (StringUtils.isNotEmpty(n.getLiteralDatatypeURI())) {
                return NodeFactoryExtra.createLiteralNode(value, null, conf.shouldObscureLiteralDatatype(n)
                        ? obfuscate(n.getLiteralDatatypeURI()) : n.getLiteralDatatypeURI());
            } else {
                return NodeFactory.createLiteral(value);
            }
        } else if (n.isURI() && conf.shouldObscureURIs()) {
            return NodeFactory.createURI(obfuscate(n.getURI()));
        } else {
            return n;
        }
    }

    /**
     * Obfuscates the given string
     * 
     * @param value
     *            Value
     * @return Obfuscated value
     */
    protected abstract String obfuscate(String value);

}
