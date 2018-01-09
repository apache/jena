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

import org.apache.jena.obfuscate.hash.MD5ObfuscationProvider;

/**
 * This is a relatively simplistic and naive obfuscation that simply replaces
 * each character with a corresponding masking character based on the original
 * character type.
 * <p>
 * This basically leaves the overall structure of the original strings intact
 * but obfuscates the actual values. This may be desirable if you want to be
 * able to make general statements about the obfuscated data without knowing the
 * actual values. If you prefer to obfuscate both structure and value you
 * probably want to use something like {@link MD5ObfuscationProvider} that
 * hashes the values.
 * </p>
 */
public class NaiveObfuscationProvider extends AbstractObfuscationProvider {

    /**
     * Constants for character substitutions
     */
    private static final char LOWERCASE = 'x';
    /**
     * Constants for character substitutions
     */
    private static final char UPPERCASE = 'X';
    /**
     * Constants for character substitutions
     */
    private static final char DIGIT = '#';
    /**
     * Constants for character substitutions
     */
    private static final char PUNCTUATION = '.';
    /**
     * Constants for character substitutions
     */
    private static final char CONTROL = '^';
    /**
     * Constants for character substitutions
     */
    private static final char OTHER = '*';

    @Override
    protected String obfuscate(String value) {
        StringBuilder hiddenData = new StringBuilder(value.length());

        for (char c : value.toCharArray()) {
            if (Character.isLowerCase(c)) {
                hiddenData.append(LOWERCASE);
            } else if (Character.isUpperCase(c)) {
                hiddenData.append(UPPERCASE);
            } else if (Character.isDigit(c)) {
                hiddenData.append(DIGIT);
            } else if (Character.isISOControl(c)) {
                hiddenData.append(CONTROL);
            } else if (Character.isLowSurrogate(c) || Character.isHighSurrogate(c)) {
                // Unicode surrogates
                hiddenData.append(OTHER);
            } else if (!Character.isAlphabetic(c)) {
                // Treat anything else non-alphabetic as being punctuation
                hiddenData.append(PUNCTUATION);
            } else {
                // Anything else treat as other
                hiddenData.append(OTHER);
            }
        }

        return hiddenData.toString();
    }

}
