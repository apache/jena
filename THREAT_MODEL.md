<!--
SPDX-License-Identifier: Apache-2.0

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# Apache Jena — Threat Model (v0 draft)

## §1 Header

- **Project:** Apache Jena (`apache/jena`), `main`, against which this draft was written. A monorepo: the RDF/SPARQL Java framework (`jena-core`, `jena-arq`, `jena-base`, RIOT parsers, `jena-tdb1`/`jena-tdb2` stores, SHACL/ShEx, GeoSPARQL, text index) **and** the Fuseki HTTP server (`jena-fuseki2`).
- **Date:** 2026-06-02. **Status:** draft — for Apache Jena PMC review. **Author:** ASF Security team (drafted via the Scovetta threat-model rubric), for PMC ratification.
- **Version binding:** versioned with the project; a report against version *N* is triaged against the model as it stood at *N*.
- **Reporting cross-reference:** §8-property violations → report privately per ASF process (`security@apache.org` → `private@jena.apache.org`); §3/§9 findings are closed citing this document.
- **Provenance legend:** *(documented)* = Jena's own docs/repo; *(maintainer)* = confirmed by a Jena PMC member through this process (andy@ has ratified destination + the help-with-model request); *(inferred)* = reasoned from architecture, not yet confirmed — each has a matching §14 open question.
- **Draft confidence:** ~12 documented / ~2 maintainer / ~34 inferred.
- **What Jena is:** Apache Jena is a Java framework for building Semantic-Web / linked-data applications over RDF. It provides an in-process API to RDF data held in memory or in a native store (TDB), the ARQ SPARQL query/update engine, RIOT parsers/serialisers for RDF syntaxes (Turtle, RDF/XML, JSON-LD, N-Triples, …), and **Fuseki** — a standalone HTTP server exposing SPARQL query, SPARQL Update, and the Graph Store Protocol over the network. *(documented — README, jena.apache.org; maintainer — andy@ 2026-06-01: "an HTTP-based data server (Fuseki) and a Java API to RDF data stored in memory and in a custom database")*

## §2 Scope and intended use

- **Two deployment shapes** *(maintainer — andy@)*:
  - **Fuseki** — a long-running **HTTP server** that answers SPARQL over the network. The primary network trust surface.
  - **The Jena Java API** — `jena-core`/`jena-arq`/TDB embedded **in-process** in another application. Trusted caller; the bytes/queries it feeds Jena are that application's responsibility.
- **Caller roles** (Fuseki is a network service — the role splits):
  - **anonymous SPARQL client** — issues SPARQL queries over HTTP. **Default-public for query** *(documented — Fuseki security docs: "SPARQL endpoints are open to the public but administrative functions are limited to localhost")*.
  - **authenticated user / admin** — gated by Apache Shiro (`shiro.ini`); admin functions (`/$/*`) restricted to localhost by default *(documented)*.
  - **operator/deployer** — configures Shiro, datasets, TDB location, and which endpoints are read-only vs updatable. **Trusted.** *(inferred)*
  - **embedding application** (Java API) — trusted; supplies queries/RDF to the library. *(inferred)*

**Component-family table** *(monorepo; in/out of model):*

| Family | Entry point | Touches OS/network | In model? |
| --- | --- | --- | --- |
| Fuseki HTTP server | `jena-fuseki2` — SPARQL query / Update / Graph Store Protocol, admin `/$/*` | network (listens) | **In — primary boundary** *(documented)* |
| SPARQL engine (ARQ) | `jena-arq` — query/update eval, `SERVICE` federation, custom functions | network out (SERVICE), file (file: URLs) | **In — high value** *(inferred)* |
| RDF I/O (RIOT) | `jena-arq`/`jena-core` parsers (RDF/XML, Turtle, JSON-LD, …) | parses untrusted RDF | **In — XXE / parser-DoS surface** *(inferred)* |
| Stores | `jena-tdb1`, `jena-tdb2`, `jena-db` | filesystem | **In (engine's use); on-disk store is operator-trusted** *(inferred)* |
| IRI / langtag | `jena-iri3986`, `jena-langtag`, `jena-base` | none | **In (input parsing)** *(inferred)* |
| Validation / extensions | `jena-shacl`, `jena-shex`, `jena-geosparql`, `jena-text`, `jena-serviceenhancer` | text index; SERVICE | **In (reachable from queries)** *(inferred)* |
| Client/API helpers | `jena-rdfconnection`, `jena-querybuilder`, `jena-rdfpatch`, `jena-commonsrdf`, `jena-ontapi` | none | **In as libraries (memory/correctness)** *(inferred)* |
| CLI tools | `jena-cmds` | filesystem | **In iff fed untrusted input; usually operator-run** *(inferred)* |
| Examples / tests / benchmarks | `jena-examples`, `jena-integration-tests`, `jena-benchmarks` | n/a | **Out** *(see §3)* |

## §3 Out of scope (explicit non-goals)

- **`jena-examples`, `jena-integration-tests`, `jena-benchmarks`** — illustrative/test, not production. *(inferred)*
- **Attackers who control the host, the Fuseki config (`shiro.ini`, dataset config), the TDB data directory, or the embedding Java application.** Operator-trusted. *(inferred)*
- **The embedding application's own use of the Java API** — if an app feeds attacker-controlled SPARQL it built by string-concatenation to ARQ, that injection is the app's bug, not Jena's (analogous to SQL injection in a JDBC caller). *(inferred)*
- **Generic DoS / query-complexity exhaustion** beyond a to-be-confirmed line — Andy raised resource-volume as a concern; the §8 resource line + §14 frame it. *(inferred)*
- **Confidentiality of RDF data at rest / TLS on the wire** — operator deployment (reverse proxy for TLS; filesystem perms for TDB). *(inferred)*

## §4 Trust boundaries and data flow

- **Primary boundary: the Fuseki SPARQL endpoint.** Queries arrive over HTTP from (by default) **anonymous** clients. The boundary question is what an anonymous/low-privilege SPARQL query can reach: read data it shouldn't, **write** (SPARQL Update / GSP) without authorisation, make Fuseki issue outbound requests (`SERVICE` → SSRF), read local files (`file:` URLs / FROM), execute code (ARQ custom/JavaScript functions if enabled), or exhaust resources. *(inferred; public-query default documented)*
- **Admin boundary:** the `/$/*` admin surface is localhost-only by default *(documented)*; exposing it to the network is an operator misconfiguration.
- **RDF-parse boundary:** any endpoint that **parses** caller-supplied RDF (Update bodies, GSP PUT/POST, content negotiation) runs RIOT on untrusted bytes — the XXE (RDF/XML) and parser-DoS surface. *(inferred)*
- **Reachability preconditions:**
  - A finding in ARQ/RIOT/stores is **in-model** iff reachable from a Fuseki request at the relevant role (default: anonymous query; authenticated for Update). *(inferred)*
  - A finding reachable only through the **in-process Java API** with caller-supplied trusted input is `OUT-OF-MODEL: trusted-input` (the embedding app owns it). *(inferred)*
  - A finding requiring operator config (`shiro.ini`, exposing admin, enabling JS functions) is `OUT-OF-MODEL: trusted-input` / `non-default-build`. *(inferred)*

## §5 Assumptions about the environment

- **Runtime:** JVM (Java; "old in places" per andy@). *(maintainer)*
- **Fuseki auth:** Apache Shiro via `$FUSEKI_BASE/shiro.ini`; changing it needs a restart *(documented — Fuseki security docs)*.
- **Store:** TDB1/TDB2 on the local filesystem, assumed private to the Fuseki/JVM process. *(inferred)*
- **Network:** TLS is the deployer's (reverse proxy); Fuseki's bundled example setup is plaintext *(documented — "no TLS, passwords in plain text")*.
- **Negative side-effects inventory** (inferred — wave-1/2 target): Fuseki listens on HTTP; ARQ can make **outbound** network requests via `SERVICE` (federation) and can read **`file:`/http: URLs** named in queries (FROM/FROM NAMED/SERVICE); RIOT parses untrusted RDF; ARQ may execute **custom/JavaScript functions** if the operator enabled them; TDB reads/writes the data directory. *(inferred — these are the load-bearing confirmations)*

## §5a Build-time and configuration variants

Security-relevant configuration *(Fuseki auth documented; the rest inferred — confirm defaults):*

| Knob | Default | Effect / stance |
| --- | --- | --- |
| Fuseki Shiro auth (`shiro.ini`) | SPARQL **query** public; admin `/$/*` **localhost-only** | *(documented)* Restricting query access requires Shiro `[urls]` ACLs. |
| Fuseki example user setup | `admin`/`pw`, plaintext, no TLS | *(documented)* explicitly "not recommended for production". Any "default admin/pw in prod" report → `OUT-OF-MODEL: non-default-build`. |
| SPARQL **Update** / Graph Store write | per-dataset (read-only vs read-write service) — **default to confirm** | *(inferred)* If a dataset ships update-enabled + unauthenticated, anonymous write is in-model; if read-only by default, anonymous write is not reachable. **Wave-1 question.** |
| `SERVICE` (federated query) | **to confirm** (enabled? restrictable allow-list?) | *(inferred)* SSRF surface; whether it can be disabled / allow-listed is the key §10 lever. |
| ARQ **JavaScript / custom functions** | **to confirm** (opt-in?) | *(inferred)* If enabled, SPARQL can execute code → by-design-if-operator-enabled, like a trusted extension. |
| RDF/XML & external-entity handling in RIOT | **to confirm** (XXE off by default?) | *(inferred)* Whether external entities / `file:` access are disabled by default in the parsers. |
| Query timeout / result limits | **to confirm** | *(inferred)* the resource/DoS lever (Andy's concern). |

## §6 Assumptions about inputs

Per-surface trust table *(Fuseki defaults documented; the rest inferred):*

| Surface | Input | Attacker-controllable? | Caller/operator must enforce |
| --- | --- | --- | --- |
| Fuseki SPARQL query endpoint | SPARQL query text | **yes (anonymous by default)** | Shiro ACLs if data is sensitive; SERVICE/file/JS-function restrictions; query timeout |
| Fuseki SPARQL Update / GSP | update text / RDF body | **yes — must be authorised** | read-only-by-default or Shiro-gated write; RDF parse hardening |
| RDF parse (RIOT) anywhere | RDF/XML, Turtle, JSON-LD, … | **yes** | external-entity (XXE) off; bounded nesting/size |
| `SERVICE <url>` in a query | target URL | **yes** | SSRF egress controls / allow-list |
| `FROM` / `FROM NAMED` / `file:` URI | dataset URI | **yes** | block `file:` and arbitrary fetch from untrusted queries |
| Fuseki admin `/$/*` | dataset mgmt, backups | **must not be on the public net** | localhost-only (default) / operator network |
| Java API (`QueryExecution`, `Model.read`) | query / RDF from the app | no — the embedding app's trust | app validates its own untrusted inputs |

- **Size/shape/rate:** query-cost / result-size / parser-nesting bounds — to confirm (Andy's volume concern); §8 resource line. *(inferred)*

## §7 Adversary model

- **Anonymous SPARQL client (primary)** — can reach Fuseki's public query endpoint; goals: read non-public graphs, write via an exposed Update endpoint, SSRF via `SERVICE`, local-file read via `file:`/FROM, code execution via JS functions (if enabled), resource exhaustion via expensive queries. *(inferred; public-query default documented)*
- **Authenticated low-privilege user** — bounded by Shiro/dataset ACLs; goal: exceed them. *(inferred)*
- **Crafted-RDF attacker** — supplies malicious RDF (RDF/XML XXE, deeply-nested/oversized documents) to any parse path. *(inferred)*
- **Out of scope:** operator/host control; the embedding app supplying its own trusted input; anyone who can edit `shiro.ini` or enable JS functions. *(inferred)*

## §8 Security properties the project provides

*(All inferred pending PMC confirmation except where Fuseki defaults are documented.)*

- **Admin surface is localhost-bound by default.** Fuseki's `/$/*` admin functions are not reachable from the network unless the operator exposes them. *Violation symptom:* an admin function reachable anonymously over the network in the default config. *Severity:* CVE-class. *(documented — Fuseki security docs)*
- **Shiro access control is enforced when configured.** A Shiro `[urls]` ACL restricting an endpoint cannot be bypassed by request manipulation. *Violation symptom:* a restricted endpoint reached without satisfying its Shiro rule. *Severity:* CVE-class. *(inferred)*
- **SPARQL queries cannot escape the dataset's authorised scope.** An anonymous/low-priv query cannot read graphs, write data, reach the filesystem, or make Fuseki act as an SSRF proxy beyond what the dataset config permits. *Violation symptom:* SERVICE-SSRF, `file:` read, cross-graph read, or unauthorised write from an in-scope query. *Severity:* CVE-class. *(inferred — the core boundary to ratify; these are the classic Jena CVE classes)*
- **RDF parsing is safe against untrusted documents.** RIOT parsing of attacker RDF does not resolve external entities (XXE), execute code, or recurse/allocate unboundedly. *Violation symptom:* XXE, SSRF, or DoS from a parsed RDF document. *Severity:* CVE-class. *(inferred)*
- **Resource bounds — UNSPECIFIED.** Whether an expensive SPARQL query (Andy's volume concern) or a large RDF body is a bug or an operator-tuned limit (query timeout) is open. *(inferred)*

## §9 Security properties the project does *not* provide

- **No protection if the operator exposes the admin surface, ships the example `admin`/`pw` setup, or runs without TLS** — deployment hardening (pending §5a rulings). *(documented that the example setup is not for production)*
- **No defense when ARQ JavaScript/custom functions are enabled on an untrusted endpoint** — enabling code-executing functions and exposing them to anonymous queries is operator-chosen code execution (by-design, like a trusted extension), pending confirmation. *(inferred)* **False friend:** a SPARQL endpoint being "read-only" does not by itself prevent SSRF (`SERVICE`) or local-file read (`file:`) unless those are separately restricted.
- **No SPARQL-injection defense for the embedding application** — an app that concatenates untrusted input into a query string owns that bug (use parameterised queries / `QueryBuilder`). *(inferred)*
- **No transport security / authentication unless the operator configures Shiro + TLS.** *(documented/inferred)*
- **No generic-DoS / query-complexity guarantee** beyond a to-be-stated line. *(inferred)*
- **Well-known classes left to the caller/operator:** SSRF via `SERVICE`, local-file disclosure via `file:`/FROM, XXE in RDF/XML, SPARQL injection (embedding app), and algorithmic-complexity DoS via crafted queries. *(inferred — Jena's published CVE history clusters here; confirm in §14)*

## §10 Downstream responsibilities (operator/deployer)

- **Put Fuseki behind auth (Shiro) + TLS** before exposing sensitive data; never ship the example `admin`/`pw` setup to production. *(documented)*
- **Keep the admin `/$/*` surface localhost-only / operator-network.** *(documented)*
- **Make datasets read-only unless write is intended**, and gate SPARQL Update / GSP behind Shiro. *(inferred)*
- **Restrict or disable `SERVICE` federation and `file:` access** on endpoints reachable by untrusted clients (SSRF / local-file). *(inferred)*
- **Do not enable ARQ JavaScript/custom functions on untrusted endpoints.** *(inferred)*
- **Set query timeouts / result-size limits** appropriate to capacity (the volume lever). *(inferred)*
- **Use parameterised queries** (`QueryBuilder`/parameterised `QueryExecution`) in embedding apps; never string-concatenate untrusted input into SPARQL. *(inferred)*

## §11 Known misuse patterns

*(Draft one-liners — expand before publishing.)*

- Exposing a public, update-enabled SPARQL endpoint with no auth. *(inferred)*
- Leaving `SERVICE`/`file:` reachable from anonymous queries (SSRF / file read). *(inferred)*
- Enabling ARQ JS functions on a public endpoint. *(inferred)*
- Shipping the example `admin`/`pw` / no-TLS Fuseki setup to production. *(documented as not-for-prod)*
- Building SPARQL by concatenating untrusted strings in an embedding app. *(inferred)*
- Parsing untrusted RDF/XML without external-entity protections. *(inferred)*

## §11a Known non-findings (recurring false positives)

*(Seed list — confirmations are the highest-leverage scan-suppression input.)*

- "Fuseki SPARQL endpoint is open without auth" — public **query** is the documented default; restricting it is the operator's Shiro config. A report is `VALID` only if a *configured* restriction is bypassed or an *update/admin* surface is anonymously reachable. *(documented default)*
- "Default `admin`/`pw`, no TLS" — the example setup, documented as not-for-production → `OUT-OF-MODEL: non-default-build`. *(documented)*
- "SPARQL query consumes lots of CPU/memory" — pending the §8 resource line; likely operator-tuned (query timeout) unless super-linear on a small query. *(inferred)*
- "ARQ can call JavaScript / custom functions" — only if the operator enabled them; on a trusted/admin endpoint that's by-design. `OUT-OF-MODEL: trusted-input` / `non-default-build` unless reachable anonymously. *(inferred — confirm the default)*
- "Embedding app built an injectable SPARQL string" — the app's bug, not Jena's. `OUT-OF-MODEL: trusted-input`. *(inferred)*

## §12 Conditions that would change this model

- A change to Fuseki's default auth posture (public-query / localhost-admin), the example-setup defaults, or the SPARQL-Update default. *(documented knobs)*
- A change to `SERVICE`/`file:`/JS-function defaults or their restrictability. *(inferred)*
- A new network surface or a new parser. *(inferred)*
- A report that cannot be routed to one §13 disposition → revise the model.

## §13 Triage dispositions

| Disposition | Meaning | Licensed by |
| --- | --- | --- |
| `VALID` | Violates a §8 property via an in-scope adversary/input (config-bypass, anonymous write/admin, SSRF/file-read/XXE/code-exec from an in-scope query under default config). | §8, §6, §7 |
| `VALID-HARDENING` | No §8 property broken, but a §11 misuse is easy enough to harden (safer defaults, SERVICE allow-list, parser limits). | §11 |
| `OUT-OF-MODEL: trusted-input` | Requires operator config (shiro.ini, enabling JS functions, exposing admin) or the embedding app's own untrusted input. | §6, §7 |
| `OUT-OF-MODEL: adversary-not-in-scope` | Requires host/JVM/config control. | §7 |
| `OUT-OF-MODEL: unsupported-component` | Lands in `jena-examples` / tests / benchmarks. | §3 |
| `OUT-OF-MODEL: non-default-build` | Only manifests under a discouraged/non-default §5a setting (example creds, JS functions on, admin exposed). | §5a |
| `BY-DESIGN: property-disclaimed` | Concerns a §9-disclaimed property (operator-enabled code exec, no-TLS-by-default, embedding-app SPARQL injection). | §9 |
| `KNOWN-NON-FINDING` | Matches a §11a entry. | §11a |
| `MODEL-GAP` | Cannot be routed — triggers §12. | §12 |

## §14 Open questions for the maintainers

**Wave 1 — scope & Fuseki defaults:**
1. Confirm scope is the `apache/jena` monorepo with **Fuseki + ARQ + RIOT + TDB** as the in-model core, and `jena-examples`/tests/benchmarks out. → §2/§3.
2. **SPARQL Update / Graph Store write default:** does a Fuseki dataset ship **read-only** by default, or can it be update-enabled-and-unauthenticated? (Decides whether anonymous write is in-model or a misconfig.) → §5a/§8.
3. Confirm the documented default — public query, localhost-only admin, example `admin`/`pw` is not-for-production (`non-default-build`). → §5a/§11a.

**Wave 2 — the high-value query surfaces (the Jena CVE classes):**
4. **`SERVICE` federation (SSRF):** is it enabled by default, and can it be disabled / allow-listed? Is an SSRF via `SERVICE` from an anonymous query `VALID`? → §8/§9/§10.
5. **`file:` / arbitrary-URI access** via FROM / FROM NAMED / SERVICE: is local-file read from an untrusted query prevented by default? → §8/§9.
6. **ARQ JavaScript / custom functions:** opt-in? If enabled and reachable anonymously, is code execution `VALID` or by-design-operator-enabled? → §5a/§9/§11a.
7. **RIOT / RDF-XML XXE:** are external entities (and `file:` fetches) disabled by default in the parsers? → §8.

**Wave 3 — resources, API, meta:**
8. **Resource/DoS line** (your volume concern): is an expensive SPARQL query or huge RDF body a bug, or operator-tuned via query-timeout/result-limits? Where's the line? → §8/§11a.
9. Confirm the **in-process Java API** is modeled as trusted-caller (embedding-app SPARQL injection is the app's bug), and that parameterised queries are the recommended pattern. → §3/§9.
10. Any other recurring scanner/fuzzer false positives to seed §11a? → §11a.
11. **Meta:** Jena has no in-repo `SECURITY.md`/`AGENTS.md`; this engagement adds them + `THREAT_MODEL.md`, wiring `AGENTS.md → SECURITY.md → THREAT_MODEL.md`. The Fuseki security docs live on the website. Confirm the in-repo model is canonical and references the website docs; confirm revision ownership. → §1.
