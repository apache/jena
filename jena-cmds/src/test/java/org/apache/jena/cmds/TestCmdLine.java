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

package org.apache.jena.cmds;


import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

import org.apache.jena.cmd.*;

public class TestCmdLine {

    private static String DIR = "testing/cmd/";

    @Test
    public void test_Simple1() {
        String args[] = {""};
        CmdLineArgs cl = new CmdLineArgs(args);
        cl.process();
    }

    @Test
    public void test_Flag1() {
        String args[] = {""};
        CmdLineArgs cl = new CmdLineArgs(args);
        ArgDecl argA = new ArgDecl(false, "-a");
        cl.add(argA);
        cl.process();
        assertTrue(!cl.contains(argA), "-a argument found");
    }

    @Test
    public void test_Flag2() {
        String args[] = {"-a"};
        CmdLineArgs cl = new CmdLineArgs(args);
        ArgDecl argA = new ArgDecl(false, "-a");
        cl.add(argA);
        cl.process();
        assertTrue(cl.contains(argA), "No -a argument found");
    }

    @Test
    public void test_Flag3() {
        String args[] = {"-a", "filename"};
        CmdLineArgs cl = new CmdLineArgs(args);
        ArgDecl argA = new ArgDecl(false, "-a");
        cl.add(argA);
        cl.process();
        assertTrue(cl.contains(argA), "No -a argument found");
    }

    @Test
    public void test_Arg1() {
        String args[] = {""};
        CmdLineArgs cl = new CmdLineArgs(args);
        ArgDecl argA = new ArgDecl(true, "-arg");
        cl.add(argA);
        cl.process();
        assertTrue(!cl.contains(argA), "-arg argument found");
    }

    @Test
    public void test_Arg2() {
        String args[] = {"-arg=ARG", "filename"};
        CmdLineArgs cl = new CmdLineArgs(args);
        ArgDecl argA = new ArgDecl(true, "arg");
        cl.add(argA);
        cl.process();
        assertTrue(cl.contains(argA), "No -arg= argument found");
        assertEquals(cl.getValue(argA), "ARG");
        assertEquals(cl.getArg("arg").getValue(), "ARG");
    }

    @Test
    public void test_nArg1() {
        String args[] = {"-arg=V1", "--arg=V2", "-v"};
        CmdLineArgs cl = new CmdLineArgs(args);
        ArgDecl argA = new ArgDecl(true, "-arg");
        cl.add(argA);
        ArgDecl argV = new ArgDecl(false, "-v");
        cl.add(argV);
        cl.process();
        assertTrue(cl.contains(argA), "No -arg= argument found");

        Iterator<String> iter = cl.getValues("arg").iterator();
        assertEquals(iter.next(), "V1", ()->"Argument 1");
        assertEquals(iter.next(), "V2", ()->"Argument 2");
    }

    public void test_addSetting() {
        String args[] = {};
        CmdLineArgs cl = new CmdLineArgs(args);
        cl.process();
        cl.addArg("extra", "value");
        Arg a = cl.getArg("extra");
        assertNotNull(a);
    }

    @Test
    public void test_removeArg1() {
        String args[] = {"--arg=V1", "-v"};
        CmdLineArgs cl = new CmdLineArgs(args);
        ArgDecl argA = new ArgDecl(true, "argument", "arg");
        cl.add(argA);
        cl.removeArg(argA);
        // Exception.
        assertThrows(CmdException.class, ()->cl.process());
    }

    @Test
    public void args_no_file_1() {
        String[] args = {};
        testArgsFileGood(args, new String[0]);
    }

    @Test
    public void args_no_file_2() {
        String[] args = {"-q", "-v", "positional"};
        testArgsFileGood(args, "-q", "-v", "positional");
    }

    @Test
    public void args_file_01() {
        String[] args = {"@"+DIR+"args-good-1"};
        testArgsFileGood(args, "--arg", "--arg1", "value1", "--arg2=value2", "--empty=", "--trailingspaces", "-q", "positional 1", "positional 2");
    }

    @Test
    public void args_file_02() {
        String[] args = {"@"+DIR+"args-good-2"};
        testArgsFileGood(args, "-arg", "--", "positional");
    }

    @Test
    public void args_file_03() {
        String[] args = {"@"+DIR+"args-good-3"};
        testArgsFileGood(args, "text");
    }


    @Test
    public void args_file_bad_01() {
        String[] args = {"@"+DIR+"args-good-1", "--another"};
        testArgsFileBad(args);
    }

    @Test
    public void args_file_bad_02() {
        String[] args = {"@"+DIR+"args-good-1", "@"+DIR+"args-good-2"};
        testArgsFileBad(args);
    }

    @Test
    public void args_file_bad_03() {
        String[] args = {"@"};
        testArgsFileBad(args);
    }

    @Test
    public void args_file_bad_04() {
        String[] args = {"@ filename"};
        testArgsFileBad(args);
    }

    @Test
    public void args_file_bad_file_01() {
        String[] args = {"@"+DIR+"args-bad-1"};
        testArgsFileBad(args);
    }

    private void testArgsFileGood(String[] args, String...expected) {
        String[] args2 = Args.argsPrepare(args);
        //assertArrayEquals(expected, args2, ()->{ return "Expected: "+Arrays.asList(expected)+" Got: "+Arrays.asList(args2)});
        assertArrayEquals(expected, args2, ()->("Expected: "+Arrays.asList(expected)+" Got: "+Arrays.asList(args2)));
    }

    private void testArgsFileBad(String[] args) {
        assertThrows(CmdException.class, ()->Args.argsPrepare(args));
    }

}
