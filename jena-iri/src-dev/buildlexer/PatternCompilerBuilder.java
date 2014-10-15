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

// TODO e-mail uri list about . at end of domain name
// TODO e-mail uri list about IPv4 vs host:
// If host matches the rule for IPv4address, then it should be considered an IPv4 address literal and not a reg-name. 

package buildlexer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;

import org.apache.jena.iri.ViolationCodes ;


public class PatternCompilerBuilder implements ViolationCodes {

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

        @Override
        public void doIt(String regex, int eCount, int[] eCodes, int cCount,
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

    static long start;

    static public void main(String args[]) throws IOException {
        start = System.currentTimeMillis();
        // out = new FileWriter("src/main/java/org/apache/jena/iri/impl/iri2.jflex");
        // copy("src/main/java/org/apache/jena/iri/impl/iri.jflex");
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
        // String[]{"src/main/java/com/hp/hpl/jena/iri/impl/iri2.jflex"});
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

    static String eCodeNames[];

    static String errorCodeName(int j) {
		if (eCodeNames == null) {
            eCodeNames = constantsFromClass(ViolationCodes.class, 200);
        }
        return eCodeNames[j];
    }

	static String[] constantsFromClass(Class<?> cl, int cnt) {
		String[] names;
		names = new String[cnt];
		Field f[] = cl.getDeclaredFields();
		for (int i = 0; i < f.length; i++)
		    try {
		        names[f[i].getInt(null)] = f[i].getName();
		    } catch (IllegalArgumentException e) {
		        e.printStackTrace();
		    } catch (IllegalAccessException e) {
		        e.printStackTrace();
		    }
		return names;
	}
    
    static int count;

    static Writer out;

    static private void outRules(String name) throws IOException {
        count = 0;
        // if (true) throw new RuntimeException();
        out = new FileWriter("src/main/jflex/org/apache/jena/iri/impl/"+name+".jflex");
        copy("src/main/jflex/org/apache/jena/iri/impl/iri.jflex");
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

        MainGenerateLexers.runJFlex(new String[] { "-d", "src/main/java/org/apache/jena/iri/impl", "src/main/jflex/org/apache/jena/iri/impl/"+name+".jflex" });
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
