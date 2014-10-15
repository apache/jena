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

package org.apache.jena.iri.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.iri.ViolationCodes ;


public class ComponentPatternParser implements ViolationCodes {
    static String separators = "([(](?![?]))|([(][?])|([)])|(\\[)|(\\])|([@][{])|([}]|[a-z]-[a-z])";

    static final int OPEN_PAREN = 1;

    static final int OPEN_NON_CAPTURING_PAREN = 2;

    static final int CLOSE_PAREN = 3;

    static final int OPEN_SQ = 4;

    static final int CLOSE_SQ = 5;

    static final int OPEN_VAR = 6;

    static final int CLOSE_BRACE = 7;
    
    static final int LOWER_CASE_RANGE = 8;

    static final int OTHER = -1;

    static final Pattern keyword = Pattern.compile(separators);

    /* .NET port does not like this. Reworked.
     *
    static final Pattern splitter = Pattern.compile("(?=" + separators
            + ")|(?<=" + separators + ")");
            
    
    public ComponentPatternParser(String p) {
        split = splitter.split(p);
        field = 0;
        classify = new int[split.length];
        for (int i = 0; i < split.length; i++)
            classify[i] = classify(split[i]);
        while (field < split.length)
            next();
//        System.err.println(p + " ==> "+ rslt.toString());
        pattern = Pattern.compile(rslt.toString());
    }
*/
    // working data
    final String split[];

    final int classify[];

    int field;

    int groupCount;

    // result data
    final StringBuffer rslt = new StringBuffer();

    int shouldLowerCase;

    int mustLowerCase;

    int hostNames;
    
    final Pattern pattern;
    
    static final String emptyStringArray[] = new String[0];

    static private String[] mySplit(String p) {
        //return splitter.split(p); 
        
        Matcher m = keyword.matcher(p);
        List<String> rslt = new ArrayList<>();
        int pos = 0;
//        rslt.add("");
        while (m.find()) {
            if (m.start()>pos || pos==0) {
                rslt.add(p.substring(pos,m.start()));
            }
            rslt.add(p.substring(m.start(),m.end()));
            pos = m.end();
        }
        if (pos < p.length())
            rslt.add(p.substring(pos));
        
//        m.
//        String preSplit[] = keyword.split(p);
//        String rslt[] = new String[preSplit.length*2];
        
        return rslt.toArray(emptyStringArray);
        
    }
    
//    static private String[] mySplitx(String p) {
//        String r[] = mySplit(p);
//        String s[] = splitter.split(p);
//        if (r.length!=s.length) {
//            System.err.println("Bad lengths: "+p+","+r.length+","+s.length);
//        }
//        for (int i=0;i<r.length && i <s.length;i++)
//            if (!r[i].equals(s[i]))
//                System.err.println("Bad component: "+p+","+r[i]+","+s[i]);
//        return r;
//        
//        
//    }
    // end result data
    public ComponentPatternParser(String p) {
        split = mySplit(p);
        field = 0;
        classify = new int[split.length];
        for (int i = 0; i < split.length; i++)
            classify[i] = classify(split[i]);
        while (field < split.length)
            next();
//        System.err.println(p + " ==> "+ rslt.toString());
        pattern = Pattern.compile(rslt.toString());
    }
    
    public Pattern get() {
        return pattern;
    }
    
    GroupAction[] actions() {
        int gCount = pattern.matcher("").groupCount()+1;
        GroupAction result[] = new GroupAction[gCount];
        for (int i=1;i<gCount;i++) {
            int g = 1<<i;
            if ((mustLowerCase & g)!=0)
                result[i] = new ErrorAction(SCHEME_REQUIRES_LOWERCASE);
            else if ((shouldLowerCase & g)!=0)
                result[i] = new ErrorAction(SCHEME_PREFERS_LOWERCASE);
            else if ((hostNames & g)!=0)
                result[i] = new HostAction(i);
            else
                result[i] = GroupAction.NoAction;
        }
        return result;
    }

    private int classify(String string) {
        Matcher m = keyword.matcher(string);
        if (!m.matches())
            return OTHER;
        for (int i = 1; i <= m.groupCount(); i++)
            if (m.start(i) != -1)
                return i;
        throw new IllegalStateException(
                "IRI code internal error: no group matched.");
    }

    private void untilCloseSq() {
        while (classify[field - 1] != CLOSE_SQ) {
            if (field >= split.length)
                throw new IllegalArgumentException(
                        "Internal IRI code error. Did not find CLOSE_SQ in until().");
            add();
        }
    }

    @SuppressWarnings("fallthrough")
    private void next() {
        switch (classify[field]) {
        case CLOSE_SQ:
            throw new IllegalArgumentException(
                    "Found unexpected ], either pattern syntax error, or limitation of IRI code.");
        case OPEN_SQ:
            add();
            untilCloseSq();
            break;
        case OPEN_VAR:
            field++;
            rslt.append("(");
            groupCount++;
            if (split[field].equals("host")) {
                addHost();
            } else {
                if (split[field].equals("shouldLowerCase")) {

                    shouldLowerCase |= (1 << groupCount);
                } else if (split[field].equals("mustLowerCase")) {

                    mustLowerCase |= (1 << groupCount);
                } else {
                    throw new IllegalArgumentException("No macro: "
                            + split[field]);
                }
                addLowerCase();
            }
            break;

        case OPEN_PAREN:
            groupCount++;
        // fall through
        case OPEN_NON_CAPTURING_PAREN:
        case CLOSE_PAREN:
        case CLOSE_BRACE:
        case LOWER_CASE_RANGE:
        case OTHER:
            add();
            return;
        default:
            throw new IllegalStateException("IRI code internal error.");
        }
    }

    @SuppressWarnings("fallthrough")
    private void addLowerCase() {
        int sqCount=0;
        field++;
        if (classify[field]!=OPEN_PAREN)
            throw new IllegalArgumentException(split[field-1]+" macro syntax error");
        field++;
        rslt.append("?:(?:");  // make group non-capturing.
        StringBuffer caseInsensitiveEx = new StringBuffer();
        while (classify[field-1]!=CLOSE_PAREN || sqCount>0 ) {
            if (field >= split.length)
                throw new IllegalArgumentException(
                        "Internal IRI code error. Did not find CLOSE_PAREN in addLowerCase().");
           
            switch (classify[field]) {
            case OPEN_SQ:
                sqCount++;
                caseInsensitiveEx.append('[');
                break;
            case CLOSE_SQ:
                sqCount--;
                caseInsensitiveEx.append(']');
                break;
            case LOWER_CASE_RANGE:
                if (sqCount==0)
                  makeCaseInsensitive(caseInsensitiveEx);
                else {
                    caseInsensitiveEx.append(split[field]);
                    caseInsensitiveEx.append((char)(split[field].charAt(0)-'a'+'A'));
                    caseInsensitiveEx.append('-');
                    caseInsensitiveEx.append((char)(split[field].charAt(2)-'a'+'A'));
                }
                break;
            case OPEN_PAREN:
                if (sqCount==0)
                throw new IllegalStateException("IRI code internal error: capturing group not supported inside lowercase.");
                // fall through
            case OPEN_NON_CAPTURING_PAREN:
            case CLOSE_PAREN:  // here
            case CLOSE_BRACE:
                caseInsensitiveEx.append(split[field]);
                break;
            case OTHER:
                makeCaseInsensitive(caseInsensitiveEx);
                break;  
             default:
                 throw new IllegalStateException("IRI code internal error.");
           }
            add();
        }
        if (classify[field]!=CLOSE_BRACE)
            throw new IllegalArgumentException("case macro syntax error");
        field++;
        rslt.append("|("); // start capturing group
        rslt.append(caseInsensitiveEx);
        rslt.append(")");
    }

    private void makeCaseInsensitive(StringBuffer caseInsensitiveEx) {
        for (int i=0;i<split[field].length();i++) {
            char c = split[field].charAt(i);
            if (c>='a' && c<='z') {
                caseInsensitiveEx.append('[');
                caseInsensitiveEx.append(c);
                caseInsensitiveEx.append((char)(c-'a'+'A'));
                caseInsensitiveEx.append(']');
            }
        }
    }

    private void addHost() {
        hostNames |= (1 << groupCount);
        field++;
        if (classify[field]!=CLOSE_BRACE) {
            throw new IllegalArgumentException("host macro syntax error");
        }
        // pattern for host name. A sequence of chars that are not reserved.
        // or an IP v6 or future address which starts and ends with [ ] and may
        // include :.
        rslt.append("[^\\[\\]:/?#@!$&'()*+,;=]*|\\[[^\\[\\]/?#@!$&'()*+,;=]*\\])");
        field++;
    }

    private void add() {
        rslt.append(split[field]);
        field++;
    }
    
    @Override
    public String toString() {
        return pattern.pattern();
    }

}
