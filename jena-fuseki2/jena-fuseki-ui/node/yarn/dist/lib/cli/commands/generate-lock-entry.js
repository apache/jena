'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.examples = exports.run = undefined;

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let run = exports.run = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
    let manifest;
    if (flags.useManifest) {
      manifest = yield (_fs || _load_fs()).readJson(flags.useManifest);
    } else {
      manifest = yield config.readRootManifest();
    }
    if (!manifest.name) {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('noName'));
    }
    if (!manifest.version) {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('noVersion'));
    }

    const entry = {
      name: manifest.name,
      version: manifest.version,
      resolved: flags.resolved,
      registry: flags.registry || manifest._registry,
      optionalDependencies: manifest.optionalDependencies,
      dependencies: manifest.dependencies
    };
    const pattern = flags.pattern || `${ entry.name }@${ entry.version }`;
    reporter.log((0, (_stringify || _load_stringify()).default)({
      [pattern]: (0, (_wrapper || _load_wrapper()).implodeEntry)(pattern, entry)
    }));
  });

  return function run(_x, _x2, _x3, _x4) {
    return _ref.apply(this, arguments);
  };
})();

exports.hasWrapper = hasWrapper;
exports.setFlags = setFlags;

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

var _wrapper;

function _load_wrapper() {
  return _wrapper = require('../../lockfile/wrapper.js');
}

var _stringify;

function _load_stringify() {
  return _stringify = _interopRequireDefault(require('../../lockfile/stringify.js'));
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('../../util/fs.js'));
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function hasWrapper() {
  return false;
}

function setFlags(commander) {
  commander.option('--use-manifest <location>', 'description');
  commander.option('--resolved <resolved>', 'description');
  commander.option('--registry <registry>', 'description');
}

const examples = exports.examples = ['generate-lock-entry', 'generate-lock-entry --use-manifest ./package.json', 'generate-lock-entry --registry bower', 'generate-lock-entry --resolved local-file.tgz#hash'];