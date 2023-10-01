package structures;

public interface Vector extends Iterable<Double> {
    Vector plus(Vector other);
    Vector minus(Vector other);

    Vector scalarMultiply(double scalar);
    double dotProduct(Vector vector); // Помениял multiply на dotProduct

//    Vector multiply(Matrix matrix);
//    Подумал и понял, что это не имеет смысла,
//    потому что вектор умножается на матрицу, а не наоборот

    double getItem(int index);
    void setItem(int index, double value);

    int getLength();
}
