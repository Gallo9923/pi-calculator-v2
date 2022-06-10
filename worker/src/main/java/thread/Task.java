package thread;

import model.Counter;
import model.Point;
import model.RepeatedCounter;

import java.util.Random;

public class Task implements Runnable {

    private final int numbersToCalculate;
    private final Random r;
    private Counter counter;
    private RepeatedCounter repCounter;

    public Task(int seed, int numbersToCalculate, RepeatedCounter repCounter, Counter counter){
        this.r = new Random(seed);
        this.numbersToCalculate = numbersToCalculate;
        this.repCounter = repCounter;
        this.counter = counter;
    }

    @Override
    public void run() {

        int pointsInsideCircle = 0;
        for(int i=0; i < this.numbersToCalculate; i++){
            Point p = new Point(r.nextDouble(), r.nextDouble());

            if (Math.pow(p.getX(), 2) + Math.pow(p.getY(), 2) <= 1){
                pointsInsideCircle++;
            }
            if (repCounter != null) {
                repCounter.add(p);
            }
        }

        counter.add(pointsInsideCircle);
    }
}
