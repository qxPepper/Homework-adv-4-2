import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Условия задачи были переосмыслены.

public class Main {
    private static int[] mapSize = {10_000, 1_000_000, 100};

    private static final int MAX_INTERVAL = 1_000;
    private static final int COUNT_THREADS = 5;
    private static final int TIME_SLEEP = 1_000;

    public static void main(String[] args) throws InterruptedException {
        Map<Integer, Integer> concurrentHashMap = new ConcurrentHashMap<>(0);
        Map<Integer, Integer> synchronizedHashMap = Collections.synchronizedMap(new HashMap<>());
        int[] array;

        for (int size : mapSize) {
            array = creatingArray(size);

            MapsCompare mapConcurrent = new MapsCompare(array, concurrentHashMap);
            MapsCompare mapSynchronized = new MapsCompare(array, synchronizedHashMap);

            System.out.println("Размер числового массива = " + size);
            System.out.println("...");
            System.out.println("Время записи ConcurrentHashMap = " +
                    timeMeasure(mapConcurrent::measureWrite) + " мс.");

            System.out.println("Время записи Collections.synchronizedMap = " +
                    timeMeasure(mapSynchronized::measureWrite) + " мс.");

            System.out.println("-------------------------------------------");

            System.out.println("Время чтения ConcurrentHashMap = " +
                    timeMeasure(mapConcurrent::measureRead) + " мс.");

            System.out.println("Время чтения Collections.synchronizedMap = " +
                    timeMeasure(mapSynchronized::measureRead) + " мс.");

            System.out.println("********************************************");
        }

        //    Выводы при многократных экспериментах, в среднем:
        // 1. При малом количестве добавляемых элементов времена записи примерно равны.
        //    При малом количестве читаемых элементов времена чтения примерно равны.
        //
        // 2. При увеличении количества добавляемых элементов на 2 порядка
        //          время записи ConcurrentHashMap существенно больше Collections.synchronizedMap.
        //    А вот время чтения ConcurrentHashMap меньше Collections.synchronizedMap.
        //
        // 3. При увеличении количества добавляемых элементов ещё на 2 порядка
        //          тенденции по записи и чтению сохраняются. Разрыв растёт.
    }

    public static int[] creatingArray(int arraySize) {
        int[] array = new int[arraySize];

        for (int i = 0; i < arraySize; i++) {
            int random = (int) (Math.random() * MAX_INTERVAL);
            array[i] = random;
        }
        return array;
    }

    public static int timeMeasure(Runnable runnable) throws InterruptedException {
        Thread[] threads = new Thread[COUNT_THREADS];
        long start = System.currentTimeMillis();

        for (int i = 0; i < COUNT_THREADS; i++) {
            threads[i] = new Thread(runnable);
            threads[i].start();
            threads[i].join();
        }

        try {
            Thread.sleep(TIME_SLEEP);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long finish = System.currentTimeMillis();
        return (int) (finish - start - TIME_SLEEP);
    }
}


