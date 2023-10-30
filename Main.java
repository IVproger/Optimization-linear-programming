import structures.VectorFactory;
import structures.MatrixFactory;
import structures.Vector;
import structures.Matrix;
import structures.Inverser;
import java.util.ArrayList;
import java.util.Scanner;

interface Algorithm {}

public class Main {
    public static void main(String[] args) {
        ArrayList<Algorithm> algorithms = input();
        if (algorithms == null) return;

        Simplex simplex = (Simplex) algorithms.get(0);
        InteriorPoint interiorPoint = (InteriorPoint) algorithms.get(1);
        interiorPoint.setStartingPoint();

        //Solving the problem using Simplex algorithm
        System.out.print("\n");
        System.out.println("Simplex method algorithm: ");
        if (simplex == null) return;
        if (simplex.checkApplicability() != 1) {
            simplex.iteration0();
            simplex.goRevisedSimplex();
        }

        //Solving the problem using Interior Point algorithm
        System.out.print("\n");
        System.out.println("Interior Point algorithms: ");
        if (interiorPoint.checkApplicability()) {
            interiorPoint.solve();
        }
    }

    private static ArrayList<Algorithm> input() {
        ArrayList<Algorithm> algorithms = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);

        //Reading the input data
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

            //Initializing data for Simplex algorithm
            algorithms.add(Simplex.builder()
                    .setOptimize(opt)
                    .setNumberOfVariablesN(n)
                    .setNumberOfConstraintsM(m)
                    .setVectorC(c)
                    .setMatrixA(A)
                    .setVectorB(b)
                    .setEpsilon(e)
                    .build());
            //Initializing data for Interior Point algorithm
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

/**
 * Simplex method class
 */
class Simplex implements Algorithm {
    String optimize; // MAX or MIN
    int numberOfVariablesN;
    int numberOfConstraintsM;
    Vector c; // a vector of coefficients of objective function
    Matrix A; // a matrix of coefficients of constraint function
    Vector b; // a vector of right-hand side numbers
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

    /**
     * Method checks whether the Simplex method is applicable
     * @return 1 if not applicable, 0 otherwise
     */
    int checkApplicability() {
        //Checking if a vector of coefficients of objective function contains all zeros
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
        //Checking if a vector of right-hand side numbers contains negative elements
        for (int i = 0; i < b.getLength(); i++) {
            if (b.getItem(i) < 0) {
                System.out.println("The method is not applicable!");
                return 1;
            }
        }
        //Checking if a matrix A contains an identity matrix
        Vector basis = getBasis();
        if (basis != null) {
            basisIndexes = basis;
            return 0;
        }
        System.out.println("The method is not applicable!");
        return 1;
    }

    /**
     * Method check if the matrix A contains Identity matrix and if so returns basis vector
     * @return basis vector
     */
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

    /**
     * Creates matrix B, its inverse and vector c on iteration 0
     */
    void iteration0() {
        cBasis = VectorFactory.createEmptyVector(basisIndexes.getLength());
        B = MatrixFactory.createEmptyMatrix(basisIndexes.getLength(), basisIndexes.getLength());
        for (int i = 0; i < basisIndexes.getLength(); i++) {
            cBasis.setItem(i, c.getItem((int) basisIndexes.getItem(i)));
            B.setColumn(i, A.getColumn((int) basisIndexes.getItem(i)));
        }
        BInverse = Inverser.calculateInverse(B);
    }

    /**
     * Method for Revised Simplex algorithm
     */
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
        for (double x : xSolution) {
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

    /**
     * Method for finding non-basic variables
     */
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

    /**
     * Method finds the maximum positive element in a vector
     */
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

    /**
     * Method finds the minimal negative element in a vector
     */
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

    /**
     * Finds minimal negative or maximum positive (for maximization or minimization problems), determines the new variable to enter
     */
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

    /**
     * Determines the new variable to leave
     */
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

    /**
     * Changing basis for new entering variable
     */
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

    /**
     * Simplex builder
     */
    public static class Builder {
        private String optimize; // MAX or MIN
        private int numberOfVariablesN;
        private int numberOfConstraintsM;
        private Vector c;
        private Matrix A;
        private Vector b;
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

/**
 * Interior Point algorithm
 */
class InteriorPoint implements Algorithm {
    String optimize; // MAX or MIN
    int numberOfVariablesN;
    int numberOfConstraintsM;
    Vector c; // a vector of coefficients of objective function
    Matrix A; // a matrix of coefficients of constraint function
    Vector b; // a vector of right-hand side numbers
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

        for (int i = 0; i < startingPoint.getLength(); i++) {
            if (startingPoint.getItem(i) < 0) {
                System.out.println("The starting point is incorrect! The method is not applicable!");
                System.exit(0);
            }
        }
    }

    /**
     * Method checks algorithm applicability
     * @return true if applicable, false otherwise
     */
    public boolean checkApplicability() {
        //Checking if a vector of coefficients of objective function contains all zeros
        if (c.getNumberOfZeroElements() == c.getLength()) {
            System.out.println("The method is not applicable!");
            return false;
        }
        //Checking if a vector of right-hand side numbers contains negative elements
        for (int i = 0; i < b.getLength(); i++) {
            if (b.getItem(i) < 0) {
                System.out.println("The method is not applicable!");
                return false;
            }
        }
        return true;
    }

    /**
     * Solving problem for different alphas
     */
    public void solve() {
        solveAlpha(alpha1);
        System.out.println();
        solveAlpha(alpha2);
    }

    /**
     * Interior point algorithm for specific alpha
     */
    public void solveAlpha(double alpha) {
        //Change vector c if it is a minimization problem
        if(alpha == 0.9 && optimize.equalsIgnoreCase("min")){
            // Set c to default
            c = c.scalarMultiply(-1);
        }
        if (optimize.equalsIgnoreCase("min")) {
            c = c.scalarMultiply(-1);
        }

        int iteration = 0;
        while (true) {
            try {
                Vector temp = startingPoint;
                Matrix D = MatrixFactory.createIdentityMatrix(numberOfVariablesN);
                D.seatDiagonal(startingPoint);

                Matrix Anew = A.multiply(D);

                Matrix AnewT = Anew.getTransposed();

                Vector Cnew = D.multiplyByVector(c);

                Matrix I = MatrixFactory.createIdentityMatrix(numberOfVariablesN);

                Matrix F = Anew.multiply(AnewT);

                Matrix FI = Inverser.calculateInverse(F);

                Matrix H = AnewT.multiply(FI);

                Matrix P = I.minus(H.multiply(Anew));

                Vector cp = P.multiplyByVector(Cnew);

                double nu = Math.abs(cp.findMinValue());

                Vector ones = VectorFactory.createOnesVector(numberOfVariablesN);

                Vector result = ones.plus(cp.scalarMultiply(alpha / nu));

                result = D.multiplyByVector(result);

                startingPoint = result;

                iteration++;
                if (iteration == 1000) {
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
        System.out.println("The solution by Interior Point method Algorithm with alpha = " + alpha + ":");
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
        for (double x : startingPoint) {
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

    /**
     * Interior point builder
     */
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