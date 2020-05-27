package org.openvasp.client.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.common.io.Resources;
import com.jayway.jsonpath.JsonPath;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public static final ObjectMapper MAPPER_YAML;

    static {
        MAPPER = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .registerModule(new JsonOrgModule());

        MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US));

        MAPPER_YAML = new ObjectMapper(new YAMLFactory())
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .registerModule(new JsonOrgModule());

        MAPPER_YAML.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US));
    }

    private Json() {
    }

    @SneakyThrows
    public static <T> String toJson(
            @NonNull final T obj) {

        return MAPPER.writeValueAsString(obj);
    }

    @SneakyThrows
    public static <T> T fromJson(
            @NonNull final Class<T> clazz,
            @NonNull final String str) {

        return MAPPER.readValue(str, clazz);
    }

    @SneakyThrows
    public static <T> String toHexJson(
            @NonNull final T obj) {

        return toHex(MAPPER.writeValueAsString(obj).getBytes(), true);

    }

    @SneakyThrows
    public static <T> T fromHexJson(
            @NonNull final Class<T> clazz,
            @NonNull final String hexStr) {

        return MAPPER.readValue(new String(toBytes(hexStr)), clazz);
    }

    @SneakyThrows
    public static JsonNode readTree(
            @NonNull final String str) {

        return MAPPER.readTree(str);
    }

    public static <T> T convertValue(
            @NonNull final Class<T> clazz,
            @NonNull final JsonNode jsonNode) {

        return MAPPER.convertValue(jsonNode, clazz);
    }

    @SneakyThrows
    public static <T> T loadJson(
            @NonNull final Class<T> cls,
            @NonNull final String baseResourcePath,
            @NonNull final String resourcePath) {

        try (val stream = Json.class.getResourceAsStream("/" + baseResourcePath + resourcePath)) {
            return Json.MAPPER.readValue(stream, cls);
        }
    }

    @SneakyThrows
    public static <T> T loadYaml(
            @NonNull final Class<T> cls,
            @NonNull final String baseResourcePath,
            @NonNull final String resourcePath) {

        try (val stream = Json.class.getResourceAsStream("/" + baseResourcePath + resourcePath)) {
            return Json.MAPPER_YAML.readValue(stream, cls);
        }
    }

    public static <T> T loadJson(
            @NonNull final Class<T> cls,
            @NonNull final String path) {

        return loadJson(cls, JSON_DATA, path);
    }

    public static <T> T loadYaml(
            @NonNull final Class<T> cls,
            @NonNull final String path) {

        return loadYaml(cls, JSON_DATA, path);
    }

    @SneakyThrows
    @SuppressWarnings("UnstableApiUsage")
    public static String loadJson(
            @NonNull final String resourcePath) {

        return Resources.toString(
                Resources.getResource(JSON_DATA + resourcePath),
                Charset.defaultCharset());
    }

    public static <T> T loadTestJson(
            @NonNull final Class<T> cls,
            @NonNull final String resourcePath) {

        return loadJson(cls, JSON_TEST_DATA, resourcePath);
    }

    public static <T> T loadTestYaml(
            @NonNull final Class<T> cls,
            @NonNull final String resourcePath) {

        return loadYaml(cls, JSON_TEST_DATA, resourcePath);
    }

    @SneakyThrows
    @SuppressWarnings("UnstableApiUsage")
    public static String loadTestJson(
            @NonNull final String resourcePath) {

        return Resources.toString(
                Resources.getResource(JSON_TEST_DATA + resourcePath),
                Charset.defaultCharset());
    }

    public static <T> T readJsonPath(
            @NonNull final String resourcePath,
            @NonNull final String jsonPath) {

        val json = loadTestJson(resourcePath);
        return JsonPath.read(json, jsonPath);
    }

    @SneakyThrows
    public static <T> T loadFileJson(
            @NonNull final Class<T> cls,
            @NonNull final String path,
            final String... more) {

        try (val stream = Files.newInputStream(Paths.get(path, more))) {
            return Json.MAPPER.readValue(stream, cls);
        }
    }

    @SneakyThrows
    public static <T> T loadFileYaml(
            @NonNull final Class<T> cls,
            @NonNull final String path,
            final String... more) {

        try (val stream = Files.newInputStream(Paths.get(path, more))) {
            return Json.MAPPER_YAML.readValue(stream, cls);
        }
    }

}
