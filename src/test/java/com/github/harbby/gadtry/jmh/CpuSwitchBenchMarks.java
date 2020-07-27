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
package com.github.harbby.gadtry.jmh;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Date;
import java.util.Random;

/**
 * Switch对于CPU来说难以做分支预测；
 * 某些Switch条件如果概率比较高，可以在代码层设置提前if判断，充分利用CPU的分支预测机制
 */
public class CpuSwitchBenchMarks
{
    public enum ChannelState
    {
        CONNECTED, DISCONNECTED, SENT, RECEIVED, CAUGHT
    }

    public static void main(String[] args)
            throws RunnerException
    {
        Options opt = new OptionsBuilder()
                .include(CpuSwitchBenchMarks.class.getSimpleName())  //benchmark 所在的类的名字，注意这里是使用正则表达式对所有类进行匹配的
                //.timeUnit(TimeUnit.MICROSECONDS)
                .shouldFailOnError(true)
//                .jvmArgsPrepend("-XX")
//                .forks(4)  //进行 fork 的次数。如果 fork 数是2的话，则 JMH 会 fork 出两个进程来进行测试
//                .warmupIterations(1) //预热的迭代次数
//                .measurementIterations(5)  //实际测量的迭代次数
                .build();
        new Runner(opt).run();
    }

    @State(Scope.Benchmark)
    public static class ExecutionPlan
    {
        @Param({"1000000"})
        public int size;
        public ChannelState[] states = null;

        @Setup
        public void setUp()
        {
            ChannelState[] values = ChannelState.values();
            states = new ChannelState[size];
            Random random = new Random(new Date().getTime());
            for (int i = 0; i < size; i++) {
                int nextInt = random.nextInt(1000000);
                if (nextInt > 100) {
                    states[i] = ChannelState.RECEIVED;
                }
                else {
                    states[i] = values[nextInt % values.length];
                }
            }
        }
    }

    /**
     * benchSiwtch里是纯Switch判断
     */
    @Fork(value = 5)
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchSiwtch(ExecutionPlan plan, Blackhole bh)
    {
        int result = 0;
        for (int i = 0; i < plan.size; ++i) {
            switch (plan.states[i]) {
                case CONNECTED:
                    result += ChannelState.CONNECTED.ordinal();
                    break;
                case DISCONNECTED:
                    result += ChannelState.DISCONNECTED.ordinal();
                    break;
                case SENT:
                    result += ChannelState.SENT.ordinal();
                    break;
                case RECEIVED:
                    result += ChannelState.RECEIVED.ordinal();
                    break;
                case CAUGHT:
                    result += ChannelState.CAUGHT.ordinal();
                    break;
            }
        }
        bh.consume(result);
    }

    /**
     * benchIfAndSwitch 里用一个if提前判断state是否ChannelState.RECEIVED
     * <p>
     * 可以看到，提前if判断提高了近3倍的代码效率，这种技巧可以放在性能要求严格的地方。
     */
    @Fork(value = 5)
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchIfAndSwitch(ExecutionPlan plan, Blackhole bh)
    {
        int result = 0;
        for (int i = 0; i < plan.size; ++i) {
            ChannelState state = plan.states[i];
            if (state == ChannelState.RECEIVED) {
                result += ChannelState.RECEIVED.ordinal();
            }
            else {
                switch (state) {
                    case CONNECTED:
                        result += ChannelState.CONNECTED.ordinal();
                        break;
                    case SENT:
                        result += ChannelState.SENT.ordinal();
                        break;
                    case DISCONNECTED:
                        result += ChannelState.DISCONNECTED.ordinal();
                        break;
                    case CAUGHT:
                        result += ChannelState.CAUGHT.ordinal();
                        break;
                }
            }
        }
        bh.consume(result);
    }
}
