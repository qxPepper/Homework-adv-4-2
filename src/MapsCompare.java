import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapsCompare {
    private final int mapSize;
    private final int maxInterval;
    private final int cycleCount;

    public ThreadLocal<Integer> durationThreadLocal = new ThreadLocal<Integer>();
    private volatile long duration;

    private static final int TO_WRITE = 999;
    private static final int TIME_SLEEP = 1000;

    ConcurrentHashMap<Integer, Integer> concurrentHashMap = new ConcurrentHashMap<>(0);
    Map<Integer, Integer> synchronizedHashMap = Collections.synchronizedMap(new HashMap<>());

    public MapsCompare(int mapSize, int maxInterval, int cycleCount) {
        this.mapSize = mapSize;
        this.maxInterval = maxInterval;
        this.cycleCount = cycleCount;

        for (int i = 0; i < this.mapSize; i++) {
            int random = (int) (Math.random() * this.maxInterval);
            concurrentHashMap.put(i, random);
            synchronizedHashMap.put(i, random);
        }
    }

    public void measureWriteConcurrent() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < cycleCount; i++) {
            concurrentHashMap.put(mapSize, TO_WRITE);
        }
        finishingMeasure(start);
    }

    public void measureWriteSynchronized() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < cycleCount; i++) {
            synchronizedHashMap.put(mapSize, TO_WRITE);
        }
        finishingMeasure(start);
    }

    public void measureReadConcurrent() {
        int middleIndex = mapSize / 2;
        long start = System.currentTimeMillis();
        for (int i = 0; i < cycleCount; i++) {
            float a = concurrentHashMap.get(middleIndex);
        }
        finishingMeasure(start);
    }

    public void measureReadSynchronized() {
        int middleIndex = mapSize / 2;
        long start = System.currentTimeMillis();
        for (int i = 0; i < cycleCount; i++) {
            float a = synchronizedHashMap.get(middleIndex);
        }
        finishingMeasure(start);
    }

    public void finishingMeasure(long start) {
        try {
            Thread.sleep(TIME_SLEEP);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long finish = System.currentTimeMillis();

        durationThreadLocal.set((int) (finish - start - TIME_SLEEP));
        duration = durationThreadLocal.get();
    }

    public long getDuration() {
        return duration;
    }
}



