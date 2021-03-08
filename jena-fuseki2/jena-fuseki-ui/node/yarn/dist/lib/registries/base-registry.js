'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

var _extends2;

function _load_extends() {
  return _extends2 = _interopRequireDefault(require('babel-runtime/helpers/extends'));
}

var _misc;

function _load_misc() {
  return _misc = require('../util/misc.js');
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const objectPath = require('object-path');
const path = require('path');

class BaseRegistry {
  constructor(cwd, registries, requestManager) {
    this.requestManager = requestManager;
    this.registries = registries;
    this.config = {};
    this.folder = '';
    this.token = '';
    this.loc = '';
    this.cwd = cwd;
  }

  // the filename to use for package metadata


  //


  //


  //


  //


  //


  // absolute folder name to insert modules


  // relative folder name to put these modules


  setToken(token) {
    this.token = token;
  }

  getOption(key) {
    return this.config[key];
  }

  loadConfig() {
    return Promise.resolve();
  }

  checkOutdated(config, name, range) {
    return Promise.reject(new Error('unimplemented'));
  }

  saveHomeConfig(config) {
    return Promise.reject(new Error('unimplemented'));
  }

  request(pathname) {
    let opts = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};

    return this.requestManager.request((0, (_extends2 || _load_extends()).default)({
      url: pathname
    }, opts));
  }

  init() {
    var _this = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      _this.mergeEnv('yarn_');
      yield _this.loadConfig();
      _this.loc = path.join(_this.cwd, _this.folder);
    })();
  }

  mergeEnv(prefix) {
    // try environment variables
    for (let key in process.env) {
      key = key.toLowerCase();

      // only accept keys prefixed with the prefix
      if (key.indexOf(prefix) < 0) {
        continue;
      }

      const val = process.env[key];

      // remove bower prefix
      key = (0, (_misc || _load_misc()).removePrefix)(key, prefix);

      // replace dunders with dots
      key = key.replace(/__/g, '.');

      // replace underscores with dashes
      key = key.replace(/_/g, '-');

      // set it via a path
      objectPath.set(this.config, key, val);
    }
  }
}
exports.default = BaseRegistry;