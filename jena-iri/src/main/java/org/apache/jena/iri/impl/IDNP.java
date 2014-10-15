/*
 * Copyright 2012 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.iri.impl;

import java.net.IDN;

/**
 * IDNP: IDN Patched
 * 
 * A wrapper around java.net.IDN to avoid a bug when checking result.
 * It looks like the IDN is apply checks before fully converting, so this defers
 * a similar check until the end.
 */
public class IDNP {
    
    public static String toASCII(String host, int flags) {
        if ((flags & IDN.USE_STD3_ASCII_RULES) != 0) {
            return check(IDN.toASCII(host, flags ^ IDN.USE_STD3_ASCII_RULES));
        } else {
            return IDN.toASCII(host, flags);
        }
    }
    
    // Step through each part of the host and ensure that they conform
    private static String check(final String asciiHost) {
        String[] parts = asciiHost.split("\\.");
        for (String part: parts) checkPart(part);
        return asciiHost;
    }
    
    // Part check: no leading or trailing hyphens, hyphens and alphanumerics.
    // We try to follow java.net.IDN behaviour in our exceptions, in anticipation
    // of dumping this class.
    private static void checkPart(String part) {
        if (part.charAt(0) == '-' || 
                part.charAt(part.length() - 1) == '-') {
            throw new IllegalArgumentException("Has leading or trailing hyphen");
        }
        for (int i = 0; i < part.length(); i++) {
            char c = part.charAt(i);
            // Host is limited to hyphens and alpha numerics
            if (c != '-' &&
                    !(c >= '0' && c <= '9') &&
                    !(c >= 'a' && c <= 'z') &&
                    !(c >= 'A' && c <= 'Z')) {
                throw new IllegalArgumentException("Contains non-LDH characters");
            }
        }
    }
    
}
