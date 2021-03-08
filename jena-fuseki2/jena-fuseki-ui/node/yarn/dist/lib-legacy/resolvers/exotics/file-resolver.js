'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

var _exoticResolver;

function _load_exoticResolver() {
  return _exoticResolver = _interopRequireDefault(require('./exotic-resolver.js'));
}

var _misc;

function _load_misc() {
  return _misc = _interopRequireWildcard(require('../../util/misc.js'));
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('../../util/fs.js'));
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const invariant = require('invariant');
const path = require('path');

class FileResolver extends (_exoticResolver || _load_exoticResolver()).default {
  constructor(request, fragment) {
    super(request, fragment);
    this.loc = (_misc || _load_misc()).removePrefix(fragment, 'file:');
  }

  resolve() {
    var _this = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      let loc = _this.loc;
      if (!path.isAbsolute(loc)) {
        loc = path.join(_this.config.cwd, loc);
      }
      if (!(yield (_fs || _load_fs()).exists(loc))) {
        throw new (_errors || _load_errors()).MessageError(_this.reporter.lang('doesntExist', loc));
      }

      const manifest = yield _this.config.readManifest(loc, _this.registry);
      const registry = manifest._registry;
      invariant(registry, 'expected registry');

      manifest._remote = {
        type: 'copy',
        registry: registry,
        reference: loc
      };

      manifest._uid = manifest.version;

      return manifest;
    })();
  }
}
exports.default = FileResolver;
FileResolver.protocol = 'file';