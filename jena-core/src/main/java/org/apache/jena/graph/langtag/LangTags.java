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

package org.apache.jena.graph.langtag;

/**
 * Functions on language tag strings
 * @deprecated use org.apache.jena.langtag.LangTags
 */
@Deprecated(forRemoval = true)
public class LangTags {

    /**
     * Format language tag.
     * This is the system-wide policy for formatting language tags.
     * @deprecated use org.apache.jena.langtag.LangTags#formatLangtag(String)
     */
    @Deprecated(forRemoval=true)
    public static String formatLangtag(String input) {
        return org.apache.jena.langtag.LangTags.format(input);
    }

    /**
     * Format an language tag assumed to be valid.
     * This code only deals with langtags by the string length of the subtags.
     * <a href="https://datatracker.ietf.org/doc/html/rfc5646#section-2.1.1">RFC 5646 section 2.1.1</a>
     * @deprecated use org.apache.jena.langtag.LangTags#basicFormat(String)
     */
    @Deprecated(forRemoval=true)
    public static String basicFormat(String string) {
        return org.apache.jena.langtag.LangTags.basicFormat(string);
    }
}
