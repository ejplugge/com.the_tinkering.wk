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

package com.the_tinkering.wk.adapter.search;

import androidx.core.util.Consumer;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.db.model.Subject;

import javax.annotation.Nullable;

/**
 * Item for subject type headers.
 */
public final class SubjectItem extends ResultItem {
    private final Subject subject;

    /**
     * The constructor.
     *
     * @param subject the subject for this item
     */
    public SubjectItem(final Subject subject) {
        this.subject = subject;
    }

    /**
     * The subject for this item.
     *
     * @return the subject
     */
    public Subject getSubject() {
        return subject;
    }

    @Override
    public int getViewType() {
        if (subject.getType().isRadical()) {
            return R.id.viewTypeRadical;
        }
        if (subject.getType().isKanji()) {
            return R.id.viewTypeKanji;
        }
        return R.id.viewTypeVocabulary;
    }

    @Override
    public int getSpanSize(final int spans) {
        if (subject.getType().isRadical()) {
            return 1;
        }
        if (subject.getType().isKanji()) {
            return 1;
        }
        return spans >= 6 ? 3 : spans;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public @Nullable ResultItem getItem(final int position) {
        return position == 0 ? this : null;
    }

    @Override
    public void iterateSubjects(final Consumer<? super Subject> consumer) {
        consumer.accept(subject);
    }
}
