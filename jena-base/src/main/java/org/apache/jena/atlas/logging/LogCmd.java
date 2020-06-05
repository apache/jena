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

package org.apache.jena.atlas.logging;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.properties.PropertiesConfigurationFactory;

/** 
 * Additional logging control, for Log4j2 as used by jena-cmds.
 * <br/> 
 * This class pulls in log4j2.
 * <br/>
 * This class is split out from {@link LogCtl} to decouple the class loading dependencies.
 */
public class LogCmd {
    /**
     * Reset logging (log4j2). log4j2.properties format.
     */
    public static void resetLogging(String configString) {
        // Dispatch name to syntax.
        try (InputStream inputStream = new ByteArrayInputStream(StrUtils.asUTF8bytes(configString))) {
            resetLogging(inputStream, ".properties");
        } catch (IOException ex) {
            IO.exception(ex);
        }
    }

    private static void resetLogging(InputStream inputStream, String syntaxHint) throws IOException {
        ConfigurationSource source = new ConfigurationSource(inputStream);
        ConfigurationFactory factory = ( syntaxHint.endsWith(".properties") )
            ? new PropertiesConfigurationFactory()
            : ConfigurationFactory.getInstance();
        Configuration configuration = factory.getConfiguration(null, source);
        Configurator.initialize(configuration);
    }
    
//    public static void setCmdLogging() {
//        LogCtl.setLog4j2();
//        if ( ! LogCtl.isSetLog4j2property() )
//            resetLogging(log4j2setupCmd);
//    }

    // basic setup.
    // @formatter:off
    /** A basic logging setup. */
    public static String log4j2setup = StrUtils.strjoinNL
        ( "## Command default log4j2 setup : log4j2 properties syntax."
        , "status = error"
        , "name = PropertiesConfig"
        , "filters = threshold"
        , ""
        , "filter.threshold.type = ThresholdFilter"
        , "filter.threshold.level = INFO"

        , "appender.console.type = Console"
        , "appender.console.name = OUT"
        , "appender.console.target = SYSTEM_OUT"
        , "appender.console.layout.type = PatternLayout"
        , "appender.console.layout.pattern = %d{HH:mm:ss} %-5p %-10c{1} :: %m%n"
        , "#appender.console.layout.pattern = [%d{yyyy-MM-dd HH:mm:ss}] %-5p %-10c{1} :: %m%n"

        , "rootLogger.level                  = INFO"
        , "rootLogger.appenderRef.stdout.ref = OUT"

        , "logger.jena.name  = org.apache.jena"
        , "logger.jena.level = INFO"

        , "logger.arq-exec.name  = org.apache.jena.arq.exec"
        , "logger.arq-exec.level = INFO"

        , "logger.riot.name  = org.apache.jena.riot"
        , "logger.riot.level = INFO"
        );
    // @formatter:on
    
    
    // @formatter:off
    /** A format for commands using stderr. */
    public static String log4j2setupCmd = StrUtils.strjoinNL
        ( "## Command default log4j2 setup : log4j2 properties syntax."
        , "status = error"
        , "name = PropertiesConfig"
        , "filters = threshold"
        , ""
        , "filter.threshold.type = ThresholdFilter"
        , "filter.threshold.level = INFO"

        , "appender.console.type = Console"
        , "appender.console.name = OUT"
        , "appender.console.target = SYSTEM_ERR"
        , "appender.console.layout.type = PatternLayout"
        , "appender.console.layout.pattern = %d{HH:mm:ss} %-5p %-10c{1} :: %m%n"
        , "#appender.console.layout.pattern = [%d{yyyy-MM-dd HH:mm:ss}] %-5p %-10c{1} ::: %m%n"

        , "rootLogger.level                  = INFO"
        , "rootLogger.appenderRef.stdout.ref = OUT"

        , "logger.jena.name  = org.apache.jena"
        , "logger.jena.level = INFO"

        , "logger.arq-exec.name  = org.apache.jena.arq.exec"
        , "logger.arq-exec.level = INFO"

        , "logger.riot.name  = org.apache.jena.riot"
        , "logger.riot.level = INFO"
        );
    // @formatter:on

}

