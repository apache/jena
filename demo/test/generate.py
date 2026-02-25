#!/usr/bin/env python3
"""Generate synthetic mining RDF data for demo and load testing.

Usage:
    python generate.py --count 100              # 100 entities to stdout
    python generate.py --count 1000 > big.ttl   # 1000 entities to file
    python generate.py --count 500 --seed 42    # reproducible output
"""

import argparse
import random
import textwrap

COMMODITIES = [
    "Gold", "Copper", "Iron Ore", "Zinc", "Lead", "Silver",
    "Uranium", "Nickel", "Lithium", "Bauxite", "Coal", "Manganese",
]

STATES = ["QLD", "NSW", "WA", "SA", "NT", "VIC", "TAS"]

OPERATORS = [
    "BHP", "Rio Tinto", "Glencore", "Newmont", "Newcrest",
    "Fortescue", "South32", "Mineral Resources", "IGO", "Pilbara Minerals",
]

STATUSES = ["Active", "Historical", "Care and Maintenance", "Exploration"]

REPORT_STATUSES = ["Current", "Historical", "Superseded"]

SITE_ADJECTIVES = [
    "Creek", "Hill", "Valley", "Range", "Flat", "Ridge", "Gully",
    "Plains", "Crossing", "Downs", "Springs", "Bluff", "Knob", "Gap",
]

SITE_NAMES = [
    "Wattle", "Mulga", "Ironbark", "Spinifex", "Coolibah", "Kurrajong",
    "Brolga", "Kookaburra", "Echidna", "Goanna", "Wedgetail", "Barramundi",
    "Kakadu", "Uluru", "Tanami", "Kimberley", "Nullarbor", "Darling",
    "Murray", "Cooper", "Fitzroy", "Mitchell", "Gilbert", "Palmer",
]

DRILL_TYPES = ["DDH", "RC", "AC", "RAB"]

REPORT_TYPES = [
    "Resource Estimation", "Feasibility Study", "Exploration Summary",
    "Production Report", "Ore Reserves Statement", "Geological Assessment",
    "Drilling Program Results", "Environmental Baseline Study",
    "Geophysical Survey Report", "Metallurgical Testwork Report",
]


def escape_ttl(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"')


def pick_commodities(rng: random.Random) -> list[str]:
    n = rng.choices([1, 2, 3], weights=[50, 35, 15])[0]
    return rng.sample(COMMODITIES, n)


def generate_site(rng: random.Random, idx: int) -> str:
    name = f"{rng.choice(SITE_NAMES)} {rng.choice(SITE_ADJECTIVES)}"
    commodities = pick_commodities(rng)
    state = rng.choice(STATES)
    status = rng.choice(STATUSES)
    commodity_values = " , ".join(f'"{escape_ttl(c)}"' for c in commodities)
    return textwrap.dedent(f"""\
        ex:site-{idx:04d} a ex:Site ;
            rdfs:label "{escape_ttl(name)} Mine" ;
            ex:commodity {commodity_values} ;
            ex:state "{state}" ;
            ex:status "{status}" .
    """)


def generate_borehole(rng: random.Random, idx: int) -> str:
    prefix = rng.choice(["DDH", "RC", "AC"])
    site_name = rng.choice(SITE_NAMES).upper()[:3]
    label = f"{site_name}-{prefix}-{idx:03d}"
    drill_type = rng.choice(DRILL_TYPES)
    commodities = pick_commodities(rng)
    state = rng.choice(STATES)
    depth = rng.randint(50, 1200)
    commodity_values = " , ".join(f'"{escape_ttl(c)}"' for c in commodities)
    return textwrap.dedent(f"""\
        ex:bh-{idx:04d} a ex:Borehole ;
            rdfs:label "{label} {drill_type} Drill Hole" ;
            ex:commodity {commodity_values} ;
            ex:state "{state}" ;
            ex:depth {depth} .
    """)


def generate_report(rng: random.Random, idx: int) -> str:
    commodities = pick_commodities(rng)
    primary = commodities[0]
    state = rng.choice(STATES)
    operator = rng.choice(OPERATORS)
    status = rng.choice(REPORT_STATUSES)
    year = rng.randint(1980, 2024)
    report_type = rng.choice(REPORT_TYPES)
    site_name = f"{rng.choice(SITE_NAMES)} {rng.choice(SITE_ADJECTIVES)}"
    title = f"{site_name} {primary} {report_type} {year}"
    description = (
        f"{report_type} for the {site_name} {primary.lower()} project "
        f"in {state}. Prepared by {operator}."
    )
    commodity_values = " , ".join(f'"{escape_ttl(c)}"' for c in commodities)
    return textwrap.dedent(f"""\
        ex:report-{idx:04d} a ex:MiningReport ;
            rdfs:label "{escape_ttl(title)}" ;
            dct:description "{escape_ttl(description)}" ;
            ex:commodity {commodity_values} ;
            ex:state "{state}" ;
            ex:operator "{escape_ttl(operator)}" ;
            ex:status "{status}" ;
            ex:year {year} .
    """)


def main():
    parser = argparse.ArgumentParser(description="Generate synthetic mining RDF data")
    parser.add_argument("--count", type=int, default=100, help="Total number of entities")
    parser.add_argument("--seed", type=int, default=None, help="Random seed for reproducibility")
    args = parser.parse_args()

    rng = random.Random(args.seed)

    # Distribute entities: ~20% sites, ~40% boreholes, ~40% reports
    n_sites = max(1, args.count // 5)
    n_boreholes = max(1, (args.count - n_sites) // 2)
    n_reports = args.count - n_sites - n_boreholes

    print("## Generated synthetic mining data")
    print(f"## {args.count} entities: {n_sites} sites, {n_boreholes} boreholes, {n_reports} reports")
    print()
    print("@prefix ex:    <http://example.org/mining/> .")
    print("@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .")
    print("@prefix dct:   <http://purl.org/dc/terms/> .")
    print("@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .")
    print()

    print("## --- Sites ---\n")
    for i in range(n_sites):
        print(generate_site(rng, i))

    print("## --- Boreholes ---\n")
    for i in range(n_boreholes):
        print(generate_borehole(rng, i))

    print("## --- Mining Reports ---\n")
    for i in range(n_reports):
        print(generate_report(rng, i))


if __name__ == "__main__":
    main()
