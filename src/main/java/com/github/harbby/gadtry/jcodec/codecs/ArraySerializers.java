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

import com.github.harbby.gadtry.base.Platform;
import com.github.harbby.gadtry.jcodec.InputView;
import com.github.harbby.gadtry.jcodec.Jcodec;
import com.github.harbby.gadtry.jcodec.OutputView;
import com.github.harbby.gadtry.jcodec.Serializer;

import java.util.Comparator;

public class ArraySerializers
{
    private ArraySerializers() {}

    public static class ByteArraySerializer
            implements Serializer<byte[]>
    {
        private static final byte[] zeroArr = new byte[0];

        @Override
        public boolean isNullable()
        {
            return true;
        }

        @Override
        public void write(Jcodec jcodec, OutputView output, byte[] values)
        {
            if (values == null) {
                output.writeVarInt(0, true);
                return;
            }
            output.writeVarInt(values.length + 1, true);
            for (byte e : values) {
                output.writeByte(e);
            }
        }

        @Override
        public byte[] read(Jcodec jcodec, InputView input, Class<? extends byte[]> typeClass)
        {
            int len = input.readVarInt(true);
            if (len == 0) {
                return null;
            }
            else if (len == 1) {
                return zeroArr;
            }
            else {
                len--;
                byte[] values = new byte[len];
                for (int i = 0; i < len; i++) {
                    values[i] = input.readByte();
                }
                return values;
            }
        }

        @Override
        public Comparator<byte[]> comparator()
        {
            return Platform.getArrayComparator(byte[].class);
        }
    }

    public static class BooleanArraySerializer
            implements Serializer<boolean[]>
    {
        private static final boolean[] zeroArr = new boolean[0];

        @Override
        public boolean isNullable()
        {
            return true;
        }

        @Override
        public void write(Jcodec jcodec, OutputView output, boolean[] values)
        {
            if (values == null) {
                output.writeVarInt(0, true);
                return;
            }
            output.writeVarInt(values.length + 1, true);
            if (values.length > 0) {
                output.writeBoolArray(values);
            }
        }

        @Override
        public boolean[] read(Jcodec jcodec, InputView input, Class<? extends boolean[]> typeClass)
        {
            int len = input.readVarInt(true);
            if (len == 0) {
                return null;
            }
            else if (len == 1) {
                return zeroArr;
            }
            else {
                len--;
                boolean[] values = new boolean[len];
                input.readBoolArray(values, 0, len);
                return values;
            }
        }

        @Override
        public Comparator<boolean[]> comparator()
        {
            return Platform.getArrayComparator(boolean[].class);
        }
    }

    public static class ShortArraySerializer
            implements Serializer<short[]>
    {
        private static final short[] zeroArr = new short[0];

        @Override
        public boolean isNullable()
        {
            return true;
        }

        @Override
        public void write(Jcodec jcodec, OutputView output, short[] values)
        {
            if (values == null) {
                output.writeVarInt(0, true);
                return;
            }
            output.writeVarInt(values.length + 1, true);
            for (short e : values) {
                output.writeShort(e);
            }
        }

        @Override
        public short[] read(Jcodec jcodec, InputView input, Class<? extends short[]> typeClass)
        {
            int len = input.readVarInt(true);
            if (len == 0) {
                return null;
            }
            else if (len == 1) {
                return zeroArr;
            }
            else {
                len--;
                short[] values = new short[len];
                for (int i = 0; i < len; i++) {
                    values[i] = input.readShort();
                }
                return values;
            }
        }

        @Override
        public Comparator<short[]> comparator()
        {
            return Platform.getArrayComparator(short[].class);
        }
    }

    public static class CharArraySerializer
            implements Serializer<char[]>
    {
        private static final char[] zeroIntArr = new char[0];

        @Override
        public boolean isNullable()
        {
            return true;
        }

        @Override
        public void write(Jcodec jcodec, OutputView output, char[] values)
        {
            if (values == null) {
                output.writeVarInt(0, true);
                return;
            }
            output.writeVarInt(values.length + 1, true);
            for (char e : values) {
                output.writeChar(e);
            }
        }

        @Override
        public char[] read(Jcodec jcodec, InputView input, Class<? extends char[]> typeClass)
        {
            int len = input.readVarInt(true);
            if (len == 0) {
                return null;
            }
            else if (len == 1) {
                return zeroIntArr;
            }
            else {
                len--;
                char[] values = new char[len];
                for (int i = 0; i < len; i++) {
                    values[i] = input.readChar();
                }
                return values;
            }
        }

        @Override
        public Comparator<char[]> comparator()
        {
            return Platform.getArrayComparator(char[].class);
        }
    }

    public static class IntArraySerializer
            implements Serializer<int[]>
    {
        private static final int[] zeroArr = new int[0];

        @Override
        public boolean isNullable()
        {
            return true;
        }

        @Override
        public void write(Jcodec jcodec, OutputView output, int[] values)
        {
            if (values == null) {
                output.writeVarInt(0, true);
                return;
            }
            output.writeVarInt(values.length + 1, true);
            for (int e : values) {
                output.writeInt(e);
            }
        }

        @Override
        public int[] read(Jcodec jcodec, InputView input, Class<? extends int[]> typeClass)
        {
            int len = input.readVarInt(true);
            if (len == 0) {
                return null;
            }
            if (len == 1) {
                return zeroArr;
            }
            else {
                len--;
                int[] values = new int[len];
                for (int i = 0; i < len; i++) {
                    values[i] = input.readInt();
                }
                return values;
            }
        }

        @Override
        public Comparator<int[]> comparator()
        {
            return Platform.getArrayComparator(int[].class);
        }
    }

    public static class FloatArraySerializer
            implements Serializer<float[]>
    {
        private static final float[] zeroArr = new float[0];

        @Override
        public boolean isNullable()
        {
            return true;
        }

        @Override
        public void write(Jcodec jcodec, OutputView output, float[] values)
        {
            if (values == null) {
                output.writeVarInt(0, true);
                return;
            }
            output.writeVarInt(values.length + 1, true);
            for (float e : values) {
                output.writeFloat(e);
            }
        }

        @Override
        public float[] read(Jcodec jcodec, InputView input, Class<? extends float[]> typeClass)
        {
            int len = input.readVarInt(true);
            if (len == 0) {
                return null;
            }
            else if (len == 1) {
                return zeroArr;
            }
            else {
                len--;
                float[] values = new float[len];
                for (int i = 0; i < len; i++) {
                    values[i] = input.readByte();
                }
                return values;
            }
        }

        @Override
        public Comparator<float[]> comparator()
        {
            return Platform.getArrayComparator(float[].class);
        }
    }

    public static class LongArraySerializer
            implements Serializer<long[]>
    {
        private static final long[] zeroArr = new long[0];

        @Override
        public boolean isNullable()
        {
            return true;
        }

        @Override
        public void write(Jcodec jcodec, OutputView output, long[] values)
        {
            if (values == null) {
                output.writeVarInt(0, true);
                return;
            }
            output.writeVarInt(values.length + 1, true);
            for (long e : values) {
                output.writeLong(e);
            }
        }

        @Override
        public long[] read(Jcodec jcodec, InputView input, Class<? extends long[]> typeClass)
        {
            int len = input.readVarInt(true);
            if (len == 0) {
                return null;
            }
            if (len == 1) {
                return zeroArr;
            }
            else {
                len--;
                long[] values = new long[len];
                for (int i = 0; i < len; i++) {
                    values[i] = input.readInt();
                }
                return values;
            }
        }

        @Override
        public Comparator<long[]> comparator()
        {
            return Platform.getArrayComparator(long[].class);
        }
    }

    public static class DoubleArraySerializer
            implements Serializer<double[]>
    {
        private static final double[] zeroArr = new double[0];

        @Override
        public boolean isNullable()
        {
            return true;
        }

        @Override
        public void write(Jcodec jcodec, OutputView output, double[] values)
        {
            if (values == null) {
                output.writeVarInt(0, true);
                return;
            }
            output.writeVarInt(values.length + 1, true);
            for (double e : values) {
                output.writeDouble(e);
            }
        }

        @Override
        public double[] read(Jcodec jcodec, InputView input, Class<? extends double[]> typeClass)
        {
            int len = input.readVarInt(true);
            if (len == 0) {
                return null;
            }
            else if (len == 1) {
                return zeroArr;
            }
            else {
                len--;
                double[] values = new double[len];
                for (int i = 0; i < len; i++) {
                    values[i] = input.readInt();
                }
                return values;
            }
        }

        @Override
        public Comparator<double[]> comparator()
        {
            return Platform.getArrayComparator(double[].class);
        }
    }

    public static class StringArraySerializer
            implements Serializer<String[]>
    {
        private static final String[] zeroArr = new String[0];

        @Override
        public boolean isNullable()
        {
            return true;
        }

        @Override
        public Comparator<String[]> comparator()
        {
            return Platform.getArrayComparator(String[].class);
        }

        @Override
        public void write(Jcodec jcodec, OutputView output, String[] values)
        {
            if (values == null) {
                output.writeVarInt(0, true);
                return;
            }
            output.writeVarInt(values.length + 1, true);
            for (String e : values) {
                output.writeString(e);
            }
        }

        @Override
        public String[] read(Jcodec jcodec, InputView input, Class<? extends String[]> typeClass)
        {
            int len = input.readVarInt(true);
            if (len == 0) {
                return null;
            }
            else if (len == 1) {
                return zeroArr;
            }
            else {
                len--;
                String[] values = new String[len];
                for (int i = 0; i < len; i++) {
                    values[i] = input.readString();
                }
                return values;
            }
        }
    }
}
