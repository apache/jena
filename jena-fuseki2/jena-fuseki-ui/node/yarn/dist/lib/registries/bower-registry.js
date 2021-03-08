'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

var _baseRegistry;

function _load_baseRegistry() {
  return _baseRegistry = _interopRequireDefault(require('./base-registry.js'));
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('../util/fs.js'));
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const defaults = require('defaults');
const userHome = require('user-home');
const path = require('path');

class BowerRegistry extends (_baseRegistry || _load_baseRegistry()).default {

  loadConfig() {
    var _this = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      // docs: http://bower.io/docs/config/
      // spec: https://github.com/bower/spec/blob/master/config.md

      _this.mergeEnv('bower_');

      // merge in configs
      const possibles = [path.join('/', '.bowerrc'), path.join(userHome, '.bowerrc'),
      // TODO all .bowerrc files upwards the directory tree
      path.join(_this.cwd, '.bowerrc')];
      for (const loc of possibles) {
        if (yield (_fs || _load_fs()).exists(loc)) {
          Object.assign(_this.config, (yield (_fs || _load_fs()).readJson(loc)));
        }
      }

      defaults(_this.config, {
        registry: 'https://bower.herokuapp.com',
        directory: 'bower_components'
      });

      // TODO: warn on properties we do not support

      _this.folder = _this.config.directory;
    })();
  }
}
exports.default = BowerRegistry;
BowerRegistry.filename = 'bower.json';