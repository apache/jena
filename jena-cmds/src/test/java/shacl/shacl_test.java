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

package shacl;

import jena.cmd.CmdGeneral;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.shacl.testing.RunManifest;
import org.apache.jena.sys.JenaSystem;

public class shacl_test extends CmdGeneral {

    static {
        LogCtl.setCmdLogging();
        JenaSystem.init();
    }

    public shacl_test(String[] argv) {
        super(argv);
    }

    public static void main (String... argv) {
        new shacl_test(argv).mainRun() ;
    }
    
    @Override
    protected String getSummary() {
        return getCommandName()+" FILE";
    }

    @Override
    protected void exec() {
        if ( getPositional().isEmpty() ) {
            Log.warn(this, "No manifests");
        }
        
        for ( String fn : getPositional() ) {
            RunManifest.runTest(fn, isVerbose());
        }
    }

    @Override
    protected String getCommandName() {
        return "shacl_test";
    }
}

