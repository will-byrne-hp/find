/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Set;

@Data
public class ParametricDisplayValues {
    private final String name;
    private final String displayName;
    private final Set<ParametricDisplayValue> values;

    public ParametricDisplayValues(@JsonProperty("name") final String name, @JsonProperty("displayName") final String displayName, @JsonProperty("values") final Set<ParametricDisplayValue> values) {
        this.name = name;
        this.displayName = displayName;
        this.values = values;
    }
}
