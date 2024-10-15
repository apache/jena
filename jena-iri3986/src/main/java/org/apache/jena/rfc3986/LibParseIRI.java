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

package org.apache.jena.rfc3986;

/** Operations related to parsing IRIs */
/*package*/ class LibParseIRI {

    private static int CASE_DIFF = 'a'-'A';     // 0x20. Only for ASCII.
    /*
     * Case insensitive "startsWith" for ASCII.
     * Check whether the character and the next character match the expected characters.
     * "chars" array  should be lower case.
     *     scheme = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
     */
    /*package*/ static boolean containsAtIgnoreCase(CharSequence string, int x, char[] chars) {
        // Avoid creating any objects.
        int n = string.length();
        if ( x+chars.length-1 >= n )
            return false;
        for ( int i = 0 ; i < chars.length ; i++ ) {
            char ch = string.charAt(x+i);
            char chx = chars[i];
            if ( ch == chx )
                continue;
            // URI scheme names are ASCII.
            if ( Chars3986.range(ch, 'A', 'Z' ) && ( chx - ch == CASE_DIFF ) )
                continue;
            return false;
        }
        return true;
    }

    /** Check whether the character and the next character match the expected characters. */
    public static boolean peekFor(CharSequence string, int x, char x1, char x2) {
        int n = string.length();
        if ( x+1 >= n )
            return false;
        char ch1 = string.charAt(x);
        char ch2 = string.charAt(x+1);
        return ch1 == x1 && ch2 == x2;
    }

    public static char charAt(CharSequence string, int x) {
        if ( x >= string.length() )
            return Chars3986.EOF;
        return string.charAt(x);
    }

    /** Case insensitive test of whether a string has a prefix. */
    static boolean caseInsensitivePrefix(String string, String prefix) {
        return caseInsensitiveRegion(string, 0 , prefix);
    }

    /** Case insensitive test of whether a string contains a substring. */
    static boolean caseInsensitiveRegion(String string, int idx, String substr) {
        return string.regionMatches(true, idx, substr, 0, substr.length());
    }

    /** Case sensitive test of whether a string contains a substring at an index.. */
    static boolean region(String string, int idx, String substr) {
        return string.regionMatches(idx, substr, 0, substr.length());
    }

    // >> Copied from jena-iri for comparison.
    static String jenaIRIremoveDotSegments(String path) {
        // 5.2.4 step 1.
        int inputBufferStart = 0;
        int inputBufferEnd = path.length();
        StringBuffer output = new StringBuffer();
        // 5.2.4 step 2.
        while (inputBufferStart < inputBufferEnd) {
            String in = path.substring(inputBufferStart);
            // 5.2.4 step 2A
            if (in.startsWith("./")) {
                inputBufferStart += 2;
                continue;
            }
            if (in.startsWith("../")) {
                inputBufferStart += 3;
                continue;
            }
            // 5.2.4 2 B.
            if (in.startsWith("/./")) {
                inputBufferStart += 2;
                continue;
            }
            if (in.equals("/.")) {
                in = "/"; // don't continue, process below.
                inputBufferStart += 2; // force end of loop
            }
            // 5.2.4 2 C.
            if (in.startsWith("/../")) {
                inputBufferStart += 3;
                removeLastSeqment(output);
                continue;
            }
            if (in.equals("/..")) {
                in = "/"; // don't continue, process below.
                inputBufferStart += 3; // force end of loop
                removeLastSeqment(output);
            }
            // 5.2.4 2 D.
            if (in.equals(".")) {
                inputBufferStart += 1;
                continue;
            }
            if (in.equals("..")) {
                inputBufferStart += 2;
                continue;
            }
            // 5.2.4 2 E.
            int nextSlash = in.indexOf('/', 1);
            if (nextSlash == -1)
                nextSlash = in.length();
            inputBufferStart += nextSlash;
            output.append(in.substring(0, nextSlash));
        }
        // 5.2.4 3
        return output.toString();
    }

    private static void removeLastSeqment(StringBuffer output) {
        int ix = output.length();
        while (ix > 0) {
            ix--;
            if (output.charAt(ix) == '/')
                break;
        }
        output.setLength(ix);
    }
    // << Copied from jena-iri
}
