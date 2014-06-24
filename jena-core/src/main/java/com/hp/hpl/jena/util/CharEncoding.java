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

package com.hp.hpl.jena.util;

import java.nio.charset.Charset;
import java.util.*;
/**
 * 
 * This class provides a number of static methods which interact with
 * java.nio.charset.Charset to analyze and transform the strings identifing
 * character encodings.
 */
abstract public class CharEncoding {
    static Set<String> macEncodings = new HashSet<>() ;
    static
    {
        macEncodings.add("MacArabic") ;
        macEncodings.add("MacCentralEurope") ;
        macEncodings.add("MacCroatian") ;
        macEncodings.add("MacCyrillic") ;
        macEncodings.add("MacDingbat") ;
        macEncodings.add("MacGreek") ;
        macEncodings.add("MacHebrew") ;
        macEncodings.add("MacIceland") ;
        macEncodings.add("MacRoman") ;
        macEncodings.add("MacRomania") ;
        macEncodings.add("MacSymbol") ;
        macEncodings.add("MacThai") ;
        macEncodings.add("MacTurkish") ;
        macEncodings.add("MacUkraine") ;
    }
    private String     name ;

    private CharEncoding()
    {}

    private CharEncoding(String name)
    {
        this.name = name ;
    }
    /**
     * Gives the canonical name for this charset.
     * If {@link #isIANA()} returns true, then
     * this is the name registered at IANA.
     * If {@link #isInNIO()} returns true, and
     * {@link #isIANA()} returns false, then this name
     * will start with "x-".
     * The name is case insensitive, but not case
     * normalized.
     * @return Canonical name.
     */
    public String name() {
        return name;
    }
    /**
     * Returns true if this charset
     * registered at IANA. 
     * Since the registry may change, the results of this
     * method may not be entirely up-to-date, 
     * and draws from the knowledge in
     * the Java java.nio.charset.Charset class. 
     * If {@link #isInNIO()} returns false, no information
     * is known, and this method returns false.
     * @return true if this character encoding is IANA registered.
     */
 
    abstract public boolean isIANA();
    /**
     * Returns true if this charset is supported by
     * java.nio.charset.Charset.
     * Without this support {@link #isIANA()}
     * does not work correctly.
     * @return true if this charset is supported by
     * java.nio.charset.Charset.
     */
    abstract public boolean isInNIO();
    
    /**
     * If {@link #isIANA} or {@link #isInNIO}
     * return false, this returns a suggested warning
     * message. If {@link #isIANA} is true, then this
     * returns null.
     * @return A message (or null)
     */
    abstract public String warningMessage();
    static private class IANAnioEncoding extends CharEncoding {
        IANAnioEncoding(String name) {
            super(name);
        }
        @Override
        public boolean isIANA() {
            return true;
        }
        @Override
        public boolean isInNIO() {
            return true;
        }
        @Override
        public String warningMessage() {
            return null;
        }
    }
    static private class NonIANAnioEncoding extends CharEncoding {
        NonIANAnioEncoding(String name) {
            super(name);
        }
        @Override
        public boolean isIANA() {
            return false;
        }
        @Override
        public boolean isInNIO() {
            return true;
        }
        @Override
        public String warningMessage() {
            return "The encoding \"" + name() + "\" is not registered with IANA, and hence not suitable for Web content.";
        }
    }
    static private class NotNioEncoding extends CharEncoding {
        NotNioEncoding(String name) {
            super(name);
        }
        @Override
        public boolean isIANA() {
            return false;
        }
        @Override
        public boolean isInNIO() {
            return false;
        }
        @Override
        public String warningMessage() {
            return "The encoding \"" + name() + "\" is not fully supported; maybe try using Java 1.5 or higher (if you are not already).";
        }
    }

    /**
     * Create a new CharacterEncoding object,
     * given a name of a character encoding
     * identifying it.
     * @param enc A name.
     * @return The corresponding CharacterEncoding object.
     */
    static public CharEncoding create(String enc){
        if (Charset.isSupported(enc)) {
            String nm = Charset.forName(enc).name();
            if (nm.charAt(1)=='-'
                && (nm.charAt(0)=='x' || nm.charAt(0)=='X') )
                return new NonIANAnioEncoding(nm);
            else if (nm.startsWith("Mac") &&
               macEncodings.contains(nm) ) 
                return new NonIANAnioEncoding(nm);
            else
                return new IANAnioEncoding(nm);
        } else {
            return new NotNioEncoding(enc);
        }
    }
}
