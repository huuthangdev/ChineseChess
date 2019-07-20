package com.example.chinesechess.Model;

import java.io.IOException;
import java.io.InputStream;

class Util {

    static int readShort(InputStream in) throws IOException {
        int b0 = in.read();
        int b1 = in.read();
        if (b0 == -1 || b1 == -1) {
            throw new IOException();
        }
        return b0 | (b1 << 8);
    }

    static int readInt(InputStream in) throws IOException {
        int b0 = in.read();
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        if (b0 == -1 || b1 == -1 || b2 == -1 || b3 == -1) {
            throw new IOException();
        }
        return b0 | (b1 << 8) | (b2 << 16) | (b3 << 24);
    }

    static int binarySearch(int vl, int[] vls, int to) {
        int low = 0;
        int high = to - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (vls[mid] < vl) {
                low = mid + 1;
            } else if (vls[mid] > vl) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return -1;
    }

    private static final int[] SHELL_STEP = {0, 1, 4, 13, 40, 121, 364, 1093};

    static void shellSort(int[] mvs, int[] vls, int to) {
        int stepLevel = 1;
        while (SHELL_STEP[stepLevel] < to) {
            stepLevel++;
        }
        stepLevel--;
        while (stepLevel > 0) {
            int step = SHELL_STEP[stepLevel];
            for (int i = step; i < to; i++) {
                int mvBest = mvs[i];
                int vlBest = vls[i];
                int j = i - step;
                while (j >= 0 && vlBest > vls[j]) {
                    mvs[j + step] = mvs[j];
                    vls[j + step] = vls[j];
                    j -= step;
                }
                mvs[j + step] = mvBest;
                vls[j + step] = vlBest;
            }
            stepLevel--;
        }
    }
}