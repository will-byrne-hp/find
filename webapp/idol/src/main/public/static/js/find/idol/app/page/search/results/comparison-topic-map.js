/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'backbone',
    'i18n!find/nls/bundle',
    'i18n!find/idol/nls/comparisons',
    'find/app/page/search/results/state-token-strategy',
    'find/app/util/search-data-util',
    'find/app/model/entity-collection',
    'find/app/page/search/results/entity-topic-map-view',
    'find/app/util/results-view-container',
    'find/app/util/results-view-selection',
    'text!find/idol/templates/comparison/topic-map-comparison-view.html',
    'underscore'
], function($, Backbone, i18n, comparisonsI18n, stateTokenStrategy, searchDataUtil, EntityCollection,
            TopicMapView, ResultsViewContainer, ResultsViewSelection, html, _) {
    'use strict';

    return Backbone.View.extend({
        className: 'service-view-container',

        initialize: function(options) {
            this.searchModels = options.searchModels;

            var bothQueryModel = this.createQueryModel(this.model.get('bothText'), this.model.get('inBoth'), [this.searchModels.first, this.searchModels.second]);
            var firstQueryModel = this.createQueryModel(this.model.get('firstText'), this.model.get('onlyInFirst'), [this.searchModels.first]);
            var secondQueryModel = this.createQueryModel(this.model.get('secondText'), this.model.get('onlyInSecond'), [this.searchModels.second]);

            var resultsViews = [
                {
                    Constructor: TopicMapView,
                    id: 'first',
                    uniqueId: _.uniqueId('results-view-item-'),
                    constructorArguments: {
                        clickHandler: _.noop,
                        queryModel: firstQueryModel,
                        type: 'COMPARISON'
                    },
                    selector: {
                        displayName: comparisonsI18n['list.title.first'](this.searchModels.first.get('title')),
                        icon: 'hp-divide-in-right'
                    }
                },
                {
                    Constructor: TopicMapView,
                    id: 'both',
                    uniqueId: _.uniqueId('results-view-item-'),
                    constructorArguments: {
                        clickHandler: _.noop,
                        queryModel: bothQueryModel,
                        type: 'COMPARISON'
                    },
                    selector: {
                        displayName: comparisonsI18n['list.title.both'],
                        icon: 'hp-divide-in-center'
                    }
                },
                {
                    Constructor: TopicMapView,
                    id: 'second',
                    uniqueId: _.uniqueId('results-view-item-'),
                    constructorArguments: {
                        clickHandler: _.noop,
                        queryModel: secondQueryModel,
                        type: 'COMPARISON'
                    },
                    selector: {
                        displayName: comparisonsI18n['list.title.second'](this.searchModels.second.get('title')),
                        icon: 'hp-divide-in-right hp-flip-horizontal'
                    }
                }
            ];

            // Initially, select the left-most tab
            var initialTabId = resultsViews[0].id;

            var resultsViewSelectionModel = new Backbone.Model({
                // ID of the currently selected tab
                selectedTab: initialTabId
            });

            this.resultsViewSelection = new ResultsViewSelection({
                views: resultsViews,
                model: resultsViewSelectionModel
            });

            this.resultsViewContainer = new ResultsViewContainer({
                views: resultsViews,
                model: resultsViewSelectionModel
            });
        },

        render: function() {
            this.$el.html(html);

            this.resultsViewSelection.setElement(this.$('.topic-map-comparison-selection')).render();

            this.resultsViewContainer.setElement(this.$('.topic-map-comparison-container')).render();
        },

        createQueryModel: function(queryText, stateTokens, searchModels) {
            var indexes = _.chain(searchModels)
                .map(function(model) {
                    return searchDataUtil.buildIndexes(model.get('indexes'));
                })
                .flatten()
                .uniq()
                .value();

            return new Backbone.Model(_.extend({
                queryText: queryText,
                indexes: indexes
            }, stateTokens));
        }
    })
});
