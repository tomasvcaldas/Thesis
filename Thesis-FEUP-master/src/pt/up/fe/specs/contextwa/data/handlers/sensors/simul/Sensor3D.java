package pt.up.fe.specs.contextwa.data.handlers.sensors.simul;

import java.util.stream.DoubleStream;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class Sensor3D implements BaseSensor {
    private double[] coordX;
    private double[] coordY;
    private double[] coordZ;

    private double meanX;
    private double meanY;
    private double meanZ;

    private double mean;

    private double corrXY;
    private double corrYZ;
    private double corrXZ;

    private double sdX;
    private double sdY;
    private double sdZ;

    private PearsonsCorrelation pearson = new PearsonsCorrelation();

    public Sensor3D(int windowSize) {

        this.coordX = new double[windowSize];
        this.coordY = new double[windowSize];
        this.coordZ = new double[windowSize];

    }

    public double[] getCoordX() {
        return coordX;
    }

    public void setCoordX(double[] coordX) {
        this.coordX = coordX;
    }

    public double[] getCoordY() {
        return coordY;
    }

    public void setCoordY(double[] coordY) {
        this.coordY = coordY;
    }

    public double[] getCoordZ() {
        return coordZ;
    }

    public void setCoordZ(double[] coordZ) {
        this.coordZ = coordZ;
    }

    // mean coord X
    @Override
    public void mean() {
        this.meanX = DoubleStream.of(coordX).average().orElse(0);
        this.meanY = DoubleStream.of(coordY).average().orElse(0);
        this.meanZ = DoubleStream.of(coordZ).average().orElse(0);
        this.mean = (this.meanX + this.meanY + this.meanZ) / 3;
    }

    // SD
    @Override
    public void sDev() {
        this.sdX = sd(coordX, meanX, coordX.length);
        this.sdY = sd(coordY, meanY, coordY.length);
        this.sdZ = sd(coordZ, meanZ, coordZ.length);
    }

    // method that creates standard deviation
    private static boolean isBiasCorrected = true;

    public static double sd(double[] accXList, double accAxis1Mean, int numPoints) {
        double sd1 = DoubleStream.of(accXList).map(x -> Math.abs(x - accAxis1Mean)).reduce(0,
                (acc, current) -> acc + (current) * (current));
        return Math.sqrt(sd1 / (numPoints - (isBiasCorrected ? 1 : 0))); // same as in R and Apache Math3
    }

    @Override
    public void correlation() {
        this.corrXY = pearson.correlation(coordX, coordY);
        this.corrYZ = pearson.correlation(coordY, coordZ);
        this.corrXZ = pearson.correlation(coordX, coordZ);
    }

    public double getMeanX() {
        return meanX;
    }

    public void setMeanX(double meanX) {
        this.meanX = meanX;
    }

    public double getMeanY() {
        return meanY;
    }

    public void setMeanY(double meanY) {
        this.meanY = meanY;
    }

    public double getMeanZ() {
        return meanZ;
    }

    public void setMeanZ(double meanZ) {
        this.meanZ = meanZ;
    }

    public double getMean() {
        return mean;
    }

    public double getCorrXY() {
        return corrXY;
    }

    public void setCorrXY(double corrXY) {
        this.corrXY = corrXY;
    }

    public double getCorrYZ() {
        return corrYZ;
    }

    public void setCorrYZ(double corrYZ) {
        this.corrYZ = corrYZ;
    }

    public double getCorrXZ() {
        return corrXZ;
    }

    public void setCorrXZ(double corrXZ) {
        this.corrXZ = corrXZ;
    }

    public double getSdX() {
        return sdX;
    }

    public void setSdX(double sdX) {
        this.sdX = sdX;
    }

    public double getSdY() {
        return sdY;
    }

    public void setSdY(double sdY) {
        this.sdY = sdY;
    }

    public double getSdZ() {
        return sdZ;
    }

    public void setSdZ(double sdZ) {
        this.sdZ = sdZ;
    }

    public PearsonsCorrelation getPearson() {
        return pearson;
    }

    public void setPearson(PearsonsCorrelation pearson) {
        this.pearson = pearson;
    }

}
