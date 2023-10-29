package structures.implementations;

import structures.Matrix;
import structures.Vector;

public class ArrayMatrix implements Matrix {
    private final int rows;
    private final int cols;
    private final double[][] values;

    public ArrayMatrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.values = new double[rows][cols];

        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                this.values[i][j] = 0;
    }

    @Override
    public Matrix plus(Matrix other) {
        if (this.rows != other.getNumberOfRows() || this.cols != other.getNumberOfColumns())
            throw new IllegalArgumentException();

        Matrix result = new ArrayMatrix(this.rows, this.cols);

        for (int i = 0; i < this.rows; i++)
            for (int j = 0; j < this.cols; j++)
                result.setItem(i, j, this.values[i][j] + other.getItem(i, j));

        return result;
    }

    @Override
    public Matrix minus(Matrix other) {
        if (this.rows != other.getNumberOfRows() || this.cols != other.getNumberOfColumns())
            throw new IllegalArgumentException();

        return this.plus(other.scalarMultiply(-1));
    }

    @Override
    public Matrix scalarMultiply(double scalar) {
        Matrix result = new ArrayMatrix(this.rows, this.cols);

        for (int i = 0; i < this.rows; i++)
            for (int j = 0; j < this.cols; j++)
                result.setItem(i, j, this.values[i][j] * scalar);

        return result;
    }

    @Override
    public Matrix multiply(Matrix other) {
        if (this.cols != other.getNumberOfRows())
            throw new IllegalArgumentException();

        Matrix result = new ArrayMatrix(this.rows, other.getNumberOfColumns());

        for (int i = 0; i < this.rows; i++)
            for (int j = 0; j < other.getNumberOfColumns(); j++)
                for (int k = 0; k < this.cols; k++)
                    result.setItem(i, j, result.getItem(i, j) + this.values[i][k] * other.getItem(k, j));

        return result;
    }

    @Override
    public Vector multiplyByVector(Vector vector) {
        if (this.cols != vector.getLength())
            throw new IllegalArgumentException();

        // Treating the vector as a column vector
        Vector result = new ArrayVector(this.rows);

        for (int i = 0; i < this.rows; i++)
            for (int j = 0; j < this.cols; j++)
                result.setItem(i, result.getItem(i) + this.values[i][j] * vector.getItem(j));

        return result;
    }

    @Override
    public Matrix getTransposed() {
        Matrix result = new ArrayMatrix(this.cols, this.rows);

        for (int i = 0; i < this.cols; i++)
            for (int j = 0; j < this.rows; j++)
                result.setItem(i, j, this.values[j][i]);

        return result;
    }

    @Override
    public double getItem(int row, int col) {
        if (row >= this.rows || col >= this.cols || row < 0 || col < 0)
            throw new IndexOutOfBoundsException();

        return values[row][col];
    }

    @Override
    public int getNumberOfRows() {
        return this.rows;
    }

    @Override
    public int getNumberOfColumns() {
        return this.cols;
    }

    @Override
    public Vector getRow(int row) {
        if (row >= this.rows || row < 0)
            throw new IndexOutOfBoundsException();

        Vector v = new ArrayVector(this.cols);

        for (int i = 0; i < this.cols; i++)
            v.setItem(i, this.values[row][i]);

        return v;
    }

    @Override
    public Vector getColumn(int col) {
        if (col >= this.cols || col < 0)
            throw new IndexOutOfBoundsException();

        Vector v = new ArrayVector(this.rows);

        for (int i = 0; i < this.rows; i++)
            v.setItem(i, this.values[i][col]);

        return v;
    }

    @Override
    public void setItem(int row, int col, double value) {
        if (row >= this.rows || col >= this.cols || row < 0 || col < 0)
            throw new IndexOutOfBoundsException();

        this.values[row][col] = value;
    }

    @Override
    public void setRow(int row, Vector vector) {
        if (vector.getLength() != this.cols)
            throw new IllegalArgumentException();

        if (row >= this.rows || row < 0)
            throw new IndexOutOfBoundsException();

        for (int i = 0; i < this.cols; i++)
            this.values[row][i] = vector.getItem(i);
    }

    @Override
    public void setColumn(int col, Vector vector) {
        if (vector.getLength() != this.rows)
            throw new IllegalArgumentException();

        if (col >= this.cols || col < 0)
            throw new IndexOutOfBoundsException();

        for (int i = 0; i < this.rows; i++)
            this.values[i][col] = vector.getItem(i);
    }

    @Override
    public void print() {
        for (int i = 0; i < this.rows; i++) {
            System.out.print("| ");

            for (int j = 0; j < this.cols; j++)
                System.out.printf("%f ", this.values[i][j]);

            System.out.println("|");
        }
    }

    @Override
    public void seatDiagonal(Vector vector) {
        if (vector.getLength() != this.rows)
            throw new IllegalArgumentException();

        for (int i = 0; i < this.rows; i++)
            this.values[i][i] = vector.getItem(i);
    }

    @Override
    public Matrix transpose() {
        Matrix result = new ArrayMatrix(this.cols, this.rows);

        for (int i = 0; i < this.cols; i++)
            for (int j = 0; j < this.rows; j++)
                result.setItem(i, j, this.values[j][i]);

        return result;
    }



}
