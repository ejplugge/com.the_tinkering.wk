/*
 * Copyright 2019-2022 Ernst Jan Plugge <rmc@dds.nl>
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

package com.the_tinkering.wk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.db.model.AudioDownloadStatus;
import com.the_tinkering.wk.jobs.StartAudioDownloadJob;
import com.the_tinkering.wk.livedata.LiveTaskCounts;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.services.JobRunnerService;

import java.util.Locale;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * A custom view that shows a bracket of levels in the audio download overview.
 */
public final class DownloadAudioBracketView extends LinearLayout {
    private final ViewProxy label = new ViewProxy();
    private final ViewProxy rangeLabel = new ViewProxy();
    private final ViewProxy downloadButton = new ViewProxy();

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public DownloadAudioBracketView(final Context context) {
        super(context);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public DownloadAudioBracketView(final Context context, final @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     * @param defStyleAttr the default style
     */
    public DownloadAudioBracketView(final Context context, final @Nullable AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initialize the view.
     */
    private void init() {
        safe(() -> {
            inflate(getContext(), R.layout.download_audio_bracket, this);
            setOrientation(VERTICAL);
            setPadding(0, 0, 0, 0);
            label.setDelegate(this, R.id.label);
            rangeLabel.setDelegate(this, R.id.rangeLabel);
            downloadButton.setDelegate(this, R.id.downloadButton);
        });
    }

    /**
     * Set the bracket for this view.
     *
     * @param overview the overall overview
     * @param minLevel the lowest level in the bracket
     * @param maxLevel the highest level in the bracket
     */
    public void setBracket(final Iterable<AudioDownloadStatus> overview, final int minLevel, final int maxLevel) {
        safe(() -> {
            final int audioCount = LiveTaskCounts.getInstance().get().getAudioCount();

            int numTotal = 0;
            int numNoAudio = 0;
            int numMissingAudio = 0;
            int numPartialAudio = 0;
            int numFullAudio = 0;

            for (final AudioDownloadStatus status: overview) {
                if (status.getLevel() < minLevel || status.getLevel() > maxLevel) {
                    continue;
                }
                numTotal += status.getNumTotal();
                numNoAudio += status.getNumNoAudio();
                numMissingAudio += status.getNumMissingAudio();
                numPartialAudio += status.getNumPartialAudio();
                numFullAudio += status.getNumFullAudio();
            }

            if (audioCount > 0 || numMissingAudio == 0 && numPartialAudio == 0) {
                downloadButton.disableInteraction();
            }
            else {
                downloadButton.enableInteraction();
            }

            downloadButton.setOnClickListener(v -> safe(() -> JobRunnerService.schedule(StartAudioDownloadJob.class,
                    String.format(Locale.ROOT, "%d|%d", minLevel, maxLevel))));

            rangeLabel.setTextFormat("Levels %d-%d", minLevel, maxLevel);

            if (numMissingAudio == 0 && numPartialAudio == 0) {
                label.setText("Download finished");
            }
            else {
                label.setTextFormat("%d/%d available, %d partially done",
                        numPartialAudio + numFullAudio,
                        numTotal - numNoAudio,
                        numPartialAudio);
            }
        });
    }
}
