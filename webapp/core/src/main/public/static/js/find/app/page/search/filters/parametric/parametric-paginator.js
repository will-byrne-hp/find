/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone'
], function (_, Backbone) {
    'use strict';

    /**
     * @typedef {Object} ParametricPaginatorOptions
     * @property {Object} fetchRestrictions Parameters for the search, used to determine counts for matching values
     * @property {Function} fetchFunction Fetch parametric values for some parameters, returning a promise resolved with
     * values and totalValues
     * @property {String} fieldName The parametric field name
     * @property {Backbone.Collection} selectedValues The selected values collection, kept up to date by this object, but
     * changes made in the collection will not be reflected here
     * @property {Array<*>} indexes All indexes which should be queried for parametric values
     * @property {number} [pageSize=20] How many values to fetch in one request
     */
    /**
     * Pages over parametric values for the given field. A new page is loaded when fetchNext is called, unless a request
     * is already in flight or there are no more values to fetch.
     *
     * Values are ordered by count of documents who match the given fetch restrictions. When there are no more values
     * matching the restrictions, zero-count values are fetched.
     *
     * @param {ParametricPaginatorOptions} options
     * @constructor
     */
    const ParametricPaginator = function (options) {
        this.selectedValues = options.selectedValues;
        this.fetchFunction = options.fetchFunction;

        this.fetchOptions = {
            fetchRestrictions: options.fetchRestrictions,
            fieldName: options.fieldName,
            indexes: options.indexes,
            pageSize: options.pageSize || 20
        };

        this.stateModel = new Backbone.Model({
            loading: false,
            error: null,
            empty: false
        });

        this.valuesCollection = new Backbone.Collection();

        this.fetching = false;
        this.error = null;

        this.paginationState = {
            nextRestrictedPage: 1,
            nextPage: 1,
            totalRestrictedValues: null,
            totalValues: null
        };
    };

    _.extend(ParametricPaginator.prototype, {
        /**
         * Fetch the next page of values if there are more values and we are not already fetching.
         *
         * If no more values match the fetch restrictions, zero-count values are loaded. As many requests are made as
         * are required to fetch the page size. If more than the page size are found, they all are added (so page size
         * is a minimum). If no more zero-count values are available in the field, fetching stops.
         */
        fetchNext: function () {
            if (!this.fetching && !this.error) {
                loadPage.call(this, this.fetchOptions.pageSize);
            }
        },

        /**
         * Toggle the selected property of the given value, updating the selected parametric values collection and the
         * values collection.
         * @param value
         */
        toggleSelection: function (value) {
            const model = this.valuesCollection.findWhere({value: value});
            const isSelected = !model.get('selected');
            model.set('selected', isSelected);

            if (isSelected) {
                this.selectedValues.add({field: this.fetchOptions.fieldName, value: value});
            } else {
                this.selectedValues.remove(this.selectedValues.findWhere({field: this.fetchOptions.fieldName, value: value}));
            }
        }
    });

    /*
     * Loads at least the given number of values, one page of size pageSize at a time.
     */
    function loadPage(valuesRequired) {
        const restrictedStart = 1 + this.fetchOptions.pageSize * (this.paginationState.nextRestrictedPage - 1);
        const unrestrictedStart = 1 + this.fetchOptions.pageSize * (this.paginationState.nextPage - 1);

        let nextFetch;

        if (this.paginationState.totalRestrictedValues === null || restrictedStart <= this.paginationState.totalRestrictedValues) {
            // Fetch restricted values because we either have not fetched them before or we know there are more to fetch
            nextFetch = {
                totalKey: 'totalRestrictedValues',
                useCount: true,
                parameters: _.extend({
                    fieldNames: [this.fetchOptions.fieldName],
                    start: restrictedStart,
                    maxValues: this.fetchOptions.pageSize * this.paginationState.nextRestrictedPage
                }, this.fetchOptions.fetchRestrictions)
            };

            this.paginationState = _.defaults({nextRestrictedPage: this.paginationState.nextRestrictedPage + 1}, this.paginationState);
        } else if (this.paginationState.totalValues === null || unrestrictedStart <= this.paginationState.totalValues) {
            // Fetch unrestricted values because we either have not fetched them before or we know there are more to fetch
            nextFetch = {
                totalKey: 'totalValues',
                useCount: false,
                parameters: {
                    databases: this.fetchOptions.indexes,
                    fieldNames: [this.fetchOptions.fieldName],
                    start: unrestrictedStart,
                    maxValues: this.fetchOptions.pageSize * this.paginationState.nextPage
                }
            };

            this.paginationState = _.defaults({nextPage: this.paginationState.nextPage + 1}, this.paginationState);
        } else {
            // We have exhausted restricted and unrestricted values
            nextFetch = null;
        }

        if (nextFetch) {
            this.fetchFunction(nextFetch.parameters)
                .done(function(output) {
                    this.paginationState[nextFetch.totalKey] = output.totalValues;

                    const newValueData = output.values
                        .filter(function(data) {
                            return !this.valuesCollection.some(function(model) {
                                return model.get('value') === data.value;
                            });
                        }.bind(this))
                        .map(function(data) {
                            return {
                                count: nextFetch.useCount ? data.count : 0,
                                value: data.value,
                                selected: Boolean(this.selectedValues.findWhere({
                                    field: this.fetchOptions.fieldName,
                                    value: data.value
                                }))
                            };
                        }.bind(this));

                    this.valuesCollection.add(newValueData);

                    if (newValueData.length >= valuesRequired) {
                        this.fetching = false;
                        this.stateModel.set(determineStateAttributes.call(this));
                    } else {
                        this.fetching = true;
                        loadPage.call(this, valuesRequired - output.values.length);
                    }
                }.bind(this))
                .fail(function (error) {
                    this.error = error;
                    this.fetching = false;
                    this.stateModel.set(determineStateAttributes.call(this));
                }.bind(this));

            this.fetching = true;
        } else {
            this.fetching = false;
        }

        this.stateModel.set(determineStateAttributes.call(this));
    }

    function determineStateAttributes() {
        return {
            empty: !this.error && this.totalValues !== null && !this.fetching && this.valuesCollection.isEmpty(),
            error: this.error,
            loading: this.fetching
        };
    }

    return ParametricPaginator;

});
