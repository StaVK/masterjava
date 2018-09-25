package ru.javaops.masterjava.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {

        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];


        ExecutorService executorService = Executors.newFixedThreadPool(4);

        List<Callable<int[][]>> tasks=new ArrayList<>();

        for (int i = 0; i < matrixSize; i++) {
            tasks.add(new MultMatrix(matrixA, matrixSize, matrixB, matrixC, i));
        }
        executorService.invokeAll(tasks);

        executorService.shutdown();

//        printMatrix(matrixC);
        return matrixC;
    }

    private static class MultMatrix implements Callable<int[][]> {
        private final int[][] matrixA;
        private final int[][] matrixB;
        private final int[][] matrixC;
        private final int matrixSize;
        private final int i;

        public MultMatrix(int[][] matrixA, int matrixSize, int[][] matrixB, int[][] matrixC, int i) {
            this.matrixA = matrixA;
            this.matrixB = matrixB;
            this.matrixC = matrixC;
            this.matrixSize = matrixSize;
            this.i = i;
        }

        @Override
        public int[][] call() throws Exception {
            int thatColumn[] = new int[matrixB.length];
            for (int k = 0; k < matrixSize; k++) {
                thatColumn[k] = matrixB[k][i];
            }
            for (int j = 0; j < matrixSize; j++) {
                int thisRow[] = matrixA[j];
                int sum = 0;

                for (int k = 0; k < matrixSize; k++) {
                    sum += thisRow[k] * thatColumn[k];
                }

                matrixC[j][i] = sum;
            }
            return matrixC;
        }


    }

    // TODO optimize by https://habrahabr.ru/post/114797/
    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

/*        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += matrixA[i][k] * matrixB[k][j];
                }
                matrixC[i][j] = sum;
            }
        }*/


        final int thatColumn[] = new int[matrixB.length];

        for (int jj = 0; jj < matrixSize; jj++) {
            for (int k = 0; k < matrixSize; k++) {
                thatColumn[k] = matrixB[k][jj];
            }

            for (int i = 0; i < matrixSize; i++) {
                final int thisRow[] = matrixA[i];
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += thisRow[k] * thatColumn[k];
                }
                matrixC[i][jj] = sum;
            }
        }
//        3,504 sec
//        0,600 sec
//        printMatrix(matrixC);
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
}
