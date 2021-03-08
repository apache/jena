'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
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
          return Promise.resolve();
        }
      }

      if (usage && usage.length) {
        reporter.error(`${ reporter.lang('usage') }:`);
        for (const msg of usage) {
          reporter.error(`yarn ${ rootCommandName } ${ msg }`);
        }
      }
      return Promise.reject(new (_errors || _load_errors()).MessageError(reporter.lang('invalidCommand', subCommandNames.join(', '))));
    });

    return function run(_x2, _x3, _x4, _x5) {
      return _ref.apply(this, arguments);
    };
  })();

  let usage = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : [];

  const subCommandNames = Object.keys(subCommands);

  function setFlags(commander) {
    commander.usage(`${ rootCommandName } [${ subCommandNames.join('|') }] [flags]`);
  }

  const examples = usage.map(cmd => {
    return `${ rootCommandName } ${ cmd }`;
  });

  return { run, setFlags, examples };
};

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const camelCase = require('camelcase');