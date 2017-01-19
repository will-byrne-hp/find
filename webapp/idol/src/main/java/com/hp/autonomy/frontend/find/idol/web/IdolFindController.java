package com.hp.autonomy.frontend.find.idol.web;

import com.hp.autonomy.frontend.configuration.ConfigFileService;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.AuthenticationConfig;
import com.hp.autonomy.frontend.find.core.export.MetadataNode;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.FindController;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.find.idol.configuration.MMAP;
import com.hp.autonomy.frontend.find.idol.dashboards.IdolDashboardConfig;
import com.hp.autonomy.frontend.find.idol.export.IdolMetadataNode;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import lombok.Getter;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class IdolFindController extends FindController<IdolFindConfig, IdolFindConfig.IdolFindConfigBuilder> {

    private enum IdolMvcConstants{
        MMAP_BASE_URL("mmapBaseUrl"),
        VIEW_HIGHLIGHTING("viewHighlighting"),
        DASHBOARDS("dashboards");

        @Getter
        private final String name;

        IdolMvcConstants(final String name) {
            this.name = name;
        }
    }

    private final ConfigFileService<IdolDashboardConfig> dashConfig;


    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    protected IdolFindController(final ControllerUtils controllerUtils,
                                 final AuthenticationInformationRetriever<?, ? extends Principal> authenticationInformationRetriever,
                                 final ConfigService<? extends AuthenticationConfig<?>> authenticationConfigService,
                                 final ConfigService<IdolFindConfig> configService, final ConfigFileService<IdolDashboardConfig> dashConfig) {
        super(controllerUtils, authenticationInformationRetriever, authenticationConfigService, configService);
        this.dashConfig = dashConfig;
    }

    @Override
    protected Map<String, Object> getPublicConfig() {
        final Map<String, Object> publicConfig = new HashMap<>();
        final IdolFindConfig config = configService.getConfig();

        final MMAP mmap = config.getMmap();

        if(BooleanUtils.isTrue(mmap.getEnabled())) {
            publicConfig.put(IdolMvcConstants.MMAP_BASE_URL.getName(), mmap.getBaseUrl());
        }

        publicConfig.put(IdolMvcConstants.VIEW_HIGHLIGHTING.getName(), config.getViewConfig().getHighlighting());
        publicConfig.put(IdolMvcConstants.DASHBOARDS.getName(), dashConfig.getConfig().getDashboards());

        return publicConfig;
    }

    @Override
    protected List<MetadataNode> getMetadataNodes() {
        return Arrays.asList(IdolMetadataNode.values());
    }
}
