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

// Charts
let memoryChart = null;
let gcChart = null;
let generationsChart = null;
let gcTimeChart = null;
let componentsChart = null;

// Data storage
let historicalData = [];
const MAX_HISTORY_POINTS = 60; // 5 minutes with 5s updates

// Chart colors
const CHART_COLORS = {
    heap: 'rgba(52, 152, 219, 0.8)',
    nonHeap: 'rgba(46, 204, 113, 0.8)',
    young: 'rgba(142, 68, 173, 0.8)',
    old: 'rgba(243, 156, 18, 0.8)',
    metaspace: 'rgba(231, 76, 60, 0.8)',
    youngGc: 'rgba(52, 152, 219, 0.8)',
    oldGc: 'rgba(231, 76, 60, 0.8)'
};

// Auto refresh
let refreshInterval = 5000;
let refreshTimer = null;

// Initialize dashboard
document.addEventListener('DOMContentLoaded', () => {
    initEventListeners();
    initCharts();
    refreshData();
    startAutoRefresh();
});

// Initialize event listeners
function initEventListeners() {
    // Refresh button
    document.getElementById('refreshBtn').addEventListener('click', () => {
        refreshData();
    });

    // GC button
    document.getElementById('gcBtn').addEventListener('click', () => {
        triggerGc();
    });

    // Auto refresh toggle
    document.getElementById('autoRefreshToggle').addEventListener('change', (e) => {
        if (e.target.checked) {
            startAutoRefresh();
        } else {
            stopAutoRefresh();
        }
    });

    // Refresh interval selector
    document.getElementById('refreshInterval').addEventListener('change', (e) => {
        refreshInterval = parseInt(e.target.value);
        if (document.getElementById('autoRefreshToggle').checked) {
            stopAutoRefresh();
            startAutoRefresh();
        }
    });

    // Tab buttons
    document.querySelectorAll('.tab-button').forEach(button => {
        button.addEventListener('click', (e) => {
            // Deactivate all tabs
            document.querySelectorAll('.tab-button').forEach(btn => {
                btn.classList.remove('active');
            });
            document.querySelectorAll('.tab-pane').forEach(pane => {
                pane.classList.remove('active');
            });

            // Activate clicked tab
            e.target.classList.add('active');
            const tabId = e.target.getAttribute('data-tab') + '-tab';
            document.getElementById(tabId).classList.add('active');

            // Resize charts if needed
            window.dispatchEvent(new Event('resize'));
        });
    });
}

// Initialize charts
function initCharts() {
    // Memory usage over time chart
    const memoryCtx = document.getElementById('memoryChart').getContext('2d');
    memoryChart = new Chart(memoryCtx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [
                {
                    label: 'Heap Memory',
                    backgroundColor: CHART_COLORS.heap,
                    borderColor: CHART_COLORS.heap,
                    borderWidth: 2,
                    fill: false,
                    data: []
                },
                {
                    label: 'Non-Heap Memory',
                    backgroundColor: CHART_COLORS.nonHeap,
                    borderColor: CHART_COLORS.nonHeap,
                    borderWidth: 2,
                    fill: false,
                    data: []
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.dataset.label + ': ' + formatBytes(context.parsed.y);
                        }
                    }
                }
            },
            scales: {
                x: {
                    title: {
                        display: true,
                        text: 'Time'
                    }
                },
                y: {
                    title: {
                        display: true,
                        text: 'Memory Usage (MB)'
                    },
                    ticks: {
                        callback: function(value) {
                            return formatBytes(value, 0);
                        }
                    }
                }
            }
        }
    });

    // Garbage collection chart
    const gcCtx = document.getElementById('gcChart').getContext('2d');
    gcChart = new Chart(gcCtx, {
        type: 'bar',
        data: {
            labels: [],
            datasets: [
                {
                    label: 'Young GC',
                    backgroundColor: CHART_COLORS.youngGc,
                    data: []
                },
                {
                    label: 'Old GC',
                    backgroundColor: CHART_COLORS.oldGc,
                    data: []
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: false
                }
            },
            scales: {
                x: {
                    stacked: true,
                    title: {
                        display: true,
                        text: 'Time'
                    }
                },
                y: {
                    stacked: true,
                    title: {
                        display: true,
                        text: 'Collection Count'
                    }
                }
            }
        }
    });

    // Memory generations chart
    const generationsCtx = document.getElementById('generationsChart').getContext('2d');
    generationsChart = new Chart(generationsCtx, {
        type: 'doughnut',
        data: {
            labels: ['Young Generation', 'Old Generation', 'Metaspace'],
            datasets: [{
                data: [0, 0, 0],
                backgroundColor: [
                    CHART_COLORS.young,
                    CHART_COLORS.old,
                    CHART_COLORS.metaspace
                ]
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.label + ': ' + formatBytes(context.raw);
                        }
                    }
                }
            }
        }
    });

    // GC time chart
    const gcTimeCtx = document.getElementById('gcTimeChart').getContext('2d');
    gcTimeChart = new Chart(gcTimeCtx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [
                {
                    label: 'Young GC Time',
                    backgroundColor: CHART_COLORS.youngGc,
                    borderColor: CHART_COLORS.youngGc,
                    borderWidth: 2,
                    fill: false,
                    data: []
                },
                {
                    label: 'Old GC Time',
                    backgroundColor: CHART_COLORS.oldGc,
                    borderColor: CHART_COLORS.oldGc,
                    borderWidth: 2,
                    fill: false,
                    data: []
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: false
                }
            },
            scales: {
                x: {
                    title: {
                        display: true,
                        text: 'Time'
                    }
                },
                y: {
                    title: {
                        display: true,
                        text: 'GC Time (ms)'
                    }
                }
            }
        }
    });

    // Components chart (placeholder - will be populated with real data)
    const componentsCtx = document.getElementById('componentsChart').getContext('2d');
    componentsChart = new Chart(componentsCtx, {
        type: 'pie',
        data: {
            labels: ['No components registered'],
            datasets: [{
                data: [100],
                backgroundColor: [CHART_COLORS.heap]
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.label + ': ' + formatBytes(context.raw);
                        }
                    }
                }
            }
        }
    });
}

// Refresh data from API
function refreshData() {
    fetch('api/summary')
        .then(response => response.json())
        .then(data => {
            updateHistoricalData(data);
            updateSummaryCards(data);
            updateCharts();
            updateTables(data);
            updateLastUpdated();
        })
        .catch(error => {
            console.error('Error fetching data:', error);
        });

    fetch('api/alerts')
        .then(response => response.json())
        .then(data => {
            updateAlerts(data);
        })
        .catch(error => {
            console.error('Error fetching alerts:', error);
        });
}

// Update historical data
function updateHistoricalData(data) {
    // Add new data point
    historicalData.push(data);

    // Limit history length
    if (historicalData.length > MAX_HISTORY_POINTS) {
        historicalData.shift();
    }
}

// Update summary cards
function updateSummaryCards(data) {
    const sample = data.sample;

    // Heap memory
    const heapUsed = sample.heap.used;
    const heapMax = sample.heap.max;
    const heapPercentage = heapMax > 0 ? (heapUsed / heapMax * 100) : 0;

    document.getElementById('heapValue').textContent = formatBytes(heapUsed);
    document.getElementById('heapMax').textContent = formatBytes(heapMax);
    document.getElementById('heapFill').style.width = `${heapPercentage}%`;

    const heapCard = document.getElementById('heapCard');
    heapCard.className = 'card';
    if (heapPercentage >= 90) {
        heapCard.classList.add('critical');
    } else if (heapPercentage >= 75) {
        heapCard.classList.add('warning');
    }

    // Non-heap memory
    const nonHeapUsed = sample.nonHeap.used;
    const nonHeapMax = sample.nonHeap.max;
    const nonHeapPercentage = nonHeapMax > 0 ? (nonHeapUsed / nonHeapMax * 100) : 50; // Default to 50% if max is not available

    document.getElementById('nonHeapValue').textContent = formatBytes(nonHeapUsed);
    document.getElementById('nonHeapMax').textContent = formatBytes(nonHeapMax > 0 ? nonHeapMax : nonHeapUsed * 2);
    document.getElementById('nonHeapFill').style.width = `${nonHeapPercentage}%`;

    // GC counts
    document.getElementById('youngGcCount').textContent = sample.gc.young.count;
    document.getElementById('oldGcCount').textContent = sample.gc.old.count;

    // Components count
    const componentCount = Object.keys(data.components).length;
    document.getElementById('componentCount').textContent = componentCount;
}

// Update all charts
function updateCharts() {
    updateMemoryChart();
    updateGcChart();
    updateGenerationsChart();
    updateGcTimeChart();
    updateComponentsChart();
}

// Update memory usage chart
function updateMemoryChart() {
    // Extract data for charts
    const labels = historicalData.map(data => {
        const date = new Date(data.timestamp);
        return date.toLocaleTimeString();
    });

    const heapData = historicalData.map(data => data.sample.heap.used);
    const nonHeapData = historicalData.map(data => data.sample.nonHeap.used);

    // Update chart data
    memoryChart.data.labels = labels;
    memoryChart.data.datasets[0].data = heapData;
    memoryChart.data.datasets[1].data = nonHeapData;
    memoryChart.update();
}

// Update GC count chart
function updateGcChart() {
    // Extract data for charts
    const labels = historicalData.map(data => {
        const date = new Date(data.timestamp);
        return date.toLocaleTimeString();
    });

    // Calculate delta values for GC counts
    const youngGcData = [];
    const oldGcData = [];

    for (let i = 1; i < historicalData.length; i++) {
        const current = historicalData[i];
        const previous = historicalData[i - 1];
        
        youngGcData.push(current.sample.gc.young.count - previous.sample.gc.young.count);
        oldGcData.push(current.sample.gc.old.count - previous.sample.gc.old.count);
    }

    // Adjust labels to match the delta data (one fewer point)
    const deltaLabels = labels.slice(1);

    // Update chart data
    gcChart.data.labels = deltaLabels;
    gcChart.data.datasets[0].data = youngGcData;
    gcChart.data.datasets[1].data = oldGcData;
    gcChart.update();
}

// Update generations chart
function updateGenerationsChart() {
    if (historicalData.length === 0) return;

    const latest = historicalData[historicalData.length - 1];
    const generations = latest.sample.generations;

    generationsChart.data.datasets[0].data = [
        generations.young,
        generations.old,
        generations.metaspace
    ];
    generationsChart.update();
}

// Update GC time chart
function updateGcTimeChart() {
    // Extract data for charts
    const labels = historicalData.map(data => {
        const date = new Date(data.timestamp);
        return date.toLocaleTimeString();
    });

    // Calculate delta values for GC times
    const youngGcTimeData = [];
    const oldGcTimeData = [];

    for (let i = 1; i < historicalData.length; i++) {
        const current = historicalData[i];
        const previous = historicalData[i - 1];
        
        youngGcTimeData.push(current.sample.gc.young.timeMs - previous.sample.gc.young.timeMs);
        oldGcTimeData.push(current.sample.gc.old.timeMs - previous.sample.gc.old.timeMs);
    }

    // Adjust labels to match the delta data (one fewer point)
    const deltaLabels = labels.slice(1);

    // Update chart data
    gcTimeChart.data.labels = deltaLabels;
    gcTimeChart.data.datasets[0].data = youngGcTimeData;
    gcTimeChart.data.datasets[1].data = oldGcTimeData;
    gcTimeChart.update();
}

// Update components chart
function updateComponentsChart() {
    if (historicalData.length === 0) return;

    const latest = historicalData[historicalData.length - 1];
    const components = latest.components;
    
    if (Object.keys(components).length === 0) {
        // No components registered
        componentsChart.data.labels = ['No components registered'];
        componentsChart.data.datasets[0].data = [100];
        componentsChart.data.datasets[0].backgroundColor = [CHART_COLORS.heap];
    } else {
        // Create data for components
        const labels = [];
        const data = [];
        const colors = [];
        
        // Color generator
        const getRandomColor = () => {
            const hue = Math.floor(Math.random() * 360);
            return `hsla(${hue}, 70%, 60%, 0.8)`;
        };
        
        Object.entries(components).forEach(([name, stats], index) => {
            labels.push(name);
            data.push(stats.heapUsed + stats.offHeapUsed);
            colors.push(getRandomColor());
        });
        
        componentsChart.data.labels = labels;
        componentsChart.data.datasets[0].data = data;
        componentsChart.data.datasets[0].backgroundColor = colors;
    }
    
    componentsChart.update();
}

// Update tables
function updateTables(data) {
    // Memory pools table
    updateMemoryPoolsTable();
    
    // GC table
    updateGcTable();
    
    // Components table
    updateComponentsTable(data.components);
}

// Update memory pools table
function updateMemoryPoolsTable() {
    const table = document.getElementById('memoryPoolsTable');
    const tbody = table.querySelector('tbody');
    
    // Clear existing rows
    tbody.innerHTML = '';
    
    // Fetch memory pool information
    const memoryPoolBeans = getMemoryPoolBeans();
    
    if (memoryPoolBeans.length === 0) {
        const row = document.createElement('tr');
        row.innerHTML = '<td colspan="6" class="text-center">No memory pool data available</td>';
        tbody.appendChild(row);
        return;
    }
    
    // Add rows for each memory pool
    memoryPoolBeans.forEach(pool => {
        const row = document.createElement('tr');
        
        const usage = pool.usage;
        const usagePercent = usage.max > 0 ? (usage.used / usage.max * 100).toFixed(1) + '%' : 'N/A';
        
        row.innerHTML = `
            <td>${pool.name}</td>
            <td>${getPoolType(pool.name)}</td>
            <td>${formatBytes(usage.used)}</td>
            <td>${formatBytes(usage.committed)}</td>
            <td>${usage.max > 0 ? formatBytes(usage.max) : 'N/A'}</td>
            <td>${usagePercent}</td>
        `;
        
        tbody.appendChild(row);
    });
}

// Get memory pool type
function getPoolType(name) {
    name = name.toLowerCase();
    if (name.includes('eden') || name.includes('survivor') || name.includes('young')) {
        return 'Young Generation';
    } else if (name.includes('old') || name.includes('tenured')) {
        return 'Old Generation';
    } else if (name.includes('metaspace') || name.includes('perm')) {
        return 'Metaspace';
    } else if (name.includes('code')) {
        return 'Code Cache';
    } else {
        return 'Other';
    }
}

// Mock implementation for demo purposes
// In a real environment, this would be provided by the actual JMX data
function getMemoryPoolBeans() {
    if (historicalData.length === 0) return [];
    
    const latest = historicalData[historicalData.length - 1];
    const sample = latest.sample;
    
    // Create mock memory pool beans based on the sample data
    return [
        {
            name: 'Eden Space',
            usage: {
                used: sample.generations.young * 0.7,
                committed: sample.generations.young,
                max: sample.generations.young * 1.5
            }
        },
        {
            name: 'Survivor Space',
            usage: {
                used: sample.generations.young * 0.3,
                committed: sample.generations.young * 0.5,
                max: sample.generations.young * 0.5
            }
        },
        {
            name: 'Old Gen',
            usage: {
                used: sample.generations.old,
                committed: sample.generations.old * 1.2,
                max: sample.heap.max - (sample.generations.young * 2)
            }
        },
        {
            name: 'Metaspace',
            usage: {
                used: sample.generations.metaspace,
                committed: sample.generations.metaspace * 1.1,
                max: -1 // Metaspace can dynamically grow
            }
        }
    ];
}

// Update GC table
function updateGcTable() {
    const table = document.getElementById('gcTable');
    const tbody = table.querySelector('tbody');
    
    // Clear existing rows
    tbody.innerHTML = '';
    
    // Get latest sample
    if (historicalData.length === 0) {
        const row = document.createElement('tr');
        row.innerHTML = '<td colspan="4" class="text-center">No GC data available</td>';
        tbody.appendChild(row);
        return;
    }
    
    const latest = historicalData[historicalData.length - 1];
    const sample = latest.sample;
    
    // Add rows for young and old GC
    const youngRow = document.createElement('tr');
    youngRow.innerHTML = `
        <td>Young Generation</td>
        <td>${sample.gc.young.count}</td>
        <td>${sample.gc.young.timeMs}</td>
        <td>${sample.gc.young.count > 0 ? (sample.gc.young.timeMs / sample.gc.young.count).toFixed(2) : 0}</td>
    `;
    tbody.appendChild(youngRow);
    
    const oldRow = document.createElement('tr');
    oldRow.innerHTML = `
        <td>Old Generation</td>
        <td>${sample.gc.old.count}</td>
        <td>${sample.gc.old.timeMs}</td>
        <td>${sample.gc.old.count > 0 ? (sample.gc.old.timeMs / sample.gc.old.count).toFixed(2) : 0}</td>
    `;
    tbody.appendChild(oldRow);
}

// Update components table
function updateComponentsTable(components) {
    const table = document.getElementById('componentsTable');
    const tbody = table.querySelector('tbody');
    
    // Clear existing rows
    tbody.innerHTML = '';
    
    if (Object.keys(components).length === 0) {
        const row = document.createElement('tr');
        row.innerHTML = '<td colspan="4" class="text-center">No components registered</td>';
        tbody.appendChild(row);
        return;
    }
    
    // Add rows for each component
    Object.entries(components).forEach(([name, stats]) => {
        const row = document.createElement('tr');
        
        const total = stats.heapUsed + stats.offHeapUsed;
        
        row.innerHTML = `
            <td>${name}</td>
            <td>${formatBytes(stats.heapUsed)}</td>
            <td>${formatBytes(stats.offHeapUsed)}</td>
            <td>${formatBytes(total)}</td>
        `;
        
        tbody.appendChild(row);
    });
}

// Update alerts list
function updateAlerts(alerts) {
    const container = document.getElementById('alertsList');
    
    // Clear existing alerts
    container.innerHTML = '';
    
    if (alerts.length === 0) {
        container.innerHTML = '<div class="no-alerts">No alerts to display</div>';
        return;
    }
    
    // Sort alerts by timestamp (newest first)
    alerts.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
    
    // Add alerts to the container
    alerts.forEach(alert => {
        const alertElement = document.createElement('div');
        alertElement.className = 'alert-item';
        
        // Add appropriate class based on alert type
        if (alert.type === 'HIGH_HEAP_USAGE' || alert.type === 'HIGH_NON_HEAP_USAGE' || alert.type === 'FREQUENT_GC') {
            alertElement.classList.add('warning');
        } else if (alert.type === 'MEMORY_LEAK_SUSPECTED' || alert.type === 'LONG_GC_PAUSE') {
            alertElement.classList.add('error');
        }
        
        // Format timestamp
        const timestamp = new Date(alert.timestamp);
        const formattedTime = timestamp.toLocaleString();
        
        alertElement.innerHTML = `
            <div class="alert-time">${formattedTime}</div>
            <div class="alert-title">${formatAlertType(alert.type)}</div>
            <div class="alert-message">${alert.message}</div>
        `;
        
        container.appendChild(alertElement);
    });
}

// Format alert type for display
function formatAlertType(type) {
    return type.replace(/_/g, ' ').replace(/\w\S*/g, 
        txt => txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase());
}

// Update last updated timestamp
function updateLastUpdated() {
    const now = new Date();
    document.getElementById('lastUpdated').textContent = now.toLocaleString();
}

// Trigger garbage collection
function triggerGc() {
    fetch('api/gc', {
        method: 'POST'
    })
        .then(response => response.json())
        .then(data => {
            console.log('GC response:', data);
            // Refresh data after GC
            setTimeout(refreshData, 1000);
        })
        .catch(error => {
            console.error('Error triggering GC:', error);
        });
}

// Start auto refresh
function startAutoRefresh() {
    stopAutoRefresh(); // Clear any existing timer
    refreshTimer = setInterval(refreshData, refreshInterval);
}

// Stop auto refresh
function stopAutoRefresh() {
    if (refreshTimer) {
        clearInterval(refreshTimer);
        refreshTimer = null;
    }
}

// Format bytes to human-readable format
function formatBytes(bytes, decimals = 1) {
    if (bytes === 0) return '0 Bytes';
    if (bytes < 0) return 'N/A';
    
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
    
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}