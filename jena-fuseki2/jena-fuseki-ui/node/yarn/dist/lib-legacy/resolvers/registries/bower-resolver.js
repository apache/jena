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

var _registryResolver;

function _load_registryResolver() {
  return _registryResolver = _interopRequireDefault(require('./registry-resolver.js'));
}

var _gitResolver;

function _load_gitResolver() {
  return _gitResolver = _interopRequireDefault(require('../exotics/git-resolver.js'));
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

class BowerResolver extends (_registryResolver || _load_registryResolver()).default {

  resolveRequest() {
    return this.config.requestManager.request({
      url: `${ this.registryConfig.registry }/packages/${ this.name }`,
      json: true,
      queue: this.resolver.fetchingQueue
    });
  }

  resolve() {
    var _this = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const body = yield _this.resolveRequest();

      if (body != null) {
        return _this.fork((_gitResolver || _load_gitResolver()).default, false, `${ body.url }#${ _this.range }`);
      } else {
        throw new (_errors || _load_errors()).MessageError(_this.reporter.lang('packageNotFoundRegistry', _this.name, 'bower'));
      }
    })();
  }
}
exports.default = BowerResolver;
BowerResolver.registry = 'bower';