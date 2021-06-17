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

package org.apache.jena.shex;

import org.apache.jena.arq.junit.runners.Directories;
import org.apache.jena.arq.junit.runners.Label;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.riot.SysRIOT;
import org.apache.jena.shex.runner.RunnerShexBadSyntax;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(RunnerShexBadSyntax.class)
@Label("Shex Bad Syntax")
@Directories({
    "src/test/files/spec/negativeSyntax"
})

public class TestShexSyntaxBad {

    private static String loglevel;

    @BeforeClass public static void beforeClass() {
        loglevel = LogCtl.getLevel(SysRIOT.riotLoggerName);
        LogCtl.setLevel(SysRIOT.riotLoggerName, "FATAL");
    }

    @AfterClass public static void afterClass() {
        LogCtl.setLevel(SysRIOT.riotLoggerName, loglevel);
    }

}
