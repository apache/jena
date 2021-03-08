"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _slicedToArray2;

function _load_slicedToArray() {
  return _slicedToArray2 = _interopRequireDefault(require("babel-runtime/helpers/slicedToArray"));
}

exports.default = parsePackageName;

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const PKG_INPUT = /(^\S[^\s@]+)(?:@(\S+))?$/;

function parsePackageName(input) {
  var _PKG_INPUT$exec = PKG_INPUT.exec(input);

  var _PKG_INPUT$exec2 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_PKG_INPUT$exec, 3);

  const name = _PKG_INPUT$exec2[1];
  const version = _PKG_INPUT$exec2[2];

  return { name: name, version: version };
}