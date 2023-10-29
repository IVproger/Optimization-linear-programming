package structures.implementations;

import structures.Matrix;
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
    public Vector multiply(Matrix matrix) {
        if (this.length != matrix.getNumberOfRows())
            throw new IllegalArgumentException();

        Vector result = new ArrayVector(matrix.getNumberOfColumns());

        for (int i = 0; i < matrix.getNumberOfColumns(); i++) {
            double sum = 0;

            for (int j = 0; j < this.length; j++)
                sum += this.values[j] * matrix.getItem(j, i);

            result.setItem(i, sum);
        }

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
        return new Iterator<Double>() {
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

    @Override
    public void print() {
        for (int i = 0; i < this.length; i++)
            System.out.print(this.values[i] + " ");
        System.out.println();
    }

    /**
     * Checks if two vectors are equal
     * @param other is the second vector
     * @return true if they are equal, false otherwise
     */
    @Override
    public boolean equals(Vector other) {
        if (this.length != other.getLength())
            return false;

        for (int i = 0; i < this.length; i++)
            if (this.values[i] != other.getItem(i))
                return false;

        return true;
    }

    /**
     * Method calculates the norm of vector
     * @return the norm
     */
    @Override
    public double getNorm() {
        double sum = 0;

        for (int i = 0; i < this.length; i++)
            sum += this.values[i] * this.values[i];

        return Math.sqrt(sum);
    }

    /**
     * Method counts number of zeros in the vector
     * @return amount of 0s
     */
    @Override
    public int getNumberOfZeroElements() {
        int count = 0;

        for (int i = 0; i < this.length; i++)
            if (this.values[i] == 0)
                count++;

        return count;
    }

    /**
     * Method determines the minimal element in the vector
     * @return minimal element
     */
    @Override
    public double findMinValue() {
        double min = 1000000000;

        for (int i = 0; i < this.length; i++)
            if (this.values[i] < min)
                min = this.values[i];

        return min;
    }

}
