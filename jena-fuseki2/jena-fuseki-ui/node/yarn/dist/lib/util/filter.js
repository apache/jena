'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.sortFilter = sortFilter;
exports.matchesFilter = matchesFilter;
exports.ignoreLinesToRegex = ignoreLinesToRegex;

var _misc;

function _load_misc() {
  return _misc = require('./misc.js');
}

const minimatch = require('minimatch');
const path = require('path');

const WHITESPACE_RE = /^\s+$/;

function sortFilter(files, filters) {
  let keepFiles = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : new Set();
  let possibleKeepFiles = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : new Set();
  let ignoreFiles = arguments.length > 4 && arguments[4] !== undefined ? arguments[4] : new Set();

  for (const file of files) {
    let keep = false;

    // always keep a file if a ! pattern matches it
    for (const filter of filters) {
      if (filter.isNegation && matchesFilter(filter, file.basename, file.relative)) {
        keep = true;
        break;
      }
    }

    //
    if (keep) {
      keepFiles.add(file.relative);
      continue;
    }

    // otherwise don't keep it if a pattern matches it
    keep = true;
    for (const filter of filters) {
      if (!filter.isNegation && matchesFilter(filter, file.basename, file.relative)) {
        keep = false;
        break;
      }
    }

    if (keep) {
      possibleKeepFiles.add(file.relative);
    } else {
      ignoreFiles.add(file.relative);
    }
  }

  // exclude file
  for (const file of possibleKeepFiles) {
    const parts = path.dirname(file).split(path.sep);

    while (parts.length) {
      const folder = parts.join(path.sep);
      if (ignoreFiles.has(folder)) {
        ignoreFiles.add(file);
        break;
      }
      parts.pop();
    }
  }

  //
  for (const file of possibleKeepFiles) {
    if (!ignoreFiles.has(file)) {
      keepFiles.add(file);
    }
  }

  //
  for (const file of keepFiles) {
    const parts = path.dirname(file).split(path.sep);

    while (parts.length) {
      // deregister this folder from being ignored, any files inside
      // will still be marked as ignored
      ignoreFiles.delete(parts.join(path.sep));
      parts.pop();
    }
  }

  return { keepFiles, ignoreFiles };
}

function matchesFilter(filter, basename, loc) {
  if (filter.base && filter.base !== '.') {
    loc = path.relative(filter.base, loc);
  }
  return filter.regex.test(loc) || filter.regex.test(`/${ loc }`) || filter.regex.test(basename);
}

function ignoreLinesToRegex(lines) {
  let base = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : '.';

  return lines
  // create regex
  .map(line => {
    // remove empty lines, comments, etc
    if (line === '' || line === '!' || line[0] === '#' || WHITESPACE_RE.test(line)) {
      return null;
    }

    let pattern = line;
    let isNegation = false;

    // hide the fact that it's a negation from minimatch since we'll handle this specifally
    // ourselves
    if (pattern[0] === '!') {
      isNegation = true;
      pattern = pattern.slice(1);
    }

    // remove trailing slash
    pattern = (0, (_misc || _load_misc()).removeSuffix)(pattern, '/');

    const regex = minimatch.makeRe(pattern, { nocase: true });

    if (regex) {
      return {
        base,
        isNegation,
        regex
      };
    } else {
      return null;
    }
  }).filter(Boolean);
}