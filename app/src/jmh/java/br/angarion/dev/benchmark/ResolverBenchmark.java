package br.angarion.dev.benchmark;

import br.angarion.dev.engine.communication.DataLayout;
import br.angarion.dev.engine.communication.validator.Validator;
import br.angarion.dev.engine.communication.validator.ValidatorResponse;
import br.angarion.dev.engine.runtime.ComponentResolver;
import br.angarion.dev.engine.runtime.StaticComponentResolver;
import br.angarion.dev.engine.usecase.UseCase;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.lang.foreign.MemorySegment;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 8, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3)
@State(Scope.Thread)
@Threads(1)
public class ResolverBenchmark {
    private static final int BATCH_SIZE = 64;

    @Param({"0"})
    public int componentId;

    @Param({"0"})
    public int familyId;

    @Param({"0"})
    public int typeId;

    private ComponentResolver componentResolver;
    private int[] batchIds;
    private Object[] batchResults;

    @Setup
    public void setUp() {
        componentResolver = new ComponentResolver();

        Validator<FakeDataLayout> validator = intention -> ValidatorResponse.success();
        UseCase<FakeDataLayout> useCase = (originId, data) -> { };

        componentResolver.register(familyId, typeId, validator, useCase);

        batchIds = new int[BATCH_SIZE];
        batchResults = new Object[BATCH_SIZE];
        for (int i = 0; i < BATCH_SIZE; i++) {
            batchIds[i] = i % 3;
        }
    }

    @Benchmark
    public Object staticComponentResolver() {
        return StaticComponentResolver.getComponent(componentId);
    }

    @Benchmark
    public Object componentResolverLookup() {
        return componentResolver.lookup(familyId, typeId);
    }

    @Benchmark
    @OperationsPerInvocation(BATCH_SIZE)
    public Object staticComponentResolverBatch() {
        return br.angarion.dev.engine.runtime.StaticComponentResolverBatch.resolveBatch(batchIds, batchResults);
    }

    private static final class FakeDataLayout implements DataLayout {
        @Override
        public long size() {
            return 0L;
        }

        @Override
        public void write(MemorySegment dest) {
            // no-op on purpose: the benchmark measures resolver lookup only
        }
    }
}
