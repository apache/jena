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

package org.seaborne.dboe.sparql.test;

import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.List ;

import org.junit.runner.Description ;
import org.junit.runner.Runner ;
import org.junit.runner.notification.RunNotifier ;
import org.junit.runners.ParentRunner ;
import org.junit.runners.model.InitializationError ;

/** Runner for SPARQL Manifests.
 * Annotations supported:
 * <ul>
 * <li><tt>@Label("Some name")</tt></li>
 * <li><tt>@Manifests({"manifest1","manifest2",...})</tt></li>
 * </ul>
 *  This class sorts out the annotations, including providing before/after class,
 *  then creates a hierarchy of tests to run.
 *  @see RunnerOneManifest
 *  @see RunnerOneSPARQLTest
 */
public class RunnerSPARQL extends ParentRunner<Runner>
{
    private Description description ;
    private List<Runner> children = new ArrayList<>() ;

    // Need unique names.
    
    public RunnerSPARQL(Class<?>klass) throws InitializationError 
    {
        super(klass) ;
        String[] args = getManifests(klass) ;
        if ( args.length == 0 )
            throw new InitializationError("No manifests") ;
        String label = getLabel(klass) ;
        if ( label == null )
            label = klass.getName() ;
        description = Description.createSuiteDescription(label) ;
        LibTestSPARQL.setUpManifests(description, children, Arrays.asList(args)); 
    }        
    

    private static String getLabel(Class<?> klass) {
        Label annotation = klass.getAnnotation(Label.class);
        if (annotation == null)
            return null ;
        return annotation.value();
    }

    private static String[] getManifests(Class<?> klass) throws InitializationError {
        Manifests annotation = klass.getAnnotation(Manifests.class);
        if (annotation == null) {
            throw new InitializationError(String.format("class '%s' must have a @Manifests annotation", klass.getName()));
        }
        return annotation.value();
    }
    
    @Override
    public Description getDescription() {
        return description ;
    }

    @Override
    protected List<Runner> getChildren() {
        return children ;
    }

    @Override
    protected Description describeChild(Runner child) {
        return child.getDescription() ;
    }

    @Override
    protected void runChild(Runner child, RunNotifier notifier) {
        child.run(notifier); 
    }
}
