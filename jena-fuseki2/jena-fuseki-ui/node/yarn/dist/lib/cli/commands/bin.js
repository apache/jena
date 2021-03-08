'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
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
  return Promise.resolve();
}