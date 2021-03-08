'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
class BaseResolver {
  constructor(request, fragment) {
    this.resolver = request.resolver;
    this.reporter = request.reporter;
    this.fragment = fragment;
    this.registry = request.registry;
    this.request = request;
    this.pattern = request.pattern;
    this.config = request.config;
  }

  fork(Resolver, resolveArg) {
    for (var _len = arguments.length, args = Array(_len > 2 ? _len - 2 : 0), _key = 2; _key < _len; _key++) {
      args[_key - 2] = arguments[_key];
    }

    const resolver = new Resolver(this.request, ...args);
    resolver.registry = this.registry;
    return resolver.resolve(resolveArg);
  }

  resolve() {
    throw new Error('Not implemented');
  }
}
exports.default = BaseResolver;