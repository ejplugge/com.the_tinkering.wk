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

package com.the_tinkering.wk.model;

/**
 * A model for one item in the JLPT progress.
 */
@SuppressWarnings("JavaDoc")
public final class JoyoProgressItem {
    public long srsSystemId = 0;
    public long srsStage = 0;
    public int joyoGrade = 0;
    public int count = 0;
}
