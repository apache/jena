'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.run = undefined;

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let run = exports.run = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
    if (args.length) {
      for (const name of args) {
        const linkLoc = path.join(config.linkFolder, name);
        if (yield (_fs || _load_fs()).exists(linkLoc)) {
          yield (_fs || _load_fs()).unlink(path.join((yield (0, (_link || _load_link()).getRegistryFolder)(config, name)), name));
          reporter.success(reporter.lang('linkUnregistered', name));
        } else {
          throw new (_errors || _load_errors()).MessageError(reporter.lang('linkMissing', name));
        }
      }
    } else {
      // remove from registry
      const manifest = yield config.readRootManifest();
      const name = manifest.name;
      if (!name) {
        throw new (_errors || _load_errors()).MessageError(reporter.lang('unknownPackageName'));
      }

      const linkLoc = path.join(config.linkFolder, name);
      if (yield (_fs || _load_fs()).exists(linkLoc)) {
        yield (_fs || _load_fs()).unlink(linkLoc);
        reporter.success(reporter.lang('linkUnregistered', name));
      } else {
        throw new (_errors || _load_errors()).MessageError(reporter.lang('linkMissing', name));
      }
    }
  });

  return function run(_x, _x2, _x3, _x4) {
    return _ref.apply(this, arguments);
  };
})();

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('../../util/fs.js'));
}

var _link;

function _load_link() {
  return _link = require('./link.js');
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const path = require('path');