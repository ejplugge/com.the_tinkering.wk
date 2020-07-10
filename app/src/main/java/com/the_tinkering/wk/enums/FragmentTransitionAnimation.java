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

package com.the_tinkering.wk.enums;

import androidx.fragment.app.FragmentTransaction;

import com.the_tinkering.wk.R;

/**
 * The type of fragment transition animation to use in the SessionActivity.
 */
public enum FragmentTransitionAnimation {
    /**
     * No animation, just a static replace.
     */
    NONE {
        @Override
        public void apply(final FragmentTransaction transaction) {
            //
        }
    },

    /**
     * Slide in from left to right.
     */
    LTR {
        @Override
        public void apply(final FragmentTransaction transaction) {
            transaction.setCustomAnimations(R.anim.ltr_enter, R.anim.ltr_exit);
        }
    },

    /**
     * Slide in from right to left.
     */
    RTL {
        @Override
        public void apply(final FragmentTransaction transaction) {
            transaction.setCustomAnimations(R.anim.rtl_enter, R.anim.rtl_exit);
        }
    };

    /**
     * Apply the chosen animation to a fragment transation.
     *
     * @param transaction the transaction to modify
     */
    public abstract void apply(FragmentTransaction transaction);
}
