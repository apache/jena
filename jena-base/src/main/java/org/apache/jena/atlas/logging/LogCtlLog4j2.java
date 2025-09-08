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
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.json.JsonConfigurationFactory;
import org.apache.logging.log4j.core.config.properties.PropertiesConfigurationFactory;
import org.apache.logging.log4j.core.config.xml.XmlConfigurationFactory;
import org.apache.logging.log4j.core.config.yaml.YamlConfigurationFactory;

/**
 * Additional logging control, for Log4j2 as used by jena-cmds.
 * <p>
 * This class depends on log4j2-api and also log4j2-core.
 * These are &lt;optional&gt; dependencies for Jena which can use any slf4j provider.
 * <p>
 * This class is split out from {@link LogCtl} to decouple the dependencies.
 * <p>
 * This class is not used if log4j2 is not used.
 */
public class LogCtlLog4j2 {

    /** Default log4j2 setup */
    public static String log4j2setup = Log4j2Setup.log4j2setup();

    /**
     * Reset logging for log4j2.
     * The string is log4j2.properties format.
     */
    public static void resetLogging(String configString) {
        // This method is previous naming.
        reconfigureLog4j2fromString(configString, SyntaxHint.PROPERTIES);
    }

    /**
     * Reset logging for log4j2 from a string.
     * The resourceName is used to determine the syntax.
     */
    public static void resetLogging(InputStream inputStream, String resourceName) {
        resetLogging(inputStream, determineSyntax(resourceName));
    }

    /**
     * Reset logging for log4j2 from an {@link InputStream} with the given syntax.
     */
    public static void resetLogging(InputStream inputStream, SyntaxHint syntaxHint) {
        Configuration config = log4j2Configuration(inputStream, syntaxHint);
        reconfigureLog4j(config);
    }

    /** get logging level of a Logger as a string */
    /* package */ static String getLoggerlevel(String logger) {
        Level level = org.apache.logging.log4j.LogManager.getLogger(logger).getLevel();
        if ( level != null )
            return level.toString();
        return null;
    }

    /** Set logging level of a Logger */
    /*package*/ static void setLoggerlevel(String logger, String levelName) {
        org.apache.logging.log4j.Level level = org.apache.logging.log4j.Level.ALL;
        if ( levelName == null )
            level = null;
        else if ( levelName.equalsIgnoreCase("info") )
            level = org.apache.logging.log4j.Level.INFO;
        else if ( levelName.equalsIgnoreCase("debug") )
            level = org.apache.logging.log4j.Level.DEBUG;
        else if ( levelName.equalsIgnoreCase("warn") || levelName.equalsIgnoreCase("warning") )
            level = org.apache.logging.log4j.Level.WARN;
        else if ( levelName.equalsIgnoreCase("error") || levelName.equalsIgnoreCase("severe") )
            level = org.apache.logging.log4j.Level.ERROR;
        else if ( levelName.equalsIgnoreCase("fatal") )
            level = org.apache.logging.log4j.Level.FATAL;
        else if ( levelName.equalsIgnoreCase("OFF") )
            level = org.apache.logging.log4j.Level.OFF;
        LogCtlLog4j2.setLoggerlevel(logger, level);
    }

    /** Set logging level of a Logger */
    /*package*/ static void setLoggerlevel(String logger, Level level) {
        try {
            if ( !logger.equals("") )
                org.apache.logging.log4j.core.config.Configurator.setLevel(logger, level);
            else
                org.apache.logging.log4j.core.config.Configurator.setRootLevel(level);
        } catch (NoClassDefFoundError ex) {
            Log.warnOnce(LogCtlLog4j2.class, "Log4j2 Configurator not found", LogCtl.class);
        }
    }

    /** Set logging level of a Logger */
    /*package*/ static void setLoggerlevel(Logger logger, Level level) {
        try {
            org.apache.logging.log4j.core.config.Configurator.setLevel(logger, level);
        } catch (NoClassDefFoundError ex) {
            Log.warnOnce(LogCtlLog4j2.class, "Log4j2 Configurator not found", LogCtl.class);
        }
    }

    /**
     * Enum for possible syntax of a Log4j configuration file.
     * <p>
     * Note that the JSON and YAML forms, require additional jars. See
     * <a href="https://logging.apache.org/log4j/2.x/runtime-dependencies.html#log4j-core"
     * >"dependencies for log4j-core"</a> for more information.
     */
    public enum SyntaxHint {
        PROPERTIES("properties"),
        XML("xml"),
        JSON("json"),
        YAML("yaml");

        // The syntax name is assumed to be the file extension.
        // This can be used as the name of a syntax.
        private String syntaxName;
        SyntaxHint(String syntaxName) { this.syntaxName = syntaxName; }

        /** Return the {@code SyntaxHint} for a name (case insensitive) or null */
        static SyntaxHint fromName(String name) {
            for ( SyntaxHint hint : SyntaxHint.values() ) {
                if ( hint.syntaxName.equalsIgnoreCase(name) )
                    return hint;
            }
            return null;
        }
    }

    /**
     * Reconfigure log4j2 from a file.
     * <p>
     * The file syntax is determined by the file extension (".properties" or ".xml").
     * <p>
     * Existing loggers are reconfigured by this function.
     */
    public static void reconfigureLog4j2fromFile(String filename) {
        if ( true ) {
            // This particular case can be done with Log4J directly.
            // That will extend to all plugins.
            Configurator.initialize(null, filename);
            return;
        }
        // Use the same logic as the other operations.
        // JSON and YAML usage require addition jars on the classpath.
        SyntaxHint syntax = determineSyntax(filename);
        Configuration config = log4j2ConfigurationFromFile(filename, syntax);
        reconfigureLog4j(config);
    }

    /**
     * Reconfigure log4j2 from a file.
     * <p>
     * The file syntax is determined by the syntax hint.
     * <p>
     * Existing loggers are reconfigured by this function.
     */
    public static void reconfigureLog4j2fromFile(String filename, SyntaxHint syntaxHint) {
        Configuration config = log4j2ConfigurationFromFile(filename, syntaxHint);
        reconfigureLog4j(config);
    }

    /**
     * Reconfigure log4j2 from a string.
     * <p>
     * The syntax is given by the syntax hint.
     * <p>
     * Existing loggers are reconfigured by this function.
     */
    public static void reconfigureLog4j2fromString(String configString, SyntaxHint syntaxHint) {
        Configuration config = log4j2ConfigurationFromString(configString, syntaxHint);
        reconfigureLog4j(config);
    }

    /**
     * Reconfigure log4j from a {@link Configuration}.
     */
    private static void reconfigureLog4j(Configuration config) {
        config.initialize();
        Configurator.reconfigure(config);
    }

    /**
     * Create a log4j2 {@link Configuration} from a file.
     * <p>
     * The file syntax is determined by the syntax hint
     */
    private static Configuration log4j2ConfigurationFromFile(String filename, SyntaxHint syntaxHint) {
        URI uri = Path.of(filename).toUri();
        ConfigurationSource source = ConfigurationSource.fromUri(uri);
        return createLog4jConfiguration(source, syntaxHint);
    }

    /**
     * Create a log4j2 {@link Configuration} from a string.
     * <p>
     * TThe string syntax is determined by the syntax hint
     */
    private static Configuration log4j2ConfigurationFromString(String text, SyntaxHint syntaxHint) {
        try(InputStream input = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))) {
            return log4j2Configuration(input, syntaxHint);
        } catch (IOException ex) { throw new UncheckedIOException(ex); }
    }

    /**
     * Create a log4j2 {@link Configuration} from an {@link InputStream}.
     * <p>
     * The file syntax is determined by the syntax hint
     */
    private static Configuration log4j2Configuration(InputStream inputStream, SyntaxHint syntaxHint) {
        try {
            ConfigurationSource source = new ConfigurationSource(inputStream);
            return createLog4jConfiguration(source, syntaxHint);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Create a log4j2 {@link Configuration}.
     * <p>
     * @see org.apache.logging.log4j.core.config.Configurator
     * @see org.apache.logging.log4j.core.config.ConfigurationSource
     */
    private static Configuration createLog4jConfiguration(ConfigurationSource source, SyntaxHint syntaxHint) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(syntaxHint);
        ConfigurationFactory factory = switch(syntaxHint) {
            case PROPERTIES -> new PropertiesConfigurationFactory();
            case XML        -> new XmlConfigurationFactory();
            case JSON       -> new JsonConfigurationFactory();
            case YAML       -> new YamlConfigurationFactory();
            default -> ConfigurationFactory.getInstance();
        };
        Configuration configuration = factory.getConfiguration(null, source);
        if ( configuration == null )
            throw new UnsupportedOperationException("Can't create a configuration for '"+source+"' using '"+syntaxHint+"'");
        return configuration;
    }

    /**
     * Filename to {@link SynatxHint}.
     * <p>
     * Identify the likely syntax of a file, or throw IllegalArgumentException
     * if no such determination can be made.
     */
    private static SyntaxHint determineSyntax(String filename) {
        String ext = FilenameUtils.getExtension(filename);
        if ( ext == null )
            throw new IllegalArgumentException("No file extension");
        SyntaxHint hint = SyntaxHint.fromName(ext);
        if ( hint == null )
            throw new IllegalArgumentException("File extension not recognized: '"+ext+"'");
        return hint;
    }

    // Hide constants.
    static class Log4j2Setup {

        private static String log4j2setup() {
            return String.join(log4jSetupSep(),
                               log4j2setupBase(),
                               log4j2setupJenaLib(),
                               log4j2setupFuseki());
        }

        /** Line separate/blank line for concatenating log4j syntax fragments. */
        private static String log4jSetupSep() { return "\n"; }

        /**
         * A basic logging setup. Time and level INFO.
         */
        private static String log4j2setupBase() {
            return """
                    ## Log4j2 properties syntax.
                    status = error
                    name = JenaLoggingDft

                    # filters = threshold
                    # filter.threshold.type = ThresholdFilter
                    # filter.threshold.level = ALL

                    appender.console.type = Console
                    appender.console.name = OUT
                    appender.console.target = SYSTEM_OUT
                    appender.console.layout.type = PatternLayout
                    appender.console.layout.pattern = %d{HH:mm:ss} %-5p %-15c{1} :: %m%n
                    # appender.console.layout.pattern = [%d{yyyy-MM-dd HH:mm:ss}] %-5p %-15c{1} :: %m%n

                    rootLogger.level                  = INFO
                    rootLogger.appenderRef.stdout.ref = OUT
                    """;
        }
        /** Default log4j fragment needed for Jena command line tools. */
        private static String log4j2setupJenaLib() {
            return """
                    logger.jena.name  = org.apache.jena
                    logger.jena.level = INFO

                    logger.arq-exec.name  = org.apache.jena.arq.exec
                    logger.arq-exec.level = INFO

                    logger.riot.name  = org.apache.jena.riot
                    logger.riot.level = INFO
                    """;
        }
        /** Additional log4j fragment for Fuseki in case the general default is used with embedded Fuseki. */
        private static String log4j2setupFuseki() {
            return """
                    # Fuseki. In case this logging setup gets install for embedded Fuseki.

                    logger.fuseki.name  = org.apache.jena.fuseki
                    logger.fuseki.level = INFO
                    logger.fuseki-fuseki.name  = org.apache.jena.fuseki.Fuseki
                    logger.fuseki-fuseki.level = INFO

                    logger.fuseki-server.name  = org.apache.jena.fuseki.Server
                    logger.fuseki-server.level = INFO

                    logger.fuseki-config.name  = org.apache.jena.fuseki.Config
                    logger.fuseki-config.level = INFO

                    logger.fuseki-admin.name  = org.apache.jena.fuseki.Admin
                    logger.fuseki-admin.level = INFO

                    logger.jetty.name  = org.eclipse.jetty
                    logger.jetty.level = WARN

                    logger.shiro.name = org.apache.shiro
                    logger.shiro.level = WARN

                    # This goes out in NCSA format
                    appender.plain.type = Console
                    appender.plain.name = PLAIN
                    appender.plain.layout.type = PatternLayout
                    appender.plain.layout.pattern = %m%n

                    logger.fuseki-request.name                   = org.apache.jena.fuseki.Request
                    logger.fuseki-request.additivity             = false
                    logger.fuseki-request.level                  = OFF
                    logger.fuseki-request.appenderRef.plain.ref  = PLAIN
                    """;
        }

    }
}
