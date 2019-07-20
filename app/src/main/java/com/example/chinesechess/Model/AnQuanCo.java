package com.example.chinesechess.Model;


class AnQuanCo {
    private int[] state = new int[256];
    private int x, y;

    private void swap(int i, int j) {
        int t = state[i];
        state[i] = state[j];
        state[j] = t;
    }

    AnQuanCo(byte[] key) {
        x = 0;
        y = 0;
        for (int i = 0; i < 256; i++) {
            state[i] = i;
        }
        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + state[i] + key[i % key.length]) & 0xff;
            swap(i, j);
        }
    }

    private int nextByte() {
        x = (x + 1) & 0xff;
        y = (y + state[x]) & 0xff;
        swap(x, y);
        int t = (state[x] + state[y]) & 0xff;
        return state[t];
    }

    int nextLong() {
        int n0, n1, n2, n3;
        n0 = nextByte();
        n1 = nextByte();
        n2 = nextByte();
        n3 = nextByte();
        return n0 + (n1 << 8) + (n2 << 16) + (n3 << 24);
    }
}
