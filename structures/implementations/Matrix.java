package structures.implementations;

import structures.Vector;

public class Matrix implements structures.Matrix {
    @Override
    public structures.Matrix plus(structures.Matrix other) {
        return null;
    }

    @Override
    public structures.Matrix minus(structures.Matrix other) {
        return null;
    }

    @Override
    public structures.Matrix scalarMultiply(double scalar) {
        return null;
    }

    @Override
    public structures.Matrix multiply(structures.Matrix other) {
        return null;
    }

    @Override
    public Vector multiplyByVector(Vector vector) {
        return null;
    }

    @Override
    public structures.Matrix getTransposed() {
        return null;
    }

    @Override
    public structures.Matrix getInverse() {
        return null;
    }

    @Override
    public double getDeterminant() {
        return 0;
    }

    @Override
    public double getItem(int row, int col) {
        return 0;
    }

    @Override
    public Vector getRow(int row) {
        return null;
    }

    @Override
    public Vector getColumn(int col) {
        return null;
    }

    @Override
    public int getNumberOfRows() {
        return 0;
    }

    @Override
    public int getNumberOfColumns() {
        return 0;
    }

    @Override
    public void setItem(int row, int col, double value) {

    }

    @Override
    public void setRow(int row, Vector vector) {

    }

    @Override
    public void setColumn(int col, Vector vector) {

    }
}
