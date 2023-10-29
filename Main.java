import structures.VectorFactory;
import structures.MatrixFactory;
import structures.Vector;
import structures.Matrix;
import structures.Inverser;

import java.nio.file.SecureDirectoryStream;
import java.util.Random;
import java.util.ArrayList;
import java.util.Scanner;


interface Algorithm {

}

public class Main {
    public static void main(String[] args) {
        ArrayList<Algorithm> algorithms = input();
        if(algorithms == null) return;
        Simplex simplex = (Simplex) algorithms.get(0);
        InteriorPoint interiorPoint = (InteriorPoint) algorithms.get(1);
        interiorPoint.setStartingPoint();

        System.out.print("\n");
        System.out.println("Simplex method algorithm: ");
        if (simplex == null) return;
        if (simplex.checkApplicability() != 1) {
            simplex.iteration0();
            simplex.goRevisedSimplex();
        }

        System.out.print("\n");
        System.out.println("Interior Point algorithms: ");
        if(interiorPoint.checkApplicability()){
            interiorPoint.solveAlpha1();
            interiorPoint.solveAlpha2();
        }
    }

    private static ArrayList<Algorithm> input() {
        ArrayList<Algorithm> algorithms = new ArrayList<>();
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
            String e = scanner.next();


            algorithms.add(Simplex.builder()
                    .setOptimize(opt)
                    .setNumberOfVariablesN(n)
                    .setNumberOfConstraintsM(m)
                    .setVectorC(c)
                    .setMatrixA(A)
                    .setVectorB(b)
                    .setEpsilon(e)
                    .build());

            algorithms.add(InteriorPoint.builder()
                    .setOptimize(opt)
                    .setNumberOfVariablesN(n)
                    .setNumberOfConstraintsM(m)
                    .setVectorC(c)
                    .setMatrixA(A)
                    .setVectorB(b)
                    .setEpsilon(e)
                    .build());


            return algorithms;
        } catch (Exception ex) {
            System.out.println("Wrong input!");
            return null;
        }
    }

}

class Simplex implements Algorithm {
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
        int zerosInC = 0;
        for (int i = 0; i < c.getLength(); i++) {
            if (c.getItem(i) != 0) {
                break;
            }
            if (c.getItem(i) == 0) {
                zerosInC++;
            }
        }
        if (zerosInC == c.getLength()) {
            System.out.println("The method is not applicable!");
            return 1;
        }
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
        System.out.println("The solution by Simplex method Algorithm:");
        System.out.print("A vector of decision variables - x* = { ");
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
        System.out.println();

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

class InteriorPoint implements Algorithm {
    String optimize; // MAX or MIN
    int numberOfVariablesN;
    int numberOfConstraintsM;
    Vector c; // (N)
    Matrix A; // (M*N)
    Vector b; // (M)
    String epsilon; // approximation accuracy

    Vector startingPoint;

    Vector startingPoint1;

    double alpha1 = 0.5;

    double alpha2 = 0.9;

    public InteriorPoint() {
        optimize = "MAX";
        numberOfVariablesN = 0;
        numberOfConstraintsM = 0;
        epsilon = "";
    }

    public void setStartingPoint() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the starting point for Interior Point Algorithm:");
        this.startingPoint = VectorFactory.createVectorFromInput(numberOfVariablesN, scanner);
        this.startingPoint1 = startingPoint;

        for(int i = 0; i < startingPoint.getLength(); i++){
            if(startingPoint.getItem(i) < 0){
                System.out.println("The starting point is incorrect! The method is not applicable!");
                System.exit(0);
            }
        }
    }

    public boolean checkApplicability() {
        if (c.getNumberOfZeroElements() == c.getLength()) {
            System.out.println("The method is not applicable!");
            return false;
        }
        for (int i = 0; i < b.getLength(); i++) {
            if (b.getItem(i) < 0) {
                System.out.println("The method is not applicable!");
                return false;
            }
        }
        return true;
    }

    public void solveAlpha1() {
        if (optimize.equals("MIN") || optimize.equals("min")) {
            c = c.scalarMultiply(-1);
        }

        int iteration = 0;
        while (true) {
            try {
                Vector temp = startingPoint;
                Matrix D = MatrixFactory.createIdentityMatrix(numberOfVariablesN);
                D.seatDiagonal(startingPoint);

                Matrix Anew = A.multiply(D);

                Matrix AnewT = Anew.transpose();

                Vector Cnew = D.multiplyByVector(c);

                Matrix I = MatrixFactory.createIdentityMatrix(numberOfVariablesN);

                Matrix F = Anew.multiply(AnewT);

                Matrix FI = Inverser.calculateInverse(F);

                Matrix H = AnewT.multiply(FI);

                Matrix P = I.minus(H.multiply(Anew));

                Vector cp = P.multiplyByVector(Cnew);

                double nu = Math.abs(cp.findMinValue());

                Vector ones = VectorFactory.createOnesVector(numberOfVariablesN);

                Vector result = ones.plus(cp.scalarMultiply(alpha1 / nu));

                result = D.multiplyByVector(result);

                startingPoint = result;

                iteration++;
                if(iteration == 1000){
                    System.out.println("The problem does not have solution! OR The starting point is incorrect!");
                    System.exit(0);
                }

                if (result.minus(temp).getNorm() < Double.parseDouble(epsilon)) {
                    break;
                }
            } catch (Exception ex) {
                System.out.println("The problem does not have solution! OR The starting point is incorrect!");
                return;
            }
        }
        System.out.println("The solution by Interior Point method Algorithm with alpha1 = 0.5:");
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
        System.out.printf("In the last iteration " + iteration + " we have x* = { ");
        for (double x : startingPoint
        ) {
            System.out.printf(approximation, x);
        }
        System.out.println("}");
        // Calculate the solution
        if (optimize.equalsIgnoreCase("max")) {
            System.out.print("Maximum ");
        }
        if (optimize.equalsIgnoreCase("min")) {
            System.out.print("Minimum ");
        }
        System.out.printf("value of the objective function " + approximation, c.dotProduct(startingPoint));
        System.out.println();
    }

    public void solveAlpha2() {
        int iteration = 0;
        while (true) {
            try {
                Vector temp = startingPoint1;
                Matrix D = MatrixFactory.createIdentityMatrix(numberOfVariablesN);
                D.seatDiagonal(startingPoint1);

                Matrix Anew = A.multiply(D);

                Matrix AnewT = Anew.transpose();

                Vector Cnew = D.multiplyByVector(c);

                Matrix I = MatrixFactory.createIdentityMatrix(numberOfVariablesN);

                Matrix F = Anew.multiply(AnewT);

                Matrix FI = Inverser.calculateInverse(F);

                Matrix H = AnewT.multiply(FI);

                Matrix P = I.minus(H.multiply(Anew));

                Vector cp = P.multiplyByVector(Cnew);

                double nu = Math.abs(cp.findMinValue());

                Vector ones = VectorFactory.createOnesVector(numberOfVariablesN);

                Vector result = ones.plus(cp.scalarMultiply(alpha2 / nu));

                result = D.multiplyByVector(result);

                startingPoint1 = result;

                iteration++;
                if(iteration == 1000){
                    System.out.println("The problem does not have solution! OR The starting point is incorrect!");
                    System.exit(0);
                }

                if (result.minus(temp).getNorm() < Double.parseDouble(epsilon)) {
                    break;
                }
            } catch (Exception ex) {
                System.out.println("The problem does not have solution! OR The starting point is incorrect!");
                return;
            }
        }
        System.out.println("The solution by Interior Point method Algorithm with alpha = 0.9:");
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
        System.out.printf("In the last iteration " + iteration + " we have x* = { ");
        for (double x : startingPoint1
        ) {
            System.out.printf(approximation, x);
        }
        System.out.println("}");
        // Calculate the solution
        if (optimize.equalsIgnoreCase("max")) {
            System.out.print("Maximum ");
        }
        if (optimize.equalsIgnoreCase("min")) {
            System.out.print("Minimum ");
        }
        System.out.printf("value of the objective function " + approximation, c.dotProduct(startingPoint1));
        System.out.println();
    }


    public static class Builder {
        private String optimize; // MAX or MIN
        private int numberOfVariablesN;
        private int numberOfConstraintsM;
        private Vector c; // (N)
        private Matrix A; // (M*N)
        private Vector b; // (M)
        private String epsilon; // approximation accuracy

        InteriorPoint.Builder setOptimize(String optimize) {
            this.optimize = optimize;
            return this;
        }

        InteriorPoint.Builder setNumberOfVariablesN(int numberOfVariablesN) {
            this.numberOfVariablesN = numberOfVariablesN;
            return this;
        }

        InteriorPoint.Builder setNumberOfConstraintsM(int numberOfConstraintsM) {
            this.numberOfConstraintsM = numberOfConstraintsM;
            return this;
        }

        InteriorPoint.Builder setVectorC(Vector c) {
            this.c = c;
            return this;
        }

        InteriorPoint.Builder setMatrixA(Matrix A) {
            this.A = A;
            return this;
        }

        InteriorPoint.Builder setVectorB(Vector b) {
            this.b = b;
            return this;
        }

        InteriorPoint.Builder setEpsilon(String e) {
            this.epsilon = e;
            return this;
        }

        public InteriorPoint build() {
            InteriorPoint interiorPoint = new InteriorPoint();
            interiorPoint.optimize = this.optimize;
            interiorPoint.numberOfVariablesN = this.numberOfVariablesN;
            interiorPoint.numberOfConstraintsM = this.numberOfConstraintsM;
            interiorPoint.A = this.A;
            interiorPoint.c = this.c;
            interiorPoint.b = this.b;
            interiorPoint.epsilon = this.epsilon;
            return interiorPoint;
        }
    }

    public static InteriorPoint.Builder builder() {
        return new InteriorPoint.Builder();
    }
}