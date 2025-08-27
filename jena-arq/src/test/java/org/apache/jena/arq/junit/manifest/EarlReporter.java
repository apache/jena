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

package org.apache.jena.arq.junit.manifest;

import org.apache.jena.arq.junit.EarlReport;

public class EarlReporter {
    // Only one earl test report at a time.

    public static EarlReport currentEarlReport = null;

    public static void setEarlReport(EarlReport earlReport) {
        currentEarlReport = earlReport;
    }

    public static void clearEarlReport() { currentEarlReport = null; }

    public static void success(String testURI) {
        if ( currentEarlReport != null )
            currentEarlReport.success(testURI);
    }

    public static void failure(String testURI) {
        if ( currentEarlReport != null )
            currentEarlReport.failure(testURI);
    }

    public static void ignored(String testURI) {
//        if ( currentEarlReport != null )
//            currentEarlReport.notApplicable(testURI);
    }

    public static void notApplicable(String testURI) {
        if ( currentEarlReport != null )
            currentEarlReport.notApplicable(testURI);
    }
}
