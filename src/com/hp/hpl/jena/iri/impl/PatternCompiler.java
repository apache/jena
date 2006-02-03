/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

// TODO e-mail uri list about . at end of domain name
// TODO e-mail uri list about IPv4 vs host:
// If host matches the rule for IPv4address, then it should be considered an IPv4 address literal and not a reg-name. 

package com.hp.hpl.jena.iri.impl;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;

import com.hp.hpl.jena.iri.ViolationCodes;

public class PatternCompiler implements ViolationCodes {

    // static VarPattern notMatching[] = {
    // new VarPattern("[[a]&&[b]]")
    // };
/*
    static VarPattern iri = new VarPattern(
            "^(@{scheme}:)?(//@{authority})?(@{path})?(\\?@{query})?(#@{fragment})?");

    static VarPattern scheme[] = {
            new VarPattern(
                    "@{alphaPreferLowerCase}(@{alphaPreferLowerCase}|@{digit}|[-+.])*"),
            new VarPattern("(@{alphaPreferLowerCase}|@{digit}|[\\-\\+\\.])+",
                    SCHEME_MUST_START_WITH_LETTER),
            new VarPattern("a*", EMPTY_SCHEME),
            new VarPattern("[^:/\\?#]+", ILLEGAL_CHARACTER), };

    static VarPattern alphaPreferLowerCase[] = { new VarPattern("[a-z]"),
            new VarPattern("[A-Za-z]", LOWERCASE_PREFERRED), };

    static VarPattern digit[] = { new VarPattern("[0-9]") };

    static VarPattern authority[] = {
            new VarPattern("(@{userinfo}@)?@{host}(:@{port})"),
            new VarPattern("[^/\\?#]*"), };

    static VarPattern userinfo[] = {
            new VarPattern("(@{unreserved}|@{pctEncoded}|@{subDelims}|:)*"),

            new VarPattern("[^@]*", ILLEGAL_CHARACTER), };

       static VarPattern unreserved[] = {

        new VarPattern("@{unreservedNotDot}|\\."),
    };
    static VarPattern unreservedNotDot[] = {

//            new VarPattern("[\\-a-zA-Z0-9_\\~]"),
            new VarPattern("[\\-a-zA-Z0-9_\\~]|@{unwise}"),
                    
            new VarPattern("[\\-a-zA-Z0-9_\\~\\x7F-\\uFFFF\\x00-\\x08\\x0B\\x0C\\x0E\\x0F]|@{unwise}",
                    new int[] { NON_URI_CHARACTER }),

    };

    static VarPattern unreservedDNSLabel[] = {
            new VarPattern("[\\a-z0-9_]"),
            new VarPattern("[\\a-zA-Z0-9_]", LOWERCASE_PREFERRED),
            new VarPattern(
                    "[\\a-zA-Z0-9_\\xA0-\\uFFFF]",
                    NON_URI_CHARACTER)
    };

    static VarPattern pctEncoded[] = { 
        new VarPattern("a"),
        new VarPattern("%20",PERCENT_20),
        new VarPattern("%@{upperHexDig}{2}",PERCENT),
        new VarPattern("%", ILLEGAL_PERCENT_ENCODING), 
    };

    static VarPattern subDelims[] = { new VarPattern("[!$&'()*+,;=]") };

    static VarPattern port[] = { new VarPattern("@{nonZeroDigit}@{digit}*"),
            new VarPattern("@{digit}+", PORT_SHOULD_NOT_START_IN_ZERO),
            new VarPattern("0*", PORT_SHOULD_NOT_BE_EMPTY),
            new VarPattern(".*", ILLEGAL_CHARACTER), };

    static VarPattern nonZeroDigit[] = { new VarPattern("[1-9]") };

    static VarPattern unwise[] = {
            new VarPattern("a"),
            new VarPattern("[\\>\\<\\\"{}\\|\\^`]", UNWISE_CHARACTER),
            new VarPattern("[\\>\\<\\\"{}\\|\\^`\\x20]", WHITESPACE),
            new VarPattern("[\\>\\<\\\"{}\\|\\^`\\x20\\t\\n\\r]",
                    NOT_XML_SCHEMA_WHITESPACE),

    };

    static VarPattern regname[] = {
            new VarPattern("(@{label}\\.)*@{label}"),
            new VarPattern("(@{unreserved}|@{pctEncodedHost}|@{subDelims})*",
                    NOT_DNS_NAME), 
//            new VarPattern("(@{unreserved}|@{pctEncodedHost}|@{subDelims}|@{unwise})*",
//                            NOT_DNS_NAME), 
            new VarPattern("[^:/@]*", ILLEGAL_CHARACTER), };

    static VarPattern pctEncodedHost[] = {
    // Should check pct encoding is UTF-8
            // Also punycode preferred
            new VarPattern("a"),
            new VarPattern("@{pctEncoded}", USE_PUNYCODE_NOT_PERCENTS) };

    static VarPattern label[] = {
            // new VarPattern("@{acePrefix}@{labelChar}*",LABEL_HAS_ACE_PREFIX),
            // new VarPattern("!(!@{labelAny}|@{labelDoubleDash})"),
            new VarPattern("@{labelSingleDashInside}"),
            new VarPattern("-?@{labelSingleDashInside}-?",
                    DNS_LABEL_DASH_START_OR_END),
            new VarPattern("@{acePrefix}@{labelSingleDashInside}", ACE_PREFIX),
            new VarPattern("@{acePrefix}@{labelSingleDashInside}-", new int[] {
                    ACE_PREFIX, DNS_LABEL_DASH_START_OR_END }),
            new VarPattern("@{acePrefix}(@{labelChar}|-)*-", new int[] {
                    ACE_PREFIX, DOUBLE_DASH_IN_REG_NAME,
                    DNS_LABEL_DASH_START_OR_END }),
            new VarPattern("(@{labelChar}|-)*-", new int[] {
                    DOUBLE_DASH_IN_REG_NAME, DNS_LABEL_DASH_START_OR_END }),
            new VarPattern("-(@{labelChar}|-)*", new int[] {
                    DOUBLE_DASH_IN_REG_NAME, DNS_LABEL_DASH_START_OR_END }),
            new VarPattern("@{labelAny}", DOUBLE_DASH_IN_REG_NAME),

    };

    static VarPattern labelSingleDashInside[] = { new VarPattern(
            "(@{labelChar}+-)*@{labelChar}+"), };

    // static VarPattern labelDoubleDash[] = {
    // new VarPattern("@{labelChar}*--@{labelChar}*"),
    // };
    static VarPattern labelAny[] = { new VarPattern("(-|@{labelChar})+"), };

    static VarPattern acePrefix[] = { new VarPattern("[a-z0-9]{2}--"),
            new VarPattern("[a-zA-Z0-9]{2}--", LOWERCASE_PREFERRED), };

    static VarPattern labelChar[] = {
    // new VarPattern("--",DOUBLE_DASH_IN_REG_NAME),
    new VarPattern("@{unreservedDNSLabel}|@{pctEncodedHost}") };

    // static VarPattern

    static VarPattern path[] = {

    new VarPattern("@{pathAbempty}"),
    // new VarPattern("@{pathAbsolute}"),
            // new VarPattern("@{pathNoscheme}"),
            new VarPattern("@{pathRootless}"),
            // new VarPattern("@{pathEmpty}"),
            new VarPattern("[^?#]*"),

    };

    static VarPattern pathAbempty[] = { new VarPattern("(\\/@{segment})*"), };

    static VarPattern pathRootless[] = { new VarPattern(
            "@{segmentNz}(\\/@{segment})*"), };

    static VarPattern segment[] = {
            new VarPattern("(a?|@{nonDotSegment})"),
            new VarPattern("a?|\\.|\\.\\.|@{nonDotSegment}", NON_INITIAL_DOT_SEGMENT),
            new VarPattern("[^/?#]*", ILLEGAL_CHARACTER), 
    };
    static VarPattern nonDotSegment[] = {
//      new VarPattern("@{pchar}*(@{pcharNotDot}|(\\.\\.\\.))@{pchar}*"),  
      new VarPattern(".{0,2}(@{pcharNotDot}|(\\.\\.\\.))@{pchar}*"),  
      
    };
        
    
    static VarPattern segmentNz[] = {
            new VarPattern("(\\.|(\\.\\.\\/)*(\\.\\.)|@{pchar}+)"),
            new VarPattern("[^/?#]+", ILLEGAL_CHARACTER), };

    static VarPattern pchar[] = { new VarPattern(
            "@{unreserved}|@{pctEncoded}|@{subDelims}|[:@]"), };

    static VarPattern pcharNotDot[] = { 
        new VarPattern(
            "@{unreservedNotDot}|@{pctEncoded}|@{subDelims}|[:@]"), };

    static VarPattern query[] = { 
            new VarPattern("(@{pchar}|[/\\?])*"),
            new VarPattern("[^#]*", ILLEGAL_CHARACTER), };

    static VarPattern fragment[] = { 
            new VarPattern("(@{pchar}|[/\\?])*"),
            new VarPattern("[^]*", ILLEGAL_CHARACTER), };
*/


    private static final class ExpandAndOutput extends Expansion {
        int exc[];
        int sub[];
        boolean incExc;
        /**
         * output those for which no errors in exclude,
         * and all errors in sub[] occur
         * or the inverse: at least one error in exclude
         * occurs, and at least one error in sub doesn't
         * @param exclude
         */
        ExpandAndOutput(int exclude[], int subset[], boolean incExc ) {
           exc = exclude;
           sub = subset;
           this.incExc = incExc;
        }
        int ruleCount = 1;

        void doIt(String regex, int eCount, int[] eCodes, int cCount,
                String c[]) {
            
            if (incExc == 
                ( (!overlap(exc,eCount, eCodes)) &&
                  subset(sub,eCount, eCodes) ) )
            try {
                out.write("/*\n");
                for (int j = 0; j < cCount; j++) {
                    out.write(c[j]);
                    out.write('\n');
                }
                out.write("*/\n");
        
                out.write(regex);
                out.write(" {\n");
                count++;
                out.write("rule("+count+"); ");
                for (int i = 0; i < eCount; i++)
                    out.write("error(" + errorCodeName(eCodes[i]) + ");");
                out.write("}\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        private boolean subset(int ee[], int el, int[]eCodes) {
            for (int i=0;i<ee.length;i++)
                if (!in(ee[i],el,eCodes))
                    return false;
            return true;
        }
        private boolean overlap(int ee[], int el, int[]eCodes) {
            for (int i=0;i<ee.length;i++)
                if (in(ee[i],el,eCodes))
                    return true;
            return false;
        }
        private boolean in(int e0, int eCount, int[] eCodes) {
            for (int i=0; i<eCount; i++)
                if (eCodes[i]==e0)
                     return true;
            return false;
        }
    }

    static VarPattern ipLiteral[] = { 
        new VarPattern("\\[@{ipVFuture}\\]"),
        new VarPattern("\\[@{ipV6Address}\\]"),
        new VarPattern("\\[[^]*",IP_V6_OR_FUTURE_ADDRESS_SYNTAX)
     };

    static VarPattern ipVFuture[] = {
            new VarPattern("v@{lowerHexDig}+\\.[-a-zA-Z0-9._~!$&'()*+,;=:]*") 
    };

    static VarPattern ipV6Address[] = {
            new VarPattern("((@{h16}:){6}@{ls32}" + "|::(@{h16}:){5}@{ls32}"
                    + "|@{h16}?::(@{h16}:){4}@{ls32}"
                    + "|((@{h16}:){0,1}@{h16})?::(@{h16}:){3}@{ls32}"
                    + "|((@{h16}:){0,2}@{h16})?::(@{h16}:){2}@{ls32}"
                    + "|((@{h16}:){0,3}@{h16})?::(@{h16}:){1}@{ls32}"
                    + "|((@{h16}:){0,4}@{h16})?::@{ls32}"
                    + "|((@{h16}:){0,5}@{h16})?::@{h16}"
                    + "|((@{h16}:){0,6}@{h16})?::)") 
    };

    static VarPattern h16[] = { new VarPattern("@{lowerHexDig}{1,4}"), };

    static VarPattern ls32[] = { new VarPattern(
            "(@{h16}:@{h16}|@{ipV4Address})"), 
            };

    static VarPattern ipV4Address[] = {
            new VarPattern("(@{decOctet}\\.){3}@{decOctet}"),
            new VarPattern("([0-9]+\\.){3}[0-9]+", IP_V4_OCTET_RANGE),
            new VarPattern("[0-9\\.]+\\.[0-9\\.]+", IP_V4_HAS_FOUR_COMPONENTS), 
            };

    static VarPattern decOctet[] = { new VarPattern(
            "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])") };

    static VarPattern regname[] = {
        new VarPattern("(@{label}\\.)*@{label}"),
        new VarPattern("[^]*", NOT_DNS_NAME), 
    };
//        new VarPattern("(@{unreserved}|@{pctEncodedHost}|@{subDelims}|@{unwise})*",
//                        NOT_DNS_NAME), 
//        new VarPattern("[^:/@]*", ILLEGAL_CHARACTER), };

    static VarPattern host[] = { 
        new VarPattern("@{ipLiteral}"),
        new VarPattern("@{ipV4Address}"), 
        new VarPattern("@{regname}"),
//        new VarPattern("[^:]*", ILLEGAL_CHARACTER),

};

    static VarPattern lowerHexDig[] = { new VarPattern("[0-9a-f]"),
        new VarPattern("[0-9A-Fa-f]", IPv6ADDRESS_SHOULD_BE_LOWERCASE), };
/*
static VarPattern upperHexDig[] = {
        new VarPattern("[0-9A-F]"),
        new VarPattern("[0-9A-Fa-f]", PERCENT_ENCODING_SHOULD_BE_UPPERCASE), };
*/

   /* 
static VarPattern pctEncodedHost[] = {
// Should check pct encoding is UTF-8
        // Also punycode preferred
        new VarPattern("a"),
        new VarPattern("@{pctEncoded}", USE_PUNYCODE_NOT_PERCENTS) };
*/
static VarPattern label[] = {
        // new VarPattern("@{acePrefix}@{labelChar}*",LABEL_HAS_ACE_PREFIX),
        // new VarPattern("!(!@{labelAny}|@{labelDoubleDash})"),
        new VarPattern("@{labelPrefix}(@{labelInside}@{labelPostfix})?"),

};

static VarPattern labelInside[] = {

    new VarPattern("@{labelSingleDashInside}?"),
    new VarPattern("(@{labelChar}|-)*", 
            DOUBLE_DASH_IN_REG_NAME),
};

static VarPattern labelPrefix[] = {
    new VarPattern("@{labelChar}"),
    new VarPattern("-|@{labelChar}",DNS_LABEL_DASH_START_OR_END),
    new VarPattern("@{labelChar}|@{acePrefix}",ACE_PREFIX),

    new VarPattern("@{labelChar}|@{acePrefix}|-",new int[] {
            ACE_PREFIX,
            DNS_LABEL_DASH_START_OR_END }),
};


static VarPattern labelPostfix[] = {
    new VarPattern("@{labelChar}"),
    new VarPattern("-|@{labelChar}",DNS_LABEL_DASH_START_OR_END),
};
    
    
 

static VarPattern labelSingleDashInside[] = { new VarPattern(
        "(@{labelChar}+-)*@{labelChar}+"), };

static VarPattern acePrefix[] = { new VarPattern("@{letterDigit}{2}--"), };

static VarPattern letterDigit[] = { new VarPattern("[a-z0-9]"),
        new VarPattern("[a-zA-Z0-9]", LOWERCASE_PREFERRED), };

static VarPattern labelChar[] = {
new VarPattern("@{unreservedDNSLabel}") };


static VarPattern unreservedDNSLabel[] = {
        new VarPattern("@{letterDigit}|_"),
        new VarPattern(
                "@{letterDigit}|[_\\x80-\\uFFFF]",
                NON_URI_CHARACTER)
};

        static long start;

    static public void main(String args[]) throws IOException {
        start = System.currentTimeMillis();
        // out = new FileWriter("src/com/hp/hpl/jena/iri/impl/iri2.jflex");
        // copy("src/com/hp/hpl/jena/iri/impl/iri.jflex");
//        outRules("scheme");
//        outRules("userinfo");
        outRules("host");
//        outRules("port");
//        outRules("path");
//        outRules("query");
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

    static VarPattern[] lookup(String name) {
        try {
            Field f = PatternCompiler.class.getDeclaredField(name);
            return (VarPattern[]) f.get(null);
        } catch (RuntimeException rte) {
            throw rte;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String eCodeNames[];

    static String errorCodeName(int j) {
        if (eCodeNames == null) {
            eCodeNames = new String[200];
            Field f[] = ViolationCodes.class.getDeclaredFields();
            for (int i = 0; i < f.length; i++)
                try {
                    eCodeNames[f[i].getInt(null)] = f[i].getName();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
        }
        return eCodeNames[j];
    }
    
    public static int errorCode(String s) throws NoSuchFieldException {
        Field f;
        try {
            f = ViolationCodes.class.getDeclaredField(s);
            return f.getInt(null);
        } catch (SecurityException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
    }

    static int count;

    static Writer out;

    static private void outRules(String name) throws IOException {
        count = 0;
        // if (true) throw new RuntimeException();
        out = new FileWriter("src/com/hp/hpl/jena/iri/impl/"+name+".jflex");
        copy("src/com/hp/hpl/jena/iri/impl/iri.jflex");
        out.write("%class Lexer");
        out.write(name.substring(0, 1).toUpperCase());
        out.write(name.substring(1));
        out.write("\n%%\n");
        int exc1[]=
        new int[]{DOUBLE_DASH_IN_REG_NAME,NOT_DNS_NAME};
        int empty[]= new int[0];
        int sub1[] = new int[]{ACE_PREFIX};
//        int sub2[] = new int[]{DOUBLE_DASH_IN_REG_NAME,ACE_PREFIX};
        int sub4[] = new int[]{DOUBLE_DASH_IN_REG_NAME};
        int sub3[] = new int[]{NOT_DNS_NAME};
        new ExpandAndOutput(exc1,empty,true).expand("@{" + name + "}");
     //   new ExpandAndOutput(empty,sub2,true).expand("@{" + name + "}");
        new ExpandAndOutput(sub1,sub4,true).expand("@{" + name + "}");
        new ExpandAndOutput(empty,sub3,true).expand("@{" + name + "}");
        
        out.write("\n");
        System.out.println(name + ": " + count + " expansions");
        out.close();

        JFlex.Main
                .main(new String[] { "src/com/hp/hpl/jena/iri/impl/"+name+".jflex" });
        System.out.println(System.currentTimeMillis() - start);

    }
    /*
     * 
     * Unicode LTR stuff:
     * 
     * 200E ????-??- ????? ???? 200F ?????-??-???? ???? 202A ????-??-?????
     * ????????? 202B ?????-??-???? ????????? 202C ??? ??????????? ??????????
     * 202D ????-??-????? ???????? 202E ?????-??-???? ????????
     * 
     * XSD preserve No normalization is done, the value is not changed (this is
     * the behavior required by [XML 1.0 (Second Edition)] for element content)
     * replace All occurrences of #x9 (tab), #xA (line feed) and #xD (carriage
     * return) are replaced with #x20 (space) collapse After the processing
     * implied by replace, contiguous sequences of #x20's are collapsed to a
     * single #x20, and leading and trailing #x20's are removed.
     * 
     * 
     * <xs:simpleType name="anyURI" id="anyURI"> <xs:annotation> <xs:appinfo>
     * <hfp:hasFacet name="length"/> <hfp:hasFacet name="minLength"/>
     * <hfp:hasFacet name="maxLength"/> <hfp:hasFacet name="pattern"/>
     * <hfp:hasFacet name="enumeration"/> <hfp:hasFacet name="whiteSpace"/>
     * <hfp:hasProperty name="ordered" value="false"/> <hfp:hasProperty
     * name="bounded" value="false"/> <hfp:hasProperty name="cardinality"
     * value="countably infinite"/> <hfp:hasProperty name="numeric"
     * value="false"/> </xs:appinfo> <xs:documentation
     * source="http://www.w3.org/TR/xmlschema-2/#anyURI"/> </xs:annotation>
     * <xs:restriction base="xs:anySimpleType"> <xs:whiteSpace fixed="true"
     * value="collapse" id="anyURI.whiteSpace"/> </xs:restriction>
     * </xs:simpleType>
     * 
     * XML 1.0
     * 
     * [2] Char ::= #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] |
     * [#x10000-#x10FFFF] /* any Unicode character, excluding the surrogate
     * blocks, FFFE, and FFFF.
     * 
     * 
     * Note:
     * 
     * Document authors are encouraged to avoid "compatibility characters", as
     * defined in section 6.8 of [Unicode] (see also D21 in section 3.6 of
     * [Unicode3]). The characters defined in the following ranges are also
     * discouraged. They are either control characters or permanently undefined
     * Unicode characters:
     * 
     * [#x7F-#x84], [#x86-#x9F], [#xFDD0-#xFDDF], [#1FFFE-#x1FFFF],
     * [#2FFFE-#x2FFFF], [#3FFFE-#x3FFFF], [#4FFFE-#x4FFFF], [#5FFFE-#x5FFFF],
     * [#6FFFE-#x6FFFF], [#7FFFE-#x7FFFF], [#8FFFE-#x8FFFF], [#9FFFE-#x9FFFF],
     * [#AFFFE-#xAFFFF], [#BFFFE-#xBFFFF], [#CFFFE-#xCFFFF], [#DFFFE-#xDFFFF],
     * [#EFFFE-#xEFFFF], [#FFFFE-#xFFFFF], [#10FFFE-#x10FFFF].
     * 
     * 
     * XML 1.1 [Definition: A parsed entity contains text, a sequence of
     * characters, which may represent markup or character data.] [Definition: A
     * character is an atomic unit of text as specified by ISO/IEC 10646
     * [ISO/IEC 10646]. Legal characters are tab, carriage return, line feed,
     * and the legal characters of Unicode and ISO/IEC 10646. The versions of
     * these standards cited in A.1 Normative References were current at the
     * time this document was prepared. New characters may be added to these
     * standards by amendments or new editions. Consequently, XML processors
     * MUST accept any character in the range specified for Char.] Character
     * Range [2] Char ::= [#x1-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF] /*
     * any Unicode character, excluding the surrogate blocks, FFFE, and FFFF. * /
     * [2a] RestrictedChar ::= [#x1-#x8] | [#xB-#xC] | [#xE-#x1F] | [#x7F-#x84] |
     * [#x86-#x9F]
     * 
     * The mechanism for encoding character code points into bit patterns MAY
     * vary from entity to entity. All XML processors MUST accept the UTF-8 and
     * UTF-16 encodings of Unicode [Unicode]; the mechanisms for signaling which
     * of the two is in use, or for bringing other encodings into play, are
     * discussed later, in 4.3.3 Character Encoding in Entities.
     * 
     * Note:
     * 
     * Document authors are encouraged to avoid "compatibility characters", as
     * defined in Unicode [Unicode]. The characters defined in the following
     * ranges are also discouraged. They are either control characters or
     * permanently undefined Unicode characters:
     * 
     * [#x7F-#x84], [#x86-#x9F], [#xFDD0-#xFDDF], [#1FFFE-#x1FFFF],
     * [#2FFFE-#x2FFFF], [#3FFFE-#x3FFFF], [#4FFFE-#x4FFFF], [#5FFFE-#x5FFFF],
     * [#6FFFE-#x6FFFF], [#7FFFE-#x7FFFF], [#8FFFE-#x8FFFF], [#9FFFE-#x9FFFF],
     * [#AFFFE-#xAFFFF], [#BFFFE-#xBFFFF], [#CFFFE-#xCFFFF], [#DFFFE-#xDFFFF],
     * [#EFFFE-#xEFFFF], [#FFFFE-#xFFFFF], [#10FFFE-#x10FFFF].
     */

}

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP All rights
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

