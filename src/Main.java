import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    private static final int NUMBER_OF_VARIATIONS = 4;
    private static final int NUMBER_OF_TESTS = 10;
    private static final int TIME_SLEEP = 5000;
    private static final int MAX_INTERVAL = 1_000;
    private static final double NANO_TO_MIL = 1_000_000.0;

    private static final int[] COUNT_THREADS = {8, 16};
    private static final int[] MAP_SIZE = {1_000, 100_000, 5_000_000, 100_000};

    private static Thread[] threads;
    private static int[] array;
    private static double sumConcurrentHashMap = 0.0;
    private static double sumSynchronizedHashMap = 0.0;

    public static void main(String[] args) {
        //В первых 3-х шагах потоков - 8, размеры int-ового массива - 1_000, 100_000, 5_000_000.
        //На последнем 4-м шаге потоков - 16, размер int-ового массива - 100_000.
        for (int i = 0; i < NUMBER_OF_VARIATIONS; i++) {
            array = creatingArray(MAP_SIZE[i]);

            if (i < NUMBER_OF_VARIATIONS - 1) {
                threads = new Thread[COUNT_THREADS[0]];
            } else {
                threads = new Thread[COUNT_THREADS[1]];
            }

            Map<Integer, Integer> concurrentHashMap = new ConcurrentHashMap<>(0);
            Map<Integer, Integer> synchronizedHashMap = Collections.synchronizedMap(new HashMap<>());

            MapsCompare mapConcurrent = new MapsCompare(array, concurrentHashMap);
            MapsCompare mapSynchronized = new MapsCompare(array, synchronizedHashMap);

            System.out.println("ЗАПИСЬ:");
            System.out.println("...");
            testsOfTime(mapConcurrent::measureWrite, mapSynchronized::measureWrite);

            System.out.println("--------------------------------------------");

            System.out.println("ЧТЕНИЕ:");
            System.out.println("...");
            testsOfTime(mapConcurrent::measureRead, mapSynchronized::measureRead);

            System.out.println("*********************************************");
            System.out.println();
        }
        /*
         Ввывод:
            При любых вариациях числа потоков и числа элементов в int-овом массиве в операциях
            записи/чтения ConcurrentHashMap быстрее Collections.synchronizedMap!
            Где-то существенно, где-то нет.

            Например по записи: 8 потоков, 100_000 элементов разница в скорости заметна. Относительно этих значений -
                при уменьшении числа элементов разница уменьшается,
                при увеличении числа элементов разница растёт.
            По чтению: разница меньше (хотя ожидаласть больше).

            По записи: при увеличении с 8 до 16 потоков, при количестве элементов 100_000,
                разница немного меньше (хотя ожидаласть больше).
            По чтению: изменений в тенденции относительно 8 потоков не обнаружилось.

            Для более точного сравнения надо провести больше замеров.
            Тем более, время от времени наблюдались резкие всплески, которые, по идее, надо отсекать.
        */
    }

    public static void testsOfTime(Runnable measureConcurrent, Runnable measureSynchronized) {
        for (int i = 0; i < NUMBER_OF_TESTS; i++) {
            double timeConcurrentHashMap = timeMeasure(measureConcurrent);
            double timeSynchronizedHashMap = timeMeasure(measureSynchronized);

            System.out.println("Время ConcurrentHashMap = " + timeConcurrentHashMap + " мс.");
            System.out.println("Время Collections.synchronizedMap = " + timeSynchronizedHashMap + " мс.");

            try {
                Thread.sleep(TIME_SLEEP);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sumConcurrentHashMap += timeConcurrentHashMap;
            sumSynchronizedHashMap += timeSynchronizedHashMap;

            System.out.println();
        }

        System.out.println("Размер числового массива = " + array.length +
                ", количество потоков = " + threads.length);
        System.out.println("Среднее время ConcurrentHashMap = " +
                (sumConcurrentHashMap / NUMBER_OF_TESTS) + " мс.");
        System.out.println("Среднее время SynchronizedHashMap = " +
                (sumSynchronizedHashMap / NUMBER_OF_TESTS) + " мс.");
    }

    public static double timeMeasure(Runnable runnable) {
        double result;
        long current = System.nanoTime();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(runnable);
            threads[i].start();
        }
        result = (System.nanoTime() - current) / NANO_TO_MIL;

        return result;
    }

    public static int[] creatingArray(int arraySize) {
        int[] array = new int[arraySize];

        for (int i = 0; i < arraySize; i++) {
            int random = (int) (Math.random() * MAX_INTERVAL);
            array[i] = random;
        }
        return array;
    }
}


