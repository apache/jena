'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.USED = exports.REMOVED_ANCESTOR = exports.ENVIRONMENT_IGNORE = undefined;

var _set;

function _load_set() {
  return _set = _interopRequireDefault(require('babel-runtime/core-js/set'));
}

var _slicedToArray2;

function _load_slicedToArray() {
  return _slicedToArray2 = _interopRequireDefault(require('babel-runtime/helpers/slicedToArray'));
}

var _misc;

function _load_misc() {
  return _misc = require('./util/misc.js');
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const invariant = require('invariant');

const ENVIRONMENT_IGNORE = exports.ENVIRONMENT_IGNORE = 'ENVIRONMENT_IGNORE';
const REMOVED_ANCESTOR = exports.REMOVED_ANCESTOR = 'REMOVED_ANCESTOR';
const USED = exports.USED = 'USED';

class PackageReference {
  constructor(request, info, remote) {
    this.resolver = request.resolver;
    this.lockfile = request.lockfile;
    this.requests = [];
    this.config = request.config;

    this.registry = remote.registry;
    this.version = info.version;
    this.name = info.name;
    this.uid = info._uid;

    this.remote = remote;

    this.dependencies = [];

    this.permissions = {};
    this.patterns = [];
    this.optional = null;
    this.visibility = { [ENVIRONMENT_IGNORE]: 0, [REMOVED_ANCESTOR]: 0, [USED]: 0 };
    this.root = false;
    this.ignore = false;
    this.fresh = false;
    this.location = null;

    this.addRequest(request);
  }

  setFresh(fresh) {
    this.fresh = fresh;
  }

  setLocation(loc) {
    return this.location = loc;
  }

  addRequest(request) {
    this.requests.push(request);

    if (!request.parentRequest) {
      this.root = true;
    }
  }

  prune() {
    for (const selfPattern of this.patterns) {
      // remove ourselves from the resolver
      this.resolver.removePattern(selfPattern);
    }
  }

  addDependencies(deps) {
    this.dependencies = this.dependencies.concat(deps);
  }

  setPermission(key, val) {
    this.permissions[key] = val;
  }

  hasPermission(key) {
    if (key in this.permissions) {
      return this.permissions[key];
    } else {
      return false;
    }
  }

  addPattern(pattern, manifest) {
    this.resolver.addPattern(pattern, manifest);

    this.patterns.push(pattern);

    const shrunk = this.lockfile.getLocked(pattern);
    if (shrunk && shrunk.permissions) {
      for (const _ref of (0, (_misc || _load_misc()).entries)(shrunk.permissions)) {
        var _ref2 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref, 2);

        const key = _ref2[0];
        const perm = _ref2[1];

        this.setPermission(key, perm);
      }
    }
  }

  addOptional(optional) {
    if (this.optional == null) {
      // optional is uninitialised
      this.optional = optional;
    } else if (!optional) {
      // otherwise, ignore all subsequent optional assignments and only accept ones making
      // this not optional
      this.optional = false;
    }
  }

  calculateVisibility() {
    let nowIgnore = false;
    const stack = this.visibility;

    // if we don't use this module then mark it as ignored
    if (stack[USED] === 0) {
      nowIgnore = true;
    }

    // if we have removed as many ancestors as it's used then it's out of the tree
    if (stack[REMOVED_ANCESTOR] >= stack[USED]) {
      nowIgnore = true;
    }

    this.ignore = nowIgnore;
  }

  addVisibility(action) {
    let ancestry = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : new (_set || _load_set()).default();

    this.visibility[action]++;
    this.calculateVisibility();

    if (ancestry.has(this)) {
      return;
    }
    ancestry.add(this);

    // go through and update all transitive dependencies to be ignored
    for (const pattern of this.dependencies) {
      const pkg = this.resolver.getResolvedPattern(pattern);
      if (!pkg) {
        continue;
      }

      const ref = pkg._reference;
      invariant(ref, 'expected package reference');
      ref.addVisibility(action, ancestry);
    }
  }
}
exports.default = PackageReference;