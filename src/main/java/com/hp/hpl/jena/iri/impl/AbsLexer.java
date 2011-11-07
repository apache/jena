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

package com.hp.hpl.jena.iri.impl;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.*;

import com.hp.hpl.jena.iri.ViolationCodes;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UCharacterCategory;
import com.ibm.icu.lang.UProperty;
import com.ibm.icu.text.Normalizer;

abstract class AbsLexer implements ViolationCodes {

    /* user code: */
    protected Parser parser;
    protected int range;

    /*
    yyreset(null);
    this.zzAtEOF = true;
    int length = parser.end(range)-parser.start(range);
    zzEndRead = length;
    while (length > zzBuffer.length)
        zzBuffer = new char[zzBuffer.length*2];

    */
    synchronized public void analyse(Parser p,int r) {
        parser = p;
        range = r;
        if (!parser.has(range)) 
            return;
        parser.uri.getChars(
                parser.start(range),
                parser.end(range),
                zzBuffer(),
                0);
       try {
            yylex();
       }
       catch (java.io.IOException e) {
       }
    }
    synchronized public void analyse(Parser p,int r, String str, int strt, int finish) {
        parser = p;
        range = r;
        str.getChars(
                strt,
                finish,
                zzBuffer(),
                0);
       try {
            yylex();
       }
       catch (java.io.IOException e) {
       }
    }
    
    
    abstract  int yylex() throws java.io.IOException;
    abstract char[] zzBuffer();
    
    protected void error(int e) {
        parser.recordError(range,e);
    }
    
    final protected void rule(int rule) {
        parser.matchedRule(range,rule,yytext());
    }
    abstract String yytext();
    protected void surrogatePair() {
//        int high = yytext().charAt(0);
//        int low = yytext().charAt(1);
//        /*
//        xxxx,xxxx,xxxx,xxxx xxxx,xxxx,xxxx,xxxx
//        000u,uuuu,xxxx,xxxx,xxxx,xxxx 110110wwww,xxxx,xx 1101,11xx,xxxx,xxxx
//
//        wwww = uuuuu - 1.
//        */
//        int bits0_9 = low & ((1<<10)-1);
//        int bits10_15 = (high & ((1<<6)-1))<<10;
//        int bits16_20 = (((high >> 6) & ((1<<4)-1))+1)<<16;
        try {
           String txt = yytext();
        difficultCodePoint(
                UCharacter.getCodePoint(txt.charAt(0),
                        txt.charAt(1)),
                        txt);
        }
        catch (IllegalArgumentException e){
            // TODO bad surrogate
        }
    }

    private void difficultCodePoint(int codePoint, String txt) {
        /* Legal XML
        #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
         */
        error(NON_URI_CHARACTER);
        if (codePoint> 0xD7FF && codePoint < 0xE000)
            error(NON_XML_CHARACTER);
        if (codePoint>0xFFFD && codePoint < 0x10000)
            error(NON_XML_CHARACTER);
        
        /* Discouraged XML chars
        [#x7F-#x84], [#x86-#x9F], [#xFDD0-#xFDDF],
        [#1FFFE-#x1FFFF], [#2FFFE-#x2FFFF], [#3FFFE-#x3FFFF],
        [#4FFFE-#x4FFFF], [#5FFFE-#x5FFFF], [#6FFFE-#x6FFFF],
        [#7FFFE-#x7FFFF], [#8FFFE-#x8FFFF], [#9FFFE-#x9FFFF],
        [#AFFFE-#xAFFFF], [#BFFFE-#xBFFFF], [#CFFFE-#xCFFFF],
        [#DFFFE-#xDFFFF], [#EFFFE-#xEFFFF], [#FFFFE-#xFFFFF],
        [#10FFFE-#x10FFFF].
        */
        
        if ( codePoint >= 0xFDD0 && codePoint <= 0xFDDF)
            error(DISCOURAGED_XML_CHARACTER);
        if (codePoint>0x10000) {
            int lowBits = (codePoint&0xFFFF);
            if (lowBits==0xFFFE||lowBits==0xFFFF)
                error(DISCOURAGED_XML_CHARACTER);
        }
        
        // TODO more char tests, make more efficient
        
        if (UCharacter.hasBinaryProperty(codePoint,UProperty.DEPRECATED))
            error(DEPRECATED_UNICODE_CHARACTER);
        if (!UCharacter.isDefined(codePoint)) {
            error(UNDEFINED_UNICODE_CHARACTER);
        }
        switch (UCharacter.getType(codePoint)) {
        case UCharacterCategory.PRIVATE_USE:
            error(PRIVATE_USE_CHARACTER);
            break;
        case UCharacterCategory.CONTROL:
            error(UNICODE_CONTROL_CHARACTER);
            break;
        case UCharacterCategory.UNASSIGNED:
            error(UNASSIGNED_UNICODE_CHARACTER);
            break;
        }
        Normalizer.QuickCheckResult qcr = Normalizer.quickCheck(txt,Normalizer.NFC); 
        if (qcr.equals(Normalizer.NO)) {
            error(NOT_NFC);
        } else if (qcr.equals(Normalizer.MAYBE)) {
            error(MAYBE_NOT_NFC);
        }
        qcr = Normalizer.quickCheck(txt,Normalizer.NFKC); 
        if (qcr.equals(Normalizer.NO)) {
            error(NOT_NFKC);
        } else if (qcr.equals(Normalizer.MAYBE)) {
            error(MAYBE_NOT_NFKC);
        }
        if (UCharacter.isWhitespace(codePoint)||UCharacter.isUWhiteSpace(codePoint)) {
            error(UNICODE_WHITESPACE);
        }
        
        
        if (isCompatibilityChar(codePoint))
            error(COMPATIBILITY_CHARACTER);
        
        // compatibility char
        // defn is NFD != NFKD, ... hmmm
        
    }


    private boolean isCompatibilityChar(int codePoint) {
        switch (UCharacter.getIntPropertyValue(codePoint,UProperty.DECOMPOSITION_TYPE)) {
        case UCharacter.DecompositionType.CANONICAL:
        case UCharacter.DecompositionType.NONE:
            switch (UCharacter.UnicodeBlock.of(codePoint).getID()) {
            case UCharacter.UnicodeBlock.CJK_COMPATIBILITY_ID:
                /*(U+FA0E, U+FA0F, U+FA11, U+FA13, U+FA14, U+FA1F, U+FA21,
                        U+FA23, U+FA24, U+FA27, U+FA28, and U+FA29)
                        */
                switch (codePoint) {
                case 0xFA0E:
                case 0xFA0F:
                case 0xFA11:
                case 0xFA13:
                case 0xFA14:
                case 0xFA1F:
                case 0xFA21:
                case 0xFA23:
                case 0xFA24:
                case 0xFA27:
                case 0xFA28:
                case 0xFA29:
                    return false;
                }
                return true;
            case UCharacter.UnicodeBlock.CJK_COMPATIBILITY_FORMS_ID:
            case UCharacter.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT_ID:
            case UCharacter.UnicodeBlock.CJK_RADICALS_SUPPLEMENT_ID:
            case UCharacter.UnicodeBlock.KANGXI_RADICALS_ID:
            case UCharacter.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO_ID:
                return true;
            }
            break;
        default:
            return true;
        }
        return
        !Normalizer.normalize(codePoint,Normalizer.NFD).equals(
                Normalizer.normalize(codePoint,Normalizer.NFKD)
                );
       
    }


    protected void difficultChar() {
        difficultCodePoint(yytext().charAt(0),yytext());
    }
    static private long start;   
    static public void main(String args[]) throws IOException {
        start = System.currentTimeMillis();
        // out = new FileWriter("src/com/hp/hpl/jena/iri/impl/iri2.jflex");
        // copy("src/com/hp/hpl/jena/iri/impl/iri.jflex");
        outRules("scheme");
        outRules("userinfo");
        outRules("xhost");
        outRules("port");
        outRules("path");
        outRules("query");
//        outRules("fragment");
        // out.close();
        //        
        // JFlex.Main.main(new
        // String[]{"src/com/hp/hpl/jena/iri/impl/iri2.jflex"});
        System.out.println(System.currentTimeMillis() - start);
    }

    private static void copy(String fname) throws IOException {
        Reader in = new FileReader(fname);
        char buf[] = new char[2048];
        while (true) {
            int sz = in.read(buf);
            if (sz == -1)
                break;
            out.write(buf, 0, sz);
        }
        in.close();
    }
//    static int count;

    static Writer out;

    static private void outRules(String name) throws IOException {
//        count = 0;
        String jflexFile = "src/com/hp/hpl/jena/iri/impl/"+name+".jflex";
        
        if (name.equals("scheme")|| name.equals("port")) {
            
        } else {
            out = new FileWriter("tmp.jflex");
            copy(jflexFile);
            jflexFile = "tmp.jflex";
            copy("src/com/hp/hpl/jena/iri/impl/xchar.jflex");
            out.close();
        }
        runJFlex(
//        JFlex.Main
//                .main(
                		new String[] { "-d", "src/com/hp/hpl/jena/iri/impl", jflexFile });
        System.out.println(System.currentTimeMillis() - start);

    }
	static void runJFlex(String[] strings) {
		Method main = null;
		try {
			Class<?> jflex = Class.forName("JFlex.Main");
			main = jflex.getMethod("main", new Class[]{
					strings.getClass()});
		} catch (Exception e) {
			System.err.println("Please include JFlex.jar on the classpath.");
			System.exit(1);
		} 
		try {
			main.invoke(null, new Object[]{strings});
		} catch (Exception e) {
			System.err.println("Problem interacting with JFlex");
			e.printStackTrace();
			System.exit(2);
		} 
		
	}




}
