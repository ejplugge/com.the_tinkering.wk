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

package com.the_tinkering.wk.components;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.the_tinkering.wk.db.Converters;

import java.io.IOException;
import java.util.Date;

import javax.annotation.Nullable;

/**
 * Custom deserializer for dates, hardcoded for the format used in the WaniKani API.
 * This is used instead of a simple dateformat since date parsing is done incorrectly
 * on some old Android versions. Specifically, Android 4.1 in the emulator.
 *
 * <p>
 *     Since I couldn't figure out why the parsing was incorrect, I decided to just
 *     sidestep the problem by making a simple custom parser.
 * </p>
 */
public final class WaniKaniApiDateDeserializer extends StdDeserializer<Date> {
    /**
     * For serialization.
     */
    private static final long serialVersionUID = -477653136384621832L;

    /**
     * The constructor.
     */
    @SuppressWarnings("unused")
    public WaniKaniApiDateDeserializer() {
        super(Date.class);
    }

    @Override
    public @Nullable Date deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
        if (p.hasToken(JsonToken.VALUE_STRING)) {
            return Converters.parseDate(p.getText().trim());
        }
        return null;
    }
}
