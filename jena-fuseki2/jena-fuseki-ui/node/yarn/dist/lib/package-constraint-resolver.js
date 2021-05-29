'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
const semver = require('semver');

// This isn't really a "proper" constraint resolver. We just return the highest semver
// version in the versions passed that satisfies the input range. This vastly reduces
// the complexity and is very efficient for package resolution.

class PackageConstraintResolver {
  constructor(config, reporter) {
    this.reporter = reporter;
    this.config = config;
  }

  reduce(versions, range) {
    return Promise.resolve(semver.maxSatisfying(versions, range, this.config.looseSemver));
  }
}
exports.default = PackageConstraintResolver;