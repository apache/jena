'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _promise;

function _load_promise() {
  return _promise = _interopRequireDefault(require('babel-runtime/core-js/promise'));
}

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

var _keys;

function _load_keys() {
  return _keys = _interopRequireDefault(require('babel-runtime/core-js/object/keys'));
}

exports.default = function (rootCommandName, subCommands) {
  let run = (() => {
    var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
      const subName = camelCase(args.shift() || '');
      const isValidCommand = subName && subCommandNames.indexOf(subName) >= 0;
      if (isValidCommand) {
        const command = subCommands[subName];
        const res = yield command(config, reporter, flags, args);
        if (res !== false) {
          return (_promise || _load_promise()).default.resolve();
        }
      }

      if (usage && usage.length) {
        reporter.error(`${ reporter.lang('usage') }:`);
        for (const msg of usage) {
          reporter.error(`yarn ${ rootCommandName } ${ msg }`);
        }
      }
      return (_promise || _load_promise()).default.reject(new (_errors || _load_errors()).MessageError(reporter.lang('invalidCommand', subCommandNames.join(', '))));
    });

    return function run(_x2, _x3, _x4, _x5) {
      return _ref.apply(this, arguments);
    };
  })();

  let usage = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : [];

  const subCommandNames = (0, (_keys || _load_keys()).default)(subCommands);

  function setFlags(commander) {
    commander.usage(`${ rootCommandName } [${ subCommandNames.join('|') }] [flags]`);
  }

  const examples = usage.map(cmd => {
    return `${ rootCommandName } ${ cmd }`;
  });

  return { run: run, setFlags: setFlags, examples: examples };
};

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const camelCase = require('camelcase');