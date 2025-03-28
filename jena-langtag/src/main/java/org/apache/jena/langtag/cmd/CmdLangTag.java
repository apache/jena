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

package org.apache.jena.langtag.cmd;

import org.apache.jena.langtag.LangTag;
import org.apache.jena.langtag.LangTagException;
import org.apache.jena.langtag.SysLangTag;

public class CmdLangTag {

    public static void main(String[] args) {
        if ( args.length != 1 ) {
            System.err.println("Requires one argument.");
            System.exit(1);
        }

        String languageTag = args[0];
        if ( languageTag.isEmpty() ) {
            System.err.println("Empty string for language tag");
            System.exit(1);
        }
        if ( languageTag.isBlank() ) {
            System.err.println("Blank string for language tag");
            System.exit(1);
        }
        if ( languageTag.contains(" ") || languageTag.contains("\t") || languageTag.contains("\n") || languageTag.contains("\r") ) {
            System.err.println("Language tag contains white space");
            System.exit(1);
        }
        if ( languageTag.contains("--") ) {
            System.err.println("Illgeal language tag. String contains '--'");
            System.exit(1);
        }

        try {
            System.out.printf("%-16s %s\n", "Input:", languageTag);
            LangTag langTag = SysLangTag.create(languageTag);
            print("Formatted:",   langTag.str(),            true);
            print("Language:",    langTag.getLanguage(),    true);
            print("Script:",      langTag.getScript(),      true);
            print("Region:",      langTag.getRegion(),      true);
            print("Variant:",     langTag.getVariant(),     false);
            print("Extension:",   langTag.getExtension(),   false);
            print("Private Use:", langTag.getPrivateUse(),  false);
        } catch (LangTagException ex) {
            System.out.println("Bad language tag");
            System.out.printf("%s\n", ex.getMessage());
            System.exit(1);
        }
    }

    private static void print(String label, String value, boolean always) {
        if ( value == null ) {
            if ( ! always )
                return;
            value = "-";
        }
        System.out.printf("  %-14s %s\n", label, value);
    }
}
