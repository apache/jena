/**
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

import java.io.* ;
import java.lang.reflect.Method ;

public class AbsLexerBuilder
{
    static private long start;   
    static public void main(String args[]) throws IOException {
        start = System.currentTimeMillis();
        // out = new FileWriter("src/main/jflex/org/apache/jena/iri/impl/iri2.jflex");
        // copy("src/main/main/jflex/org/apache/jena/iri/impl/iri.jflex");
        outRules("scheme");
        outRules("userinfo");
        outRules("xhost");
        outRules("port");
        outRules("path");
        outRules("query");
        //      outRules("fragment");
        // out.close();
        //        
        // JFlex.Main.main(new
        // String[]{"src/main/main/jflex/org/apache/jena/iri/impl/iri2.jflex"});
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
    //  static int count;

    static Writer out;

    static private void outRules(String name) throws IOException {
        //      count = 0;
        String jflexFile = "src/main/jflex/org/apache/jena/iri/impl/"+name+".jflex";

        if (name.equals("scheme")|| name.equals("port")) {

        } else {
            out = new FileWriter("tmp.jflex");
            copy(jflexFile);
            jflexFile = "tmp.jflex";
            copy("src/main/jflex/org/apache/jena/iri/impl/xchar.jflex");
            out.close();
        }
        runJFlex(new String[] { "-d", "src/main/java/org/apache/jena/iri/impl", jflexFile });
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

