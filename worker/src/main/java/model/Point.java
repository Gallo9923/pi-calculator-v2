package model;

import java.util.Comparator;

public class Point implements Comparable<Point> {

    public static double EPSILON = 0;
    public static double SQR_EPSILON = 0;

    private double x;
    private double y;

    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double getX(){
        return this.x;
    }

    public double getY(){
        return this.y;
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }


    @Override
    public boolean equals(final Object obj) {

        if (this == obj){
            return true;
        }

        if (!(obj instanceof Point)){
            return false;
        }

        if (obj == null){
            return false;
        }

        Point p2 = (Point)obj;

        if ( (Math.pow((this.x - p2.getX()), 2) + Math.pow((this.y - p2.getY()), 2)) <= SQR_EPSILON){
            return true;
        }

        return false;
    }


    public static void setEpsilonPower(int power){
        Point.EPSILON = Math.pow(10, -Math.abs(power));
        Point.SQR_EPSILON = Math.pow(Point.EPSILON, 2);
    }

    @Override
    public int compareTo(Point o) {
        if (Point.EPSILON != 0) {
            if ( (Math.pow((this.x - o.getX()), 2) + Math.pow((this.y - o.getY()), 2)) <= SQR_EPSILON){
                return 0;
            }else if (this.x < o.getX()){
                return -1;
            }else if (this.x > o.getX()){
                return 1;
            }else{
                if (this.y < o.getY()){
                    return -1;
                }else if (this.y > o.getY()) {
                    return 1;
                }
            }
        }

        if (this.x < o.getX()){
            return -1;
        }else if (this.x > o.getX()){
            return 1;
        }else{
            if (this.y < o.getY()){
                return -1;
            }else if (this.y > o.getY()) {
                return 1;
            }else{
                return 0;
            }
        }
    }
}
