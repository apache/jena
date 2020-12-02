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

package arq;

import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.util.QueryExecUtils;
import arq.cmdline.CmdARQ;
import arq.cmdline.ModResultsIn;
import arq.cmdline.ModResultsOut;

/** Read and write result sets */

public class rset extends CmdARQ {
    ModResultsIn  modInput  = new ModResultsIn();
    ModResultsOut modOutput = new ModResultsOut();

    static String usage     = rset.class.getName() + " [--in syntax] [--out syntax] [--file FILE | FILE ]";

    public static void main(String...argv) {
        new rset(argv).mainRun();
    }

    public rset(String[] argv) {
        super(argv);
        super.addModule(modInput);
        super.addModule(modOutput);
    }

    @Override
    protected void processModulesAndArgs() {
        super.processModulesAndArgs();
    }

    @Override
    protected String getSummary() {
        return usage;
    }

    @Override
    protected void exec() {
        ResultSet rs = modInput.getResultSet();
        QueryExecUtils.outputResultSet(rs, null, modOutput.getResultsFormat(), System.out);
    }

    @Override
    protected String getCommandName() {
        return "rset";
    }

}
