Yep — a **high-level “system context” diagram + a couple of mini sequence/flow diagrams** is usually the sweet spot. The big one answers “what are the boxes and how do they connect?”, and the minis answer “what actually happens for create/update vs ETL distribution?”.

Below is a clean set you can drop into docs (Mermaid).

## Diagram 1 — System overview (containers + main data paths)

```mermaid
flowchart LR
  subgraph Client
    UI[UI\nSubmit Create/Update]
  end

  subgraph API_Tier[API Tier]
    API[API\nCreate/Update Endpoint]
  end

  subgraph Write_Path[Write Path]
    D[Dispatcher / RDF Delta\nCreate/Update object\nEmit RDF Patch]
    F[Fuseki\nTriplestore]
  end

  subgraph Data_Products[Data Products]
    ETL[ETL\nPull public data\nPublish datasets/files]
    Cons[Consumers\nAPIs / Files / Feeds]
  end

  UI -->|HTTP| API
  API -->|Command / event| D
  D -->|RDF Patch| F

  ETL -->|Reads| F
  ETL -->|Push / transmit| Cons
```

## Diagram 2 — Create/Update request flow (sequence)

```mermaid
sequenceDiagram
  autonumber
  participant UI as UI
  participant API as API
  participant D as RDF Delta / Dispatcher
  participant F as Fuseki

  UI->>API: POST/PUT create/update payload
  API->>D: validate + map to domain command
  D->>D: build RDF changeset / patch
  D->>F: apply RDF Patch
  F-->>D: ack (applied / conflict / error)
  D-->>API: result (id/version/status)
  API-->>UI: 200/202 + response body
```

**Notes you may want to reflect in labels (optional):**

* If the write path is async, have API return **202 Accepted** with a job id, and UI polls / subscribes.
* If you care about optimistic concurrency, show **ETag/version** coming from Fuseki or derived state.

## Diagram 3 — RDF Delta as an internal pattern (event-sourced-ish patch log)

This illustrates *why* the dispatcher exists (append-only patches, replay, downstream subscribers, etc.).

```mermaid
flowchart TB
  API[API Command] --> D[Dispatcher\nNormalise + Authorise\nCreate RDF Patch]

  D --> L[(RDF Patch Log / Stream\nRDF Delta)]
  L --> F[Fuseki\nMaterialised store]
  L --> S[Other subscribers\n(optional)]
  S --> X[Validation / indexing / cache\n(optional)]
```

If you don’t actually have a patch log/stream and it’s purely “dispatcher generates patch then posts to Fuseki”, remove the log box and keep it as a simple write adapter.

## Diagram 4 — ETL publication + distribution modes

This one is handy because ETL “to consumers” can mean different mechanisms (push files, post to APIs, publish to a bucket, etc.).

```mermaid
flowchart LR
  F[Fuseki] -->|SPARQL/graph read| ETL[ETL Orchestrator]

  ETL -->|Dataset build| Stage[(Staging\nfiles/graphs)]
  Stage -->|Publish files| Files[Static hosting / object store]
  Stage -->|Post payloads| PushAPI[Consumer APIs]
  Stage -->|Stream updates| Feed[Feed/queue/topic]
  Files --> Cons[Consumers]
  PushAPI --> Cons
  Feed --> Cons
```

## Is it sensible to have mini diagrams?

Yes — very. A practical doc set is:

* **1x Overview container diagram** (Diagram 1)
* **1x Sequence diagram per major use case**

  * Create/Update write path (Diagram 2)
  * Optional: Read/query path (UI → API → Fuseki)
* **1x “pattern” diagram** for anything non-obvious

  * RDF Delta / patch-log / dispatcher responsibility split (Diagram 3)
* **1x Distribution diagram** for ETL consumer delivery (Diagram 4)

If you want, I can also add a **read/query** mini diagram (often clarifies that writes go via dispatcher but reads can go direct to Fuseki or via API).
