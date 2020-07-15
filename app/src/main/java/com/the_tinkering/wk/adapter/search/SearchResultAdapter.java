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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.enums.SearchSortOrder;
import com.the_tinkering.wk.enums.SubjectType;
import com.the_tinkering.wk.fragments.SearchResultFragment;
import com.the_tinkering.wk.model.AdvancedSearchParameters;
import com.the_tinkering.wk.model.SubjectCardBinder;
import com.the_tinkering.wk.util.Logger;
import com.the_tinkering.wk.util.WeakLcoRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * RecyclerView adapter for advanced search results.
 */
public final class SearchResultAdapter extends RecyclerView.Adapter<ResultItemViewHolder> {
    private static final Logger LOGGER = Logger.get(SearchResultAdapter.class);

    private final WeakLcoRef<SearchResultFragment> fragmentRef;
    private final RootItem rootItem = new RootItem();
    private SearchSortOrder sortOrder = SearchSortOrder.TYPE;
    private int numSubjects = 0;
    private long searchTime = 0;
    private Collection<String> collapsedTags = new HashSet<>();
    private boolean showingForm = false;
    private @Nullable AdvancedSearchParameters parameters = null;
    private final SubjectCardBinder binder = new SubjectCardBinder();

    /**
     * The constructor.
     * @param fragment the fragment this adapter belongs to
     */
    public SearchResultAdapter(final SearchResultFragment fragment) {
        fragmentRef = new WeakLcoRef<>(fragment);
        setHasStableIds(false);
    }

    /**
     * The sort order to apply.
     *
     * @param sortOrder the order
     */
    public void setSortOrder(final SearchSortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * The number of subjects currently contained in the adapter.
     * Not the same as the number of items, since this doesn't count non-subject items like headers.
     *
     * @return the number
     */
    public int getNumSubjects() {
        return numSubjects;
    }

    /**
     * The tags of headers that are collapsed.
     *
     * @return the tags
     */
    public Collection<String> getCollapsedTags() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return collapsedTags;
    }

    /**
     * The tags of headers that are collapsed.
     *
     * @param collapsedTags the tags
     */
    public void setCollapsedTags(final Collection<String> collapsedTags) {
        this.collapsedTags = new HashSet<>(collapsedTags);
    }

    /**
     * Is the adapter showing an item for the search form?.
     *
     * @return true if it is
     */
    public boolean isShowingForm() {
        return showingForm;
    }

    /**
     * Is the adapter showing an item for the search form?.
     *
     * @param showingForm true if it is
     * @param layoutManager the current layout manager in use
     */
    public void setShowingForm(final boolean showingForm, final @Nullable RecyclerView.LayoutManager layoutManager) {
        if (this.showingForm && !showingForm) {
            this.showingForm = false;
            notifyItemRemoved(0);
        }
        if (!this.showingForm && showingForm) {
            this.showingForm = true;
            notifyItemInserted(0);
            if (layoutManager != null) {
                layoutManager.scrollToPosition(0);
            }
        }
    }

    /**
     * Set the search parameters for this search, if this was an advanced search.
     *
     * @param parameters the parameters
     */
    public void setParameters(final AdvancedSearchParameters parameters) {
        this.parameters = parameters;
    }

    private @Nullable ResultItem getItem(final int position) {
        if (showingForm) {
            if (position == 0) {
                return new SearchFormItem();
            }
            return rootItem.getItem(position - 1);
        }
        return rootItem.getItem(position);
    }

    @Override
    public ResultItemViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        try {
            switch (viewType) {
                case R.id.viewTypeRadical: {
                    final View view = binder.createView(SubjectType.WANIKANI_RADICAL, parent);
                    return new SubjectItemViewHolder(this, view, binder, fragmentRef.get());
                }
                case R.id.viewTypeKanji: {
                    final View view = binder.createView(SubjectType.WANIKANI_KANJI, parent);
                    return new SubjectItemViewHolder(this, view, binder, fragmentRef.get());
                }
                case R.id.viewTypeVocabulary: {
                    final View view = binder.createView(SubjectType.WANIKANI_VOCAB, parent);
                    return new SubjectItemViewHolder(this, view, binder, fragmentRef.get());
                }
                case R.id.viewTypeSearchForm: {
                    final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    final View view = inflater.inflate(R.layout.search_result_form, parent, false);
                    return new SearchFormItemViewHolder(this, view, requireNonNull(parameters), fragmentRef.get());
                }
                case R.id.viewTypeItemTypeHeader: {
                    final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    final int id = sortOrder.isSingleLevel() ? R.layout.search_result_top_header : R.layout.search_result_sub_header;
                    final View view = inflater.inflate(id, parent, false);
                    return new ItemTypeHeaderItemViewHolder(this, view);
                }
                case R.id.viewTypeLevelHeader: {
                    final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    final View view = inflater.inflate(R.layout.search_result_top_header, parent, false);
                    return new LevelHeaderItemViewHolder(this, view);
                }
                case R.id.viewTypeAvailableAtHeader: {
                    final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    final View view = inflater.inflate(R.layout.search_result_top_header, parent, false);
                    return new AvailableAtHeaderItemViewHolder(this, view, searchTime);
                }
                case R.id.viewTypeSrsStageHeader: {
                    final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    final View view = inflater.inflate(R.layout.search_result_top_header, parent, false);
                    return new SrsStageHeaderItemViewHolder(this, view);
                }
                default:
            }
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
        return new DummyViewHolder(this, new AppCompatTextView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(final ResultItemViewHolder holder, final int position) {
        try {
            final @Nullable ResultItem item = getItem(position);
            if (item != null) {
                holder.bind(item);
            }
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    @Override
    public int getItemCount() {
        try {
            int count = rootItem.getCount();
            if (showingForm) {
                count++;
            }
            return count;
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
        return 0;
    }

    @Override
    public int getItemViewType(final int position) {
        try {
            final @Nullable ResultItem item = getItem(position);
            if (item == null) {
                return 0;
            }
            return item.getViewType();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
        return 0;
    }

    /**
     * Get the size of an item in spans (columns).
     *
     * @param position the position of the item
     * @param spans the total number of spans in the layout manager
     * @return the span size
     */
    public int getItemSpanSize(final int position, final int spans) {
        try {
            final @Nullable ResultItem item = getItem(position);
            if (item == null) {
                return 1;
            }
            return item.getSpanSize(spans);
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
        return 0;
    }

    /**
     * Replace the current result set with a new one.
     *
     * @param result the search result
     */
    public void setResult(final List<Subject> result) {
        rootItem.clear();
        searchTime = System.currentTimeMillis();
        final Comparator<Subject> comparator = sortOrder.getComparator(searchTime);
        Collections.sort(result, comparator);
        final int size = result.size();
        int start = 0;
        while (start < size) {
            final Subject startSubject = result.get(start);
            int end = start + 1;
            while (end < size && comparator.compare(startSubject, result.get(end)) == 0) {
                end++;
            }
            final HeaderItem header;
            if (sortOrder.isSingleLevel()) {
                header = SearchSortOrder.createSubLevelHeaderItem(null, startSubject);
                if (collapsedTags.contains(header.getTag())) {
                    header.setCollapsed(true);
                }
                rootItem.addItem(header);
            }
            else {
                @Nullable HeaderItem topLevelHeader = rootItem.findHeaderItem(sortOrder.getTopLevelTag(startSubject, searchTime));
                if (topLevelHeader == null) {
                    topLevelHeader = sortOrder.createTopLevelHeaderItem(startSubject, searchTime);
                    rootItem.addItem(topLevelHeader);
                }
                if (collapsedTags.contains(topLevelHeader.getTag())) {
                    topLevelHeader.setCollapsed(true);
                }
                header = SearchSortOrder.createSubLevelHeaderItem(topLevelHeader.getTag(), startSubject);
                if (collapsedTags.contains(header.getTag())) {
                    header.setCollapsed(true);
                }
                topLevelHeader.addItem(header);
            }
            for (int i=start; i<end; i++) {
                header.addItem(new SubjectItem(result.get(i)));
            }
            start = end;
        }
        numSubjects = size;
        notifyDataSetChanged();
    }

    /**
     * Get the subjects in this adapter, in view order.
     *
     * @return the subjects
     */
    public List<Subject> getSubjects() {
        final List<Subject> result = new ArrayList<>();
        rootItem.iterateSubjects(result::add);
        return result;
    }

    /**
     * Get an array of subject IDs for this result in the order shown.
     *
     * @return the IDs
     */
    public long[] getSubjectIds() {
        final long[] result = new long[numSubjects];
        rootItem.iterateSubjects(new Consumer<Subject>() {
            private int index = 0;
            @Override
            public void accept(final Subject t) {
                if (index < result.length) {
                    result[index++] = t.getId();
                }
            }
        });
        return result;
    }

    /**
     * Get an array of subject IDs that are resurrectable.
     *
     * @return the IDs
     */
    public long[] getResurrectableSubjectIds() {
        final List<Subject> subjects = new ArrayList<>();
        rootItem.iterateSubjects(t -> {
            if (t.isResurrectable()) {
                subjects.add(t);
            }
        });
        final long[] result = new long[subjects.size()];
        for (int i=0; i<result.length; i++) {
            result[i] = subjects.get(i).getId();
        }
        return result;
    }

    /**
     * Get an array of subject IDs that are burnable.
     *
     * @return the IDs
     */
    public long[] getBurnableSubjectIds() {
        final List<Subject> subjects = new ArrayList<>();
        rootItem.iterateSubjects(t -> {
            if (t.isBurnable()) {
                subjects.add(t);
            }
        });
        final long[] result = new long[subjects.size()];
        for (int i=0; i<result.length; i++) {
            result[i] = subjects.get(i).getId();
        }
        return result;
    }
}
