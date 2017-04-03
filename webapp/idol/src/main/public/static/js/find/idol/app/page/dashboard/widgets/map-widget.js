/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    './saved-search-widget',
    'find/app/page/search/results/map-results-view-strategy',
    'find/app/page/search/results/map-view',
    'find/app/model/documents-collection'
], function (_, $, SavedSearchWidget, mapResultsViewStrategy, MapView, DocumentsCollection) {
    'use strict';

    return SavedSearchWidget.extend({
        viewType: 'map',

        initialize: function (options) {
            SavedSearchWidget.prototype.initialize.apply(this, arguments);

            this.resultSets = [{
                collection: new DocumentsCollection(),
                markers: {}
            }];

            this.mapResultsViewStrategy = mapResultsViewStrategy({
                allowIncrement: false,
                resultsStep: this.widgetSettings.maxResults || 1000,
                clusterMarkers: this.widgetSettings.clusterMarkers || false,
                locationFields: this.widgetSettings.locationFieldPairs,
                mapViewOptions: {
                    addControl: false,
                    centerCoordinates: this.widgetSettings.centerCoordinates,
                    initialZoom: this.widgetSettings.zoomLevel,
                    removeZoomControl: true,
                    disableInteraction: true
                },
                resultSets: this.resultSets,
                toggleLoading: $.noop
            });
        },

        render: function () {
            SavedSearchWidget.prototype.render.apply(this);
            this.mapResultsViewStrategy.mapView.setElement(this.$content).render();
        },

        getData: function () {
            const resultSet = this.resultSets[0];
            resultSet.model = this.queryModel;
            const maybePromise = this.mapResultsViewStrategy.reloadMarkers();
            if (!maybePromise) {
                return $.when();
            }

            return maybePromise.done(function () {
                resultSet.collection.each(function (model) {
                    this.mapResultsViewStrategy.getMarkersFromDocumentModel(model, resultSet.markers);
                    const locations = model.get('locations');
                }.bind(this));

                this.mapResultsViewStrategy.addMarkersToMap(resultSet.markers, resultSet.clusterLayer, false);
            }.bind(this));
        },

        exportData: function () {
            return this.mapResultsViewStrategy.mapView.exportData().then(function (data) {
                return {
                    data: data,
                    type: 'map'
                }
            });
        }
    });
});
