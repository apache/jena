/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri.impl;

import java.util.HashMap;
import java.util.Map;

abstract public class Expansion {

    int errors[] = new int[100];

    String comments[] = new String[100];
    int cIx = 0;
    int eIx = 0;

    public Expansion() {
    }

    Map doing = new HashMap();

    abstract void doIt(String regex, int eCount, int eCodes[], int cCount, String coms[]);

    void expand(String data) {
        int at;

        at = data.indexOf("@{labelI");
        if ( at==-1 )
          at = data.indexOf("@{");
        if (at == -1)
            doIt(data, eIx, errors, cIx, comments);
        else {
//            String prefix = data.substring(0, at);
            int match = data.indexOf('}', at);
            String varName = data.substring(at + 2, match);
            if (doing.containsKey(varName)) {
//                System.err.println("Possible Recursion: " + varName);
                String p = (String) doing.get(varName);
                String nData = data.replaceAll("@\\{" + varName + "\\}", p);
                expand(nData);
            } else {
//                String postfix = data.substring(match + 1);
                VarPattern vp[] = PatternCompiler.lookup(varName);
                int eIxBase = eIx;
                for (int i = 0; i < vp.length; i++) {

                    addErrors(vp[i].errors);
                    String nData;
                    String p;
                    try {
                        p = vp[i].pattern;
                        p = p.replaceAll("\\\\", "\\\\\\\\");
                        p = p.replaceAll("\\$", "\\\\\\$");
                        p = "(" + p + ")";
                        doing.put(varName, p);
                        nData = data.replaceAll("@\\{" + varName + "\\}", p);
                    } catch (RuntimeException e) {
                        System.err.println(data);
                        System.err.println(varName);
                        System.err.println(vp[i].pattern);
                        throw e;

                    }
                    comments[cIx++] = varName + " => " + p;
                    expand(nData);
                    eIx = eIxBase;
                    cIx--;
                }
                doing.remove(varName);
            }
        }
    }

    void addErrors(int e[]) {
        for (int i = 0; i < e.length; i++)
            errors[eIx++] = e[i];
    }

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

