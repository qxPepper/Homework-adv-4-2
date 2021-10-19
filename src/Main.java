import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    private static final int NUMBER_OF_TESTS = 5;
    private static final int[] COUNT_THREADS = {8, 16};
    private static final int[] MAP_SIZE = {1_000, 100_000, 1_000_000, 100_000};
    private static final int MAX_INTERVAL = 1_000;
    private static final double NANO_TO_MIL = 1_000_000.0;
    private static final int TIME_SLEEP = 1_000;

    private static int[] array;
    private static int dimensionOfThreadArray;

    public static void main(String[] args) {
        //В первых 3-х шагах потоков - 8, размеры int-ового массива - 1_000, 100_000, 1_000_000.
        //На последнем 4-м шаге потоков - 16, размер int-ового массива - 100_000.
        for (int i = 0; i < MAP_SIZE.length; i++) {
            array = creatingArray(MAP_SIZE[i]);
            dimensionOfThreadArray = i < MAP_SIZE.length - 1 ? COUNT_THREADS[0] : COUNT_THREADS[1];

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

            Например по записи: при увеличении числа элементов разница растёт.
            По чтению: при увеличении числа элементов разница растёт сущестенно более резко.

            При увеличении с 8 до 16 потоков, при количестве элементов 100_000,
            в операциях записи/чтения преимущество в скорости ConcurrentHashMap перед Collections.synchronizedMap
            заметно больше при большем числе потоков.

            Для более точного сравнения надо провести больше замеров.
            Тем более, время от времени наблюдались резкие всплески, которые, по идее, надо отсекать.
        */
    }

    public static void testsOfTime(Runnable Concurrent, Runnable Synchronized) {
        double sumConcurrentHashMap = 0.0;
        double sumSynchronizedHashMap = 0.0;
        Runnable runnable;
        String message;
        boolean mapAttribute;

        for (int i = 0; i < NUMBER_OF_TESTS; i++) {
            mapAttribute = false;

            for (int j = 0; j < 2; j++) {
                runnable = mapAttribute ? Synchronized : Concurrent;
                message = mapAttribute ? "Collections.synchronizedMap" : "ConcurrentHashMap";

                double measureTime = timeMeasure(runnable);
                System.out.println("Время " + message + " = " + measureTime + " мс.");

                try {
                    Thread.sleep(TIME_SLEEP);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                sumConcurrentHashMap = mapAttribute ? sumConcurrentHashMap : sumConcurrentHashMap + measureTime;
                sumSynchronizedHashMap = mapAttribute ? sumSynchronizedHashMap + measureTime : sumSynchronizedHashMap;

                mapAttribute = true;
            }
            System.out.println();
        }

        System.out.println("Размер числового массива = " + array.length +
                ", количество потоков = " + dimensionOfThreadArray);
        System.out.println("Среднее время ConcurrentHashMap = " +
                (sumConcurrentHashMap / NUMBER_OF_TESTS) + " мс.");
        System.out.println("Среднее время SynchronizedHashMap = " +
                (sumSynchronizedHashMap / NUMBER_OF_TESTS) + " мс.");
        System.out.println("Отношение в среднем ConcurrentHashMap / SynchronizedHashMap = " +
                (sumConcurrentHashMap / sumSynchronizedHashMap));
    }

    public static double timeMeasure(Runnable runnable) {
        double result;
        Thread[] threads = new Thread[dimensionOfThreadArray];

        long current = System.nanoTime();
        for (int i = 0; i < dimensionOfThreadArray; i++) {
            threads[i] = new Thread(runnable);
            threads[i].start();
        }
        for (int i = 0; i < dimensionOfThreadArray; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

// Первый прогон
// запись 0.46, 0.35, 0.35, 0.24
// чтение 0.42, 0.17, 0.08, 0.1

// Второй прогон
// запись 0.39, 0.23, 0.3, 0.2
// чтение 0.61, 0.13, 0.08, 0.09
