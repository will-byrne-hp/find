/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/configuration',
    'find/app/page/search/results/entity-topic-map-view',
    'find/app/util/topic-map-view',
    'mock/model/entity-collection'
], function(Backbone, configuration, EntityTopicMapView, TopicMapView, EntityCollection) {
    'use strict';

    describe('EntityTopicMapView', function() {
        beforeEach(function() {
            configuration.and.returnValue({
                errorCallSupportString: 'Custom call support message',
                topicMapMaxResults: 1000
            });

            this.clickHandler = jasmine.createSpy('clickHandler');
            this.queryModel = new Backbone.Model();
            this.queryModel.getIsoDate = jasmine.createSpy('getIsoDate');

            this.createView = function() {
                this.view = new EntityTopicMapView({
                    clickHandler: this.clickHandler,
                    queryModel: this.queryModel,
                    type: 'QUERY'
                });

                // The view only updates when visible
                this.view.$el.appendTo(document.body);

                this.view.render();
                this.view.update();

                this.topicMap = TopicMapView.instances[0];
                this.entityCollection = EntityCollection.instances[0];
            };
        });

        afterEach(function() {
            configuration.and.stub();
            this.view.remove();
            TopicMapView.reset();
            EntityCollection.reset();
        });

        describe('rendered with no entities in the collection', function() {
            beforeEach(function() {
                this.createView();
            });

            it('shows the empty message', function() {
                expect(this.view.$('.entity-topic-map-empty')).not.toHaveClass('hide');
            });

            it('does not show the loading indicator', function() {
                expect(this.view.$('.entity-topic-map-loading')).toHaveClass('hide');
            });

            it('does not show the error message', function() {
                expect(this.view.$('.entity-topic-map-error')).toHaveClass('hide');
            });

            it('does not show the topic map', function() {
                expect(this.view.$('.entity-topic-map')).toHaveClass('hide');
            });
        });

        describe('rendered with entities in the collection', function() {
            beforeEach(function() {
                this.createView();
                this.entityCollection.add([
                    {text: 'gin', occurrences: 12, docsWithPhrase: 7, cluster: 0},
                    {text: 'siege', occurrences: 23, docsWithPhrase: 1, cluster: 0},
                    {text: 'pneumatic', occurrences: 2, docsWithPhrase: 2, cluster: 1}
                ]);
                this.entityCollection.trigger('sync');
            });

            it('does not show the loading indicator', function() {
                expect(this.view.$('.entity-topic-map-loading')).toHaveClass('hide');
            });

            it('does not show the error message', function() {
                expect(this.view.$('.entity-topic-map-error')).toHaveClass('hide');
            });

            it('does not show the empty message', function() {
                expect(this.view.$('.entity-topic-map-empty')).toHaveClass('hide');
            });

            it('renders a topic map with data from the entity collection', function() {
                expect(this.topicMap.setData).toHaveBeenCalled();
                expect(this.topicMap.draw).toHaveBeenCalled();

                expect(this.topicMap.setData.calls.mostRecent().args[0]).toEqual([
                    {name: 'gin', size: 8, children: [{name: 'gin', size: 7}, {name: 'siege', size: 1}]},
                    {name: 'pneumatic', size: 2, children: [{name: 'pneumatic', size: 2}]}
                ]);

                expect(this.view.$('.entity-topic-map')).not.toHaveClass('hide');
            });

            describe('when the entities collection is fetched', function() {
                beforeEach(function() {
                    this.entityCollection.trigger('request');
                });

                it('shows the loading indicator', function() {
                    expect(this.view.$('.entity-topic-map-loading')).not.toHaveClass('hide');
                });

                it('does not show the error message', function() {
                    expect(this.view.$('.entity-topic-map-error')).toHaveClass('hide');
                });

                it('does not show the empty message', function() {
                    expect(this.view.$('.entity-topic-map-empty')).toHaveClass('hide');
                });

                it('hides the topic map', function() {
                    expect(this.view.$('.entity-topic-map')).toHaveClass('hide');
                });

                describe('then the fetch succeeds with no results', function() {
                    beforeEach(function() {
                        this.entityCollection.reset();
                        this.entityCollection.trigger('sync');
                    });

                    it('hides the loading indicator', function() {
                        expect(this.view.$('.entity-topic-map-loading')).toHaveClass('hide');
                    });

                    it('does not show the error message', function() {
                        expect(this.view.$('.entity-topic-map-error')).toHaveClass('hide');
                    });

                    it('shows the empty message', function() {
                        expect(this.view.$('.entity-topic-map-empty')).not.toHaveClass('hide');
                    });

                    it('does not show the topic map', function() {
                        expect(this.view.$('.entity-topic-map')).toHaveClass('hide');
                    });
                });

                describe('then the fetch fails', function() {
                    beforeEach(function() {
                        this.entityCollection.reset();
                        this.entityCollection.trigger('error', this.entityCollection, {status: 400});
                    });

                    it('hides the loading indicator', function() {
                        expect(this.view.$('.entity-topic-map-loading')).toHaveClass('hide');
                    });

                    describe('then the error message', function() {
                        it('is displayed', function() {
                            expect(this.view.$('.entity-topic-map-error')).not.toHaveClass('hide');
                        });

                        it('contains the custom "call support string"', function() {
                            expect(this.view.$('.entity-topic-map-error')).toContainText(configuration().errorCallSupportString);
                        });
                    });

                    it('does not show the empty message', function() {
                        expect(this.view.$('.entity-topic-map-empty')).toHaveClass('hide');
                    });

                    it('does not show the topic map', function() {
                        expect(this.view.$('.entity-topic-map')).toHaveClass('hide');
                    });
                });

                describe('then the fetch is aborted', function() {
                    beforeEach(function() {
                        this.entityCollection.reset();
                        this.entityCollection.trigger('error', this.entityCollection, {status: 0});
                    });

                    it('does not hide the loading indicator', function() {
                        expect(this.view.$('.entity-topic-map-loading')).not.toHaveClass('hide');
                    });

                    it('does not show the error message', function() {
                        expect(this.view.$('.entity-topic-map-error')).toHaveClass('hide');
                    });

                    it('does not show the empty message', function() {
                        expect(this.view.$('.entity-topic-map-empty')).toHaveClass('hide');
                    });

                    it('does not show the topic map', function() {
                        expect(this.view.$('.entity-topic-map')).toHaveClass('hide');
                    });
                });
            });
        });
    });
});
