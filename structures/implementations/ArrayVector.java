package structures.implementations;

import structures.Vector;

import java.util.Iterator;

public class ArrayVector implements Vector {
    private final int length;
    private final double[] values;

    public ArrayVector(int length) {
        this.length = length;
        this.values = new double[length];

        for (int i = 0; i < length; i++)
            this.values[i] = 0;
    }

    @Override
    public Vector plus(Vector other) {
        if (this.length != other.getLength())
            throw new IllegalArgumentException();

        Vector result = new ArrayVector(this.length);

        for (int i = 0; i < this.length; i++)
            result.setItem(i, this.values[i] + other.getItem(i));

        return result;
    }

    @Override
    public Vector minus(Vector other) {
        if (this.length != other.getLength())
            throw new IllegalArgumentException();

        return this.plus(other.scalarMultiply(-1));
    }

    @Override
    public Vector scalarMultiply(double scalar) {
        Vector result = new ArrayVector(this.length);

        for (int i = 0; i < this.length; i++)
            result.setItem(i, this.values[i] * scalar);

        return result;
    }

    @Override
    public double dotProduct(Vector vector) {
        if (this.length != vector.getLength())
            throw new IllegalArgumentException();

        double result = 0;

        for (int i = 0; i < this.length; i++)
            result += this.values[i] * vector.getItem(i);

        return result;
    }

    @Override
    public double getItem(int index) {
        if (index < 0 || index >= length)
            throw new IndexOutOfBoundsException();

        return values[index];
    }

    @Override
    public void setItem(int index, double value) {
        if (index < 0 || index >= length)
            throw new IndexOutOfBoundsException();

        values[index] = value;
    }

    @Override
    public int getLength() {
        return this.length;
    }

    @Override
    public Iterator<Double> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < length;
            }

            @Override
            public Double next() {
                return values[index++];
            }
        };
    }
}
