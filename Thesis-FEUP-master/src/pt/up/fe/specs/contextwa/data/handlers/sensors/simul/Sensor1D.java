package pt.up.fe.specs.contextwa.data.handlers.sensors.simul;

import java.util.stream.DoubleStream;

public class Sensor1D implements BaseSensor {
    private double[] coordX;

    private double meanX;
    private double sdX;

    public Sensor1D(int windowSize) {
        this.coordX = new double[windowSize];
    }

    public double getMean() {
        return meanX;
    }

    public double getSdX() {
        return sdX;
    }

    public void setSdX(double sdX) {
        this.sdX = sdX;
    }

    public double[] getCoordX() {
        return coordX;
    }

    public void setCoordX(double[] coordX) {
        this.coordX = coordX;
    }

    // mean coord X
    @Override
    public void mean() {
        this.meanX = DoubleStream.of(coordX).average().orElse(0);
    }

    // SD
    @Override
    public void sDev() {
        this.sdX = sd(coordX, meanX, coordX.length);
    }

    // method that creates standard deviation
    private static boolean isBiasCorrected = true;

    public static double sd(double[] accXList, double accAxis1Mean, int numPoints) {
        double sd1 = DoubleStream.of(accXList).map(x -> Math.abs(x - accAxis1Mean)).reduce(0,
                (acc, current) -> acc + (current) * (current));
        return Math.sqrt(sd1 / (numPoints - (isBiasCorrected ? 1 : 0))); // same as in R and Apache Math3
    }

}
