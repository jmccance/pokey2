define([
  'underscore'
], function (_) {
  'use strict';

  var commentValidator = /\S/;

  var Estimate = function (attrs) {
    attrs = attrs || {};
    this.hours = attrs.hours;
    this.comment = attrs.comment;
  };

  Estimate.prototype = {
    /**
     * Test whether the comment is valid. A valid comment either has a numerical hours field or a non-blank comment.
     *
     * @returns {boolean} true iff the estimate is valid
     */
    isValid: function () {
      return _.isNumber(this.hours) || (!_.isUndefined(this.comment) && commentValidator.test(this.comment));
    }
  };

  return Estimate;
});
