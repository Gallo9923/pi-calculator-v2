package model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.Semaphore;

public class PIResult implements Counter{

    private static final int MAX_AVAILABLE = 1;

    private final Semaphore semaphore;
    private int pointsInside;

    public PIResult(){
        this.semaphore = new Semaphore(PIResult.MAX_AVAILABLE, true);
        this.pointsInside = 0;
    }

    @Override
    public void add(int q){
        try {
            this.semaphore.acquire();
            this.pointsInside += q;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            semaphore.release();
        }
    }

    @Override
    public int getResult(){
        return this.pointsInside;
    }

}
