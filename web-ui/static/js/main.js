/**
 * main.js - Common utilities for the Civilization Simulator Web UI.
 *
 * Provides:
 *   - Number formatting helpers
 *   - Year display formatting (BCE/CE)
 */

/* global window, document */

'use strict';

var CivSim = (function () {

    // ---------------------------------------------------------------
    // Formatting utilities
    // ---------------------------------------------------------------

    /**
     * Formats a number with locale-appropriate thousands separators.
     * @param {number} n
     * @returns {string}
     */
    function formatNumber(n) {
        if (n == null) { return '---'; }
        return Number(n).toLocaleString('en-US', { maximumFractionDigits: 0 });
    }

    /**
     * Formats a number with a fixed number of decimal places.
     * @param {number} n
     * @param {number} decimals
     * @returns {string}
     */
    function formatDecimal(n, decimals) {
        if (n == null) { return '---'; }
        return Number(n).toLocaleString('en-US', {
            minimumFractionDigits: decimals,
            maximumFractionDigits: decimals
        });
    }

    /**
     * Formats a year as BCE/CE.
     * @param {number} year  negative = BCE, positive = CE
     * @returns {string}
     */
    function formatYear(year) {
        if (year < 0) {
            return Math.abs(year) + ' BCE';
        }
        return year + ' CE';
    }

    /**
     * Formats a percentage from a 0-1 fraction.
     * @param {number} fraction
     * @returns {string}
     */
    function formatPercent(fraction) {
        if (fraction == null) { return '---'; }
        return (fraction * 100).toFixed(1) + '%';
    }

    // ---------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------

    return {
        formatNumber: formatNumber,
        formatDecimal: formatDecimal,
        formatYear: formatYear,
        formatPercent: formatPercent
    };

})();
