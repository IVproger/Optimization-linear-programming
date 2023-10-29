package structures;

public interface Vector extends Iterable<Double> {
    Vector plus(Vector other);
    Vector minus(Vector other);

    Vector scalarMultiply(double scalar);
    double dotProduct(Vector vector);
    Vector multiply(Matrix matrix);

    double getItem(int index);
    void setItem(int index, double value);

    int getLength();

    void print();

    boolean equals(Vector other);

    double getNorm();

    int getNumberOfZeroElements();

    double findMinValue();
}
