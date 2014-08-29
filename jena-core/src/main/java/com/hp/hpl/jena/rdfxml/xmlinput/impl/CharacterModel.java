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

package com.hp.hpl.jena.rdfxml.xmlinput.impl;

import java.text.Normalizer;

/**
 * Some support for the Character Model Recommendation
 * from the W3C (currently in second last call working 
 * draft).
 */
public class CharacterModel {
	static private final boolean SWITCH_OFF = false;
	/** Is this string in Unicode Normal Form C.
	 * @param str The string to be tested.
	 */
	static public boolean isNormalFormC(String str) {
	    try {
	   return SWITCH_OFF || Normalizer.isNormalized(str,Normalizer.Form.NFC);
	    }
	    catch (ArrayIndexOutOfBoundsException e) {
	        String normalized = Normalizer.normalize(str, Normalizer.Form.NFC);
	        return normalized.equals(str);
	    }
	}
    
	
	/* Does this string start with a composing character as defined
	 * by the 
	 * <a href="http://www.w3.org/TR/charmod">
	 * Character Model 2nd Last Call Working Draft</a>.
	 * @param str The string to be tested.
	 */
	static public boolean startsWithComposingCharacter(String str) {
		return SWITCH_OFF ? false :  (str.length()==0?false:isComposingChar(str.charAt(0)));
	}
/** Is this string fully normalized as defined
	 * by the 
	 * <a href="http://www.w3.org/TR/charmod">
	 * Character Model 2nd Last Call Working Draft</a>.
	 * @param str The string to be tested.
	 */
	static public boolean isFullyNormalizedConstruct(String str) {
		return SWITCH_OFF || (isNormalFormC(str) && !startsWithComposingCharacter(str));
	}
	/** Is the character a composing character as defined
	 * by the 
	 * <a href="http://www.w3.org/TR/charmod">
	 * Character Model 2nd Last Call Working Draft</a>.
	 * @param x The character to be tested.
	 */
   static public boolean isComposingChar(char x) {
   	if ( SWITCH_OFF )
   		return false;
   	switch (x) { 
// Brahmi-derived scripts
case 0X09BE: // BENGALI VOWEL SIGN AA 
case 0X09D7: // BENGALI AU LENGTH MARK 
case 0X0B3E: // ORIYA VOWEL SIGN AA 
case 0X0B56: // ORIYA AI LENGTH MARK 
case 0X0B57: // ORIYA AU LENGTH MARK 
case 0X0BBE: // TAMIL VOWEL SIGN AA 
case 0X0BD7: // TAMIL AU LENGTH MARK 
case 0X0CC2: // KANNADA VOWEL SIGN UU 
case 0X0CD5: // KANNADA LENGTH MARK 
case 0X0CD6: // KANNADA AI LENGTH MARK 
case 0X0D3E: // MALAYALAM VOWEL SIGN AA 
case 0X0D57: // MALAYALAM AU LENGTH MARK 
case 0X0DCF: // SINHALA VOWEL SIGN AELA-PILLA 
case 0X0DDF: // SINHALA VOWEL SING GAYANUKITTA 
case 0X0FB5: // TIBETAN SUBJOINED LETTER SSA 
case 0X0FB7: // TIBETAN SUBJOINED LETTER HA 
case 0X102E: // MYANMAR VOWEL SIGN II 
// Hangul vowels 
case 0X1161: // HANGUL JUNGSEONG A 
case 0X1162: // HANGUL JUNGSEONG AE 
case 0X1163: // HANGUL JUNGSEONG YA 
case 0X1164: // HANGUL JUNGSEONG YAE 
case 0X1165: // HANGUL JUNGSEONG EO 
case 0X1166: // HANGUL JUNGSEONG E 
case 0X1167: // HANGUL JUNGSEONG YEO 
case 0X1168: // HANGUL JUNGSEONG YE 
case 0X1169: // HANGUL JUNGSEONG O 
case 0X116A: // HANGUL JUNGSEONG WA 
case 0X116B: // HANGUL JUNGSEONG WAE 
case 0X116C: // HANGUL JUNGSEONG OE 
case 0X116D: // HANGUL JUNGSEONG YO 
case 0X116E: // HANGUL JUNGSEONG U 
case 0X116F: // HANGUL JUNGSEONG WEO 
case 0X1170: // HANGUL JUNGSEONG WE 
case 0X1171: // HANGUL JUNGSEONG WI 
case 0X1172: // HANGUL JUNGSEONG YU 
case 0X1173: // HANGUL JUNGSEONG EU 
case 0X1174: // HANGUL JUNGSEONG YI 
case 0X1175: // HANGUL JUNGSEONG I 
// Hangul trailing consonants 
case 0X11A8: // HANGUL JONGSEONG KIYEOK 
case 0X11A9: // HANGUL JONGSEONG SSANGKIYEOK 
case 0X11AA: // HANGUL JONGSEONG KIYEOK-SIOS 
case 0X11AB: // HANGUL JONGSEONG NIEUN 
case 0X11AC: // HANGUL JONGSEONG NIEUN-CIEUC 
case 0X11AD: // HANGUL JONGSEONG NIEUN-HIEUH 
case 0X11AE: // HANGUL JONGSEONG TIKEUT 
case 0X11AF: // HANGUL JONGSEONG RIEUL 
case 0X11B0: // HANGUL JONGSEONG RIEUL-KIYEOK 
case 0X11B1: // HANGUL JONGSEONG RIEUL-MIEUM 
case 0X11B2: // HANGUL JONGSEONG RIEUL-PIEUP 
case 0X11B3: // HANGUL JONGSEONG RIEUL-SIOS 
case 0X11B4: // HANGUL JONGSEONG RIEUL-THIEUTH 
case 0X11B5: // HANGUL JONGSEONG RIEUL-PHIEUPH 
case 0X11B6: // HANGUL JONGSEONG RIEUL-HIEUH 
case 0X11B7: // HANGUL JONGSEONG MIEUM 
case 0X11B8: // HANGUL JONGSEONG PIEUP 
case 0X11B9: // HANGUL JONGSEONG PIEUP-SIOS 
case 0X11BA: // HANGUL JONGSEONG SIOS 
case 0X11BB: // HANGUL JONGSEONG SSANGSIOS 
case 0X11BC: // HANGUL JONGSEONG IEUNG 
case 0X11BD: // HANGUL JONGSEONG CIEUC 
case 0X11BE: // HANGUL JONGSEONG CHIEUCH 
case 0X11BF: // HANGUL JONGSEONG KHIEUKH 
case 0X11C0: // HANGUL JONGSEONG THIEUTH 
case 0X11C1: // HANGUL JONGSEONG PHIEUPH 
case 0X11C2: // HANGUL JONGSEONG HIEUH 
   return true;
   default:
   return isCombining(x);
   	}
   }
   
    /*
     * Replacing icu4j function for non-zero combining class
     * Data from {@link http://unicode.org/cldr/utility/list-unicodeset.jsp?a=%5B:Canonical_Combining_Class!=0:%5D}
     */
    private static boolean isCombining(int c) {
        // Common case
        if (c < 0x0300) return false;
        
        if (c >= 0x0300 && c <= 0x034E) return true;
        if (c >= 0x0350 && c <= 0x036F) return true;
        if (c >= 0x0483 && c <= 0x0487) return true;
        if (c >= 0x0591 && c <= 0x05BD) return true;
        if (c >= 0x0610 && c <= 0x061A) return true;
        if (c >= 0x064B && c <= 0x065F) return true;
        if (c >= 0x06D6 && c <= 0x06DC) return true;
        if (c >= 0x06DF && c <= 0x06E4) return true;
        if (c >= 0x06EA && c <= 0x06ED) return true;
        if (c >= 0x0730 && c <= 0x074A) return true;
        if (c >= 0x07EB && c <= 0x07F3) return true;
        if (c >= 0x0816 && c <= 0x0819) return true;
        if (c >= 0x081B && c <= 0x0823) return true;
        if (c >= 0x0825 && c <= 0x0827) return true;
        if (c >= 0x0829 && c <= 0x082D) return true;
        if (c >= 0x0859 && c <= 0x085B) return true;
        if (c >= 0x0951 && c <= 0x0954) return true;
        if (c >= 0x0E38 && c <= 0x0E3A) return true;
        if (c >= 0x0E48 && c <= 0x0E4B) return true;
        if (c >= 0x0EC8 && c <= 0x0ECB) return true;
        if (c >= 0x0F7A && c <= 0x0F7D) return true;
        if (c >= 0x0F82 && c <= 0x0F84) return true;
        if (c >= 0x135D && c <= 0x135F) return true;
        if (c >= 0x1939 && c <= 0x193B) return true;
        if (c >= 0x1A75 && c <= 0x1A7C) return true;
        if (c >= 0x1B6B && c <= 0x1B73) return true;
        if (c >= 0x1CD0 && c <= 0x1CD2) return true;
        if (c >= 0x1CD4 && c <= 0x1CE0) return true;
        if (c >= 0x1CE2 && c <= 0x1CE8) return true;
        if (c >= 0x1DC0 && c <= 0x1DE6) return true;
        if (c >= 0x1DFC && c <= 0x1DFF) return true;
        if (c >= 0x20D0 && c <= 0x20DC) return true;
        if (c >= 0x20E5 && c <= 0x20F0) return true;
        if (c >= 0x2CEF && c <= 0x2CF1) return true;
        if (c >= 0x2DE0 && c <= 0x2DFF) return true;
        if (c >= 0x302A && c <= 0x302F) return true;
        if (c >= 0xA8E0 && c <= 0xA8F1) return true;
        if (c >= 0xA92B && c <= 0xA92D) return true;
        if (c >= 0xAAB2 && c <= 0xAAB4) return true;
        if (c >= 0xFE20 && c <= 0xFE26) return true;
        if (c >= 0x00010A38 && c <= 0x00010A3A) return true;
        if (c >= 0x0001D165 && c <= 0x0001D169) return true;
        if (c >= 0x0001D16D && c <= 0x0001D172) return true;
        if (c >= 0x0001D17B && c <= 0x0001D182) return true;
        if (c >= 0x0001D185 && c <= 0x0001D18B) return true;
        if (c >= 0x0001D1AA && c <= 0x0001D1AD) return true;
        if (c >= 0x0001D242 && c <= 0x0001D244) return true;

        switch (c) {
            case 0x05BF:
            case 0x05C1:
            case 0x05C2:
            case 0x05C4:
            case 0x05C5:
            case 0x05C7:
            case 0x0670:
            case 0x06E7:
            case 0x06E8:
            case 0x0711:
            case 0x093C:
            case 0x094D:
            case 0x09BC:
            case 0x09CD:
            case 0x0A3C:
            case 0x0A4D:
            case 0x0ABC:
            case 0x0ACD:
            case 0x0B3C:
            case 0x0B4D:
            case 0x0BCD:
            case 0x0C4D:
            case 0x0C55:
            case 0x0C56:
            case 0x0CBC:
            case 0x0CCD:
            case 0x0D4D:
            case 0x0DCA:
            case 0x0EB8:
            case 0x0EB9:
            case 0x0F18:
            case 0x0F19:
            case 0x0F35:
            case 0x0F37:
            case 0x0F39:
            case 0x0F71:
            case 0x0F72:
            case 0x0F74:
            case 0x0F80:
            case 0x0F86:
            case 0x0F87:
            case 0x0FC6:
            case 0x1037:
            case 0x1039:
            case 0x103A:
            case 0x108D:
            case 0x1714:
            case 0x1734:
            case 0x17D2:
            case 0x17DD:
            case 0x18A9:
            case 0x1A17:
            case 0x1A18:
            case 0x1A60:
            case 0x1A7F:
            case 0x1B34:
            case 0x1B44:
            case 0x1BAA:
            case 0x1BE6:
            case 0x1BF2:
            case 0x1BF3:
            case 0x1C37:
            case 0x1CED:
            case 0x20E1:
            case 0x2D7F:
            case 0x3099:
            case 0x309A:
            case 0xA66F:
            case 0xA67C:
            case 0xA67D:
            case 0xA6F0:
            case 0xA6F1:
            case 0xA806:
            case 0xA8C4:
            case 0xA953:
            case 0xA9B3:
            case 0xA9C0:
            case 0xAAB0:
            case 0xAAB7:
            case 0xAAB8:
            case 0xAABE:
            case 0xAABF:
            case 0xAAC1:
            case 0xABED:
            case 0xFB1E:
            case 0x000101FD:
            case 0x00010A0D:
            case 0x00010A0F:
            case 0x00010A3F:
            case 0x00011046:
            case 0x000110B9:
            case 0x000110BA:
                return true;
            default:
                return false;
        }
    }

   
/*   
   static public void main(String args[]) {
   	int ch = Integer.parseInt(args[0],16);
   	System.out.println(UCharacter.getCombiningClass(ch));
   }
 */
}
