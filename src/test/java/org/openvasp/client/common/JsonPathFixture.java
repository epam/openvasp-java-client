package org.openvasp.client.common;

import com.jayway.jsonpath.JsonPath;
import lombok.NonNull;

import static org.assertj.core.api.Assertions.assertThat;

public final class JsonPathFixture {

    private final String json;

    public JsonPathFixture(@NonNull final String json) {
        this.json = json;
    }

    public void assertEquals(@NonNull final String actualValue, @NonNull final String jsonPath) {
        final String expectedValue = JsonPath.read(json, jsonPath);
        assertThat(actualValue).isEqualTo(expectedValue);
    }

}
