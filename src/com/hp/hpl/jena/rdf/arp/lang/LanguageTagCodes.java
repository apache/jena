/*
 *  (c) Copyright 2001  Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 * * $Id: LanguageTagCodes.java,v 1.2 2003-08-27 13:05:54 andy_seaborne Exp $
   
   AUTHOR:  Jeremy J. Carroll
*/
/*
 * LanguageTagCodes.java
 *
 * Created on July 25, 2001, 10:09 AM
 */

package com.hp.hpl.jena.rdf.arp.lang;

/** Informational values about language codes.
 * Values to be OR-ed together.
 *
 * @author jjc
 */
public interface LanguageTagCodes {

/** The special tag <CODE>i-default</CODE>.
 */    
    public static final int LT_DEFAULT = 0x0100;
    
/** A tag with non-standard extra subtags.
 * Set for langauge tags with
 * additional subtags over their
 * IANA registration, or a third subtag
 * for unregistered tags of the form
 * ISO639Code-ISO3166Code.
 */    
    public static final int LT_EXTRA = 0x0080;
    
/** A tag in the IANA registry.
 */    
    public static final int LT_IANA = 0x1024;
    
/** An illegal tag.
 * Some rule of RFC3066 failed, or
 * the tag is not in IANA, or ISO639 or ISO3166.
 */    
    public static final int LT_ILLEGAL = 0x8000;
    
/** The second subtag is from ISO3166 and identifies
 * a country.
 */    
    public static final int LT_ISO3166 = 0x0010;
    
/** The first subtag is from ISO639-1 or ISO639-2
 * and identifies a language,
 */    
    public static final int LT_ISO639 = 0x0001;
    
/** A special ISO639-2 local use language tag.
 * A three letter code 'q[a-t][a-z]'.
 */    
    public static final int LT_LOCAL_USE = 0x0800;
    
/** The special ISO639-2 language tag <CODE>mul</CODE>.
 * This indicates multiple langauges.
 */    
    public static final int LT_MULTIPLE = 0x0400;
    
/** An RFC3066 private use tag.
 * A language tag of the form <CODE>x-????</CODE>.
 */    
    public static final int LT_PRIVATE_USE = 0x0002;
    
/** The undetermined ISO639-2 lanaguge <CODE>und</CODE>.
 */    
    public static final int LT_UNDETERMINED = 0x0200;
    
/** A langauge tag that is deprecated in the IANA registry.
 */    
    public static final int LT_IANA_DEPRECATED = 0x2000;
    
}

