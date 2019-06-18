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
package com.github.harbby.gadtry.graph;

import java.util.function.Function;

public class SearchContext<N, E>
{
    private final Function<Route<N, E>, Boolean> nextRule;
    private final Function<SearchContext<N, E>, Boolean> globalRule;
    private final long searchStartTime = System.currentTimeMillis();

    private Route<N, E> lastRoute;
    private int number = 0;

    public SearchContext(
            Function<Route<N, E>, Boolean> nextRule,
            Function<SearchContext<N, E>, Boolean> globalRule)
    {
        this.nextRule = nextRule;
        this.globalRule = globalRule;
    }

    void setLastRoute(Route<N, E> lastRoute)
    {
        this.lastRoute = lastRoute;
        this.number++;
    }

    public Route<N, E> getLastRoute()
    {
        return lastRoute;
    }

    public long getSearchStartTime()
    {
        return searchStartTime;
    }

    public long getSearchTime()
    {
        return System.currentTimeMillis() - searchStartTime;
    }

    public int getFindNodeNumber()
    {
        return number;
    }

    public Function<Route<N, E>, Boolean> getNextRule()
    {
        return nextRule;
    }

    public Function<SearchContext<N, E>, Boolean> getGlobalRule()
    {
        return globalRule;
    }
}
