/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.jena.sparql.test;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.riot.RDFDataMgr ;
import org.junit.runner.Description ;
import org.junit.runner.Runner ;
import org.junit.runner.notification.Failure ;
import org.junit.runner.notification.RunNotifier ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.junit.EarlTestCase ;

/** Run one manifest, which can contain included manifests and individual tests */  
public class RunnerOneManifest extends Runner
{
    private static int count = 0 ;
    private static int depth = 0 ;
    private Description description ;
    private List<Runner> runners = new ArrayList<>() ;
    
    public RunnerOneManifest(String manifest) {
        int count$ = (++count) ;
        Model model = RDFDataMgr.loadModel(manifest) ;
        List<String> manifests = LibTestSPARQL.getIncludes(model) ;

        List<String> x = LibTestSPARQL.getNames(model) ;
        String name = x.get(0) ;
        name = LibTestSPARQL.fixupName(name) ;
        // Names must be unique else Eclipse will not report them. 
        description = Description.createSuiteDescription(/*count+": "+*/name) ;
        
        // ---- Includes
        depth++ ;
        LibTestSPARQL.setUpManifests(description, getRunners(), manifests);
        -- depth ;

        // ---- Tests
        List<EarlTestCase> tests = LibTestSPARQL.generateTests(model) ;
        for ( EarlTestCase tc : tests ) {
            if ( tc == null ) {
                System.err.println("TestCase is null") ;
                continue ;
            }
            String label = tc.getName() ;
            label = LibTestSPARQL.fixupName(label) ;
            Runner r = new RunnerOneSPARQLTest(tc) ;
            description.addChild(r.getDescription());
            getRunners().add(r) ;
        } 
    }
    
    @Override
    public Description getDescription() {
        return description ;
    }

    @Override
    public void run(RunNotifier notifier) {
        notifier.fireTestStarted(description);
        try {
            for ( Runner r : getRunners() )
                r.run(notifier) ;
        } catch (Exception e) {
            notifier.fireTestAssumptionFailed(new Failure(description, e)) ;
        } catch (Throwable e) {
            notifier.fireTestFailure(new Failure(description, e)) ;
        } finally {
            notifier.fireTestFinished(description); 
        }
    }

    public List<Runner> getRunners() {
        return runners;
    }
}
