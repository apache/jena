/**
 * Copyright 2018 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.ext.io.github.galbiston.rdf_tables.cli;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import java.util.Arrays;
import java.util.List;

/**
 *
 *
 */
public class DelimiterValidator implements IParameterValidator {

    private static final List<String> RESERVED_CHARACTERS = Arrays.asList(":", "^", "|");
    private static final List<String> KEYWORDS = Arrays.asList("tab", "space", "comma");

    @Override
    public void validate(String name, String value) throws ParameterException {
        String val = value.toLowerCase();

        if (!KEYWORDS.contains(val) && value.length() > 1) {
            throw new ParameterException("Parameter " + name + " and value " + value + " is longer than a single character.");
        }

        for (String reserved : RESERVED_CHARACTERS) {
            if (val.contains(reserved)) {
                throw new ParameterException("Parameter " + name + " and value " + value + " contains reserved character from " + String.join(", ", RESERVED_CHARACTERS) + ".");
            }
        }
    }

    public static char getDelimiterCharacter(String delimiter) {

        switch (delimiter.toLowerCase()) {
            case "space":
                return ' ';
            case "tab":
                return '\t';
            case "comma":
                return ',';
            default:
                return delimiter.charAt(0);
        }

    }

}
