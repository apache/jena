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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

import org.apache.jena.cmd.Arg;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdLineArgs;

public class TestCmdLine {
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
        assertTrue("-a argument found", !cl.contains(argA));
    }

    @Test
    public void test_Flag2() {
        String args[] = {"-a"};
        CmdLineArgs cl = new CmdLineArgs(args);
        ArgDecl argA = new ArgDecl(false, "-a");
        cl.add(argA);
        cl.process();
        assertTrue("No -a argument found", cl.contains(argA));
    }

    @Test
    public void test_Flag3() {
        String args[] = {"-a", "filename"};
        CmdLineArgs cl = new CmdLineArgs(args);
        ArgDecl argA = new ArgDecl(false, "-a");
        cl.add(argA);
        cl.process();
        assertTrue("No -a argument found", cl.contains(argA));
    }

    @Test
    public void test_Arg1() {
        String args[] = {""};
        CmdLineArgs cl = new CmdLineArgs(args);
        ArgDecl argA = new ArgDecl(true, "-arg");
        cl.add(argA);
        cl.process();
        assertTrue("-arg argument found", !cl.contains(argA));
    }

    @Test
    public void test_Arg2() {
        String args[] = {"-arg=ARG", "filename"};
        CmdLineArgs cl = new CmdLineArgs(args);
        ArgDecl argA = new ArgDecl(true, "arg");
        cl.add(argA);
        cl.process();
        assertTrue("No -arg= argument found", cl.contains(argA));
        assertEquals("", cl.getValue(argA), "ARG");
        assertEquals("", cl.getArg("arg").getValue(), "ARG");
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
        assertTrue("No -arg= argument found", cl.contains(argA));

        Iterator<String> iter = cl.getValues("arg").iterator();
        assertEquals("Argument 1", iter.next(), "V1");
        assertEquals("Argument 2", iter.next(), "V2");
    }

    public void test_addSetting() {
        String args[] = {};
        CmdLineArgs cl = new CmdLineArgs(args);
        cl.process();
        cl.addArg("extra", "value");
        Arg a = cl.getArg("extra");
        assertNotNull(a);
    }


    @Test(expected = CmdException.class)
    public void test_removeArg1() {
        String args[] = {"--arg=V1", "-v"};
        CmdLineArgs cl = new CmdLineArgs(args);
        ArgDecl argA = new ArgDecl(true, "argument", "arg");
        cl.add(argA);
        cl.removeArg(argA);
        // Exception.
        cl.process();
    }
}
