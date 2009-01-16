/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.util;

import java.nio.charset.Charset;
import java.util.*;
/**
 * 
 * This class provides a number of static methods which interact with
 * java.nio.charset.Charset to analyze and transform the strings identifing
 * character encodings.
 * 
 * @author Jeremy J. Carroll
 *  
 */
abstract public class CharEncoding {
    static Set<String> macEncodings = new HashSet<String>() ;
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

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

