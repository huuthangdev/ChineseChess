package com.example.chinesechess.Control;
import androidx.annotation.NonNull;
import com.example.chinesechess.Model.TimKiemViTriCo;
import com.example.chinesechess.Model.ViTri;
import com.example.chinesechess.R;
import java.util.ArrayDeque;
import java.util.Deque;

import static com.example.chinesechess.Control.ThietLap.RESP_CAPTURE;
import static com.example.chinesechess.Control.ThietLap.RESP_CAPTURE2;
import static com.example.chinesechess.Control.ThietLap.RESP_CHECK;
import static com.example.chinesechess.Control.ThietLap.RESP_CHECK2;
import static com.example.chinesechess.Control.ThietLap.RESP_CLICK;
import static com.example.chinesechess.Control.ThietLap.RESP_DRAW;
import static com.example.chinesechess.Control.ThietLap.RESP_ILLEGAL;
import static com.example.chinesechess.Control.ThietLap.RESP_LOSS;
import static com.example.chinesechess.Control.ThietLap.RESP_MOVE;
import static com.example.chinesechess.Control.ThietLap.RESP_MOVE2;
import static com.example.chinesechess.Control.ThietLap.RESP_WIN;

public class AI {

    private View mGameView;
    private String currentFen;
    private int sqSelected, mvLast;
    private volatile boolean thinking = false;
    private boolean flipped = false;
    private int level = 0;
    private ViTri pos = new ViTri();
    private TimKiemViTriCo search = new TimKiemViTriCo(pos, 16);
    private Deque<String> mHistoryList = new ArrayDeque<>();
    private GameCallBack mGameCallback;

    public AI(View gameView) {
        this(gameView, null);
    }

    private AI(@NonNull View gameView, GameCallBack callback) {
        mGameCallback = callback;
        mGameView = gameView;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setCallback(GameCallBack callback) {
        this.mGameCallback = callback;
    }

    public void drawGameBoard() {
        for (int x = ViTri.FILE_LEFT; x <= ViTri.FILE_RIGHT; x++) {
            for (int y = ViTri.RANK_TOP; y <= ViTri.RANK_BOTTOM; y++) {
                int sq = ViTri.COORD_XY(x, y);
                sq = (flipped ? ViTri.SQUARE_FLIP(sq) : sq);
                int xx = x - ViTri.FILE_LEFT;
                int yy = y - ViTri.RANK_TOP;
                int pc = pos.squares[sq];
                if (pc > 0) {
                    mGameView.drawPiece(pc, xx, yy);
                }
                if (sq == sqSelected || sq == ViTri.SRC(mvLast) ||
                        sq == ViTri.DST(mvLast)) {
                    mGameView.drawSelected(xx, yy);
                }
            }
        }
    }

    public String getCurrentFen() {
        return currentFen;
    }

    public void restart(boolean flipped, int handicap) {
        if (!thinking) {
            this.flipped = flipped;
            int index = (handicap >= ViTri.STARTUP_FEN.length || handicap < 0) ? 0 : handicap;
            currentFen = ViTri.STARTUP_FEN[index];
            mHistoryList.clear();
            startPlay();
        }
    }

    public void retract() {
        if (!thinking) {
            String fen = popHistory();
            if (fen != null) {
                currentFen = fen;
                startPlay();
            }
        }
    }

    private void startPlay() {
        pos.fromFen(currentFen);
        sqSelected = mvLast = 0;
        if (flipped && pos.sdPlayer == 0) {
            thinking();
        } else {
            mGameView.postRepaint();
        }
    }

    private void blockRepaint() {
        mGameView.postRepaint();
    }

    public void clickSquare(int sq_) {
        if (thinking) {
            return;
        }
        int sq = (flipped ? ViTri.SQUARE_FLIP(sq_) : sq_);
        int pc = pos.squares[sq];
        if ((pc & ViTri.SIDE_TAG(pos.sdPlayer)) != 0) {
            if (sqSelected > 0) {
                drawSquare();
            }
            if (mvLast > 0) {
                drawMove();
                mvLast = 0;
            }
            sqSelected = sq;
            drawSquare();
            playSound(RESP_CLICK);
            mGameView.postRepaint();
        } else if (sqSelected > 0) {
            int mv = ViTri.MOVE(sqSelected, sq);
            if (!pos.legalMove(mv)) {
                return;
            }
            if (!pos.makeMove(mv)) {
                playSound(RESP_ILLEGAL);
                return;
            }
            int response = pos.inCheck() ? RESP_CHECK :
                    pos.captured() ? RESP_CAPTURE : RESP_MOVE;
            if (pos.captured()) {
                pos.setIrrev();
            }
            mvLast = mv;
            sqSelected = 0;
            drawMove();
            playSound(response);
            if (!getResult()) {
                thinking();
            } else {
                mGameView.postRepaint();
            }
        }
    }

    private void drawSquare() {
    }

    private void drawMove() {
    }

    private void playSound(int response) {
        if (mGameCallback != null) {
            mGameCallback.postPlaySound(response);
        }
    }

    private void showMessage(int stringResId) {
        if (mGameCallback != null) {
            mGameCallback.postShowMessage(stringResId);
        }
    }

    private void thinking() {
        thinking = true;
        new Thread() {
            public void run() {
                mGameCallback.postStartThink();
                search.prepareSearch();
                blockRepaint();
                mvLast = search.searchMain(100 << level);
                pos.makeMove(mvLast);
                drawMove();
                drawMove();
                int response = pos.inCheck() ? RESP_CHECK2 :
                        pos.captured() ? RESP_CAPTURE2 : RESP_MOVE2;
                if (pos.captured()) {
                    pos.setIrrev();
                }
                getResult(response);
                thinking = false;
                mGameView.postRepaint();
                mGameCallback.postEndThink();
            }
        }.start();
    }

    private boolean getResult() {
        return getResult(-1);
    }

    private boolean getResult(int response) {
        if (pos.isMate()) {
            playSound(response < 0 ? RESP_WIN : RESP_LOSS);
            showMessage(response < 0 ?
                    R.string.congratulations_you_win : R.string.you_lose_and_try_again);
            return true;
        }
        int vlRep = pos.repStatus(3);
        if (vlRep > 0) {
            vlRep = (response < 0 ? pos.repValue(vlRep) : -pos.repValue(vlRep));
            playSound(vlRep > ViTri.WIN_VALUE ? RESP_LOSS :
                    vlRep < -ViTri.WIN_VALUE ? RESP_WIN : RESP_DRAW);
            showMessage(vlRep > ViTri.WIN_VALUE ?
                    R.string.play_too_long_as_lose : vlRep < -ViTri.WIN_VALUE ?
                    R.string.pc_play_too_long_as_lose : R.string.standoff_as_draw);
            return true;
        }
        if (pos.moveNum > 100) {
            playSound(RESP_DRAW);
            showMessage(R.string.both_too_long_as_draw);
            return true;
        }
        if (response >= 0) {
            playSound(response);
            pushHistory(currentFen);
            currentFen = pos.toFen();
        }
        return false;
    }

    private void pushHistory(String fen) {
        if (mHistoryList.size() >= ThietLap.MAX_HISTORY_SIZE) {
            mHistoryList.poll();
        }
        mHistoryList.offer(fen);
    }

    private String popHistory() {
        if (mHistoryList.size() == 0) {
            showMessage(R.string.no_more_histories);
            playSound(RESP_ILLEGAL);
            return null;
        }
        playSound(RESP_MOVE2);
        return mHistoryList.pollLast();
    }
}
