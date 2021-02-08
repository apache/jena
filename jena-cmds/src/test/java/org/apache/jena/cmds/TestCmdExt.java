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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.jena.cmd.Cmds;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestCmdExt {

    private static String CMD = "jena.$ext$";
    private static PrintStream stderr = null;

    @Before
    public void beforeTest() {
        stderr = System.err;
        System.setErr(new PrintStream(NullOutputStream.NULL_OUTPUT_STREAM));
    }

    @After
    public void afterTest() {
        if ( stderr != null )
            System.setErr(stderr);
    }

    @Test
    public void cmd_ext_1() {
        AtomicReference<String> holder = new AtomicReference<>(null);

        Cmds.injectCmd(CMD, (a) -> holder.set(a[0]));
        Cmds.exec(CMD, new String[]{"set"});

        assertNotNull(holder.get());
    }

    @Test
    public void cmd_ext_2() {
        AtomicReference<String> holder = new AtomicReference<>(null);

        Cmds.injectCmd(CMD, (a) -> holder.set(a[0]));
        // No call ; nothing happened.
        assertNull(holder.get());
    }

    @Test
    public void cmd_ext_3() {
        AtomicReference<String> holder = new AtomicReference<>(null);
        // Not registered!
        Cmds.exec(CMD, new String[]{"set"});
    }
}
