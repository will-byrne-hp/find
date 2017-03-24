/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.export.service;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.find.core.export.service.ExportFormat;
import com.hp.autonomy.frontend.find.core.export.service.PlatformDataExportService;
import com.hp.autonomy.frontend.find.core.export.service.PlatformDataExportStrategy;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.idol.configuration.AciServiceRetriever;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import com.hp.autonomy.types.requests.idol.actions.query.QueryActions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

@Component
class IdolPlatformDataExportService implements PlatformDataExportService<IdolQueryRequest, AciErrorException> {
    private final HavenSearchAciParameterHandler parameterHandler;
    private final AciServiceRetriever aciServiceRetriever;
    private final Map<ExportFormat, PlatformDataExportStrategy> exportStrategies;

    @Autowired
    public IdolPlatformDataExportService(final HavenSearchAciParameterHandler parameterHandler,
                                         final AciServiceRetriever aciServiceRetriever,
                                         final PlatformDataExportStrategy[] exportStrategies) {
        this.parameterHandler = parameterHandler;
        this.aciServiceRetriever = aciServiceRetriever;

        this.exportStrategies = new EnumMap<>(ExportFormat.class);
        for(final PlatformDataExportStrategy exportStrategy : exportStrategies) {
            this.exportStrategies.put(exportStrategy.getExportFormat(), exportStrategy);
        }
    }

    @Override
    public void exportQueryResults(final OutputStream outputStream, final IdolQueryRequest queryRequest, final ExportFormat exportFormat, final Collection<String> selectedFieldIds, final long totalResults) throws AciErrorException, IOException {
        final AciParameters aciParameters = new AciParameters(QueryActions.Query.name());

        parameterHandler.addSearchRestrictions(aciParameters, queryRequest.getQueryRestrictions());
        parameterHandler.addSearchOutputParameters(aciParameters, queryRequest);
        if(queryRequest.getQueryType() != QueryRequest.QueryType.RAW) {
            parameterHandler.addQmsParameters(aciParameters, queryRequest.getQueryRestrictions());
        }

        final PlatformDataExportStrategy exportStrategy = exportStrategies.get(exportFormat);
        final Collection<String> fieldNames = exportStrategy.getFieldNames(IdolMetadataNode.values(), selectedFieldIds);

        exportStrategy.writeHeader(outputStream, fieldNames);

        for (int i = 0; i < totalResults; i += PAGINATION_SIZE) {
            final IdolQueryRequest paginatedQueryRequest = queryRequest.toBuilder()
                    .start(i + 1)
                    .maxResults(i + PAGINATION_SIZE)
                    .build();
            aciServiceRetriever.getAciService(paginatedQueryRequest.getQueryType()).executeAction(aciParameters, new ExportQueryResponseProcessor(exportStrategy, outputStream, selectedFieldIds));
        }
    }

    @Override
    public Collection<ExportFormat> handlesFormats() {
        return exportStrategies.keySet();
    }
}