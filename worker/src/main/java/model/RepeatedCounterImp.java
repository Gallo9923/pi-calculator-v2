package model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.Semaphore;

import datastructures.AVLBSTree;

public class RepeatedCounterImp implements RepeatedCounter{

    private static final int MAX_AVAILABLE = 1;

    private AVLBSTree<Point, BigInteger> counterTree;
    private final Semaphore semaphore;
    private BigInteger n;

    public RepeatedCounterImp(BigInteger n) {
        counterTree = new AVLBSTree<Point, BigInteger>();
        this.semaphore = new Semaphore(MAX_AVAILABLE, true);
        this.n = n;
    }

    @Override
    public void add(Point p) {
        try {
            this.semaphore.acquire();
            this.counterTree.add(p, BigInteger.ONE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            semaphore.release();
        }
    }

    @Override
    public BigDecimal getRepeatedNumPercentage() {
        return new BigDecimal(counterTree.getSum()).divide(new BigDecimal(n));
    }
    
}
