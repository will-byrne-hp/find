/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'jquery',
    'find/idol/app/page/search/filters/indexes/idol-indexes-view',
    'databases-view/js/idol-databases-collection',
    'jasmine-jquery'
], function(Backbone, _, $, IndexesView, DatabasesCollection) {

    describe('Indexes View', function() {
        var INDEXES = _.map(['a','b','c'], function(name) {
            return {name: name, id: name};
        });

        beforeEach(function() {
            this.indexesCollection = new DatabasesCollection();
            this.selectedIndexesCollection = new DatabasesCollection();

            this.queryModel = new Backbone.Model();

            this.indexesView = new IndexesView({
                queryModel: this.queryModel,
                indexesCollection: this.indexesCollection,
                selectedDatabasesCollection: this.selectedIndexesCollection
            });

            this.idElement = function(indexAttributes) {
                return this.indexesView.$('li[data-id="' + indexAttributes.id + '"]');
            };

            this.indexesView.render();

            this.indexesCollection.reset(INDEXES);
            this.queryModel.set('indexes', _.pluck(INDEXES, 'id'));
        });

        describe('after initialization', function() {
            it('should display indexes in the IndexesCollection', function() {
                var elements = this.indexesView.$el.find('[data-id]');

                var dataIds = _.map(elements, function (element) {
                    return $(element).attr('data-id');
                });

                expect(dataIds).toContain(INDEXES[0].id);
                expect(dataIds).toContain(INDEXES[1].id);
                expect(dataIds).toContain(INDEXES[2].id);
            });

            it('sets all indexes on the selected indexes collection', function() {
                expect(this.selectedIndexesCollection.length).toBe(3);
            });

            it('does not select indexes in the UI', function() {
                expect(this.indexesView.$('i.hp-icon hp-check')).toHaveLength(0);
            });

            describe('clicking an index once', function() {
                beforeEach(function() {
                    this.idElement(INDEXES[0]).click();
                });

                it('updates the selected indexes collection', function() {
                    expect(this.selectedIndexesCollection).toHaveLength(1);
                    expect(this.selectedIndexesCollection.first().get('name')).toEqual(INDEXES[0].name);
                });

                it('should check the clicked index', function() {
                    var checkedCheckbox = this.idElement(INDEXES[0]).find('i');
                    var uncheckedCheckboxOne = this.idElement(INDEXES[1]).find('i');
                    var uncheckedCheckboxTwo = this.idElement(INDEXES[2]).find('i');

                    expect(checkedCheckbox).toHaveClass('hp-check');
                    expect(uncheckedCheckboxOne).not.toHaveClass('hp-check');
                    expect(uncheckedCheckboxTwo).not.toHaveClass('hp-check');
                });
            });

            describe('clicking an index twice', function() {
                beforeEach(function() {
                    this.idElement(INDEXES[0]).click().click();
                });

                it('updates the selected indexes collection with all of the indexes', function() {
                    expect(this.selectedIndexesCollection.length).toBe(3);

                    _.each(INDEXES, function(index) {
                        expect(this.selectedIndexesCollection.findWhere({name: index.name})).toBeDefined();
                    }, this);
                });

                it('should leave the indexes selected in the ui unchanged', function() {
                    expect(this.indexesView.$('i.hp-icon hp-check')).toHaveLength(0);
                });
            });

        });

        describe('when selected indexes collection', function() {
            describe('is set to contain all but the first index', function() {
                beforeEach(function() {
                    this.selectedIndexesCollection.set(_.tail(INDEXES));
                });

                it('should select the right indexes', function() {
                    var uncheckedCheckbox = this.idElement(INDEXES[0]).find('i');
                    var checkedCheckboxOne = this.idElement(INDEXES[1]).find('i');
                    var checkedCheckboxTwo = this.idElement(INDEXES[2]).find('i');

                    expect(uncheckedCheckbox).not.toHaveClass('hp-check');
                    expect(checkedCheckboxOne).toHaveClass('hp-check');
                    expect(checkedCheckboxTwo).toHaveClass('hp-check');
                });
            });

            describe('is set to contain only first index', function() {
                beforeEach(function() {
                    this.selectedIndexesCollection.set(_.head(INDEXES));
                });

                it('should select only the first index', function() {
                    var checkedCheckbox = this.idElement(INDEXES[0]).find('i');
                    var uncheckedCheckboxOne = this.idElement(INDEXES[1]).find('i');
                    var uncheckedCheckboxTwo = this.idElement(INDEXES[2]).find('i');

                    expect(checkedCheckbox).toHaveClass('hp-check');
                    expect(uncheckedCheckboxOne).not.toHaveClass('hp-check');
                    expect(uncheckedCheckboxTwo).not.toHaveClass('hp-check');
                });
            });
        });

    });

});