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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.the_tinkering.wk.db.Converters;

import java.io.IOException;
import java.util.Date;

import javax.annotation.Nullable;

/**
 * Custom serializer for dates, hardcoded for the format used in the WaniKani API.
 * This the serializing counterpart to WaniKaniApiDateDeserializer.
 */
public final class WaniKaniApiDateSerializer extends StdSerializer<Date> {
    /**
     * For serialization.
     */
    private static final long serialVersionUID = -7517817866239623130L;

    /**
     * The constructor.
     */
    @SuppressWarnings("unused")
    public WaniKaniApiDateSerializer() {
        super(Date.class);
    }

    @Override
    public void serialize(final @Nullable Date value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        gen.writeString(Converters.formatDate(value));
    }
}
