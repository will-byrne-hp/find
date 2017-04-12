---
title: Creating Widgets
layout: default
---
# Creating a Widget
## Introduction
### What are Widgets
Simply put, Widgets are the components that make up a dashboard. A widget is a configurable JavaScript module that is rendered on the dashboard grid as a [Backbone][backbone] View. Widgets can be configured using the `dashboards.json` configuration file and can take a custom settings object.

### Assumptions and Constraints
- A widget must be responsive. The grid system we have implemented means that a widget can be any rectangular shape so it must be able to handle being short, tall, thin and wide without breaking the UI.
- The widget should not take a long time to load. Given this is a dashboard system it needs to be be responsive and load in a timely manner.
- The widgets should all run as separate instances and not interfere with other widgets.
- A widget is non interactive. Widgets should only display information and if the `onClick` function is used it should navigate to the source of the data used to render the display.

### Widget Examples

#### Topic Map
![Topic Map Widget][topic-map]

Here we have the Topic Map Widget. This is one of the standard built in widgets which is backed by a saved search (explained [here][ssw]). In a running instance of Find, this widget will automatically resize when the window is resized and it will update the saved search and Topic Map after a specified interval. There can be multiple of these on a single dashboard without them conflicting with each other. 

#### Current Time and Date
![Current Time and Date][time-and-date]

The Current Time and Date widget is a standard widget (explained [here][sw]). This is not backed by any data from the server other than the widget specific settings and will not update periodically. Like the Topic Map above it will resize itself when the window size changes and can be set to a rectangle of any size / orientation.

#### Results List Widget
![Results List Widget][results-list]

This is the Results List Widget, it is backed by a saved search and will display the top n results as specified in the configuration file. The results list widget will resize itself when the window fires resize events and will alter its layout when necessary to accommodate the data and the specified view layout. The widget will also refresh the data when the interval has passed so if the saved search has changed then the changes will be reflected in the widget.

## Widget Types
There are three types of widget currently supported in Find: [Standard][sw], [Updating][uw] and [SavedSearch][ssw] widgets. These three widget types should cover most use cases and can all be implemented quickly by extending their abstract views.

### Standard Widget
A standard widget is designed to be largely static. This is not to say that the widget itself can not change (see the [Current Time and Date Widget][ctdw] above) but that it will not be requesting more data from a server or re-rendering the display other than to handle resizing. For example: a widget to display a company-wide video update.
> **Note:** If the widget is just a static piece of HTML or an image then the static content or static image widgets should be used and the HTML / image URI passed in via widget settings.

### Updating Widget
An Updating Widget is the same as a [Standard Widget][sw] but will update itself every n seconds which is defined in the configuration file. The update triggers a function call which needs to be overridden in the implementation of the abstract view. This type of widget should be used when there is a need to fetch data or re-render on a regular interval. For example a widget to display the weather may poll the api of a third party weather service. 

> **Note:** The [Saved Search Widget][ssw] extends the Updating Widget abstract view and will call update in the same manner.

### Saved Search Widget

The Saved Search Widget is an extension of the [Updating Widget][uw] that is backed by a saved search. As seen in the [Results List Widget][rlw] above this will call the update method after the interval has passed and will re-fetch the saved search data which can then be used to retrieve the search results needed by the widget to update the display (in the case of the [Results List Widget][rlw], it will be the documents collection).

> **Note:** The saved search can be either a query or a snapshot and as snapshots do not change it will not update if a snapshot is specified.

## Development
### Shared Development
#### Widget Registry
All widgets are located and instantiated via the widget registry (`widget-registry.js`). This is where the widget source files are loaded via [Require.js][requirejs]. When a widget is loaded by the dashboard it uses the name specified in the configuration file to perform a lookup in the widget registry to retrieve the constructor.

A widget registry entry looks like this:
```javascript
SunburstWidget: {
    Constructor: SunburstWidget
}
```
The key for the object property (`SunburstWidget`) is the name used in the configuration file to refer to this widget. The constructor property should be the Backbone View constructor for the widget view. Widgets should be written in a separate file and loaded via [Require.js][requirejs] into the regitstry.

#### HTML and Layout
The layout of the widgets is very simple. Each widget has a title (optional) and a content div, these use `display: flex` so if there is no title then the content will expand to take up 100% of the height. For the purposes of widget development the only element of consequence is the widget content div, this is passed to view from the abstract widget view as `$content` and is available after calling the render method on the widget abstract view. For example:
```javascript
Widget.prototype.render.apply(this);
this.$content.html(someHtml);
```

All widget types come with a loading spinner which is displayed until the `initialised()` function is called. This will hide the loading spinner and show the content, ideally this should be called at the end of the render method just after the `$content` element is populated.

The widgets are sized and laid out in a grid pattern (as explained in the admin guide) the size of which is specified on the dashboard. The widget size is then calculated based upon its starting coordinates and width and height in grid squares and added as an inline style. This necessitates that the widget must be capable of handling multiple alternate layouts, for example the [Results List Widget][rlw] will switch to a column based layout when it would allow for more data to be displayed.

#### Functions and Properties
`clickable` is the only property used by all widget types. It takes a `boolean` value and determines whether the widget click handler is called when the user clicks the widget. As mentioned above the widget should be non interactive and the click handler should only be used to navigate to the data source of the widget

We provide an `onResize` function to handle window resize events which should be used rather than implementing separate listeners as this also handles the sidebar opening and collapsing as well.

The `onClick` method is provided to handle click functionality and will be called if the widget is clicked anywhere. This will only be called if the `clickable` property is set to `true`.
> **Note:** This should not be overridden by a saved search widget as that already has a click handler function.

#### Widget Settings
In the configuration file there is a `widgetSettings` object that will look something like this: 
```json
"widgetSettings": {
    "key": "some value",
    "key2": {
        "subkey": "another value",
        "subkey2" : [1,2,3,4,5,6]
    }
}
```
These values are passed in to the widget when it is initialised in the options parameter and stored as a variable on the view. For example the above would be accessed via:
```javascript
initialize: function(options) {
    Widget.prototype.initialize.apply(this, arguments);
    this.key = this.widgetSettings.key;
    this.key2 = this.widgetSettings.key2;
    this.subkey = this.widgetSettings.key2.subkey
    this.subkey2 = this.widgetSettings.key2.subkey2
}
```

### Standard Widget Development
The standard widgets are very simple and utilise nothing additional to the above shared settings when implemented. As mentioned above most uses of this type of widget could be replaced with a `StaticContentWidget` or `StaticImageWidget`.
#### Example
```javascript
define([
    './widget' // load the abstract widget view.
], function(Widget) {
    'use strict';

    return Widget.extend({
        initialize: function(options) {
            Widget.prototype.initialize.apply(this, arguments);

            this.subject = this.widgetSettings.subject || 'world'; // setting to determine who to greet.
        },

        render: function() {
            Widget.prototype.render.apply(this);
            this.$content.html('Hello, ' + this.subject + '!'); // render some html greeting.
            this.initialised(); // remove the loading spinner.
        }
    });
});
```

### Updating Widget Development
The updating widget utilises a set of functions to handle the update. These need to be implemented carefully to ensure it works with the `TimeLastRefreshedWidget`.

#### Functions
`doUpdate(done)` is the main update function that is called when the dashboard refreshes all of the widgets. The done parameter is a callback that must be called when the widget has finished updating (if this is not called the `TimeLastRefreshed` widget will not know the update has finished). This function should re-fetch any data needed to render the widget and then update the UI accordingly. The loading spinner is handled by the abstract view and does not need to be shown and hidden manually.

`onCancelled` is called when the update has been cancelled for any reason. This function should cancel any pending requests made by the widget and resolve or remove any outstanding promises.

#### Example
```javascript
define([
    './updating-widget', // load the abstract updating-widget view.
    './some-weather-service' // load some weather service.
], function(UpdatingWidget, SomeWeatherService) {
    'use strict';

    return UpdatingWidget.extend({
        initialize: function(options) {
            UpdatingWidget.prototype.initialize.apply(this, arguments);
            this.weatherService = new SomeWeatherService({location: this.widgetSettings.location}); // create some weather service with a location from the settings.
            this.subject = this.widgetSettings.subject || 'world'; // setting to determine who to greet.
        },

        render: function() {
            Widget.prototype.render.apply(this);
            this.$content.html('Hello, ' + this.subject + '! The weather near you is: <span class="weather"></span>'); // render some html greeting with a weather option.
            this.initialised(); // remove the loading spinner.
        }

        doUpdate(done) {
            this.weatherService.getWeather({ // perform some fetch on the weather service.
                success: function(weather) {
                    this.$('.weather').html(weather); // render the weather.
                    done(); // call update callback to signify completion.
                }
            })
        }

        onCancelled: function() {
            if (this.weatherService.getPromise()) { // if the weather service is fetching.
                this.weatherService.cancelPromise(); // perform some request cancellation.
            }
        }
    });
});
```

### Saved Search Widget Development
The saved search widgets are an extension of the updating widgets so incorporate all of the above apart from `doUpdate` which should not be overridden as the saved search widget already handles this for you (see `getData` below). The saved search abstract view handles the retrieval of the saved search during initialisation and updates.

#### Functions
`postInitialize` is a function that is run after the saved search has been fetched successfully. This can optionally return a promise in which case `getData` will not be called until is has been resolved. This is useful for loading any extra objects or views that are contingent on the information in the saved search.

`getData` is the main method for retrieving the data needed to render the view, it is called in by `doUpdate` in the abstract widget (which is why this should not be overridden). In the [Results List Widget][rlw] this is used to fetch the document collection, there is a listener on the collection which renders the new results and calculates what can be displayed. This should return a promise which will be used to handle the `doUpdate` callback.

#### Properties
`savedSearchModel` is the model that controls the saved search information. This is controlled by the abstract view and should be considered read only.

`queryModel` is available if this is required. This contains the same information as the saved search model in a different format and is mainly used for internal purposes.

`viewType` is the results view that should be loaded on click. This property is optional and will default to the first configured results view if nothing is specified.

### Example
```javascript
define([
    'underscore', // import underscore for templating.
    './saved-search-widget', // import the abstract saved search widget.
    'find/app/model/documents-collection', // import the documents collection.
    'moment' // import moment for time parsing.
], function(_, SavedSearchWidget, DocumentsCollection, moment) {
    'use strict';

    return SavedSearchWidget.extend({
        viewType: 'list', // when clicket take user to the saved search with the list view displayed.

        template: _.template('<span>Latest result is: <%-title%> <br> it was indexed on: <%-date%></span>'), // template for the document.

        initialize: function(options) {
            SavedSearchWidget.prototype.initialize.apply(this, arguments);

            this.documentsCollection = new DocumentsCollection(); // create the collection.

            this.listenTo(this.documentsCollection, 'add', function(attributes) { // add a listener to alter the html when a new model is added
                this.$content.html(this.template({
                    title: attributes.title,
                    date: moment(attributes.date).format()
                }))
            });

        },

        getData: function() {
            return this.documentsCollection.fetch({ // fetch the document based on the saved search
                data: {
                    text: this.queryModel.get('queryText'),
                    max_results: 1,
                    indexes: this.queryModel.get('indexes'),
                    field_text: this.queryModel.get('fieldText'),
                    min_date: this.queryModel.getIsoDate('minDate'),
                    max_date: this.queryModel.getIsoDate('maxDate'),
                    sort: 'date',
                    summary: 'context',
                    queryType: 'MODIFIED',
                    highlight: false
                },
                reset: false
            });
        }
    });
});
```

[requirejs]: http://requirejs.org/
[backbone]: http://backbonejs.org/
[topic-map]: ./topic-map.png
[time-and-date]: ./time-and-date.png
[results-list]: ./results-list.png
[ctdw]:#current-time-and-date
[rlw]:#results-list-widget
[sw]:#standard-widget
[uw]:#updating-widget
[ssw]:#saved-search-widget