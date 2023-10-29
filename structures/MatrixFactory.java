package structures;

import structures.implementations.ArrayMatrix;

import java.util.Scanner;

public class MatrixFactory {
    public static Matrix createEmptyMatrix(int rows, int cols) {
        return new ArrayMatrix(rows, cols);
    }

    public static Matrix createIdentityMatrix(int size) {
        Matrix matrix = new ArrayMatrix(size, size);

        for (int i = 0; i < size; i++)
            matrix.setItem(i, i, 1.0);

        return matrix;
    }

    public static Matrix createMatrix(double[][] items) {
        int n = items.length;
        int m = items[0].length;

        Matrix result = new ArrayMatrix(n, m);
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                result.setItem(i, j, items[i][j]);

        return result;
    }

    public static Matrix createMatrixFromRows(Vector[] rows) {
        int n = rows.length;
        int m = rows[0].getLength();

        Matrix result = new ArrayMatrix(n, m);
        for (int i = 0; i < n; i++)
            result.setRow(i, rows[i]);

        return result;
    }

    public static Matrix createMatrixFromColumns(Vector[] columns) {
        int n = columns[0].getLength();
        int m = columns.length;

        Matrix result = new ArrayMatrix(n, m);
        for (int i = 0; i < m; i++)
            result.setColumn(i, columns[i]);

        return result;
    }

    public static Matrix createMatrixFromInput(int rows, int cols, Scanner scanner) {
        Matrix matrix = new ArrayMatrix(rows, cols);

        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                matrix.setItem(i, j, scanner.nextDouble());

        return matrix;
    }
}
