/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package reports;
//package com.ibm.lld.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.openjena.atlas.lib.FileOps ;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.util.Timer ;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.block.FileMode ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

//public class TDBOutOfMemoryTest 
public class ReportOutOfMemoryManyGraphsTDB
{
    //public static final String TDB_DIR = "D:/work/relm/outofmem_jena_DB";
    public static final String TDB_DIR = "DB1";
    public static final int NOGRAPHS = 100000; // Number of data graphs to load

    public static void main( String[] args ) {
        
        if ( false )
        {
            // Set the TDB properties file.
            System.setProperty("com.hp.hpl.jena.tdb.settings", "tdb.properties") ;

            System.out.printf("Block read cache      = %d\n", SystemTDB.BlockReadCacheSize) ;
            System.out.printf("Block write cache     = %d\n", SystemTDB.BlockWriteCacheSize) ;

            System.out.printf("Node -> NodeId cache  = %d\n", SystemTDB.Node2NodeIdCacheSize) ;
            System.out.printf("NodeId -> Node cache  = %d\n", SystemTDB.NodeId2NodeCacheSize) ;
        }
        System.out.printf("Max mem: %,dM\n", (Runtime.getRuntime().maxMemory()/(1024*1024))) ;
        
        if ( true )
        {
            System.out.println("DIRECT mode") ;
            SystemTDB.setFileMode(FileMode.direct) ;
        }
        
        FileOps.clearDirectory(TDB_DIR) ;
        System.out.println("> Starting test: " + new java.util.Date());
        Timer timer = new Timer() ;
        timer.startTimer() ;
        
        Dataset dataset = TDBFactory.createDataset(TDB_DIR);
        System.out.println("> Initial number of indexed graphs: " + dataset.asDatasetGraph().size());
        try {
            for (int i=0; i<NOGRAPHS; ) {
                InputStream instream = getGraph(i); // the RDF graph to load
                dataset.getLock().enterCriticalSection(Lock.WRITE);
                try {
                    Model model = dataset.getNamedModel("https://jazz.net/jazz/resource/itemName/com.ibm.team.workitem.WorkItem/" + i);
                    model.read(instream, null);
                    //model.close();
                } finally { dataset.getLock().leaveCriticalSection() ; }
                if (++i % 100 == 0) System.out.println(i/100 + "00 at: " + new java.util.Date());
                instream.close();
            }
            TDB.sync(dataset);
            dataset.close();
            System.out.println("> Done at: " + new java.util.Date());
            long x = timer.endTimer() ;
            System.out.printf("%,d graphs in %,.2f sec\n", NOGRAPHS, (x/1000.0)) ; 

        }
        catch (IOException e) {
            System.out.println("> Failed: " + e.getMessage());
        }
    }

    private static InputStream getGraph(int no) {
        String graph = GRAPH_TEMPLATE.replaceAll("%NUMBER%", String.valueOf(no));
        return new ByteArrayInputStream(graph.getBytes());
    }
    
    private static final String GRAPH_TEMPLATE =
        "<rdf:RDF\n" +
        "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
        "    xmlns:j.0=\"http://open-services.net/ns/core#\"\n" +
        "    xmlns:j.1=\"http://open-services.net/ns/cm-x#\"\n" +
        "    xmlns:j.2=\"http://purl.org/dc/terms/\"\n" +
        "    xmlns:j.3=\"http://open-services.net/ns/cm#\"\n" +
        "    xmlns:j.5=\"http://jazz.net/xmlns/prod/jazz/rtc/ext/1.0/\"\n" +
        "    xmlns:j.4=\"http://jazz.net/xmlns/prod/jazz/rtc/cm/1.0/\" > \n" +
        "  <rdf:Description rdf:nodeID=\"A0\">\n" +
        "    <j.2:title>@mandrew</j.2:title>\n" +
        "    <rdf:type rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement\"/>\n" +
        "    <rdf:object rdf:resource=\"https://jazz.net/jazz/oslc/automation/persons/_y4JlcPYJEdqU64Cr2VV0dQ\"/>\n" +
        "    <rdf:predicate rdf:resource=\"http://jazz.net/xmlns/prod/jazz/rtc/cm/1.0/com.ibm.team.workitem.linktype.textualReference.textuallyReferenced\"/>\n" +
        "    <rdf:subject rdf:resource=\"https://jazz.net/jazz/resource/itemName/com.ibm.team.workitem.WorkItem/%NUMBER%\"/>\n" +
        "  </rdf:Description>\n" +
        "  <rdf:Description rdf:about=\"https://jazz.net/jazz/resource/itemName/com.ibm.team.workitem.WorkItem/%NUMBER%\">\n" +
        "    <j.2:title rdf:parseType=\"Literal\">Process REST Service doesn't scale</j.2:title>\n" +
        "    <rdf:type rdf:resource=\"http://open-services.net/ns/cm#ChangeRequest\"/>\n" +
        "    <j.4:com.ibm.team.workitem.linktype.textualReference.textuallyReferenced rdf:resource=\"https://jazz.net/jazz/oslc/automation/persons/_P_wUELLTEduhAusIxeOxbA\"/>\n" +
        "    <j.4:com.ibm.team.workitem.linktype.textualReference.textuallyReferenced rdf:resource=\"https://jazz.net/jazz/oslc/automation/persons/_gTuTMG62Edu8R4joT9P1Ug\"/>\n" +
        "    <j.4:com.ibm.team.workitem.linktype.textualReference.textuallyReferenced rdf:resource=\"https://jazz.net/jazz/oslc/automation/persons/_y4JlcPYJEdqU64Cr2VV0dQ\"/>\n" +
        "    <j.4:com.ibm.team.workitem.linktype.textualReference.textuallyReferenced rdf:resource=\"https://jazz.net/jazz/oslc/automation/persons/_vCJP8OeKEduR89vYjZT89g\"/>\n" +
        "    <j.4:com.ibm.team.workitem.linktype.textualReference.textuallyReferenced rdf:resource=\"https://jazz.net/sandbox02/ccm/service/com.ibm.team.process.internal.common.service.IProcessRestService/processAreasForUser?userId=shilpat\"/>\n" +
        "    <j.4:com.ibm.team.workitem.linktype.textualReference.textuallyReferenced rdf:resource=\"https://jazz.net/jazz/oslc/automation/persons/_DziEAHHfEdyLLb7t1B32_A\"/>\n" +
        "    <j.4:com.ibm.team.workitem.linktype.textualReference.textuallyReferenced rdf:resource=\"https://jazz.net/wiki/bin/view/Main/DraftTeamProcessRestApi#Project_Areas_collection\"/>\n" +
        "    <j.4:com.ibm.team.workitem.linktype.textualReference.textuallyReferenced rdf:resource=\"https://jazz.net/jazz/resource/itemName/com.ibm.team.workitem.WorkItem/154737\"/>\n" +
        "    <j.3:fixed>false</j.3:fixed>\n" +
        "    <j.0:discussion rdf:resource=\"https://jazz.net/jazz/oslc/workitems/_p0Lr8D7SEeCl0bUDoWAOSQ/rtc_cm:comments\"/>\n" +
        "    <j.2:contributor rdf:resource=\"https://jazz.net/jazz/oslc/users/_y4JlcPYJEdqU64Cr2VV0dQ\"/>\n" +
        "    <j.5:client rdf:resource=\"https://jazz.net/jazz/oslc/enumerations/_Q2fMII8EEd2Q-OW8dr3S5w/client/client.literal.l12\"/>\n" +
        "    <j.4:plannedFor rdf:resource=\"https://jazz.net/jazz/oslc/iterations/_VceosAh8EeC72Mz-78YBKQ\"/>\n" +
        "    <j.2:modified>2011-02-23T22:33:45.764Z</j.2:modified>\n" +
        "    <j.5:contextId>_Q2fMII8EEd2Q-OW8dr3S5w</j.5:contextId>\n" +
        "    <j.4:timeSheet rdf:resource=\"https://jazz.net/jazz/oslc/workitems/_p0Lr8D7SEeCl0bUDoWAOSQ/rtc_cm:timeSheet\"/>\n" +
        "    <j.4:filedAgainst rdf:resource=\"https://jazz.net/jazz/resource/itemOid/com.ibm.team.workitem.Category/_YNQI4I8FEd2Q-OW8dr3S5w\"/>\n" +
        "    <j.3:verified>false</j.3:verified>\n" +
        "    <j.4:correctedEstimate></j.4:correctedEstimate>\n" +
        "    <j.1:priority rdf:resource=\"https://jazz.net/jazz/oslc/enumerations/_Q2fMII8EEd2Q-OW8dr3S5w/priority/4\"/>\n" +
        "    <j.3:approved>false</j.3:approved>\n" +
        "    <j.3:status>In Progress</j.3:status>\n" +
        "    <j.2:type>Defect</j.2:type>\n" +
        "    <j.4:modifiedBy rdf:resource=\"https://jazz.net/jazz/oslc/users/_y4JlcPYJEdqU64Cr2VV0dQ\"/>\n" +
        "    <j.2:created>2011-02-22T22:35:15.682Z</j.2:created>\n" +
        "    <j.4:timeSpent></j.4:timeSpent>\n" +
        "    <j.3:closed>false</j.3:closed>\n" +
        "    <j.0:shortTitle rdf:parseType=\"Literal\">Defect %NUMBER%</j.0:shortTitle>\n" +
        "    <j.4:state rdf:resource=\"https://jazz.net/jazz/oslc/workflows/_Q2fMII8EEd2Q-OW8dr3S5w/states/bugzillaWorkflow/2\"/>\n" +
        "    <j.4:estimate></j.4:estimate>\n" +
        "    <j.2:identifier>%NUMBER%</j.2:identifier>\n" +
        "    <j.2:description rdf:datatype=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral\">On Jazz.net, we have a 3.0 &amp;quot;sandbox&amp;quot; deployed (CCM+JTS) which allows any jazz.net user to create a project to try out RTC.&amp;nbsp; We're seeing massive performance problems due to an apparent scalability problem in process.&amp;nbsp; Currently, the sandbox has &amp;gt; 100 projects created. This is causing the following issues:&lt;br&gt;&lt;/br&gt;&lt;br&gt;&lt;/br&gt;1) Sandbox home page loads a list of the current user's projects by calling process rest service with user id.&amp;nbsp; This request takes &amp;gt; 60 seconds.&lt;br&gt;&lt;/br&gt;2) CCM app gets stuck on &amp;quot;Loading...&amp;quot; for &amp;gt; 60 seconds, spinning on the request to InitializationData.&amp;nbsp; InitData is waiting to get the response from process's initializer (which is doing a lookup based on the name of the project).&lt;br&gt;&lt;/br&gt;3) Home menu hangs for a long time waiting for the list of projects to populate (There's also a UI scaleability issue in the home menu... the number of projects exceeds available space in the viewport, but that's another item).&lt;br&gt;&lt;/br&gt;&lt;br&gt;&lt;/br&gt;This is a blocker. Process must be able to scale to hundreds, possibly thousands of projects, without slowing down the loading of the web UI.</j.2:description>\n" +
        "    <j.4:foundIn rdf:resource=\"https://jazz.net/jazz/resource/itemOid/com.ibm.team.workitem.Deliverable/_kuFJcPDhEd-1FumPcb1epw\"/>\n" +
        "    <j.5:howfound rdf:resource=\"https://jazz.net/jazz/oslc/enumerations/_Q2fMII8EEd2Q-OW8dr3S5w/howfound/howfound.literal.l3\"/>\n" +
        "    <j.2:subject>service</j.2:subject>\n" +
        "    <j.1:severity rdf:resource=\"https://jazz.net/jazz/oslc/enumerations/_Q2fMII8EEd2Q-OW8dr3S5w/severity/5\"/>\n" +
        "    <j.4:type rdf:resource=\"https://jazz.net/jazz/oslc/types/_Q2fMII8EEd2Q-OW8dr3S5w/defect\"/>\n" +
        "    <j.1:project rdf:resource=\"https://jazz.net/jazz/oslc/projectareas/_Q2fMII8EEd2Q-OW8dr3S5w\"/>\n" +
        "    <j.2:creator rdf:resource=\"https://jazz.net/jazz/oslc/users/_gTuTMG62Edu8R4joT9P1Ug\"/>\n" +
        "    <j.4:teamArea rdf:resource=\"https://jazz.net/jazz/oslc/teamareas/_ER2xcI8FEd2Q-OW8dr3S5w\"/>\n" +
        "    <j.3:reviewed>false</j.3:reviewed>\n" +
        "    <j.5:archived>false</j.5:archived>\n" +
        "    <j.4:resolvedBy rdf:resource=\"https://jazz.net/jazz/oslc/users/_YNh4MOlsEdq4xpiOKg5hvA\"/>\n" +
        "    <j.5:os rdf:resource=\"https://jazz.net/jazz/oslc/enumerations/_Q2fMII8EEd2Q-OW8dr3S5w/OS/OS.literal.l1\"/>\n" +
        "    <j.3:inprogress>true</j.3:inprogress>\n" +
        "    <j.0:serviceProvider rdf:resource=\"https://jazz.net/jazz/oslc/contexts/_Q2fMII8EEd2Q-OW8dr3S5w/workitems/services\"/>\n" +
        "    <j.4:progressTracking rdf:resource=\"https://jazz.net/jazz/oslc/workitems/_p0Lr8D7SEeCl0bUDoWAOSQ/progressTracking\"/>\n" +
        "    <j.4:com.ibm.team.filesystem.workitems.change_set.com.ibm.team.scm.ChangeSet rdf:resource=\"https://jazz.net/jazz/resource/itemOid/com.ibm.team.scm.ChangeSet/_p2Q08T7lEeC50ZOFeYh_9w\"/>\n" +
        "    <j.4:com.ibm.team.filesystem.workitems.change_set.com.ibm.team.scm.ChangeSet rdf:resource=\"https://jazz.net/jazz/resource/itemOid/com.ibm.team.scm.ChangeSet/_S7U3gT8XEeC50ZOFeYh_9w\"/>\n" +
        "  </rdf:Description>\n" +
        "  <rdf:Description rdf:nodeID=\"A1\">\n" +
        "    <j.2:title>https://jazz.net/sandbox02/ccm/service/com.ibm.team.process.internal.common.service.IProcessRestService/processAreasForUser?userId=shilpat</j.2:title>\n" +
        "    <rdf:type rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement\"/>\n" +
        "    <rdf:object rdf:resource=\"https://jazz.net/sandbox02/ccm/service/com.ibm.team.process.internal.common.service.IProcessRestService/processAreasForUser?userId=shilpat\"/>\n" +
        "    <rdf:predicate rdf:resource=\"http://jazz.net/xmlns/prod/jazz/rtc/cm/1.0/com.ibm.team.workitem.linktype.textualReference.textuallyReferenced\"/>\n" +
        "    <rdf:subject rdf:resource=\"https://jazz.net/jazz/resource/itemName/com.ibm.team.workitem.WorkItem/%NUMBER%\"/>\n" +
        "  </rdf:Description>\n" +
        "  <rdf:Description rdf:nodeID=\"A2\">\n" +
        "    <j.2:title>@packham</j.2:title>\n" +
        "    <rdf:type rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement\"/>\n" +
        "    <rdf:object rdf:resource=\"https://jazz.net/jazz/oslc/automation/persons/_vCJP8OeKEduR89vYjZT89g\"/>\n" +
        "    <rdf:predicate rdf:resource=\"http://jazz.net/xmlns/prod/jazz/rtc/cm/1.0/com.ibm.team.workitem.linktype.textualReference.textuallyReferenced\"/>\n" +
        "    <rdf:subject rdf:resource=\"https://jazz.net/jazz/resource/itemName/com.ibm.team.workitem.WorkItem/%NUMBER%\"/>\n" +
        "  </rdf:Description>\n" +
        "  <rdf:Description rdf:nodeID=\"A3\">\n" +
        "    <j.2:title>154737: Replace ProjectAreaWebUIInitializionData with a dynamic module</j.2:title>\n" +
        "    <rdf:type rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement\"/>\n" +
        "    <rdf:object rdf:resource=\"https://jazz.net/jazz/resource/itemName/com.ibm.team.workitem.WorkItem/154737\"/>\n" +
        "    <rdf:predicate rdf:resource=\"http://jazz.net/xmlns/prod/jazz/rtc/cm/1.0/com.ibm.team.workitem.linktype.textualReference.textuallyReferenced\"/>\n" +
        "    <rdf:subject rdf:resource=\"https://jazz.net/jazz/resource/itemName/com.ibm.team.workitem.WorkItem/%NUMBER%\"/>\n" +
        "  </rdf:Description>\n" +
        "  <rdf:Description rdf:nodeID=\"A4\">\n" +
        "    <j.2:title>@retchles</j.2:title>\n" +
        "    <rdf:type rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement\"/>\n" +
        "    <rdf:object rdf:resource=\"https://jazz.net/jazz/oslc/automation/persons/_gTuTMG62Edu8R4joT9P1Ug\"/>\n" +
        "    <rdf:predicate rdf:resource=\"http://jazz.net/xmlns/prod/jazz/rtc/cm/1.0/com.ibm.team.workitem.linktype.textualReference.textuallyReferenced\"/>\n" +
        "    <rdf:subject rdf:resource=\"https://jazz.net/jazz/resource/itemName/com.ibm.team.workitem.WorkItem/%NUMBER%\"/>\n" +
        "  </rdf:Description>\n" +
        "  <rdf:Description rdf:nodeID=\"A5\">\n" +
        "    <j.2:title>Changes in Process - &lt;No Comment&gt; - Jared Burns - Feb 23, 2011 1:36 AM</j.2:title>\n" +
        "    <rdf:type rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement\"/>\n" +
        "    <rdf:object rdf:resource=\"https://jazz.net/jazz/resource/itemOid/com.ibm.team.scm.ChangeSet/_S7U3gT8XEeC50ZOFeYh_9w\"/>\n" +
        "    <rdf:predicate rdf:resource=\"http://jazz.net/xmlns/prod/jazz/rtc/cm/1.0/com.ibm.team.filesystem.workitems.change_set.com.ibm.team.scm.ChangeSet\"/>\n" +
        "    <rdf:subject rdf:resource=\"https://jazz.net/jazz/resource/itemName/com.ibm.team.workitem.WorkItem/%NUMBER%\"/>\n" +
        "  </rdf:Description>\n" +
        "  <rdf:Description rdf:nodeID=\"A6\">\n" +
        "    <j.2:title>@mjarvis</j.2:title>\n" +
        "    <rdf:type rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement\"/>\n" +
        "    <rdf:object rdf:resource=\"https://jazz.net/jazz/oslc/automation/persons/_P_wUELLTEduhAusIxeOxbA\"/>\n" +
        "    <rdf:predicate rdf:resource=\"http://jazz.net/xmlns/prod/jazz/rtc/cm/1.0/com.ibm.team.workitem.linktype.textualReference.textuallyReferenced\"/>\n" +
        "    <rdf:subject rdf:resource=\"https://jazz.net/jazz/resource/itemName/com.ibm.team.workitem.WorkItem/%NUMBER%\"/>\n" +
        "  </rdf:Description>\n" +
        "  <rdf:Description rdf:nodeID=\"A7\">\n" +
        "    <j.2:title>Changes in Process - Performance test - Martha Andrews - Feb 22, 2011 9:45 PM</j.2:title>\n" +
        "    <rdf:type rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement\"/>\n" +
        "    <rdf:object rdf:resource=\"https://jazz.net/jazz/resource/itemOid/com.ibm.team.scm.ChangeSet/_p2Q08T7lEeC50ZOFeYh_9w\"/>\n" +
        "    <rdf:predicate rdf:resource=\"http://jazz.net/xmlns/prod/jazz/rtc/cm/1.0/com.ibm.team.filesystem.workitems.change_set.com.ibm.team.scm.ChangeSet\"/>\n" +
        "    <rdf:subject rdf:resource=\"https://jazz.net/jazz/resource/itemName/com.ibm.team.workitem.WorkItem/%NUMBER%\"/>\n" +
        "  </rdf:Description>\n" +
        "  <rdf:Description rdf:nodeID=\"A8\">\n" +
        "    <j.2:title>@storaskar</j.2:title>\n" +
        "    <rdf:type rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement\"/>\n" +
        "    <rdf:object rdf:resource=\"https://jazz.net/jazz/oslc/automation/persons/_DziEAHHfEdyLLb7t1B32_A\"/>\n" +
        "    <rdf:predicate rdf:resource=\"http://jazz.net/xmlns/prod/jazz/rtc/cm/1.0/com.ibm.team.workitem.linktype.textualReference.textuallyReferenced\"/>\n" +
        "    <rdf:subject rdf:resource=\"https://jazz.net/jazz/resource/itemName/com.ibm.team.workitem.WorkItem/%NUMBER%\"/>\n" +
        "  </rdf:Description>\n" +
        "  <rdf:Description rdf:nodeID=\"A9\">\n" +
        "    <j.2:title>https://jazz.net/wiki/bin/view/Main/DraftTeamProcessRestApi#Project_Areas_collection</j.2:title>\n" +
        "    <rdf:type rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement\"/>\n" +
        "    <rdf:object rdf:resource=\"https://jazz.net/wiki/bin/view/Main/DraftTeamProcessRestApi#Project_Areas_collection\"/>\n" +
        "    <rdf:predicate rdf:resource=\"http://jazz.net/xmlns/prod/jazz/rtc/cm/1.0/com.ibm.team.workitem.linktype.textualReference.textuallyReferenced\"/>\n" +
        "    <rdf:subject rdf:resource=\"https://jazz.net/jazz/resource/itemName/com.ibm.team.workitem.WorkItem/%NUMBER%\"/>\n" +
        "  </rdf:Description>\n" +
        "</rdf:RDF>\n";

}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */