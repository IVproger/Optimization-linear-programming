package structures;

import structures.implementations.ArrayMatrix;

public class Inverser {
    /**
     * Method for calculating the inverse of a matrix
     * @param matrix is the source matrix
     * @return the inverse matrix
     */
    public static Matrix calculateInverse(Matrix matrix) {
        if (matrix.getNumberOfRows() != matrix.getNumberOfColumns())
            throw new IllegalArgumentException("Matrix must be square to calculate inverse");

        double determinant = calculateDeterminant(matrix);
        if (determinant == 0) throw new IllegalArgumentException("Matrix is not invertible");

        Matrix inverse = new ArrayMatrix(matrix.getNumberOfRows(), matrix.getNumberOfColumns());

        for (int i = 0; i < matrix.getNumberOfRows(); i++) {
            for (int j = 0; j < matrix.getNumberOfColumns(); j++) {
                Matrix submatrix = calculateSubMatrixExcluding(matrix, i, j);
                double subDeterminant = calculateDeterminant(submatrix);
                inverse.setItem(j, i, Math.pow(-1, i + j) * subDeterminant / determinant);
            }
        }

        return inverse;
    }

    /**
     * Method for calculating the matrix determinant
     * @param matrix is the source matrix
     * @return the determinant
     */
    public static double calculateDeterminant(Matrix matrix) {
        if (matrix.getNumberOfRows() != matrix.getNumberOfColumns())
            throw new IllegalArgumentException("Matrix must be square to calculate determinant");

        if (matrix.getNumberOfRows() == 1) return matrix.getItem(0, 0);

        double determinant = 0;

        for (int i = 0; i < matrix.getNumberOfColumns(); i++) {
            Matrix submatrix = calculateSubMatrixExcluding(matrix, 0, i);
            double subDeterminant = calculateDeterminant(submatrix);
            determinant += Math.pow(-1, i) * matrix.getItem(0, i) * subDeterminant;
        }

        return determinant;
    }

    /**
     * Method for constructing a new matrix from an old one without a row and a column
     * @param matrix is the source matrix
     * @param row is the row to be excluded
     * @param column is the column to be excluded
     * @return new matrix
     */
    public static Matrix calculateSubMatrixExcluding(Matrix matrix, int row, int column) {
        Matrix submatrix = new ArrayMatrix(matrix.getNumberOfRows() - 1, matrix.getNumberOfColumns() - 1);

        int submatrixRow = 0;
        int submatrixColumn = 0;

        for (int i = 0; i < matrix.getNumberOfRows(); i++) {
            if (i == row) continue;

            for (int j = 0; j < matrix.getNumberOfColumns(); j++) {
                if (j == column) continue;

                submatrix.setItem(submatrixRow, submatrixColumn, matrix.getItem(i, j));
                submatrixColumn++;
            }

            submatrixRow++;
            submatrixColumn = 0;
        }

        return submatrix;
    }
}
