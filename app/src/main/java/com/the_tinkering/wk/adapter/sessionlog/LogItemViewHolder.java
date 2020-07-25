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

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Abstract base class for view holders for session log items.
 */
public abstract class LogItemViewHolder extends RecyclerView.ViewHolder {
    /**
     * The adapter this holder was created for.
     */
    protected final SessionLogAdapter adapter;

    /**
     * The constructor.
     *
     * @param adapter the adapter this holder was created for
     * @param view the view for this holder
     */
    protected LogItemViewHolder(final SessionLogAdapter adapter, final View view) {
        super(view);
        this.adapter = adapter;
    }

    /**
     * Bind the given item to this holder.
     *
     * @param newItem the new item to bind
     */
    public abstract void bind(LogItem newItem);
}
