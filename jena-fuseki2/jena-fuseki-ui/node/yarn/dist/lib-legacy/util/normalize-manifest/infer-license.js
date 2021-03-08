'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = inferLicense;

var _licenses;

function _load_licenses() {
  return _licenses = _interopRequireDefault(require('./licenses.js'));
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function clean(str) {
  return str.replace(/[^A-Za-z\s]/g, ' ').replace(/[\s]+/g, ' ').trim().toLowerCase();
}

const REGEXES = {
  Unlicense: [/http:\/\/unlicense.org\//],
  WTFPL: [/DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE/, /WTFPL\b/],
  ISC: [/The ISC License/, /ISC\b/],
  Apache: [/Apache License\b/],
  MIT: [/MIT\b/],
  BSD: [/BSD\b/]
};

function inferLicense(license) {
  // check if we have any explicit licenses
  const cleanLicense = clean(license);
  for (const licenseName in (_licenses || _load_licenses()).default) {
    const testLicense = (_licenses || _load_licenses()).default[licenseName];
    if (cleanLicense.search(testLicense) >= 0) {
      return licenseName;
    }
  }

  // infer based on some keywords
  for (const licenseName in REGEXES) {
    for (const regex of REGEXES[licenseName]) {
      if (license.search(regex) >= 0) {
        return `${ licenseName }*`;
      }
    }
  }

  return null;
}