"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
class MessageError extends Error {
  constructor(msg, code) {
    super(msg);
    this.code = code;
  }

}

exports.MessageError = MessageError;
class SecurityError extends MessageError {}

exports.SecurityError = SecurityError;
class SpawnError extends MessageError {}
exports.SpawnError = SpawnError;