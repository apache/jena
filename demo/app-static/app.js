/* Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0 */

// ---------------------------------------------------------------------------
// Configuration — adjust these if your Fuseki setup differs
// ---------------------------------------------------------------------------

const CONFIG_PATH = 'config.ttl';
const FUSEKI_BASE = 'http://localhost:3030';
const RESULT_LIMITS = [10, 100, 1000, 10000];
const DEFAULT_LIMIT = 10;
const FACET_LIMITS = [10, 25, 50, 100, 500];
const DEFAULT_FACET_LIMIT = 10;

// ---------------------------------------------------------------------------
// RDF namespace constants
// ---------------------------------------------------------------------------

const RDF = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#';
const SH = 'http://www.w3.org/ns/shacl#';
const TEXT = 'http://jena.apache.org/text#';
const IDX = 'urn:jena:lucene:index#';
const FUSEKI = 'http://jena.apache.org/fuseki#';

const SPARQL_PREFIXES = `\
PREFIX luc:  <urn:jena:lucene:index#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dct:  <http://purl.org/dc/terms/>
PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX ex:   <http://example.org/mining/>
`;

// ---------------------------------------------------------------------------
// Utility functions
// ---------------------------------------------------------------------------

function shortName(uri) {
    const h = uri.lastIndexOf('#');
    if (h >= 0) return uri.substring(h + 1);
    const s = uri.lastIndexOf('/');
    if (s >= 0) return uri.substring(s + 1);
    return uri;
}

function escapeSparql(text) {
    return text.replace(/\\/g, '\\\\').replace(/'/g, "\\'");
}

function escapeHtml(text) {
    const el = document.createElement('span');
    el.textContent = text;
    return el.innerHTML;
}

function timeStamp() {
    return new Date().toLocaleTimeString('en-GB', { hour12: false });
}

/**
 * Convert selected facet filters to CQL2-JSON string.
 * Input: {field: [val1, val2], ...} → CQL2-JSON with AND across fields, IN/= within.
 * Returns null if no filters are active.
 */
function buildCqlFilter(selected) {
    const clauses = [];
    for (const [field, values] of Object.entries(selected)) {
        if (!values || values.length === 0) continue;
        if (values.length === 1) {
            clauses.push({op: '=', args: [{property: field}, values[0]]});
        } else {
            clauses.push({op: 'in', args: [{property: field}, values]});
        }
    }
    if (clauses.length === 0) return null;
    if (clauses.length === 1) return JSON.stringify(clauses[0]);
    return JSON.stringify({op: 'and', args: clauses});
}

// ---------------------------------------------------------------------------
// N3 store helpers
// ---------------------------------------------------------------------------

const nn = (iri) => N3.DataFactory.namedNode(iri);

function getObject(store, subject, predicateIri) {
    const objs = store.getObjects(subject, nn(predicateIri), null);
    return objs.length > 0 ? objs[0] : null;
}

function getObjects(store, subject, predicateIri) {
    return store.getObjects(subject, nn(predicateIri), null);
}

function getSubjects(store, predicateIri, objectIri) {
    return store.getSubjects(nn(predicateIri), nn(objectIri), null);
}

function getLiteral(store, subject, predicateIri) {
    const obj = getObject(store, subject, predicateIri);
    return obj ? obj.value : null;
}

function walkList(store, head) {
    const items = [];
    let current = head;
    while (current && current.value !== RDF + 'nil') {
        const first = getObject(store, current, RDF + 'first');
        if (first) items.push(first);
        current = getObject(store, current, RDF + 'rest');
    }
    return items;
}

function pathToString(store, pathNode) {
    if (pathNode.termType === 'NamedNode') {
        return shortName(pathNode.value);
    }
    const inv = getObject(store, pathNode, SH + 'inversePath');
    if (inv) {
        return '^' + shortName(inv.value);
    }
    const first = getObject(store, pathNode, RDF + 'first');
    if (first) {
        const items = walkList(store, pathNode);
        return items.map(item => shortName(item.value)).join(' / ');
    }
    return pathNode.value;
}

// ---------------------------------------------------------------------------
// Config parser — reads config.ttl via N3.js
// ---------------------------------------------------------------------------

function parseTurtle(text) {
    return new Promise((resolve, reject) => {
        const store = new N3.Store();
        const parser = new N3.Parser();
        parser.parse(text, (error, quad) => {
            if (error) { reject(error); return; }
            if (quad) { store.addQuad(quad); return; }
            resolve(store);
        });
    });
}

function extractConfig(store) {
    let indexNodes = getSubjects(store, RDF + 'type', TEXT + 'TextIndexShacl');
    if (indexNodes.length === 0) indexNodes = getSubjects(store, RDF + 'type', TEXT + 'TextIndexLucene');
    if (indexNodes.length === 0) throw new Error('No text:TextIndexShacl or text:TextIndexLucene found in config');
    const indexNode = indexNodes[0];

    const storeValues = getLiteral(store, indexNode, TEXT + 'storeValues') === 'true';
    const maxFacetHits = parseInt(getLiteral(store, indexNode, TEXT + 'maxFacetHits') || '0', 10);

    // Allow config.ttl to override FUSEKI_BASE via idx:fusekiBase on the index node
    const fusekiBase = getLiteral(store, indexNode, IDX + 'fusekiBase') || FUSEKI_BASE;

    const serviceNodes = getSubjects(store, RDF + 'type', FUSEKI + 'Service');
    let datasetName = 'dataset';
    if (serviceNodes.length > 0) {
        const name = getLiteral(store, serviceNodes[0], FUSEKI + 'name');
        if (name) datasetName = name;
    }

    const shapesHead = getObject(store, indexNode, TEXT + 'shapes');
    const shapeNodes = shapesHead ? walkList(store, shapesHead) : [];

    const shapes = [];
    const facetFields = [];
    const predicateToFacet = {};
    const seenFacets = new Set();

    for (const shapeNode of shapeNodes) {
        const targetClass = getObject(store, shapeNode, SH + 'targetClass');
        const shape = {
            name: shortName(shapeNode.value),
            targetClass: targetClass ? shortName(targetClass.value) : '?',
            fields: [],
        };

        const propNodes = getObjects(store, shapeNode, SH + 'property');
        for (const propNode of propNodes) {
            const fieldName = getLiteral(store, propNode, IDX + 'fieldName');
            const fieldType = getObject(store, propNode, IDX + 'fieldType');
            const pathNode = getObject(store, propNode, SH + 'path');
            if (!fieldName || !fieldType) continue;

            const facetable = getLiteral(store, propNode, IDX + 'facetable') === 'true';
            const multiValued = getLiteral(store, propNode, IDX + 'multiValued') === 'true';
            const defaultSearch = getLiteral(store, propNode, IDX + 'defaultSearch') === 'true';
            const sortable = getLiteral(store, propNode, IDX + 'sortable') === 'true';
            const pathStr = pathNode ? pathToString(store, pathNode) : '?';

            shape.fields.push({
                name: fieldName,
                path: pathStr,
                fieldType: shortName(fieldType.value),
                facetable,
                multiValued,
                defaultSearch,
                sortable,
            });

            if (facetable && !seenFacets.has(fieldName)) {
                seenFacets.add(fieldName);
                facetFields.push(fieldName);
            }

            if (facetable && pathNode) {
                if (pathNode.termType === 'NamedNode') {
                    predicateToFacet[shortName(pathNode.value)] = fieldName;
                } else {
                    // Sequence path: map first predicate to this facet field
                    const first = getObject(store, pathNode, RDF + 'first');
                    if (first && first.termType === 'NamedNode') {
                        predicateToFacet[shortName(first.value)] = fieldName;
                    }
                    // Inverse path: map the inverted predicate
                    const inv = getObject(store, pathNode, SH + 'inversePath');
                    if (inv && inv.termType === 'NamedNode') {
                        predicateToFacet[shortName(inv.value)] = fieldName;
                    }
                }
            }
        }

        shapes.push(shape);
    }

    return {
        endpoint: `${fusekiBase}/${datasetName}/query`,
        storeValues,
        maxFacetHits,
        shapes,
        facetFields,
        predicateToFacet,
    };
}

async function loadConfig() {
    const resp = await fetch(`${CONFIG_PATH}?t=${Date.now()}`);
    if (!resp.ok) throw new Error(`Failed to fetch ${CONFIG_PATH}: ${resp.status}`);
    const text = await resp.text();
    console.log('[loadConfig] config.ttl (first 2000 chars):', text.substring(0, 2000));
    console.log('[loadConfig] config.ttl length:', text.length);
    const store = await parseTurtle(text);
    return extractConfig(store);
}

// ---------------------------------------------------------------------------
// Alpine.js component: Search page
// ---------------------------------------------------------------------------

function searchApp() {
    return {
        q: '',
        limit: DEFAULT_LIMIT,
        resultLimits: RESULT_LIMITS,
        maxFacetValues: DEFAULT_FACET_LIMIT,
        facetLimits: FACET_LIMITS,
        selected: {},
        facetFields: [],
        predicateToFacet: {},
        facets: {},
        facetExpanded: {},
        cards: [],
        error: null,
        loading: false,
        showLoading: false,
        _loadingTimer: null,
        description: '',
        endpoint: '',
        queryLog: [],

        async init() {
            let config;
            try {
                config = await loadConfig();
            } catch (e) {
                this.error = `Failed to load config: ${e.message}`;
                return;
            }

            this.endpoint = config.endpoint;
            this.facetFields = config.facetFields;
            this.predicateToFacet = config.predicateToFacet;

            this.loadFromUrl();
            await this.executeSearch();

            window.addEventListener('popstate', async () => {
                this.loadFromUrl();
                await this.executeSearch();
            });
        },

        // --- Query log ---

        logQuery(label, query, durationMs) {
            const dur = durationMs != null ? ` (${(durationMs / 1000).toFixed(2)}s)` : '';
            this.queryLog.unshift({
                time: timeStamp(),
                label: label + dur,
                query: query.trim(),
            });
        },

        // --- URL management ---

        pushUrl() {
            const params = new URLSearchParams();
            if (this.q.trim()) params.set('q', this.q.trim());
            for (const f of this.facetFields) {
                for (const v of (this.selected[f] || [])) {
                    params.append(f, v);
                }
            }
            const qs = params.toString();
            const url = qs ? '?' + qs : window.location.pathname;
            history.pushState(null, '', url);
        },

        loadFromUrl() {
            const params = new URLSearchParams(window.location.search);
            this.q = params.get('q') || '';
            for (const f of this.facetFields) {
                this.selected[f] = params.getAll(f);
            }
        },

        // --- Actions ---

        async search() {
            this.pushUrl();
            await this.executeSearch();
        },

        async toggleFacet(field, value) {
            if (!this.selected[field]) this.selected[field] = [];
            const idx = this.selected[field].indexOf(value);
            if (idx >= 0) {
                this.selected[field].splice(idx, 1);
            } else {
                this.selected[field].push(value);
            }
            this.pushUrl();
            await this.executeSearch();
        },

        async clearFilters() {
            for (const f of this.facetFields) {
                this.selected[f] = [];
            }
            this.pushUrl();
            await this.executeSearch();
        },

        hasActiveFilters() {
            return this.facetFields.some(f => (this.selected[f] || []).length > 0);
        },

        isSelected(field, value) {
            return (this.selected[field] || []).includes(value);
        },

        visibleFacets(fieldName) {
            const all = this.facets[fieldName] || [];
            if (this.facetExpanded[fieldName] || all.length <= 5) return all;
            return all.slice(0, 5);
        },

        // --- SPARQL execution ---

        async runSparql(query) {
            const resp = await fetch(this.endpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/sparql-query',
                    'Accept': 'application/sparql-results+json',
                },
                body: query,
            });
            if (!resp.ok) throw new Error(`SPARQL error: ${resp.status} ${resp.statusText}`);
            return resp.json();
        },

        // --- Query builders ---

        buildSearchQuery() {
            const term = this.q.trim() || '*';
            const escaped = escapeSparql(term);
            const cqlFilter = buildCqlFilter(this.selected);
            const filterArg = cqlFilter ? ` '${cqlFilter}'` : '';
            const facetFieldsJson = JSON.stringify(this.facetFields);

            return `${SPARQL_PREFIXES}
SELECT ?entity ?score ?totalHits ?field ?value ?count
WHERE {
    { (?entity ?score ?_lit ?totalHits) luc:query ('default' '${escaped}'${filterArg} ${this.limit}) }
    UNION
    { (?field ?value ?count) luc:facet ('default' '${escaped}' '${facetFieldsJson}'${filterArg} ${this.maxFacetValues}) }
}`;
        },

        buildDetailQuery(uris) {
            const values = uris.map(u => `<${u}>`).join(' ');
            return `${SPARQL_PREFIXES}
SELECT ?entity ?label ?type ?p ?o
WHERE {
    VALUES ?entity { ${values} }
    ?entity rdfs:label ?label .
    ?entity rdf:type ?type .
    FILTER(?type != rdfs:Resource)
    OPTIONAL {
      ?entity ?p ?o .
      FILTER(?p != rdf:type && ?p != rdfs:label && !isBlank(?o))
    }
}`;
        },

        // --- Result parsing ---

        parseUnionResults(data) {
            const hits = [];
            const facets = {};
            let totalHits = null;
            for (const row of (data.results?.bindings || [])) {
                if (row.entity) {
                    hits.push({
                        uri: row.entity.value,
                        score: parseFloat(row.score.value),
                    });
                    if (totalHits === null && row.totalHits) {
                        totalHits = parseInt(row.totalHits.value, 10);
                    }
                } else if (row.field) {
                    const f = row.field.value;
                    if (!facets[f]) facets[f] = [];
                    facets[f].push({
                        value: row.value.value,
                        count: parseInt(row.count.value, 10),
                    });
                }
            }
            return { hits, facets, totalHits };
        },

        mergeFacets(rawFacets) {
            const merged = {};
            for (const f of this.facetFields) {
                const values = {};
                for (const fv of (rawFacets[f] || [])) {
                    values[fv.value] = fv;
                }
                for (const sv of (this.selected[f] || [])) {
                    if (!values[sv]) {
                        values[sv] = { value: sv, count: 0 };
                    }
                }
                merged[f] = Object.values(values).sort((a, b) =>
                    b.count - a.count || a.value.localeCompare(b.value)
                );
            }
            return merged;
        },

        parseEntityDetails(data, scores) {
            const entities = {};
            for (const row of (data.results?.bindings || [])) {
                const uri = row.entity.value;
                if (!entities[uri]) {
                    const typeUri = row.type?.value || '';
                    entities[uri] = {
                        uri,
                        label: row.label.value,
                        entityType: shortName(typeUri),
                        score: scores[uri] || 0,
                        description: null,
                        properties: {},
                        tags: [],
                    };
                }
                const card = entities[uri];
                if (row.p && row.o) {
                    const pred = shortName(row.p.value);
                    const raw = row.o.value;
                    const obj = row.o.type === 'uri' ? shortName(raw) : raw;
                    if (!card.properties[pred]) card.properties[pred] = [];
                    if (!card.properties[pred].includes(obj)) {
                        card.properties[pred].push(obj);
                    }
                }
            }

            const cards = Object.values(entities);
            for (const card of cards) {
                if (card.properties.description) {
                    card.description = card.properties.description[0];
                }
                for (const [pred, values] of Object.entries(card.properties)) {
                    if (pred === 'description') continue;
                    const facetField = this.predicateToFacet[pred] || null;
                    for (const v of values) {
                        const active = facetField ? this.isSelected(facetField, v) : false;
                        card.tags.push({
                            pred,
                            value: v,
                            facetField,
                            isActive: active,
                            clickable: !!facetField,
                            cssClass: !facetField ? 'rtag-neutral'
                                : active ? 'rtag-active'
                                : 'rtag-clickable',
                        });
                    }
                }
            }

            return cards.sort((a, b) => b.score - a.score);
        },

        // --- Description ---

        buildDescription(hitCount, totalHits, totalSec) {
            const parts = [];
            const q = this.q.trim();
            if (q) {
                parts.push(`Search for <strong>\u201c${escapeHtml(q)}\u201d</strong>`);
            } else {
                parts.push('Showing <strong>all entities</strong>');
            }

            const filters = [];
            for (const [field, values] of Object.entries(this.selected)) {
                if (!values || values.length === 0) continue;
                const quoted = values.map(v => `\u201c${escapeHtml(v)}\u201d`);
                if (quoted.length === 1) {
                    filters.push(`${escapeHtml(field)} = ${quoted[0]}`);
                } else {
                    filters.push(`(${escapeHtml(field)} = ${quoted.join(' OR ')})`);
                }
            }
            if (filters.length > 0) {
                parts.push('filtered by ' + filters.join(' AND '));
            }

            let result = parts.join(' ') + ' \u2014 ';
            if (totalHits != null && totalHits > hitCount) {
                result += `<strong>${hitCount.toLocaleString()}</strong> of <strong>${totalHits.toLocaleString()}</strong> results`;
            } else {
                result += `<strong>${(totalHits || hitCount).toLocaleString()}</strong> results`;
            }
            if (totalSec != null) {
                result += ` in <strong>${totalSec.toFixed(2)}s</strong>`;
            }
            return result;
        },

        // --- Search execution ---

        async executeSearch() {
            this.loading = true;
            this.showLoading = true;
            clearTimeout(this._loadingTimer);
            const loadStart = performance.now();
            this.error = null;

            try {
                const searchQuery = this.buildSearchQuery();
                const activeFilters = Object.entries(this.selected)
                    .filter(([, v]) => v && v.length > 0).length;
                const searchLabel = (this.q.trim() || '*')
                    + (activeFilters > 0 ? ` + ${activeFilters} filter${activeFilters > 1 ? 's' : ''}` : '');

                let t0 = performance.now();
                const data = await this.runSparql(searchQuery);
                const searchMs = performance.now() - t0;
                this.logQuery(`Search: ${searchLabel}`, searchQuery, searchMs);

                const { hits, facets, totalHits } = this.parseUnionResults(data);
                this.facets = this.mergeFacets(facets);

                if (hits.length > 0) {
                    const uris = hits.slice(0, this.limit).map(h => h.uri);
                    const scores = Object.fromEntries(hits.map(h => [h.uri, h.score]));
                    const detailQuery = this.buildDetailQuery(uris);

                    t0 = performance.now();
                    const detailData = await this.runSparql(detailQuery);
                    const detailMs = performance.now() - t0;
                    this.logQuery(`Details: ${uris.length} entities`, detailQuery, detailMs);

                    this.cards = this.parseEntityDetails(detailData, scores);
                } else {
                    this.cards = [];
                }

                const totalSec = (performance.now() - loadStart) / 1000;
                this.description = this.buildDescription(hits.length, totalHits, totalSec);
            } catch (e) {
                if (e.name === 'TypeError' || (e.message && (e.message.includes('Failed to fetch') || e.message.includes('NetworkError')))) {
                    this.error = `Cannot connect to Fuseki at ${this.endpoint}. Is the server running?`;
                } else {
                    this.error = `Query failed: ${e.message}`;
                }
                this.cards = [];
            }

            this.loading = false;
            const elapsed = performance.now() - loadStart;
            const remaining = Math.max(0, 400 - elapsed);
            this._loadingTimer = setTimeout(() => { this.showLoading = false; }, remaining);
        },
    };
}

// ---------------------------------------------------------------------------
// Alpine.js component: Config page
// ---------------------------------------------------------------------------

function configApp() {
    return {
        config: null,
        error: null,

        async init() {
            try {
                this.config = await loadConfig();
                console.log('[configApp] shapes:', this.config.shapes.length);
                for (const s of this.config.shapes) {
                    console.log(`[configApp] ${s.name}: ${s.fields.length} fields →`, s.fields.map(f => f.name));
                }
            } catch (e) {
                this.error = `Failed to load config: ${e.message}`;
            }
        },
    };
}

// ---------------------------------------------------------------------------
// Alpine.js component: Stats page
// ---------------------------------------------------------------------------

function statsApp() {
    return {
        stats: null,
        error: null,
        loading: true,

        async init() {
            try {
                const config = await loadConfig();
                const endpoint = config.endpoint;
                const facetFields = config.facetFields;
                const t0 = performance.now();

                // 1. Total entities + facet counts in one query
                const facetFieldsJson = JSON.stringify(facetFields);
                const statsQuery = `${SPARQL_PREFIXES}
SELECT ?entity ?score ?totalHits ?field ?value ?count
WHERE {
    { (?entity ?score ?_lit ?totalHits) luc:query ('default' '*' 0) }
    UNION
    { (?field ?value ?count) luc:facet ('default' '*' '${facetFieldsJson}' 0) }
}`;
                const statsData = await this.runSparql(endpoint, statsQuery);
                const statsMs = performance.now() - t0;

                // Parse union results
                let totalHits = 0;
                const facets = {};
                for (const row of (statsData.results?.bindings || [])) {
                    if (row.totalHits && totalHits === 0) {
                        totalHits = parseInt(row.totalHits.value, 10);
                    }
                    if (row.field) {
                        const f = row.field.value;
                        if (!facets[f]) facets[f] = [];
                        facets[f].push({
                            value: row.value.value,
                            count: parseInt(row.count.value, 10),
                        });
                    }
                }
                // Sort each facet by count desc
                for (const f of Object.keys(facets)) {
                    facets[f].sort((a, b) => b.count - a.count);
                }

                // 2. Triple count
                const t1 = performance.now();
                const countQuery = `SELECT (COUNT(*) AS ?count) WHERE { ?s ?p ?o }`;
                const countData = await this.runSparql(endpoint, countQuery);
                const countMs = performance.now() - t1;
                const tripleCount = parseInt(
                    countData.results?.bindings?.[0]?.count?.value || '0', 10
                );

                const totalMs = performance.now() - t0;

                this.stats = {
                    totalEntities: totalHits,
                    totalTriples: tripleCount,
                    shapes: config.shapes.length,
                    facetableFields: facetFields.length,
                    facets,
                    facetFields,
                    statsQueryMs: statsMs,
                    countQueryMs: countMs,
                    totalMs,
                };
            } catch (e) {
                if (e.name === 'TypeError' || (e.message && (e.message.includes('Failed to fetch') || e.message.includes('NetworkError')))) {
                    this.error = `Cannot connect to Fuseki. Is the server running?`;
                } else {
                    this.error = `Query failed: ${e.message}`;
                }
            }
            this.loading = false;
        },

        async runSparql(endpoint, query) {
            const resp = await fetch(endpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/sparql-query',
                    'Accept': 'application/sparql-results+json',
                },
                body: query,
            });
            if (!resp.ok) throw new Error(`SPARQL error: ${resp.status} ${resp.statusText}`);
            return resp.json();
        },
    };
}
