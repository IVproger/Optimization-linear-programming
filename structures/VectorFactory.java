package structures;

import structures.implementations.ArrayVector;

import java.util.Scanner;

public class VectorFactory {
    public static Vector createEmptyVector(int length) {
        return new ArrayVector(length);
    }

    public static Vector createVector(double[] items) {
        int n = items.length;

        Vector result = new ArrayVector(n);
        for (int i = 0; i < n; i++)
            result.setItem(i, items[i]);

        return result;
    }

    public static Vector createVectorFromInput(int length, Scanner scanner) {
        Vector vector = new ArrayVector(length);

        for (int i = 0; i < length; i++)
            vector.setItem(i, scanner.nextDouble());

        return vector;
    }
}
