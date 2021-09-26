public class Main {
    static int mapSize = 500;                                              //размер мапы
    static int maxInterval = 1_000;                                        //верхняя граница интервала значений мапы
    static int[] cycleCount = new int[]{1_000, 1_000_000, 1_000_000};      //количество циклов записи/чтения
    static int[] countThreads = new int[]{3, 3, 15};                       //количество потоков

    static MapsCompare mapsCompare;

    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < cycleCount.length; i++) {
            mapsCompare = new MapsCompare(mapSize, maxInterval, cycleCount[i]);
            generalMeasurements(i);
            System.out.println("*******************************************");
        }

        System.out.println("Выводы:");
        System.out.println("При относительно небольших количествах записи/чтения время примерно одинаково.");
        System.out.println("При существенном увеличении числа записей Synchronized существенно быстрее ConcurrentHashMap.");
        System.out.println("А вот читает ConcurrentHashMap в примерно в 2 раза быстрее чем Synchronized.");
        System.out.println("Если увеличить число потоков, то все времена уменьшаются, пропорции при этом сохраняются.");
    }

    public static void generalMeasurements(int index) throws InterruptedException {
        float fw1 = timeAction(mapsCompare::measureWriteConcurrent, countThreads[index]);
        System.out.println("Время записи ConcurrentHashMap = " + fw1);

        float fw2 = timeAction(mapsCompare::measureWriteSynchronized, countThreads[index]);
        System.out.println("Время записи Synchronized = " + fw2);

        System.out.println("-------------------------------------------");

        float wr1 = timeAction(mapsCompare::measureReadConcurrent, countThreads[index]);
        System.out.println("Время чтения ConcurrentHashMap = " + wr1);

        float wr2 = timeAction(mapsCompare::measureReadSynchronized, countThreads[index]);
        System.out.println("Время чтения Synchronized = " + wr2);
    }

    public static float timeAction(Runnable runnable, int countThreads) throws InterruptedException {
        float middle = 0;
        Thread[] threads = new Thread[countThreads];
        for (int i = 0; i < countThreads; i++) {
            threads[i] = new Thread(runnable);

            threads[i].start();
            threads[i].join();
            middle += mapsCompare.getDuration();
        }
        return middle / countThreads;
    }
}
