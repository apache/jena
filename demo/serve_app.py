#!/usr/bin/env python3
"""Serve the demo app and proxy Fuseki requests through the same origin."""

from __future__ import annotations

import argparse
import http.client
import io
import posixpath
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.parse import urlsplit
from urllib.request import Request, urlopen


class DemoAppHandler(SimpleHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def __init__(self, *args, directory: str, backend: str, proxy_prefix: str, **kwargs):
        self.backend = backend.rstrip("/")
        self.proxy_prefix = "/" + proxy_prefix.strip("/")
        super().__init__(*args, directory=directory, **kwargs)

    def do_GET(self):
        if self.path.startswith(self.proxy_prefix + "/"):
            self._proxy_request()
            return
        super().do_GET()

    def do_POST(self):
        if self.path.startswith(self.proxy_prefix + "/"):
            self._proxy_request()
            return
        self.send_error(405, "Method Not Allowed")

    def do_OPTIONS(self):
        if self.path.startswith(self.proxy_prefix + "/"):
            self.send_response(204)
            self.send_header("Content-Length", "0")
            self.end_headers()
            return
        self.send_error(405, "Method Not Allowed")

    def translate_path(self, path: str) -> str:
        path = path.split("?", 1)[0].split("#", 1)[0]
        trailing_slash = path.rstrip().endswith("/")
        parts = [part for part in posixpath.normpath(path).split("/") if part and part not in (".", "..")]
        resolved = Path(self.directory)
        for part in parts:
            resolved /= part
        if trailing_slash:
            resolved /= ""
        return str(resolved)

    def _proxy_request(self):
        target = self.backend + self.path[len(self.proxy_prefix):]
        body = None
        content_length = self.headers.get("Content-Length")
        if content_length:
            body = self.rfile.read(int(content_length))

        headers = {}
        for name in ("Content-Type", "Accept", "Authorization"):
            value = self.headers.get(name)
            if value:
                headers[name] = value

        request = Request(target, data=body, headers=headers, method=self.command)

        try:
            with urlopen(request) as response:
                payload = response.read()
                self.send_response(response.status)
                self._copy_response_headers(response.headers, len(payload))
                self.end_headers()
                if payload:
                    self.wfile.write(payload)
        except HTTPError as error:
            payload = error.read()
            self.send_response(error.code, error.reason)
            self._copy_response_headers(error.headers, len(payload))
            self.end_headers()
            if payload:
                self.wfile.write(payload)
        except URLError as error:
            message = f"Proxy error: {error.reason}\n".encode("utf-8")
            self.send_response(502, "Bad Gateway")
            self.send_header("Content-Type", "text/plain; charset=utf-8")
            self.send_header("Content-Length", str(len(message)))
            self.end_headers()
            self.wfile.write(message)

    def _copy_response_headers(self, headers: http.client.HTTPMessage, content_length: int):
        excluded = {"connection", "content-length", "transfer-encoding", "server", "date"}
        for key, value in headers.items():
            if key.lower() not in excluded:
                self.send_header(key, value)
        self.send_header("Content-Length", str(content_length))


def main():
    parser = argparse.ArgumentParser(description="Serve the demo app with a Fuseki proxy.")
    parser.add_argument("--port", type=int, default=8000)
    parser.add_argument("--directory", default="app-static")
    parser.add_argument("--backend", required=True)
    parser.add_argument("--proxy-prefix", default="/fuseki")
    args = parser.parse_args()

    handler = partial(
        DemoAppHandler,
        directory=str(Path(args.directory).resolve()),
        backend=args.backend,
        proxy_prefix=args.proxy_prefix,
    )
    server = ThreadingHTTPServer(("0.0.0.0", args.port), handler)
    print(f"Serving app at http://localhost:{args.port}")
    print(f"Proxying {args.proxy_prefix} -> {args.backend}")
    server.serve_forever()


if __name__ == "__main__":
    main()
