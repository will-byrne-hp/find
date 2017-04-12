# How to customise Haven Search Components without checking out the module

All Services, Components, and Beans defined in HavenSearchComponents are designed to be easily overridden from client
applications such as Find.

Please note that in all the following examples the custom classes annotated with @Service, @Component, or @Configuration
must be located under the com.hp.autonomy.frontend.find package. There is a process which scans this particular package at application startup.

## Adding/changing/removing parameters sent to Idol

One common customisation is to alter the parameters being sent to Idol. Almost all parameters for the various different
actions are set in HavenSearchAciParameterHandler.

In order to customise this class add a custom class annotated with @Component and @Primary in an appropriate package (see above) which
extends HavenSearchAciParameterHandlerProxy. Then override the method you want to customise.

The @Component annotation means that Find will locate and initialise the class on startup, and the @Primary annotation
means that it will use it in place of the default implementation in all scenarios.

For example, if you wanted to change the value of the ViewServer StripScript parameter to false, you could add the
following class:

    package com.hp.autonomy.frontend.find.idol.search;

    import com.autonomy.aci.client.util.AciParameters;
    import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandlerProxy;
    import com.hp.autonomy.searchcomponents.idol.view.IdolViewRequest;
    import com.hp.autonomy.types.requests.idol.actions.view.params.ViewParams;
    import org.springframework.context.annotation.Primary;
    import org.springframework.stereotype.Component;

    @Component
    @Primary
    public class CustomParameterHandler extends HavenSearchAciParameterHandlerProxy {
        @Override
        public void addViewParameters(final AciParameters aciParameters, final String reference, final IdolViewRequest viewRequest) {
            super.addViewParameters(aciParameters, reference, viewRequest);
            aciParameters.remove(ViewParams.StripScript.name());
        }
    }

This removes the StripScript parameter, which defaults to false. We could also explicitly set it to false:

    @Override
    public void addViewParameters(final AciParameters aciParameters, final String reference, final IdolViewRequest viewRequest) {
        super.addViewParameters(aciParameters, reference, viewRequest);
        aciParameters.remove(ViewParams.StripScript.name());
        aciParameters.add(ViewParams.StripScript.name(), false);
    }

It is also possible to define the customised class inline as part of a @Configuration class, achieving the same effect:

    package com.hp.autonomy.frontend.find.idol.beanconfiguration;

    import com.autonomy.aci.client.util.AciParameters;
    import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
    import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandlerProxy;
    import com.hp.autonomy.searchcomponents.idol.view.IdolViewRequest;
    import com.hp.autonomy.types.requests.idol.actions.view.params.ViewParams;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.context.annotation.Primary;

    @Configuration
    public class CustomConfiguration {
        @Bean
        @Primary
        public HavenSearchAciParameterHandler customHavenSearchAciParameterHandler() {
            return new HavenSearchAciParameterHandlerProxy() {
                @Override
                public void addViewParameters(final AciParameters aciParameters, final String reference, final IdolViewRequest viewRequest) {
                    super.addViewParameters(aciParameters, reference, viewRequest);
                    aciParameters.remove(ViewParams.StripScript.name());
                }
            };
        }
    }

## Adding new request parameters

Request objects can be extended via composition or reimplemented.
If we wanted to optionally enable or disable the ViewServer StripScript parameter we could create a new request object as follows:

    package com.hp.autonomy.frontend.find.idol.view;

    import com.hp.autonomy.searchcomponents.idol.view.IdolViewRequest;
    import com.hp.autonomy.searchcomponents.idol.view.IdolViewRequestBuilder;

    public class CustomViewRequest implements IdolViewRequest {
        private static final long serialVersionUID = 2496478671179292570L;

        private final IdolViewRequest viewRequest;
        private final boolean stripScript;

        private CustomViewRequest(final CustomViewRequestBuilder builder) {
            viewRequest = builder.viewRequestBuilder.build();
            stripScript = builder.stripScript;
        }

        @Override
        public String getDocumentReference() {
            return viewRequest.getDocumentReference();
        }

        @Override
        public String getDatabase() {
            return viewRequest.getDatabase();
        }

        @Override
        public String getHighlightExpression() {
            return viewRequest.getHighlightExpression();
        }

        public boolean isStripScript() {
            return stripScript;
        }

        @Override
        public IdolViewRequestBuilder toBuilder() {
            return new CustomViewRequestBuilder(this);
        }

        public static CustomViewRequestBuilder builder(final IdolViewRequest otherViewRequest) {
            return new CustomViewRequestBuilder(otherViewRequest);
        }

        public static class CustomViewRequestBuilder implements IdolViewRequestBuilder {
            private final IdolViewRequestBuilder viewRequestBuilder;
            private boolean stripScript = true;

            private CustomViewRequestBuilder(final IdolViewRequest viewRequest) {
                viewRequestBuilder = viewRequest.toBuilder();
            }

            private CustomViewRequestBuilder(final CustomViewRequest customViewRequest) {
                viewRequestBuilder = customViewRequest.viewRequest.toBuilder();
                stripScript = customViewRequest.stripScript;
            }

            @Override
            public IdolViewRequestBuilder documentReference(final String documentReference) {
                viewRequestBuilder.documentReference(documentReference);
                return this;
            }

            @Override
            public IdolViewRequestBuilder database(final String database) {
                viewRequestBuilder.database(database);
                return this;
            }

            @Override
            public IdolViewRequestBuilder highlightExpression(final String highlightExpression) {
                viewRequestBuilder.highlightExpression(highlightExpression);
                return this;
            }

            public IdolViewRequestBuilder stripScript(final boolean stripScript) {
                this.stripScript = stripScript;
                return this;
            }

            @Override
            public CustomViewRequest build() {
                return new CustomViewRequest(this);
            }
        }
    }

We could then construct the object by doing

    final IdolViewRequest viewRequest = CustomViewRequest.builder(existingViewRequest)
        .stripScript(false)
        .build();

It is then possible to access the new stripScript parameter in the parameter handler:

    @Override
    public void addViewParameters(final AciParameters aciParameters, final String reference, final ViewRequest<String> viewRequest) {
        super.addViewParameters(aciParameters, reference, viewRequest);
        aciParameters.remove(ViewParams.StripScript.name());
        aciParameters.add(ViewParams.StripScript.name(), ((CustomViewRequest) viewRequest).isStripScript());
    }

Alternatively we could reimplement the request object:

    package com.hp.autonomy.frontend.find.idol.view;

    import com.hp.autonomy.searchcomponents.idol.view.IdolViewRequest;
    import lombok.Builder;
    import lombok.Data;

    @Data
    @Builder(toBuilder = true)
    public class CustomViewRequest implements IdolViewRequest {
        private static final long serialVersionUID = 2496478671179292570L;

        private final String documentReference;
        private final String database;
        private final String highlightExpression;
        private final boolean stripScript;
    }

In this case we construct the request object as follows:

    final IdolViewRequest viewRequest = CustomViewRequest.builder()
        .documentReference(documentReference)
        .database(database)
        .highlightExpression(highlightExpression)
        .striScript(false)
        .build();

## Customising a Service

Altering the behaviour of a HavenSearchComponents Service class is similar to overriding HavenSearchAciParameterHandler
described above, but we have not (at the time of writing) created any proxy classes so the default class would need
extending manually, via composition.

For example, customising the DocumentService which handles Idol Content/Qms queries can be done as follows:

    package com.hp.autonomy.frontend.find.idol.search;

    import com.autonomy.aci.client.services.AciErrorException;
    import com.hp.autonomy.searchcomponents.core.search.StateTokenAndResultCount;
    import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentsService;
    import com.hp.autonomy.searchcomponents.idol.search.IdolGetContentRequest;
    import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
    import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
    import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
    import com.hp.autonomy.searchcomponents.idol.search.IdolSuggestRequest;
    import com.hp.autonomy.types.requests.Documents;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Qualifier;
    import org.springframework.context.annotation.Primary;
    import org.springframework.stereotype.Service;

    import java.util.List;

    @Service
    @Primary
    class CustomDocumentService implements IdolDocumentsService {
        private final IdolDocumentsService documentsService;

        @Autowired
        CustomDocumentService(
                @Qualifier(DOCUMENTS_SERVICE_BEAN_NAME)
                final IdolDocumentsService documentsService
        ) {
            this.documentsService = documentsService;
        }

        @Override
        public Documents<IdolSearchResult> queryTextIndex(final IdolQueryRequest queryRequest) throws AciErrorException {
            ... custom code ...
            final Documents<IdolSearchResult> results = documentsService.queryTextIndex(queryRequest);
            ... custom code ...
            return results;
        }

        @Override
        public Documents<IdolSearchResult> findSimilar(final IdolSuggestRequest suggestRequest) throws AciErrorException {
            return documentsService.findSimilar(suggestRequest);
        }

        @Override
        public List<IdolSearchResult> getDocumentContent(final IdolGetContentRequest getContentRequest) throws AciErrorException {
            return documentsService.getDocumentContent(getContentRequest);
        }

        @Override
        public String getStateToken(final IdolQueryRestrictions queryRestrictions, final int maxResults, final boolean promotions) throws AciErrorException {
            return documentsService.getStateToken(queryRestrictions, maxResults, promotions);
        }

        @Override
        public StateTokenAndResultCount getStateTokenAndResultCount(final IdolQueryRestrictions queryRestrictions, final int maxResults, final boolean promotions) throws AciErrorException {
            return documentsService.getStateTokenAndResultCount(queryRestrictions, maxResults, promotions);
        }
    }

## Customising a Component

Customising a HavenSearchComponents Component class is very similar to customising a service class.

For example, the QueryExecutor implementation (executes query/suggest actions against Idol) is overridden in Find as follows:

    package com.hp.autonomy.frontend.find.idol.search;

    import com.autonomy.aci.client.services.AciErrorException;
    import com.autonomy.aci.client.util.AciParameters;
    import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
    import com.hp.autonomy.searchcomponents.idol.search.QueryExecutor;
    import com.hp.autonomy.types.idol.responses.QueryResponseData;
    import com.hp.autonomy.types.idol.responses.SuggestResponseData;
    import com.hp.autonomy.types.requests.qms.actions.query.params.QmsQueryParams;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Qualifier;
    import org.springframework.context.annotation.Primary;
    import org.springframework.stereotype.Component;

    @Primary
    @Component
    class FindQueryExecutor implements QueryExecutor {
        private final QueryExecutor queryExecutor;

        @Autowired
        public FindQueryExecutor(
                @Qualifier(QUERY_EXECUTOR_BEAN_NAME)
                final QueryExecutor queryExecutor) {
            this.queryExecutor = queryExecutor;
        }

        @Override
        public boolean performQuery(final QueryRequest.QueryType queryType) throws AciErrorException {
            return queryExecutor.performQuery(queryType);
        }

        @Override
        public QueryResponseData executeQuery(final AciParameters aciParameters, final QueryRequest.QueryType queryType) throws AciErrorException {
            ... custom code ...
            final QueryResponseData responseData = queryExecutor.executeQuery(aciParameters, queryType);
            ... custom code ...

            return responseData;
        }

        @Override
        public SuggestResponseData executeSuggest(final AciParameters aciParameters, final QueryRequest.QueryType queryType) throws AciErrorException {
            return queryExecutor.executeSuggest(aciParameters, queryType);
        }
    }

## Customising a Bean

HavenSearchComponents contains a number of Beans defined inline with Spring configuration classes.
These can all be overridden by specifying a Bean of the same name or type (as appropriate).

For example, the http client object which sets options for http queries to Idol could be customised as follows:

    package com.hp.autonomy.frontend.find.idol.beanconfiguration;

    import com.hp.autonomy.searchcomponents.idol.beanconfiguration.HavenSearchIdolConfiguration;
    import org.apache.http.client.HttpClient;
    import org.apache.http.config.SocketConfig;
    import org.apache.http.impl.client.HttpClientBuilder;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;

    @Configuration
    public class CustomConfiguration {
        @Bean(name = HavenSearchIdolConfiguration.HTTP_CLIENT_BEAN_NAME)
        public HttpClient httpClient() {
            final SocketConfig socketConfig = SocketConfig.custom()
                    .setSoTimeout(90000)
                    .build();

            return HttpClientBuilder.create()
                    .setMaxConnPerRoute(20)
                    .setMaxConnTotal(120)
                    .setDefaultSocketConfig(socketConfig)
                    .build();
        }
    }