import structures.Matrix;
import structures.MatrixFactory;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        // Example

        Matrix A = MatrixFactory.createMatrixFromInput(3, 2);
        System.out.println(A.getInverse());
    }
}

