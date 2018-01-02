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
package org.apache.jena.obfuscate.config;

import org.apache.jena.graph.Node;

/**
 * Interface for specifying what should be obfuscated
 */
public interface ObfuscationConfiguration {

    /**
     * Whether URIs should be obfuscated
     * 
     * @return True/False
     */
    public default boolean shouldObscureURIs() {
        return true;
    }

    /**
     * Whether variable names should be obfuscated
     * 
     * @return True/False
     */
    public default boolean shouldObscureVariables() {
        return true;
    }

    /**
     * Whether blank node labels should be obfuscated
     * 
     * @return True/False
     */
    public default boolean shouldObscureBlankNodes() {
        return true;
    }

    /**
     * Whether literals should be obfuscated *
     * <p>
     * If this returns {@code true} then the
     * {@link #shouldObscureLiteralValue(Node)}/
     * {@link #shouldObscureLiteralDatatype(Node)}/{@link #shouldObscureLiteralLanguages(Node)}
     * will be called to determine exactly how the literal is obfuscated
     * </p>
     * 
     * @return True/False
     */
    public default boolean shouldObscureLiterals() {
        return true;
    }

    /**
     * Whether literal datatype URIs should be obfuscated
     * <p>
     * If {@link #shouldObscureLiterals()} returns {@code true} and the literal
     * has a datatype URI then this method is called to see if the datatype URI
     * should be obfuscated.
     * </p>
     * <p>
     * This allows for implementations to selectively obfuscate only certain
     * datatype URIs and potentially not obfuscate their values. Or vice versa
     * to obfuscate values and preserve datatypes so the semantics of the
     * obfuscated literal and implied.
     * </p>
     * 
     * @param literal
     *            The literal whose datatype URI might be obfuscated
     * @return True/False
     */
    public default boolean shouldObscureLiteralDatatype(Node literal) {
        return true;
    }

    /**
     * Whether literal values should be obfuscated
     * <p>
     * If {@link #shouldObscureLiterals()} returns {@code true} then this method
     * is called to see if the value should be obfuscated.
     * </p>
     * <p>
     * This allows for implementations to selectively obfuscate only certain
     * values and potentially not obfuscate their datatype URIs. Or vice versa
     * to preserve values and obfuscate datatypes.
     * </p>
     * 
     * @param literal
     *            The literal whose value might be obfuscated
     * @return True/False
     */
    public default boolean shouldObscureLiteralValue(Node literal) {
        return true;
    }

    /**
     * Whether literal languages should be obfuscated
     * <p>
     * If {@link #shouldObscureLiterals()} returns {@code true} and the literal
     * has a language tag then this method is called to see if the language tag
     * should be obfuscated.
     * </p>
     * <p>
     * This allows for implementations to selectively obfuscate only certain
     * language tags and potentially not obfuscate their values. Or vice versa
     * to obfuscate values and preserve language tags so the semantics of the
     * obfuscated literal and implied.
     * </p>
     * 
     * @param literal
     *            The literal whose language tag might be obfuscated
     * @return True/False
     */
    public default boolean shouldObscureLiteralLanguages(Node literal) {
        return true;
    }
}
