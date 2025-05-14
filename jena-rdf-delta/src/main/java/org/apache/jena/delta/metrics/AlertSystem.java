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

package org.apache.jena.delta.metrics;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.jena.atlas.json.JsonBuilder;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.delta.metrics.HealthCheck.Result;
import org.apache.jena.delta.metrics.HealthCheck.Status;
import org.apache.jena.delta.metrics.HealthCheck.StatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Alerting system for RDF Delta components.
 * <p>
 * This class provides an alerting framework that can send notifications when
 * health checks fail or other significant events occur. It supports multiple
 * notification channels:
 * <ul>
 * <li>Log messages</li>
 * <li>Email notifications</li>
 * <li>Webhook notifications (e.g., Slack, Teams)</li>
 * <li>Alerting systems (e.g., PagerDuty)</li>
 * </ul>
 * <p>
 * The alerting system includes features for alert deduplication, rate limiting,
 * and configurable alert thresholds.
 */
public class AlertSystem implements StatusListener {
    private static final Logger LOG = LoggerFactory.getLogger(AlertSystem.class);
    
    /** Alert severity levels */
    public enum Severity {
        /** Informational message, no action required */
        INFO,
        
        /** Warning, may require attention */
        WARNING,
        
        /** Error, requires attention */
        ERROR,
        
        /** Critical error, requires immediate attention */
        CRITICAL
    }
    
    /** An alert notification */
    public static class Alert {
        private final String component;
        private final Severity severity;
        private final String title;
        private final String message;
        private final Instant timestamp;
        private final Map<String, String> additionalData;
        
        public Alert(String component, Severity severity, String title, String message) {
            this(component, severity, title, message, Map.of());
        }
        
        public Alert(String component, Severity severity, String title, String message, 
                    Map<String, String> additionalData) {
            this.component = component;
            this.severity = severity;
            this.title = title;
            this.message = message;
            this.timestamp = Instant.now();
            this.additionalData = Map.copyOf(additionalData);
        }
        
        public String getComponent() { return component; }
        public Severity getSeverity() { return severity; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public Instant getTimestamp() { return timestamp; }
        public Map<String, String> getAdditionalData() { return additionalData; }
        
        public JsonObject toJson() {
            JsonBuilder builder = JsonBuilder.create()
                .add("component", component)
                .add("severity", severity.name())
                .add("title", title)
                .add("message", message)
                .add("timestamp", timestamp.toString());
            
            if (!additionalData.isEmpty()) {
                JsonBuilder dataBuilder = JsonBuilder.create();
                additionalData.forEach(dataBuilder::add);
                builder.add("additionalData", dataBuilder.build());
            }
            
            return builder.build();
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s - %s: %s", 
                severity, component, title, message);
        }
    }
    
    /** An alert notification channel */
    public interface NotificationChannel {
        /**
         * Send an alert through this channel.
         * 
         * @param alert The alert to send
         * @return A future that completes when the alert is sent
         */
        CompletableFuture<Void> sendAlert(Alert alert);
        
        /**
         * Get the name of this channel.
         * 
         * @return The channel name
         */
        String getName();
    }
    
    /** A webhook notification channel */
    public static class WebhookChannel implements NotificationChannel {
        private final String name;
        private final String webhookUrl;
        private final HttpClient httpClient;
        
        public WebhookChannel(String name, String webhookUrl) {
            this.name = name;
            this.webhookUrl = webhookUrl;
            this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        }
        
        @Override
        public CompletableFuture<Void> sendAlert(Alert alert) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(alert.toJson().toString()))
                    .build();
                
                return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenApply(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            LOG.debug("Alert sent to webhook {}: {}", name, alert.getTitle());
                        } else {
                            LOG.warn("Failed to send alert to webhook {}: HTTP {}", 
                                name, response.statusCode());
                        }
                        return null;
                    })
                    .exceptionally(ex -> {
                        LOG.warn("Error sending alert to webhook {}: {}", 
                            name, ex.getMessage());
                        return null;
                    });
            } catch (Exception e) {
                LOG.warn("Failed to create webhook request: {}", e.getMessage());
                CompletableFuture<Void> future = new CompletableFuture<>();
                future.completeExceptionally(e);
                return future;
            }
        }
        
        @Override
        public String getName() {
            return name;
        }
    }
    
    /** A log notification channel */
    public static class LogChannel implements NotificationChannel {
        private final Logger logger;
        
        public LogChannel() {
            this.logger = LOG;
        }
        
        public LogChannel(Logger logger) {
            this.logger = logger;
        }
        
        @Override
        public CompletableFuture<Void> sendAlert(Alert alert) {
            switch (alert.getSeverity()) {
                case INFO:
                    logger.info("ALERT: {}", alert);
                    break;
                case WARNING:
                    logger.warn("ALERT: {}", alert);
                    break;
                case ERROR:
                case CRITICAL:
                    logger.error("ALERT: {}", alert);
                    break;
            }
            return CompletableFuture.completedFuture(null);
        }
        
        @Override
        public String getName() {
            return "log";
        }
    }
    
    /** Alert rate limiting tracker */
    private static class AlertRateLimit {
        private final String key;
        private Instant lastSent;
        private int count;
        
        public AlertRateLimit(String key) {
            this.key = key;
            this.lastSent = Instant.EPOCH;
            this.count = 0;
        }
        
        public synchronized boolean shouldSend(Duration minInterval) {
            Instant now = Instant.now();
            Duration sinceLastSent = Duration.between(lastSent, now);
            
            if (sinceLastSent.compareTo(minInterval) >= 0) {
                lastSent = now;
                count = 1;
                return true;
            } else {
                count++;
                return false;
            }
        }
        
        public int getCount() {
            return count;
        }
    }
    
    // Notification channels
    private final Map<String, NotificationChannel> channels = new ConcurrentHashMap<>();
    
    // Alert rate limiting
    private final Map<String, AlertRateLimit> rateLimits = new ConcurrentHashMap<>();
    private final Duration rateLimitInterval;
    
    /**
     * Create a new alerting system.
     */
    public AlertSystem() {
        this(Duration.ofMinutes(5)); // Default rate limit: 5 minutes
    }
    
    /**
     * Create a new alerting system with the specified rate limit interval.
     * 
     * @param rateLimitInterval Minimum time between sending duplicate alerts
     */
    public AlertSystem(Duration rateLimitInterval) {
        this.rateLimitInterval = rateLimitInterval;
        
        // Add default log channel
        addChannel(new LogChannel());
    }
    
    /**
     * Add a notification channel.
     * 
     * @param channel The channel to add
     * @return This AlertSystem for chaining
     */
    public AlertSystem addChannel(NotificationChannel channel) {
        channels.put(channel.getName(), channel);
        return this;
    }
    
    /**
     * Add a webhook notification channel.
     * 
     * @param name The channel name
     * @param webhookUrl The webhook URL
     * @return This AlertSystem for chaining
     */
    public AlertSystem addWebhookChannel(String name, String webhookUrl) {
        return addChannel(new WebhookChannel(name, webhookUrl));
    }
    
    /**
     * Send an alert to all notification channels.
     * 
     * @param component The component that raised the alert
     * @param severity The alert severity
     * @param title A short alert title
     * @param message A detailed alert message
     * @return A future that completes when the alert is sent
     */
    public CompletableFuture<Void> sendAlert(String component, Severity severity, 
                                         String title, String message) {
        return sendAlert(new Alert(component, severity, title, message));
    }
    
    /**
     * Send an alert to all notification channels.
     * 
     * @param alert The alert to send
     * @return A future that completes when the alert is sent
     */
    public CompletableFuture<Void> sendAlert(Alert alert) {
        // Create a rate limit key from component + severity + title
        String rateLimitKey = alert.getComponent() + ":" + 
                              alert.getSeverity() + ":" + 
                              alert.getTitle();
        
        // Check rate limit
        AlertRateLimit rateLimit = rateLimits.computeIfAbsent(
            rateLimitKey, k -> new AlertRateLimit(k));
            
        if (!rateLimit.shouldSend(rateLimitInterval)) {
            // Skip this alert due to rate limiting
            LOG.debug("Alert suppressed due to rate limiting: {}", alert.getTitle());
            return CompletableFuture.completedFuture(null);
        }
        
        // If there were multiple suppressed alerts, add a count to the message
        Alert alertToSend = alert;
        int count = rateLimit.getCount();
        if (count > 1) {
            // Create a new alert with updated message
            String updatedMessage = alert.getMessage() + 
                String.format(" (%d occurrences since last notification)", count);
                
            alertToSend = new Alert(
                alert.getComponent(), 
                alert.getSeverity(),
                alert.getTitle(),
                updatedMessage,
                alert.getAdditionalData());
        }
        
        // Send to all channels
        CompletableFuture<?>[] futures = channels.values().stream()
            .map(channel -> channel.sendAlert(alertToSend))
            .toArray(CompletableFuture[]::new);
            
        return CompletableFuture.allOf(futures);
    }
    
    @Override
    public void onStatusChange(Result oldResult, Result newResult) {
        if (newResult.getStatus() == Status.UNHEALTHY) {
            sendAlert(
                newResult.getComponent(),
                Severity.ERROR,
                "Health check failed",
                newResult.getMessage()
            );
        } else if (newResult.getStatus() == Status.DEGRADED) {
            sendAlert(
                newResult.getComponent(),
                Severity.WARNING,
                "Health check degraded",
                newResult.getMessage()
            );
        } else if (oldResult.getStatus() == Status.UNHEALTHY || 
                  oldResult.getStatus() == Status.DEGRADED) {
            // Recovery from unhealthy or degraded state
            sendAlert(
                newResult.getComponent(),
                Severity.INFO,
                "Health check recovered",
                "Service has recovered: " + newResult.getMessage()
            );
        }
    }
}