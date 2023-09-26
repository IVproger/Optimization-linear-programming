package structures;

public interface Vector extends Iterable<Double> {
    Vector plus(Vector other);
    Vector minus(Vector other);

    Vector scalarMultiply(double scalar);
    Vector multiply(Vector vector);
    Vector multiply(Matrix matrix);

    double getItem(int index);
    void setItem(int index, double value);

    int getLength();
}