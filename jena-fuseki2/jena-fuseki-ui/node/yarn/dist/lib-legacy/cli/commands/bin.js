'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _promise;

function _load_promise() {
  return _promise = _interopRequireDefault(require('babel-runtime/core-js/promise'));
}

exports.hasWrapper = hasWrapper;
exports.run = run;

var _yarnResolver;

function _load_yarnResolver() {
  return _yarnResolver = _interopRequireDefault(require('../../resolvers/registries/yarn-resolver.js'));
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const path = require('path');

function hasWrapper() {}

function run(config, reporter, flags, args) {
  const binFolder = path.join(config.cwd, config.registries[(_yarnResolver || _load_yarnResolver()).default.registry].folder, '.bin');
  console.log(binFolder);
  return (_promise || _load_promise()).default.resolve();
}