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

package com.hp.hpl.jena.rdf.arp.impl;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.Normalizer;
/**
 * Some support for the Character Model Recommendation
 * from the W3C (currently in second last call working 
 * draft).
 * 
 * @author Jeremy Carroll
 *
 * 
 */
public class CharacterModel {
	static private final boolean SWITCH_OFF = false;
	/** Is this string in Unicode Normal Form C.
	 * @param str The string to be tested.
	 */
	static public boolean isNormalFormC(String str) {
//	    try {
	   return SWITCH_OFF || Normalizer.  isNormalized(str,Normalizer.NFC,0);
//	    }
//	    catch (ArrayIndexOutOfBoundsException e) {
//	        // false below means "NFC" see javadoc for compose().
//	        String normalized = Normalizer.compose(str,false);
//	        return normalized.equals(str);
//	    }
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
   return UCharacter.getCombiningClass(x) != 0;
   	}
   }
/*   
   static public void main(String args[]) {
   	int ch = Integer.parseInt(args[0],16);
   	System.out.println(UCharacter.getCombiningClass(ch));
   }
 */
}
