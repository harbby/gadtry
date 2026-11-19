/*
 * Copyright (C) 2018 The GadTry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.harbby.gadtry.graph.export;

/**
 * Main flow direction of a rendered graph.
 */
public enum Direction
{
    LEFT_TO_RIGHT("LR"),
    TOP_TO_BOTTOM("TB");

    private final String code;

    Direction(String code)
    {
        this.code = code;
    }

    /**
     * @return the short form shared by graphviz {@code rankdir} and mermaid: {@code LR} or {@code TB}
     */
    public String code()
    {
        return code;
    }
}
