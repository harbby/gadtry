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
package com.github.harbby.gadtry.jcodec.codecs;

import com.github.harbby.gadtry.jcodec.InputView;
import com.github.harbby.gadtry.jcodec.OutputView;
import com.github.harbby.gadtry.jcodec.Serializer;

import java.util.Arrays;
import java.util.Comparator;

public class ArraySerializers
{
    private ArraySerializers() {}

    public static class ByteArraySerializer
            implements Serializer<byte[]>
    {
        private static final byte[] zeroArr = new byte[0];

        @Override
        public void write(OutputView output, byte[] values)
        {
            if (values == null) {
                output.writeVarInt(0, false);
                return;
            }
            output.writeVarInt(values.length + 1, false);
            for (byte e : values) {
                output.writeByte(e);
            }
        }

        @Override
        public byte[] read(InputView input)
        {
            int len = input.readVarInt(false) - 1;
            if (len == -1) {
                return null;
            }
            if (len == 0) {
                return zeroArr;
            }
            byte[] values = new byte[len];
            for (int i = 0; i < len; i++) {
                values[i] = input.readByte();
            }
            return values;
        }

        @Override
        public Comparator<byte[]> comparator()
        {
            return Arrays::compare;
        }
    }

    public static class BooleanArraySerializer
            implements Serializer<boolean[]>
    {
        private static final boolean[] zeroArr = new boolean[0];

        @Override
        public void write(OutputView output, boolean[] values)
        {
            if (values == null) {
                output.writeVarInt(0, false);
                return;
            }
            output.writeVarInt(values.length + 1, false);
            if (values.length > 0) {
                output.writeBoolArray(values);
            }
        }

        @Override
        public boolean[] read(InputView input)
        {
            int len = input.readVarInt(false) - 1;
            if (len == -1) {
                return null;
            }
            if (len == 0) {
                return zeroArr;
            }
            boolean[] values = new boolean[len];
            input.readBoolArray(values, 0, len);
            return values;
        }

        @Override
        public Comparator<boolean[]> comparator()
        {
            return Arrays::compare;
        }
    }

    public static class ShortArraySerializer
            implements Serializer<short[]>
    {
        private static final short[] zeroArr = new short[0];

        @Override
        public void write(OutputView output, short[] values)
        {
            if (values == null) {
                output.writeVarInt(0, false);
                return;
            }
            output.writeVarInt(values.length + 1, false);
            for (short e : values) {
                output.writeShort(e);
            }
        }

        @Override
        public short[] read(InputView input)
        {
            int len = input.readVarInt(false) - 1;
            if (len == -1) {
                return null;
            }
            if (len == 0) {
                return zeroArr;
            }
            short[] values = new short[len];
            for (int i = 0; i < len; i++) {
                values[i] = input.readShort();
            }
            return values;
        }

        @Override
        public Comparator<short[]> comparator()
        {
            return Arrays::compare;
        }
    }

    public static class CharArraySerializer
            implements Serializer<char[]>
    {
        private static final char[] zeroIntArr = new char[0];

        @Override
        public void write(OutputView output, char[] values)
        {
            if (values == null) {
                output.writeVarInt(0, false);
                return;
            }
            output.writeVarInt(values.length + 1, false);
            for (char e : values) {
                output.writeChar(e);
            }
        }

        @Override
        public char[] read(InputView input)
        {
            int len = input.readVarInt(false) - 1;
            if (len == -1) {
                return null;
            }
            if (len == 0) {
                return zeroIntArr;
            }
            char[] values = new char[len];
            for (int i = 0; i < len; i++) {
                values[i] = input.readChar();
            }
            return values;
        }

        @Override
        public Comparator<char[]> comparator()
        {
            return Arrays::compare;
        }
    }

    public static class IntArraySerializer
            implements Serializer<int[]>
    {
        private static final int[] zeroArr = new int[0];

        @Override
        public void write(OutputView output, int[] values)
        {
            if (values == null) {
                output.writeVarInt(0, false);
                return;
            }
            output.writeVarInt(values.length + 1, false);
            for (int e : values) {
                output.writeInt(e);
            }
        }

        @Override
        public int[] read(InputView input)
        {
            int len = input.readVarInt(false) - 1;
            if (len == -1) {
                return null;
            }
            if (len == 0) {
                return zeroArr;
            }
            int[] values = new int[len];
            for (int i = 0; i < len; i++) {
                values[i] = input.readInt();
            }
            return values;
        }

        @Override
        public Comparator<int[]> comparator()
        {
            return Arrays::compare;
        }
    }

    public static class FloatArraySerializer
            implements Serializer<float[]>
    {
        private static final float[] zeroArr = new float[0];

        @Override
        public void write(OutputView output, float[] values)
        {
            if (values == null) {
                output.writeVarInt(0, false);
                return;
            }
            output.writeVarInt(values.length + 1, false);
            for (float e : values) {
                output.writeFloat(e);
            }
        }

        @Override
        public float[] read(InputView input)
        {
            int len = input.readVarInt(false) - 1;
            if (len == -1) {
                return null;
            }
            if (len == 0) {
                return zeroArr;
            }
            float[] values = new float[len];
            for (int i = 0; i < len; i++) {
                values[i] = input.readByte();
            }
            return values;
        }

        @Override
        public Comparator<float[]> comparator()
        {
            return Arrays::compare;
        }
    }

    public static class LongArraySerializer
            implements Serializer<long[]>
    {
        private static final long[] zeroArr = new long[0];

        @Override
        public void write(OutputView output, long[] values)
        {
            if (values == null) {
                output.writeVarInt(0, false);
                return;
            }
            output.writeVarInt(values.length + 1, false);
            for (long e : values) {
                output.writeLong(e);
            }
        }

        @Override
        public long[] read(InputView input)
        {
            int len = input.readVarInt(false) - 1;
            if (len == -1) {
                return null;
            }
            if (len == 0) {
                return zeroArr;
            }
            long[] values = new long[len];
            for (int i = 0; i < len; i++) {
                values[i] = input.readInt();
            }
            return values;
        }

        @Override
        public Comparator<long[]> comparator()
        {
            return Arrays::compare;
        }
    }

    public static class DoubleArraySerializer
            implements Serializer<double[]>
    {
        private static final double[] zeroArr = new double[0];

        @Override
        public void write(OutputView output, double[] values)
        {
            if (values == null) {
                output.writeVarInt(0, false);
                return;
            }
            output.writeVarInt(values.length + 1, false);
            for (double e : values) {
                output.writeDouble(e);
            }
        }

        @Override
        public double[] read(InputView input)
        {
            int len = input.readVarInt(false) - 1;
            if (len == -1) {
                return null;
            }
            if (len == 0) {
                return zeroArr;
            }
            double[] values = new double[len];
            for (int i = 0; i < len; i++) {
                values[i] = input.readInt();
            }
            return values;
        }

        @Override
        public Comparator<double[]> comparator()
        {
            return Arrays::compare;
        }
    }
}
