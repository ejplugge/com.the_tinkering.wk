/*
 * Copyright 2019-2020 Ernst Jan Plugge <rmc@dds.nl>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.the_tinkering.wk.tasks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.core.util.Consumer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.the_tinkering.wk.BuildConfig;
import com.the_tinkering.wk.Constants;
import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.Identification;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.api.RateLimiter;
import com.the_tinkering.wk.api.model.WaniKaniEntity;
import com.the_tinkering.wk.db.AppDatabase;
import com.the_tinkering.wk.db.Converters;
import com.the_tinkering.wk.db.model.TaskDefinition;
import com.the_tinkering.wk.enums.OnlineStatus;
import com.the_tinkering.wk.livedata.LiveApiProgress;
import com.the_tinkering.wk.livedata.LiveApiState;
import com.the_tinkering.wk.util.DbLogger;
import com.the_tinkering.wk.util.Logger;
import com.the_tinkering.wk.util.StreamUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.annotation.Nullable;
import javax.net.ssl.HttpsURLConnection;

import static com.the_tinkering.wk.Constants.HTTP_TOO_MANY_REQUESTS;
import static com.the_tinkering.wk.Constants.HTTP_UNPROCESSABLE_ENTITY;
import static com.the_tinkering.wk.Constants.MINUTE;
import static com.the_tinkering.wk.Constants.SECOND;
import static com.the_tinkering.wk.enums.OnlineStatus.NO_CONNECTION;
import static com.the_tinkering.wk.enums.OnlineStatus.NO_WIFI;
import static com.the_tinkering.wk.enums.OnlineStatus.ON_WIFI;
import static com.the_tinkering.wk.util.ObjectSupport.safe;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

/**
 * Abstract base class for background tasks. This class handles logging and exception
 * handling around the execution of the task itself, and a handful of network-related
 * helper methods.
 */
public abstract class ApiTask {
    private static final Logger LOGGER = Logger.get(ApiTask.class);

    /**
     * The task definition this invocation is defined by.
     */
    protected final TaskDefinition taskDefinition;

    /**
     * The constructor.
     *
     * @param taskDefinition the task definition this invocation is defined by
     */
    protected ApiTask(final TaskDefinition taskDefinition) {
        this.taskDefinition = taskDefinition;
    }

    /**
     * Determine the connectivity status of the device: online (on WiFi), online (not on WiFi), offline.
     *
     * @return the device's online status
     */
    public static OnlineStatus getOnlineStatus() {
        final @Nullable ConnectivityManager connMgr = (ConnectivityManager) WkApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr == null) {
            return NO_CONNECTION;
        }

        final @Nullable NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected()) {
            return ON_WIFI;
        }

        final @Nullable NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return NO_WIFI;
        }

        return NO_CONNECTION;
    }

    /**
     * Helper method to do a GET WaniKani API call and return the JSON response. Sets an API error
     * status if needed, and returns null if no JSON-parseable response body could be received for
     * any reason. Respects the API rate limits and will back off if the API signals we're going
     * too fast anyway.
     *
     * @param uri the request URI, which is either absolute (https://...) or site-relative (starts with '/')
     * @return the response body, parsed as a JSON document
     */
    private static @Nullable JsonNode getApiCall(final String uri) {
        RateLimiter.getInstance().prepare();
        final ObjectMapper mapper = Converters.getObjectMapper();
        final AppDatabase db = WkApplication.getDatabase();
        @Nullable HttpsURLConnection connection = null;
        try {
            String urlString = uri;
            if (!urlString.startsWith("https://") && !urlString.startsWith("http://")) {
                urlString = "https://api.wanikani.com" + uri;
            }
            final URL url = new URL(urlString);
            LOGGER.info("Fetching: %s", url);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + GlobalSettings.Api.getApiKey());
            connection.setRequestProperty("Wanikani-Revision", Constants.API_VERSION);
            connection.setRequestProperty("User-Agent", Identification.APP_NAME_UA + "/" + BuildConfig.VERSION_NAME);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout((int) (10 * SECOND));
            connection.setReadTimeout((int) MINUTE);
            connection.getHeaderFields();
            LOGGER.info("Response code: %d %s", connection.getResponseCode(), connection.getResponseMessage());
            try (final InputStream is = connection.getInputStream()) {
                final JsonNode value = mapper.readTree(is);
                LOGGER.info("Response body: %s", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value));
                return value;
            }
        }
        catch (final Exception e) {
            int code = 0;
            if (connection != null) {
                try {
                    code = connection.getResponseCode();
                    // Last resort attempt to log the response body in case of errors
                    if (code >= 100) {
                        LOGGER.info("Response code: %d %s", code, connection.getResponseMessage());
                        final byte[] body = StreamUtil.slurp(connection.getErrorStream());
                        LOGGER.info("Response body: %s", new String(body, "ISO-8859-1"));
                    }
                } catch (final Exception e1) {
                    //
                }
            }
            if (code == HTTP_UNAUTHORIZED) {
                // Unauthorized
                db.propertiesDao().setApiKeyRejected(true);
                db.propertiesDao().setApiInError(true);
                LiveApiState.getInstance().forceUpdate();
            }
            else if (code == HTTP_TOO_MANY_REQUESTS) {
                // Too many requests
                RateLimiter.getInstance().pause();
            }
            else {
                // Server error >= 500 or some other error
                db.propertiesDao().setApiInError(true);
                LiveApiState.getInstance().forceUpdate();
            }
            LOGGER.error(e, "API error");
            return null;
        }
    }

    /**
     * Helper method to do a POST or PUT WaniKani API call and return the JSON response. This is mostly
     * identical to getApiCall(), but the HTTP method is a parameter, as is the request body. The body
     * is sent as a JSON document, created from the requestBody object using the JSON object mapper.
     *
     * @param uri the request URI, which is either absolute (https://...) or site-relative (starts with '/')
     * @param method the HTTP method, could be any valid method but should be either POST or PUT
     * @param requestBody the request body to sent to the server
     * @return the response body, parsed as a JSON document
     */
    protected static @Nullable JsonNode postApiCall(final String uri, final String method, final Object requestBody) {
        RateLimiter.getInstance().prepare();
        final ObjectMapper mapper = Converters.getObjectMapper();
        final AppDatabase db = WkApplication.getDatabase();
        @Nullable HttpsURLConnection connection = null;
        try {
            String urlString = uri;
            if (!urlString.startsWith("https://") && !urlString.startsWith("http://")) {
                urlString = "https://api.wanikani.com" + uri;
            }
            final URL url = new URL(urlString);
            LOGGER.info("Posting: %s", url);
            LOGGER.info("Request body: %s", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestBody));
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + GlobalSettings.Api.getApiKey());
            connection.setRequestProperty("Wanikani-Revision", Constants.API_VERSION);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("User-Agent", Identification.APP_NAME_UA + "/" + BuildConfig.VERSION_NAME);
            connection.setRequestMethod(method);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout((int) (10 * SECOND));
            connection.setReadTimeout((int) MINUTE);
            try (final OutputStream os = connection.getOutputStream()) {
                mapper.writeValue(os, requestBody);
            }
            connection.getHeaderFields();
            LOGGER.info("Response code: %d %s", connection.getResponseCode(), connection.getResponseMessage());
            try (final InputStream is = connection.getInputStream()) {
                final JsonNode value = mapper.readTree(is);
                LOGGER.info("Response body: %s", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value));
                return value;
            }
        }
        catch (final Exception e) {
            int code = 0;
            if (connection != null) {
                try {
                    code = connection.getResponseCode();
                    // Last resort attempt to log the response body in case of errors
                    if (code >= 100) {
                        LOGGER.info("Response code: %d %s", code, connection.getResponseMessage());
                        final byte[] body = StreamUtil.slurp(connection.getErrorStream());
                        LOGGER.info("Response body: %s", new String(body, "ISO-8859-1"));
                    }
                } catch (final Exception e1) {
                    //
                }
            }
            if (code == HTTP_UNAUTHORIZED) {
                // Unauthorized
                db.propertiesDao().setApiKeyRejected(true);
                db.propertiesDao().setApiInError(true);
                LiveApiState.getInstance().forceUpdate();
            }
            else if (code == HTTP_TOO_MANY_REQUESTS) {
                // Too many requests
                RateLimiter.getInstance().pause();
            }
            else if (code == HTTP_UNPROCESSABLE_ENTITY) {
                // Server refuses the entity, discard the error
                LOGGER.error(e, "API error");
                return NullNode.getInstance();
            }
            else {
                // Server error >= 500 or some other error
                db.propertiesDao().setApiInError(true);
                LiveApiState.getInstance().forceUpdate();
            }
            LOGGER.error(e, "API error");
            return null;
        }
    }

    /**
     * Make a GET API call that will return a single entity in the
     * response (rather than a collection or a report). The "data" attribute of the
     * response body is deserialized to an object of the requested type.
     *
     * @param uri the request URI, which is either absolute (https://...) or site-relative (starts with '/')
     * @param cls the class to map the response entity to
     * @param <T> the type of the response entity
     * @return the parsed response or null in case of any error
     */
    protected static @Nullable <T> T singleEntityApiCall(final String uri, final Class<T> cls) {
        final AppDatabase db = WkApplication.getDatabase();
        final ObjectMapper mapper = Converters.getObjectMapper();
        try {
            final @Nullable JsonNode body = getApiCall(uri);
            if (body == null) {
                return null;
            }
            if (!body.has("data")) {
                db.propertiesDao().setApiInError(true);
                LiveApiState.getInstance().forceUpdate();
                return null;
            }
            final JsonNode data = body.get("data");
            final JsonParser parser = mapper.treeAsTokens(data);
            return mapper.readValue(parser, cls);
        } catch (final IOException e) {
            LOGGER.error(e, "API data error");
            return null;
        }
    }

    /**
     * If this is a paged response, extract the URL for the next page from the response body.
     *
     * @param body the body to examine
     * @return the next URL or null if not paged or at the last page
     */
    private static @Nullable String getNextUrl(final JsonNode body) {
        if (!body.has("pages")) {
            return null;
        }
        final JsonNode pages = body.get("pages");
        if (!pages.has("next_url")) {
            return null;
        }
        return pages.get("next_url").textValue();
    }

    /**
     * Parse an entity from an API response, respecting WK's specific representation of entities.
     * Specifically, the ID, object and data_updated_at properties are outside of the entity data
     * object itself, but we want them to be included in the entity class, if at all.
     *
     * @param body the JSON node containing the entity
     * @param cls the class to map the response entity to
     * @param <T> the type of the response entity
     * @return the parsed entity
     * @throws IOException if the mapping failed for any reason
     */
    protected static @Nullable <T extends WaniKaniEntity> T parseEntity(final JsonNode body, final Class<T> cls) throws IOException {
        final AppDatabase db = WkApplication.getDatabase();

        if (!body.has("data") || !body.has("data_updated_at") || !body.has("id") || !body.has("object")) {
            db.propertiesDao().setApiInError(true);
            LiveApiState.getInstance().forceUpdate();
            return null;
        }

        final ObjectMapper mapper = Converters.getObjectMapper();
        final JsonParser parser = mapper.treeAsTokens(body.get("data"));
        final T value = mapper.readValue(parser, cls);
        value.setId(body.get("id").asInt());
        value.setObject(body.get("object").asText());
        value.setUpdatedAt(Converters.parseDate(body.get("data_updated_at").asText()));
        return value;
    }

    /**
     * Retrieve a collection from the API, including any subsequent pages in a multi-page response.
     * Rather than returning a List result, invoke a consumer callback to handle each response entity.
     *
     * @param uri the request URI, which is either absolute (https://...) or site-relative (starts with '/')
     * @param cls the class to map the response entity to
     * @param consumer the consumer to handle each returned entity
     * @param <T> the type of the response entity
     * @return true if the entire response has been received and processed successfully. If false,
     *         some or all of the entities in the response may still have been processed.
     */
    protected static <T extends WaniKaniEntity> boolean collectionApiCall(final String uri, final Class<? extends T> cls, final Consumer<T> consumer) {
        final AppDatabase db = WkApplication.getDatabase();
        try {
            @Nullable String nextUrl = uri;
            while (nextUrl != null) {
                final @Nullable JsonNode body = getApiCall(nextUrl);
                if (body == null) {
                    return false;
                }
                if (!body.has("data")) {
                    db.propertiesDao().setApiInError(true);
                    LiveApiState.getInstance().forceUpdate();
                    return false;
                }
                final JsonNode data = body.get("data");
                LiveApiProgress.addEntities(data.size());
                for (final JsonNode element: data) {
                    consumer.accept(parseEntity(element, cls));
                    LiveApiProgress.addProcessedEntity();
                }
                nextUrl = getNextUrl(body);
            }
            return true;
        } catch (final Exception e) {
            LOGGER.error(e, "API data error");
            return false;
        }
    }

    /**
     * Download a file. This is just a straight GET request, and the response is stored in a file.
     * The download initially writes to a temporary file, and if the download was successful, then
     * the temporary file is renamed to the final name. This prevents partial downloads from
     * lingering.
     *
     * @param urlString the absolute URL to fetch
     * @param tempFile the temporary file to write to while downloading, must be on the same filesystem as outputFile.
     * @param outputFile the eventual location for the downloaded file.
     */
    protected static void downloadFile(final String urlString, final File tempFile, final File outputFile) {
        try {
            final URL url = new URL(urlString);
            LOGGER.info("Download: %s", url);
            final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout((int) (10 * SECOND));
            connection.setReadTimeout((int) MINUTE);
            connection.getHeaderFields();
            LOGGER.info("Response code: %d %s", connection.getResponseCode(), connection.getResponseMessage());
            try (final InputStream is = connection.getInputStream(); final OutputStream os = new FileOutputStream(tempFile)) {
                StreamUtil.pump(is, os);
                //noinspection ResultOfMethodCallIgnored
                tempFile.renameTo(outputFile);
            }
        }
        catch (final Exception e) {
            LOGGER.error(e, "Download error");
        }
    }

    /**
     * Run this task, including logging and exception handling.
     */
    public final void run() {
        LOGGER.info("%s started with data: %s", DbLogger.getSimpleClassName(getClass()), taskDefinition.getData());
        safe(() -> {
            LiveApiProgress.reset(false, "");
            runLocal();
        });
        LiveApiProgress.reset(false, "");
        LOGGER.info("%s finished", DbLogger.getSimpleClassName(getClass()));
    }

    /**
     * Check if the current network circumstances and API status allow this task to run right now.
     *
     * @return true if it can run now
     */
    public abstract boolean canRun();

    /**
     * The actual implementation of the task. This method is responsible for deleting its
     * task definition if it doesn't need to retried later.
     */
    protected abstract void runLocal();
}
