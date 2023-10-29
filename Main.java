import structures.VectorFactory;
import structures.MatrixFactory;
import structures.Vector;
import structures.Matrix;
import structures.Inverser;

import java.util.Random;
import java.util.ArrayList;
import java.util.Scanner;


interface Algorithm {

}

public class Main {
    public static void main(String[] args) {
//        ArrayList<Algorithm> algorithms = input();
//        if(algorithms == null) return;
//        Simplex simplex = (Simplex) algorithms.get(0);
//        InteriorPoint interiorPoint = (InteriorPoint) algorithms.get(1);
//
//        if (simplex == null) return;
//        if (simplex.checkApplicability() != 1) {
//            simplex.iteration0();
//            simplex.goRevisedSimplex();
//        }

        // TODO check the methods applicability and if they are applicable, then solve the problem
        // TODO find algorithm to find the starting point for Interior Point method
        // TODO clean code and add comments
        // TODO push into GitHub
        // TODO add README.md


        // Test 1
        InteriorPoint interiorPoint = InteriorPoint.builder()
                .setOptimize("max")
                .setNumberOfVariablesN(6)
                .setNumberOfConstraintsM(3)
                .setVectorC(VectorFactory.createVector(new double[]{9, 10, 16, 0, 0, 0}))
                .setMatrixA(MatrixFactory.createMatrix(new double[][]{{18, 15, 12, 1, 0, 0}, {6, 4, 8, 0, 1, 0}, {5, 3, 3, 0, 0, 1}}))
                .setVectorB(VectorFactory.createVector(new double[]{360, 192, 180}))
                .setEpsilon("0.0001")
                .build();

//        interiorPoint.setStartingPoint(new double[]{1, 1, 1, 315, 174, 169});
        interiorPoint.getStartingPoint();
        interiorPoint.startingPoint.print();





//        interiorPoint.solve();
//
//        System.out.println("\n\n");
//        Simplex simplex = Simplex.builder()
//                .setOptimize("max")
//                .setNumberOfVariablesN(6)
//                .setNumberOfConstraintsM(3)
//                .setVectorC(VectorFactory.createVector(new double[]{9, 10, 16, 0, 0, 0}))
//                .setMatrixA(MatrixFactory.createMatrix(new double[][]{{18, 15, 12, 1, 0, 0}, {6, 4, 8, 0, 1, 0}, {5, 3, 3, 0, 0, 1}}))
//                .setVectorB(VectorFactory.createVector(new double[]{360, 192, 180}))
//                .setEpsilon("0.0001")
//                .build();
//        if (simplex == null) return;
//        if (simplex.checkApplicability() != 1) {
//            simplex.iteration0();
//            simplex.goRevisedSimplex();
//        }






        // Test 2
//        InteriorPoint interiorPoint = InteriorPoint.builder()
//                .setOptimize("max")
//                .setNumberOfVariablesN(4)
//                .setNumberOfConstraintsM(2)
//                .setVectorC(VectorFactory.createVector(new double[]{1, 1, 0, 0}))
//                .setMatrixA(MatrixFactory.createMatrix(new double[][]{{2, 4, 1, 0}, {1, 3, 0, -1}}))
//                .setVectorB(VectorFactory.createVector(new double[]{16, 9}))
//                .setEpsilon("0.0001")
//                .build();
//
//        interiorPoint.setStartingPoint(new double[]{0.5, 3.5, 1, 2});
//
//        interiorPoint.solve();


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

class InteriorPoint implements Algorithm {
    String optimize; // MAX or MIN
    int numberOfVariablesN;
    int numberOfConstraintsM;
    Vector c; // (N)
    Matrix A; // (M*N)
    Vector b; // (M)
    String epsilon; // approximation accuracy

    Vector startingPoint;

    double alpha1 = 0.5;

    double alpha2 = 0.9;

    double solution;


    public InteriorPoint() {
        optimize = "MAX";
        numberOfVariablesN = 0;
        numberOfConstraintsM = 0;
        epsilon = "";
    }

    public Vector getStartingPoint() {
        startingPoint = VectorFactory.createZeroVector(numberOfVariablesN);
        Random random = new Random();

        while (true) {
            for (int i = 0; i < numberOfVariablesN - numberOfConstraintsM; i++) {
                int intValue = random.nextInt(500); // Adjust the range as needed
                double doubleValue = random.nextDouble();
                startingPoint.setItem(i, (double) intValue+doubleValue);
            }
            for (int i = numberOfVariablesN - numberOfConstraintsM; i < numberOfVariablesN; i++) {
                startingPoint.setItem(i, 1.0);
            }
            startingPoint.print();
            if (A.multiplyByVector(startingPoint).equals(b)) {
                if (startingPoint.getNumberOfZeroElements() < 2) {
                    break;
                }
            }
        }

        return startingPoint;
    }

    public Vector setStartingPoint(double[] startingPoint) {
        this.startingPoint = VectorFactory.createVector(startingPoint);
        return this.startingPoint;
    }

    public void solve() {
        if (optimize.equals("MIN") || optimize.equals("min")) {
            c = c.scalarMultiply(-1);
        }

        int iteration = 0;
        while (true) {
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

//            System.out.println("In iteration " + iteration + " we have x* = [");
//            startingPoint.print();
//            System.out.println("]");
            iteration++;

            if (result.minus(temp).getNorm() < Double.parseDouble(epsilon)) {
                break;
            }
        }
        System.out.println("The solution by Interior Point method Algorithm:");
        System.out.println("In the last iteration " + iteration + " we have x* = {");
        startingPoint.print();
        System.out.println("}");
        // Calculate the solution
        if (optimize.equalsIgnoreCase("max")) {
            System.out.print("Maximum ");
        }
        if (optimize.equalsIgnoreCase("min")) {
            System.out.print("Minimum ");
        }
        System.out.printf("value of the objective function " + c.dotProduct(startingPoint));
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