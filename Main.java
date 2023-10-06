import structures.VectorFactory;
import structures.MatrixFactory;
import structures.Vector;
import structures.Matrix;
import structures.Inverser;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Simplex simplex = input();
        if (simplex == null) return;

        if (simplex.checkApplicability() != 1) {
            simplex.iteration0();
            simplex.goRevisedSimplex();
        }
    }

    private static Simplex input() {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("Do we need to maximize or minimize function? Enter \"max\" or \"min\" without quotation marks");
            String opt = scanner.nextLine().toLowerCase();
            if (!opt.equals("max") && !opt.equals("min"))
                throw new Exception();

            System.out.println("Enter the number of variables");
            int n = Integer.parseInt(scanner.nextLine());
            System.out.println("Enter the number of constraints");
            int m = Integer.parseInt(scanner.nextLine());

            System.out.println("Enter a vector of coefficients of objective function - c.");
            Vector c = VectorFactory.createVectorFromInput(n, scanner);
            System.out.println("Enter a matrix of coefficients of constraint function - A.");
            Matrix A = MatrixFactory.createMatrixFromInput(m, n, scanner);
            System.out.println("Enter a vector of right-hand side numbers - b.");
            Vector b = VectorFactory.createVectorFromInput(m, scanner);

            System.out.println("Enter an approximation accuracy - epsilon.");
            double e = scanner.nextDouble();

            Simplex simplex = Simplex.builder()
                    .setOptimize(opt)
                    .setNumberOfVariablesN(n)
                    .setNumberOfConstraintsM(m)
                    .setVectorC(c)
                    .setMatrixA(A)
                    .setVectorB(b)
                    .setEpsilon(e)
                    .build();

            return simplex;
        } catch (Exception ex) {
            System.out.println("Wrong input!");
            return null;
        }
    }

}

class Simplex {
    String optimize; // MAX or MIN
    int numberOfVariablesN;
    int numberOfConstraintsM;
    Vector c; // (N)
    Matrix A; // (M*N)
    Vector b; // (M)
    String epsilon; // approximation accuracy
    Vector basisIndexes;
    Vector cBasis;
    Vector xBasis;
    Matrix B;
    Matrix BInverse;
    Vector zRow;
    Matrix nonBasicVectors;
    Vector cNonBasic;
    Vector nonBasicIndexes;
    double solution;

    public Simplex() {
        optimize = "MAX";
        numberOfVariablesN = 0;
        numberOfConstraintsM = 0;
        epsilon = "";
    }

    int checkApplicability() {
        for (int i = 0; i < b.getLength(); i++) {
            if (b.getItem(i) < 0) {
                System.out.println("The method is not applicable!");
                return 1;
            }
        }
        Vector basis = getBasis();
        if (basis != null) {
            basisIndexes = basis;
            return 0;
        }
        System.out.println("The method is not applicable!");
        return 1;
    }

    // check if the matrix A contains Identity matrix and if so returns basis vector (indexes)
    Vector getBasis() {
        Vector basis = VectorFactory.createEmptyVector(numberOfConstraintsM);
        int basicVectors = 0;
        for (int i = 0; i < A.getNumberOfColumns(); i++) {
            Vector columnVector = A.getColumn(i);
            int index1 = -1;
            int numOf0 = 0;
            for (int j = 0; j < columnVector.getLength(); j++) {
                double item = columnVector.getItem(j);
                if (item == 1) {
                    index1 = j;
                } else if (item == 0) {
                    numOf0++;
                } else {
                    break;
                }
            }
            if (index1 != -1 && numOf0 == numberOfConstraintsM - 1) {
                basis.setItem(index1, i);
                basicVectors++;
            }
        }
        if (basicVectors == numberOfConstraintsM) {
            return basis;
        }
        return null;
    }

    void iteration0() {
        cBasis = VectorFactory.createEmptyVector(basisIndexes.getLength());
        B = MatrixFactory.createEmptyMatrix(basisIndexes.getLength(), basisIndexes.getLength());
        for (int i = 0; i < basisIndexes.getLength(); i++) {
            cBasis.setItem(i, c.getItem((int) basisIndexes.getItem(i)));
            B.setColumn(i, A.getColumn((int) basisIndexes.getItem(i)));
        }
        BInverse = Inverser.calculateInverse(B);
    }

    void goRevisedSimplex() {
        int enteringVector = 0;
        int leavingVector;
        while (enteringVector != -1) {
            setNonBasicVariables();
            xBasis = BInverse.multiplyByVector(b);
            zRow = cBasis.multiply(BInverse.multiply(nonBasicVectors)).minus(cNonBasic);
            solution = cBasis.dotProduct(xBasis);
            enteringVector = optimalityComputations();
            if (enteringVector != -1) {
                leavingVector = feasibilityComputations(enteringVector);
                if (leavingVector != -1) {
                    changeBasis(leavingVector, enteringVector);
                }
            }
        }
        Vector xSolution = VectorFactory.createEmptyVector(numberOfVariablesN);
        for (int i = 0; i < numberOfVariablesN; i++) {
            xSolution.setItem(i, 0);
        }
        for (int i = 0; i < basisIndexes.getLength(); i++) {
            xSolution.setItem((int) basisIndexes.getItem(i), xBasis.getItem(i));
        }
        System.out.print("A vector of decision variables - x* = {");
        int signs = 0;
        boolean afterComma = false;
        for (int i = 0; i < epsilon.length(); i++) {
            if (epsilon.charAt(i) == '.' || epsilon.charAt(i) == ',') {
                afterComma = true;
            }
            if (afterComma && epsilon.charAt(i) >= '0' && epsilon.charAt(i) <= '9') {
                signs++;
            }
        }
        String approximation = "%." + signs + "f ";
        for (double x : xSolution
        ) {
            System.out.printf(approximation, x);
        }
        System.out.println("}");
        if (optimize.equalsIgnoreCase("max")) {
            System.out.print("Maximum ");
        }
        if (optimize.equalsIgnoreCase("min")) {
            System.out.print("Minimum ");
        }
        System.out.printf("value of the objective function " + approximation, solution);
    }

    void setNonBasicVariables() {
        nonBasicVectors = MatrixFactory.createEmptyMatrix(numberOfConstraintsM, numberOfVariablesN - basisIndexes.getLength());
        cNonBasic = VectorFactory.createEmptyVector(numberOfVariablesN - basisIndexes.getLength());
        nonBasicIndexes = VectorFactory.createEmptyVector(numberOfVariablesN - basisIndexes.getLength());
        int next = 0;
        for (int i = 0; i < A.getNumberOfColumns(); i++) {
            boolean inBasis = false;
            for (int j = 0; j < basisIndexes.getLength(); j++) {
                if (basisIndexes.getItem(j) == i) {
                    inBasis = true;
                    break;
                }
            }
            if (!inBasis) {
                nonBasicVectors.setColumn(next, A.getColumn(i));
                cNonBasic.setItem(next, c.getItem(i));
                nonBasicIndexes.setItem(next, i);
                next++;
            }
        }
    }

    int maxPositiveItemIndex(Vector vector) {
        int res = -1;
        double max = 0;
        for (int i = 0; i < vector.getLength(); i++) {
            if (vector.getItem(i) > max) {
                res = i;
                max = vector.getItem(i);
            }
        }
        return res;
    }

    int minNegativeItemIndex(Vector vector) {
        int res = -1;
        double min = 0;
        for (int i = 0; i < vector.getLength(); i++) {
            if (vector.getItem(i) < min) {
                res = i;
                min = vector.getItem(i);
            }
        }
        return res;
    }

    int optimalityComputations() {
        int enteringVector = -1;
        if (optimize.equalsIgnoreCase("max")) {
            enteringVector = minNegativeItemIndex(zRow);
        } else if (optimize.equalsIgnoreCase("min")) {
            enteringVector = maxPositiveItemIndex(zRow);
        }
        if (enteringVector == -1) {
            return enteringVector;
        }
        return (int) nonBasicIndexes.getItem(enteringVector);
    }

    int feasibilityComputations(int entVector) {
        Vector BInvPEnt = BInverse.multiplyByVector(A.getColumn(entVector));
        Vector ratio = VectorFactory.createEmptyVector(A.getNumberOfRows());
        for (int i = 0; i < A.getNumberOfRows(); i++) {
            if (BInvPEnt.getItem(i) != 0) {
                ratio.setItem(i, xBasis.getItem(i) / BInvPEnt.getItem(i));
            } else {
                ratio.setItem(i, 0);
            }
        }
        double min = Double.MAX_VALUE;
        int res = -1;
        for (int i = 0; i < BInvPEnt.getLength(); i++) {
            if (ratio.getItem(i) < min && ratio.getItem(i) > 0) {
                min = ratio.getItem(i);
                res = i;
            }
        }
        if (res == -1) {
            return res;
        }
        return (int) basisIndexes.getItem(res);
    }

    void changeBasis(int leaving, int entering) {
        int indLeaving = 0;
        for (int i = 0; i < basisIndexes.getLength(); i++) {
            if (basisIndexes.getItem(i) == leaving) {
                indLeaving = i;
                break;
            }
        }
        basisIndexes.setItem(indLeaving, entering);
        B.setColumn(indLeaving, A.getColumn(entering));
        cBasis.setItem(indLeaving, c.getItem(entering));
        BInverse = Inverser.calculateInverse(B);
    }

    public static class Builder {
        private String optimize; // MAX or MIN
        private int numberOfVariablesN;
        private int numberOfConstraintsM;
        private Vector c; // (N)
        private Matrix A; // (M*N)
        private Vector b; // (M)
        private String epsilon; // approximation accuracy

        Builder setOptimize(String optimize) {
            this.optimize = optimize;
            return this;
        }

        Builder setNumberOfVariablesN(int numberOfVariablesN) {
            this.numberOfVariablesN = numberOfVariablesN;
            return this;
        }

        Builder setNumberOfConstraintsM(int numberOfConstraintsM) {
            this.numberOfConstraintsM = numberOfConstraintsM;
            return this;
        }

        Builder setVectorC(Vector c) {
            this.c = c;
            return this;
        }

        Builder setMatrixA(Matrix A) {
            this.A = A;
            return this;
        }

        Builder setVectorB(Vector b) {
            this.b = b;
            return this;
        }

        Builder setEpsilon(String e) {
            this.epsilon = e;
            return this;
        }

        public Simplex build() {
            Simplex simplex = new Simplex();
            simplex.optimize = this.optimize;
            simplex.numberOfVariablesN = this.numberOfVariablesN;
            simplex.numberOfConstraintsM = this.numberOfConstraintsM;
            simplex.A = this.A;
            simplex.c = this.c;
            simplex.b = this.b;
            simplex.epsilon = this.epsilon;
            return simplex;
        }
    }

    public static Builder builder() {
        return new Builder();
    }


}

