/*
 *  Copyright (c) 2023 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */

package org.eclipse.edc.core.transform.transformer.to;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.eclipse.edc.policy.model.Action;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.Constraint;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.core.transform.transformer.TestInput.getExpanded;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.ODRL_ACTION_ATTRIBUTE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.ODRL_CONSTRAINT_ATTRIBUTE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.ODRL_TARGET_ATTRIBUTE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JsonObjectToProhibitionTransformerTest {
    private static final String TARGET = "target";

    private final TransformerContext context = mock(TransformerContext.class);

    private final Action action = Action.Builder.newInstance().type("type").build();
    private final Constraint constraint = AtomicConstraint.Builder.newInstance().build();

    private JsonObjectToProhibitionTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new JsonObjectToProhibitionTransformer();

        when(context.transform(isA(JsonObject.class), eq(Action.class))).thenReturn(action);
        when(context.transform(isA(JsonObject.class), eq(Constraint.class))).thenReturn(constraint);
    }

    @ParameterizedTest
    @MethodSource("jsonSource")
    void transform_attributesAsObjects_returnPermission(JsonObject prohibition) {
        var result = transformer.transform(getExpanded(prohibition), context);

        assertThat(result).isNotNull();
        assertThat(result.getAction()).isEqualTo(action);
        assertThat(result.getConstraints()).hasSize(1);
        assertThat(result.getConstraints().get(0)).isEqualTo(constraint);

        verify(context, never()).reportProblem(anyString());
        verify(context, times(1)).transform(isA(JsonObject.class), eq(Action.class));
        verify(context, times(1)).transform(isA(JsonObject.class), eq(Constraint.class));
    }

    static Stream<JsonObject> jsonSource() {
        var jsonFactory = Json.createBuilderFactory(Map.of());
        var actionJson = jsonFactory.createObjectBuilder().add(TYPE, "action");
        var constraintJson = jsonFactory.createObjectBuilder().add(TYPE, "constraint");

        return Stream.of(
                // object
                jsonFactory.createObjectBuilder()
                        .add(ODRL_ACTION_ATTRIBUTE, actionJson)
                        .add(ODRL_CONSTRAINT_ATTRIBUTE, constraintJson)
                        .add(ODRL_TARGET_ATTRIBUTE, TARGET)
                        .build(),

                // array version
                jsonFactory.createObjectBuilder()
                        .add(ODRL_ACTION_ATTRIBUTE, jsonFactory.createArrayBuilder().add(actionJson))
                        .add(ODRL_CONSTRAINT_ATTRIBUTE, jsonFactory.createArrayBuilder().add(constraintJson))
                        .add(ODRL_TARGET_ATTRIBUTE, jsonFactory.createArrayBuilder().add(TARGET))
                        .build()

        );
    }

}
