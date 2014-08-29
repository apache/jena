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

/*
 * LanguageTag.java
 *
 * Created on July 24, 2001, 11:45 PM
 */

package com.hp.hpl.jena.rdfxml.xmlinput.lang;

import java.util.Locale ;
import java.util.Vector;

/**
 * RFC 3066, "Tags for the Identification of Languages".
 */
public class LanguageTag implements LanguageTagCodes {
    String tags[];
    /** Creates new RFC3066 LanguageTag.
     * @param tag The tag to parse and analyse.
     * @throws LanguageTagSyntaxException If the syntactic rules of RFC3066 section 2.1 are
     * broken.
 */
    public LanguageTag(String tag) throws LanguageTagSyntaxException {
       String lc = tag.toLowerCase(Locale.ENGLISH);
       Vector<String> v = new Vector<>();
       int subT;
       while (true) {
           subT = lc.indexOf('-');
           if ( subT != -1) {
               v.add(lc.substring(0,subT));
               lc = lc.substring(subT+1);
           } else 
               break;
       }
       v.add(lc);
       tags = new String[v.size()];
       v.copyInto(tags);
       int lg = tags[0].length();
       if ( lg == 0 || lg > 8 ) {
           throw new LanguageTagSyntaxException("Primary subtag must be between 1 and 8 alpha characters: " + tag);
       }
       for (int j=0;j<lg;j++) {
           int ch = tags[0].charAt(j);
           if ( ! ( 'a' <= ch && ch <= 'z' ) )
               throw new LanguageTagSyntaxException("Primary subtag must be between 1 and 8 alpha characters: " + tag);
       }    
       for (int i=1;i<tags.length;i++) {
           lg = tags[i].length();
           if ( lg == 0 || lg > 8 ) {
               throw new LanguageTagSyntaxException("Subtag " + (i+1) + " must be between 1 and 8 alphanumeric characters: " + tag);
           }
           for (int j=0;j<lg;j++) {
               int ch = tags[i].charAt(j);
               if ( ! (   ( 'a' <= ch && ch <= 'z' ) 
                      ||  ( '0' <= ch && ch <= '9' )
                      )
                   )
                   throw new LanguageTagSyntaxException("Subtag " + (i+1) + " must be between 1 and 8 alphanumeric characters: " + tag);
           }    
       }
    }
    
 
    
    // Primary tag.
    
    // Second tag.
    // Additional tags (either second or third).
    // Special cases.
    // Overall properties
    /** The properties of this LanguageTag, expressed as a bitwise or of 
     * fields from {@link LanguageTagCodes}.
     * If the tag is illegal only <CODE>LT_ILLEGAL</CODE> is reported.
     * Examples include:
     * <dl>
     * <dt><CODE>LT_ISO639</CODE></dt><dd>en <I>English.</I></dd>
     * <dt><CODE>LT_ISO639|LT_ISO3166</CODE></dt><dd>en-GB <I>British English</I></dd> 
     * <dt><CODE>LT_ILLEGAL</CODE></dt><dd>en-ENGLAND <I>No such country.</I> Never returned in combination with other values.</dd>  
     * <dt><CODE>LT_PRIVATE_USE</CODE></dt><dd>x-en-ENGLAND <I>Private tag with private semantics.</I></dd>
     * <dt><CODE>LT_IANA|LT_EXTRA</CODE></dt><dd>i-klingon-trekkie <I>Klingon + "trekkie"</I></dd>
     * <dt><CODE>LT_IANA_DEPRECATED</CODE></dt><dd>
     * <dt><CODE>LT_MULTIPLE|LT_ISO3166|LT_EXTRA</CODE></dt><dd>mul-CH-dialects</dd>
     * <dt><CODE>LT_ISO639|LT_ISO3166|LT_IANA|LT_EXTRA</CODE></dt><dd>sgn-US-MA <I>Martha's Vineyard Sign
     * Language</I></dd>
     * </dl>
     *
     * @return A bitwise or of all LT_xxx values that apply.
 */
    public int tagType() {
        IanaLanguageTag iana = IanaLanguageTag.find(this);
        Iso639 lang = Iso639.find(tags[0]);
        int rslt = iana==null?0:iana.classification;
        if ( iana != null ) {
            if ( iana.tags.length < tags.length ) {
                rslt |= LT_EXTRA;
            }
        }
        switch ( tags[0].length() ) {
            case 1:
                switch (tags[0].charAt(0)) {
                    case 'x':
                        return LT_PRIVATE_USE; // reserved for private use.
                    case 'i':
                        return iana!=null?rslt:LT_ILLEGAL;
                    default:
                            return LT_ILLEGAL;
                }
            case 2:
                if (lang==null) {
                    return LT_ILLEGAL;
                }
                rslt |= lang.classification;   // all special case tags etc.
                break;
            case 3:
                if (lang==null) {
                    return LT_ILLEGAL;
                }
                if ( lang.twoCharCode != null ) {
                    return LT_ILLEGAL; // Section 2.3 Para 2
                }
                if ( !lang.terminologyCode.equals(tags[0]) ) {
                    return LT_ILLEGAL;
                    // Section 2.3 Para 3
                }
                rslt |= lang.classification;   // all special case tags etc.
                // Section 2.3 para 4,5,6 in a separate function.
                break;
                default:
                    return LT_ILLEGAL;
        }
        if ( tags.length ==1 )
            return rslt;
        switch ( tags[1].length() ) {
            case 1:
                return LT_ILLEGAL;
            case 2:
                if ( Iso3166.find(tags[1])==null )
                    return LT_ILLEGAL;
                break;
            default:
                if ( iana == null )
                     rslt |= LT_EXTRA;
        }
        if ( tags.length > 2  && iana == null )
                     rslt |= LT_EXTRA;
        return rslt;
    }
   /** An error message describing the reason the tag
    * is illegal.
    * @return null if legal, or an error message if not.
 */
    public String errorMessage() {
        switch ( tags[0].length() ) {
            case 1:
                switch (tags[0].charAt(0)) {
                    case 'x':
                        return null; // reserved for private use.
                    case 'i':
                        if ( IanaLanguageTag.find(this)!=null )
                            return null;
                        return toString() + " not found in IANA language registry.";
                    default:
                            return "Only 'x' and 'i' single character primary language subtags are defined in RFC3066.";
                }
            case 2:
                if (Iso639.find(tags[0])==null) {
                    return 
                    
                    "ISO-639 does not define language: '"+tags[0]+"'.";
                }
                break;
            case 3:
                Iso639 lang = Iso639.find(tags[0]);
                if (lang==null) {
                    return 
                    "ISO-639 does not define language: '"+tags[0]+"'.";
                }
                if ( lang.twoCharCode != null ) {
                    return 
                    "RFC 3066 section 2.3 mandates the use of '" + lang.twoCharCode + "' instead of '" + tags[0]+"'.";
                     // Section 2.3 Para 2
                }
                if ( !lang.terminologyCode.equals(tags[0]) ) {
                    return 
                    "RFC 3066 section 2.3 mandates the use of '" + lang.terminologyCode + "' instead of '" + tags[0]+"'.";
                     // Section 2.3 Para 3
                }
                // Section 2.3 para 4,5,6 in a separate function.
                break;
                default:
                    return "No primary language subtags of length greater than 3 are currently defined.";
        }
        if ( tags.length ==1 )
            return null;
        switch ( tags[1].length() ) {
            case 1:
                return "Second language subtags of length 1 are prohibited by RFC3066.";
            case 2:
                if ( Iso3166.find(tags[1])==null )
                    return "Country code, '"+tags[1]+ "', not found in ISO 3166.";
                break;
        }
        return null;
    }
}
