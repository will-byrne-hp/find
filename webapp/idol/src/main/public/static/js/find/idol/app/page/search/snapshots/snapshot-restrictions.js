/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'find/app/page/search/filters/parametric/numeric-range-rounder',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'i18n!find/idol/nls/snapshots',
    'parametric-refinement/prettify-field-name',
    'underscore',
    'moment'
], function(rounder, i18n, indexesI18n, snapshotsI18n, prettifyFieldName, _, moment) {

    var DATE_FORMAT = 'YYYY/MM/DD HH:mm';

    function dateRestriction(title, date) {
        return date ? {title: title, content: date.format(DATE_FORMAT)} : null;
    }

    function formatEpoch(x) {
        return moment(x).format(DATE_FORMAT);
    }

    function relatedConcepts(concepts) {
        return concepts.length ? {
            title: snapshotsI18n['restrictions.relatedConcepts'],
            content: concepts.join(', ')
        } : null;
    }

    /**
     * Target attributes and an attributes processor for the "Query Restrictions" {@link DataPanelView}.
     */
    return {
        targetAttributes: [
            'queryText',
            'relatedConcepts',
            'indexes',
            'parametricValues',
            'minDate',
            'maxDate',
            'parametricRanges'
        ],

        /**
         * @type {DataPanelAttributesProcessor}
         */
        processAttributes: function(attributes) {
            var indexesContent = _.pluck(attributes.indexes, 'name').join(', ');

            var parametricRestrictions = _.map(_.groupBy(attributes.parametricValues, 'field'), function(items, field) {
                return {
                    title: prettifyFieldName(field),
                    content: _.pluck(items, 'value').join(', ')
                };
            });

            var numericRestrictions = _.map(attributes.parametricRanges, function(range) {
                var formatFunction = range.type === 'Date' ? formatEpoch : rounder().round;

                return {
                    title: prettifyFieldName(range.field),
                    content: formatFunction(range.min, range.min, range.max) + ' \u2013 ' + formatFunction(range.max, range.min, range.max)
                };
            });

            return [
                relatedConcepts(attributes.relatedConcepts),
                {
                    title: indexesI18n['search.indexes'],
                    content: indexesContent
                },
                dateRestriction(snapshotsI18n['restrictions.minDate'], attributes.minDate),
                dateRestriction(snapshotsI18n['restrictions.maxDate'], attributes.maxDate)
            ].concat(
                parametricRestrictions,
                numericRestrictions);
        }
    };
});
