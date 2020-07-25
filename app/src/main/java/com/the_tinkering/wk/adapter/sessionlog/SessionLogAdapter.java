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

package com.the_tinkering.wk.adapter.sessionlog;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.the_tinkering.wk.Actment;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.db.model.SessionItem;
import com.the_tinkering.wk.enums.SessionType;
import com.the_tinkering.wk.model.Session;
import com.the_tinkering.wk.util.WeakLcoRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.safe;
import static java.util.Objects.requireNonNull;

/**
 * RecyclerView adapter for advanced search results.
 */
public final class SessionLogAdapter extends RecyclerView.Adapter<LogItemViewHolder> {
    private @Nullable WeakLcoRef<Actment> actmentRef = null;
    private final RootItem rootItem = new RootItem();
    private final Collection<String> collapsedTags = new HashSet<>();
    private final List<EventItem> events = new ArrayList<>();

    /**
     * The constructor.
     */
    public SessionLogAdapter() {
        setHasStableIds(false);
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

    private @Nullable LogItem getItem(final int position) {
        return rootItem.getItem(position);
    }

    @Override
    public LogItemViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return safe(() -> new DummyViewHolder(this, new AppCompatTextView(parent.getContext())), () -> {
            switch (viewType) {
                case R.id.viewTypeLogHeader: {
                    final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    final View view = inflater.inflate(R.layout.session_log_header, parent, false);
                    return new HeaderItemViewHolder(this, view);
                }
                case R.id.viewTypeLogItem: {
                    final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    final View view = inflater.inflate(R.layout.session_log_item, parent, false);
                    return new ItemItemViewHolder(this, view, requireNonNull(actmentRef).get());
                }
                case R.id.viewTypeLogEvent: {
                    final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    final View view = inflater.inflate(R.layout.session_log_event, parent, false);
                    return new EventItemViewHolder(this, view, requireNonNull(actmentRef).get());
                }
                default: {
                    return new DummyViewHolder(this, new AppCompatTextView(parent.getContext()));
                }
            }
        });
    }

    @Override
    public void onBindViewHolder(final LogItemViewHolder holder, final int position) {
        safe(() -> {
            final @Nullable LogItem item = getItem(position);
            if (item != null) {
                holder.bind(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return safe(0, rootItem::getCount);
    }

    @Override
    public int getItemViewType(final int position) {
        return safe(0, () -> {
            final @Nullable LogItem item = getItem(position);
            if (item == null) {
                return 0;
            }
            return item.getViewType();
        });
    }

    /**
     * Get the size of an item in spans (columns).
     *
     * @param position the position of the item
     * @param spans the total number of spans in the layout manager
     * @return the span size
     */
    public int getItemSpanSize(final int position, final int spans) {
        return safe(0, () -> {
            final @Nullable LogItem item = getItem(position);
            if (item == null) {
                return 1;
            }
            return item.getSpanSize(spans);
        });
    }

    /**
     * Set the owner actment of the recyclerview for UI callbacks.
     *
     * @param actment the actment
     */
    public void setActment(final Actment actment) {
        actmentRef = new WeakLcoRef<>(actment);
    }

    /**
     * Initialize the adapter with the current list of session items.
     */
    @SuppressLint("NewApi")
    public void initialize() {
        rootItem.clear();
        final List<SessionItem> items = Session.getInstance().getItems();

        final ItemHeaderItem abandonedHeader = new ItemHeaderItem("abandoned", collapsedTags.contains("abandoned"), "Abandoned items");
        items.stream().filter(SessionItem::isAbandoned).forEach(item -> abandonedHeader.addItem(new ItemItem(item)));
        if (!abandonedHeader.isEmpty()) {
            rootItem.addItem(abandonedHeader);
        }

        final ItemHeaderItem completedHeader = new ItemHeaderItem("completed", collapsedTags.contains("completed"), "Completed items");
        items.stream().filter(item -> item.isPending() || item.isReported()).forEach(item -> completedHeader.addItem(new ItemItem(item)));
        if (!completedHeader.isEmpty()) {
            rootItem.addItem(completedHeader);
        }

        final ItemHeaderItem startedHeader = new ItemHeaderItem("started", collapsedTags.contains("started"), "Started items");
        items.stream().filter(SessionItem::isStarted).forEach(item -> startedHeader.addItem(new ItemItem(item)));
        if (!startedHeader.isEmpty()) {
            rootItem.addItem(startedHeader);
        }

        final ItemHeaderItem notStartedHeader = new ItemHeaderItem("notstarted", collapsedTags.contains("notstarted"), "Not-started items");
        items.stream().filter(item -> item.isActive() && !item.isStarted()).forEach(item -> notStartedHeader.addItem(new ItemItem(item)));
        if (!notStartedHeader.isEmpty()) {
            rootItem.addItem(notStartedHeader);
        }

        final EventHeaderItem eventHeader = new EventHeaderItem("events", collapsedTags.contains("events"), "Events", events);
        if (!eventHeader.isEmpty()) {
            rootItem.addItem(eventHeader);
        }

        notifyDataSetChanged();
    }

    /**
     * Clear the state for a new session.
     */
    public void clear() {
        rootItem.clear();
        events.clear();
        notifyDataSetChanged();
    }

    /*
     * Methods to add various types of events to the adapter.
     */

    @SuppressWarnings("JavaDoc")
    public void addEventStartSession(final SessionType type) {
        final String text = String.format(Locale.ROOT, "%s session started", type.getDescription());
        events.add(0, new EventItem(null, text));
    }

    @SuppressWarnings("JavaDoc")
    public void addEventLoadSession(final SessionType type) {
        final String text = String.format(Locale.ROOT, "%s session re-loaded on app startup", type.getDescription());
        events.add(0, new EventItem(null, text));
    }
}
