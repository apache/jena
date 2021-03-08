'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.setFlags = exports.run = undefined;

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

var _buildSubCommands2;

function _load_buildSubCommands() {
  return _buildSubCommands2 = _interopRequireDefault(require('./_build-sub-commands.js'));
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('../../util/fs.js'));
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const path = require('path');

var _buildSubCommands = (0, (_buildSubCommands2 || _load_buildSubCommands()).default)('cache', {
  ls: (() => {
    var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
      const files = yield (_fs || _load_fs()).readdir(config.cacheFolder);
      const body = [];

      for (const file of files) {
        if (file[0] === '.') {
          continue;
        }

        const loc = path.join(config.cacheFolder, file);

        var _ref2 = yield config.readPackageMetadata(loc);

        const registry = _ref2.registry;
        const manifest = _ref2.package;
        const remote = _ref2.remote;


        body.push([manifest.name, manifest.version, registry, remote && remote.resolved || '']);
      }

      reporter.table(['Name', 'Version', 'Registry', 'Resolved'], body);
    });

    function ls(_x, _x2, _x3, _x4) {
      return _ref.apply(this, arguments);
    }

    return ls;
  })(),
  dir: function dir(config) {
    console.log(config.cacheFolder);
  },
  clean: (() => {
    var _ref3 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
      const cacheFolder = config.cacheFolder;
      if (cacheFolder) {
        yield (_fs || _load_fs()).unlink(cacheFolder);
        yield (_fs || _load_fs()).mkdirp(cacheFolder);
        reporter.success(reporter.lang('clearedCache'));
      }
    });

    function clean(_x5, _x6, _x7, _x8) {
      return _ref3.apply(this, arguments);
    }

    return clean;
  })()
});

const run = _buildSubCommands.run;
const setFlags = _buildSubCommands.setFlags;
exports.run = run;
exports.setFlags = setFlags;