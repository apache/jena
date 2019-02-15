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

package org.apache.jena.sparql.function.library;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.graph.Node;

//import org.apache.commons.logging.*;

import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp;
import org.apache.jena.sparql.function.FunctionBase0;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/** Function that accesses the current time and returns in the timezone of the query engine. */

public class nowtz extends FunctionBase0
{
    public nowtz() { }

    public static Symbol symNowTz = SystemARQ.allocSymbol("nowtz"); 

    @Override
    public NodeValue exec() {
        Context cxt = functionEnv.getContext();
        if ( cxt.isDefined(symNowTz) ) {
            NodeValue nvx = cxt.get(symNowTz);
            return nvx; 
        }
        NodeValue nvx = execAdjust();
//        String formattedDate = fromQueryTime(cxt);
//        NodeValue nvx = NodeValue.makeNode(formattedDate, null, XSD.dateTime.getURI());
        cxt.set(symNowTz, nvx);
        return nvx;
    }
    
    private NodeValue execAdjust() {
        // NOW is UTC in Jena to make the same whoever is looking.
        // For presentation reasons, you may want it in the (server) local timezone. 
        // Calculate:
        //   fn:adjust-dateTime-to-timezone(NOW(), fn:implicit-timezone())
        //   fn:adjust-dateTime-to-timezone(NOW(), afn:timezone())
        
        // Query time, in UTC. 
        NodeValue nv = SystemVar.get(ARQConstants.sysCurrentTime, super.functionEnv);
        // Timezone as xsd:dayTimeDuration.
        NodeValue nvTz = XSDFuncOp.localTimezone();
        // Comes out as "Z", not "+00:00" because of cal.toXMLFormat() in NodeValue.makeDateTime
        return XSDFuncOp.adjustDatetimeToTimezone(nv, nvTz);
    }
    
    // For information. Do it by accessing the query current time and converting using
    // ZonedDateTime.withZoneSameInstant (from Java8 java.time). 

    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxxxxx");
    private static ZoneId zoneIdUTC = ZoneOffset.UTC;
    
    private static String fromQueryTime(Context cxt) {
        // In UTC.
        Node n = cxt.get(ARQConstants.sysCurrentTime);
        String x = (n == null) ? DateTimeUtils.nowAsXSDDateTimeString() : n.getLiteralLexicalForm();
        ZonedDateTime zdt = dtf.parse(x, ZonedDateTime::from);
        ZonedDateTime zdtLocal;
        // Convert to local timezone. (maybe should put the time into context as an Instant?)
        if ( ! zoneIdUTC.equals(ZoneId.systemDefault()) ) 
            zdtLocal = zdt.withZoneSameInstant(ZoneId.systemDefault());
        else
            zdtLocal = zdt;
        return dtf.format(zdtLocal);
    }
}
