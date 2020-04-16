package org.openvasp.client.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.common.io.Resources;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static org.openvasp.client.common.VaspUtils.toBytes;
import static org.openvasp.client.common.VaspUtils.toHex;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public final class Json {

    private static final String JSON_DATA = "json-data/";
    private static final String JSON_TEST_DATA = "json-test-data/";

    public static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .registerModule(new JsonOrgModule());

        MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US));
    }

    private Json() {
    }

    @SneakyThrows
    public static <T> String toJson(@NonNull final T obj) {
        return MAPPER.writeValueAsString(obj);
    }

    @SneakyThrows
    public static <T> T fromJson(@NonNull final Class<T> clazz, @NonNull final String str) {
        return MAPPER.readValue(str, clazz);
    }

    @SneakyThrows
    public static <T> String toHexJson(@NonNull final T obj) {
        return toHex(MAPPER.writeValueAsString(obj).getBytes(), true);

    }

    @SneakyThrows
    public static <T> T fromHexJson(@NonNull final Class<T> clazz, @NonNull final String hexStr) {
        return MAPPER.readValue(new String(toBytes(hexStr)), clazz);
    }

    @SneakyThrows
    public static JsonNode readTree(@NonNull final String str) {
        return MAPPER.readTree(str);
    }

    public static <T> T convertValue(@NonNull final Class<T> clazz, @NonNull final JsonNode jsonNode) {
        return MAPPER.convertValue(jsonNode, clazz);
    }

    @SneakyThrows
    public static <T> T loadJson(
            @NonNull final Class<T> cls,
            @NonNull final String basePath,
            @NonNull final String path) {

        try (val stream = Json.class.getResourceAsStream("/" + basePath + path)) {
            return Json.MAPPER.readValue(stream, cls);
        }
    }

    public static <T> T loadJson(@NonNull final Class<T> cls, @NonNull final String path) {
        return loadJson(cls, JSON_DATA, path);
    }

    @SneakyThrows
    @SuppressWarnings("UnstableApiUsage")
    public static String loadJson(@NonNull final String path) {
        return Resources.toString(
                Resources.getResource(JSON_DATA + path),
                Charset.defaultCharset());
    }

    public static <T> T loadTestJson(@NonNull final Class<T> cls, @NonNull final String path) {
        return loadJson(cls, JSON_TEST_DATA, path);
    }

    @SneakyThrows
    @SuppressWarnings("UnstableApiUsage")
    public static String loadTestJson(final String path) {
        return Resources.toString(
                Resources.getResource(JSON_TEST_DATA + path),
                Charset.defaultCharset());
    }

}
