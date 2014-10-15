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

package buildlexer;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.iri.impl.PatternCompiler ;
import org.apache.jena.iri.impl.VarPattern ;


abstract public class Expansion {

    int errors[] = new int[100];

    String comments[] = new String[100];
    int cIx = 0;
    int eIx = 0;

    public Expansion() {
    }

    Map<String, String> doing = new HashMap<String, String>();

    abstract public void doIt(String regex, int eCount, int eCodes[], int cCount, String coms[]);

    public void expand(String data) {
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
                String p = doing.get(varName);
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
