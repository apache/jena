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

package org.apache.jena.langtag;

/**
 * See also {@link LangTags}.
 */
public class SysLangTag {

    /**
     * Create a {@link LangTag} using the system-wide default language tag parser,
     * which is {@link LangTagRFC5646}.
     *
     */
    public static LangTag create(String languageTag) {
        return LangTagRFC5646.create(languageTag);
    }

    /**
     * Format language tag.
     * This is the system-wide policy for formatting language tags.
     */
    public static String formatLangTag(String input) {
        if ( input == null )
            return "";
        if ( input.isEmpty() )
            return input;
        return create(input).str();
    }
}
