package ru.javaops.masterjava.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {


//    private static Result pairsSum(int[] rowA, int[] rowB, int row, int column) {
//        int sum = 0;
//        for (int i = 0; i < rowA.length; i++) {
//            sum += rowA[i] * rowB[i];
//        }
//        System.out.println(String.format("test result %d %d %d ",  sum , row, column));
//        return new Result(sum, row, column);
//    }

    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];
        final int portion = matrixSize / 10;

        CompletionService<Process> futures = new ExecutorCompletionService<>(executor);
        List<Future<Process>> resultList = new ArrayList<>();

        for (int s = 0; s < 10; s++) {
            final int part = s;
            resultList.add(
                    futures.submit(() -> {
                        Process process = new Process();
                        process.compute(matrixA, matrixB, part);
                        return process;
                    })
            );
        }
        while (!resultList.isEmpty()) {
            Future<Process> poll = futures.poll();
            if (poll == null) {
                Thread.sleep(10);
            } else {


                Process result = poll.get();
                int part = result.getPart();
                int[][] res = result.getResult();
                for (int i = part*100; i < (part+1)*100; i++) {
                    matrixC[i] = res[i];
                }

                resultList.remove(poll);
            }
        }

//        int[][] m1 = new int[4][4];
//        m1[0][0]=1;
//        m1[0][1]=1;
//        m1[0][2]=1;
//        m1[0][3]=1;
//        m1[1][0]=1;
//        m1[1][1]=1;
//        m1[1][2]=1;
//        m1[1][3]=1;
//        m1[2][0]=1;
//        m1[2][1]=1;
//        m1[2][2]=1;
//        m1[2][3]=1;
//        m1[3][0]=1;
//        m1[3][1]=1;
//        m1[3][2]=1;
//        m1[3][3]=1;
//        int[][] m2 = new int[4][4];
//        m2[0][0]=2;
//        m2[0][1]=2;
//        m2[0][2]=2;
//        m2[0][3]=2;
//        m2[1][0]=2;
//        m2[1][1]=2;
//        m2[1][2]=2;
//        m2[1][3]=2;
//        m2[2][0]=2;
//        m2[2][1]=2;
//        m2[2][2]=2;
//        m2[2][3]=2;
//        m2[3][0]=2;
//        m2[3][1]=2;
//        m2[3][2]=2;
//        m2[3][3]=2;
//        int[][] ints = partition(m1, m2,2);


        return matrixC;
    }

    public static int[][] partition(int[][] matrixA, int[][] matrixB, int rows) {
        final int[][] matrixC = new int[matrixB.length][matrixA.length];

        int[] rowB = new int[matrixB.length];

        for (int i = 0; i < matrixA.length; i++) {

            for (int k = 0; k < matrixB.length; k++) {
                rowB[k] = matrixB[k][i];
            }

            for (int j = rows*100; j < (rows+1)*100; j++) {
                int sum = 0;
                int[] rowA = matrixA[j];
                for (int k = 0; k < matrixB.length; k++) {
                    sum += rowA[k] * rowB[k];
                }
                matrixC[j][i] = sum;
            }
        }
        return matrixC;
    }

    // TODO optimize by https://habrahabr.ru/post/114797/
    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
//        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixB.length][matrixA.length];

        int[] rowB = new int[matrixB.length];

        for (int i = 0; i < matrixA.length; i++) {

            for (int k = 0; k < matrixB.length; k++) {
                rowB[k] = matrixB[k][i];
            }

            for (int j = 0; j < matrixA.length; j++) {
                int sum = 0;
                int[] rowA = matrixA[j];
                for (int k = 0; k < matrixB.length; k++) {
                    sum += rowA[k] * rowB[k];
                }
                matrixC[j][i] = sum;
            }
        }
        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static class Process {
        private int[][] result;
        private int part;

        public boolean compute(int[][] matrixA, int[][]matrixB, int part){
            this.result = new int[matrixB.length][matrixA.length];
            this.part = part;
            int[] rowB = new int[matrixB.length];

            for (int i = 0; i < matrixA.length; i++) {

                for (int k = 0; k < matrixB.length; k++) {
                    rowB[k] = matrixB[k][i];
                }

                for (int j = part*100; j < (part+1)*100; j++) {
                    int sum = 0;
                    int[] rowA = matrixA[j];
                    for (int k = 0; k < matrixB.length; k++) {
                        sum += rowA[k] * rowB[k];
                    }
                    result[j][i] = sum;
                }
            }
            return true;
        }

        public int[][] getResult() {
            return result;
        }

        public int getPart() {
            return part;
        }
    }

    public static class Result {
        private final int[][] result;
        private int part;

        public Result(int[][] result, int part) {
            this.result = result;
            this.part = part;
        }

        public int[][] getResult() {
            return result;
        }

        public int getPart() {
            return part;
        }
    }
}
