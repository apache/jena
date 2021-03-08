'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.Parser = undefined;
exports.tokenise = tokenise;

exports.default = function (str) {
  str = stripBOM(str);
  const parser = new Parser(str);
  parser.next();
  return parser.parse();
};

var _constants;

function _load_constants() {
  return _constants = require('../constants.js');
}

var _errors;

function _load_errors() {
  return _errors = require('../errors.js');
}

var _map;

function _load_map() {
  return _map = _interopRequireDefault(require('../util/map.js'));
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const invariant = require('invariant');
/* eslint quotes: 0 */

const stripBOM = require('strip-bom');

const VERSION_REGEX = /^yarn lockfile v(\d+)$/;

const TOKEN_TYPES = {
  boolean: 'BOOLEAN',
  string: 'STRING',
  identifier: 'IDENTIFIER',
  eof: 'EOF',
  colon: 'COLON',
  newline: 'NEWLINE',
  comment: 'COMMENT',
  indent: 'INDENT',
  invalid: 'INVALID',
  number: 'NUMBER',
  comma: 'COMMA'
};

const VALID_PROP_VALUE_TOKENS = [TOKEN_TYPES.boolean, TOKEN_TYPES.string, TOKEN_TYPES.number];

function isValidPropValueToken(token) {
  return VALID_PROP_VALUE_TOKENS.indexOf(token.type) >= 0;
}

function* tokenise(input) {
  let lastNewline = false;
  let line = 1;
  let col = 0;

  function buildToken(type, value) {
    return { line: line, col: col, type: type, value: value };
  }

  while (input.length) {
    let chop = 0;

    if (input[0] === '\n') {
      chop++;
      line++;
      col = 0;
      yield buildToken(TOKEN_TYPES.newline);
    } else if (input[0] === '#') {
      chop++;

      let val = '';
      while (input[chop] !== '\n') {
        val += input[chop];
        chop++;
      }
      yield buildToken(TOKEN_TYPES.comment, val);
    } else if (input[0] === ' ') {
      if (lastNewline) {
        let indent = '';
        for (let i = 0; input[i] === ' '; i++) {
          indent += input[i];
        }

        if (indent.length % 2) {
          throw new TypeError('Invalid number of spaces');
        } else {
          chop = indent.length;
          yield buildToken(TOKEN_TYPES.indent, indent.length / 2);
        }
      } else {
        chop++;
      }
    } else if (input[0] === '"') {
      let val = '';
      for (let i = 0;; i++) {
        const char = input[i];
        val += char;
        if (i > 0 && char === '"' && input[i - 1] !== "\\" && input[i - 2] !== "\\") {
          break;
        }
      }
      chop = val.length;

      try {
        yield buildToken(TOKEN_TYPES.string, JSON.parse(val));
      } catch (err) {
        if (err instanceof SyntaxError) {
          yield buildToken(TOKEN_TYPES.invalid);
        } else {
          throw err;
        }
      }
    } else if (/^[0-9]/.test(input)) {
      let val = '';
      for (let i = 0; /^[0-9]$/.test(input[i]); i++) {
        val += input[i];
      }
      chop = val.length;

      yield buildToken(TOKEN_TYPES.number, +val);
    } else if (/^true/.test(input)) {
      yield buildToken(TOKEN_TYPES.boolean, true);
      chop = 4;
    } else if (/^false/.test(input)) {
      yield buildToken(TOKEN_TYPES.boolean, false);
      chop = 5;
    } else if (input[0] === ':') {
      yield buildToken(TOKEN_TYPES.colon);
      chop++;
    } else if (input[0] === ',') {
      yield buildToken(TOKEN_TYPES.comma);
      chop++;
    } else if (/^[a-zA-Z]/g.test(input)) {
      let name = "";
      for (let i = 0; i < input.length; i++) {
        const char = input[i];
        if (char === ':' || char === ' ' || char === '\n' || char === ',') {
          break;
        } else {
          name += char;
        }
      }
      chop = name.length;

      yield buildToken(TOKEN_TYPES.string, name);
    } else {
      yield buildToken(TOKEN_TYPES.invalid);
    }

    if (!chop) {
      // will trigger infinite recursion
      yield buildToken(TOKEN_TYPES.invalid);
    }

    col += chop;
    lastNewline = input[0] === '\n';
    input = input.slice(chop);
  }

  yield buildToken(TOKEN_TYPES.eof);
}

class Parser {
  constructor(input) {
    this.comments = [];
    this.tokens = tokenise(input);
  }

  onComment(token) {
    const value = token.value;
    invariant(typeof value === 'string', 'expected token value to be a string');

    const comment = value.trim();

    const versionMatch = comment.match(VERSION_REGEX);
    if (versionMatch) {
      const version = +versionMatch[1];
      if (version > (_constants || _load_constants()).LOCKFILE_VERSION) {
        throw new (_errors || _load_errors()).MessageError(`Can't install from a lockfile of version ${ version } as you're on an old yarn version that only supports ` + `versions up to ${ (_constants || _load_constants()).LOCKFILE_VERSION }. Run \`$ yarn self-update\` to upgrade to the latest version.`);
      }
    }

    this.comments.push(comment);
  }

  next() {
    const item = this.tokens.next();
    invariant(item, 'expected a token');

    const done = item.done;
    const value = item.value;

    if (done || !value) {
      throw new Error('No more tokens');
    } else if (value.type === TOKEN_TYPES.comment) {
      this.onComment(value);
      return this.next();
    } else {
      return this.token = value;
    }
  }

  unexpected() {
    let msg = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 'Unexpected token';

    throw new SyntaxError(`${ msg } ${ this.token.line }:${ this.token.col }`);
  }

  expect(tokType) {
    if (this.token.type === tokType) {
      this.next();
    } else {
      this.unexpected();
    }
  }

  eat(tokType) {
    if (this.token.type === tokType) {
      this.next();
      return true;
    } else {
      return false;
    }
  }

  parse() {
    let indent = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 0;

    const obj = (0, (_map || _load_map()).default)();

    while (true) {
      const propToken = this.token;

      if (propToken.type === TOKEN_TYPES.newline) {
        const nextToken = this.next();
        if (!indent) {
          // if we have 0 indentation then the next token doesn't matter
          continue;
        }

        if (nextToken.type !== TOKEN_TYPES.indent) {
          // if we have no indentation after a newline then we've gone down a level
          break;
        }

        if (nextToken.value === indent) {
          // all is good, the indent is on our level
          this.next();
        } else {
          // the indentation is less than our level
          break;
        }
      } else if (propToken.type === TOKEN_TYPES.indent) {
        if (propToken.value === indent) {
          this.next();
        } else {
          break;
        }
      } else if (propToken.type === TOKEN_TYPES.eof) {
        break;
      } else if (propToken.type === TOKEN_TYPES.string) {
        // property key
        const key = propToken.value;
        invariant(key, 'Expected a key');

        const keys = [key];
        this.next();

        // support multiple keys
        while (this.token.type === TOKEN_TYPES.comma) {
          this.next(); // skip comma

          const keyToken = this.token;
          if (keyToken.type !== TOKEN_TYPES.string) {
            this.unexpected('Expected string');
          }

          const key = keyToken.value;
          invariant(key, 'Expected a key');
          keys.push(key);
          this.next();
        }

        const valToken = this.token;

        if (valToken.type === TOKEN_TYPES.colon) {
          // object
          this.next();

          // parse object
          const val = this.parse(indent + 1);

          for (const key of keys) {
            obj[key] = val;
          }

          if (indent && this.token.type !== TOKEN_TYPES.indent) {
            break;
          }
        } else if (isValidPropValueToken(valToken)) {
          // plain value
          for (const key of keys) {
            obj[key] = valToken.value;
          }

          this.next();
        } else {
          this.unexpected('Invalid value type');
        }
      } else {
        this.unexpected('Unknown token');
      }
    }

    return obj;
  }
}

exports.Parser = Parser;