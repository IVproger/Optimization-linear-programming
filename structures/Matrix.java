package structures;

public interface Matrix {
    Matrix plus(Matrix other);
    Matrix minus(Matrix other);

    Matrix scalarMultiply(double scalar);
    Matrix multiply(Matrix other);
    Vector multiplyByVector(Vector vector); // as a column vector

    Matrix getTransposed();
    Matrix getInverse();
    double getDeterminant();

    double getItem(int row, int col);
    Vector getRow(int row);
    Vector getColumn(int col);

    int getNumberOfRows();
    int getNumberOfColumns();

    void setItem(int row, int col, double value);
    void setRow(int row, Vector vector);
    void setColumn(int col, Vector vector);
}
