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
package com.github.harbby.gadtry.graph.layout;

import com.github.harbby.gadtry.graph.export.Direction;

import static java.util.Objects.requireNonNull;

/**
 * Geometry used by a {@link GraphLayout}. All values are in pixels.
 * <p>
 * {@code xStep}/{@code yStep} are the distances between the origins of two neighbouring nodes:
 * along the flow direction they separate ranks, across it they separate nodes of the same rank.
 */
public final class LayoutOptions
{
    private final Direction direction;
    private final double nodeWidth;
    private final double nodeHeight;
    private final double xStep;
    private final double yStep;

    public LayoutOptions(Direction direction, double nodeWidth, double nodeHeight, double xStep, double yStep)
    {
        this.direction = requireNonNull(direction, "direction is null");
        this.nodeWidth = checkPositive(nodeWidth, "nodeWidth");
        this.nodeHeight = checkPositive(nodeHeight, "nodeHeight");
        this.xStep = checkPositive(xStep, "xStep");
        this.yStep = checkPositive(yStep, "yStep");
    }

    private static double checkPositive(double value, String name)
    {
        if (!(value > 0) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(name + " must be a positive number, got " + value);
        }
        return value;
    }

    public Direction getDirection()
    {
        return direction;
    }

    public double getNodeWidth()
    {
        return nodeWidth;
    }

    public double getNodeHeight()
    {
        return nodeHeight;
    }

    public double getXStep()
    {
        return xStep;
    }

    public double getYStep()
    {
        return yStep;
    }

    /**
     * Distance between two consecutive ranks along the flow direction.
     */
    public double rankStep()
    {
        return direction == Direction.LEFT_TO_RIGHT ? xStep : yStep;
    }

    /**
     * Distance between two neighbouring nodes of the same rank.
     */
    public double crossStep()
    {
        return direction == Direction.LEFT_TO_RIGHT ? yStep : xStep;
    }

    @Override
    public String toString()
    {
        return "LayoutOptions{direction=" + direction + ", nodeWidth=" + nodeWidth + ", nodeHeight=" + nodeHeight
                + ", xStep=" + xStep + ", yStep=" + yStep + "}";
    }
}
