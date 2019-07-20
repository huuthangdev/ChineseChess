package com.example.chinesechess.Model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import com.example.chinesechess.Control.AI;
import com.example.chinesechess.Control.ThietLap;
import com.example.chinesechess.R;


public class BanCo extends View implements com.example.chinesechess.Control.View {


    private static final int WIDTH_CELL_COUNT = 9;
    private static final int HEIGHT_CELL_COUNT = 10;
    private int mPieceTheme = ThietLap.PIECE_THEME_CARTOON;

    private float mCellWidth;
    private Bitmap[] mPiecesBitmap;
    private Canvas mCanvas;
    private AI mGameLogic;
    private RectF mPieceDstRectF;

    public BanCo(Context context) {
        this(context, null);
    }

    public BanCo(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BanCo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadAttributeSet(context, attrs);
        initView();
    }

    private void loadAttributeSet(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BanCo);
        TypedValue outValue = new TypedValue();
        ta.getValue(R.styleable.BanCo_pieceTheme, outValue);
        mPieceTheme = outValue.data;
        ta.recycle();
    }

    private void initView() {
        mGameLogic = new AI(this);
        mPieceDstRectF = new RectF();
        setBackgroundResource(R.drawable.board);
        loadBitmapResources();
    }

    public AI getGameLogic() {
        return mGameLogic;
    }

    public void setPieceTheme(int theme) {
        if (theme == mPieceTheme)
            return;
        mPieceTheme = theme;
        loadBitmapResources();
    }

    private void loadBitmapResources() {
        int[] pieceResArray = ThietLap.PIECE_RES_WOOD;
        if (mPieceTheme == ThietLap.PIECE_THEME_CARTOON) {
            pieceResArray = ThietLap.PIECE_RES_CARTOON;
        }
        mPiecesBitmap = new Bitmap[pieceResArray.length];
        for (int i = 0; i < pieceResArray.length; i++) {
            if (mPiecesBitmap[i] != null && !mPiecesBitmap[i].isRecycled()) {
                mPiecesBitmap[i].recycle();
            }
            mPiecesBitmap[i] = BitmapFactory.decodeResource(getResources(), pieceResArray[i]);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        float widthCell = widthSize * 1.0f / WIDTH_CELL_COUNT;
        float heightCell = heightSize * 1.0f / HEIGHT_CELL_COUNT;
        float cellWidth;
        if (widthCell < 0.1f || heightCell < 0.1f) {
            cellWidth = Math.max(widthCell, heightCell);
        } else {
            cellWidth = Math.min(widthCell, heightCell);
        }
        setMeasuredDimension((int) (cellWidth * WIDTH_CELL_COUNT),
                (int) (cellWidth * HEIGHT_CELL_COUNT));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCellWidth = w * 1.0f / WIDTH_CELL_COUNT;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        this.mCanvas = canvas;
        mGameLogic.drawGameBoard();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int xx = (int) (event.getX() / mCellWidth);
            int yy = (int) (event.getY() / mCellWidth);
            int sq = ViTri.COORD_XY(xx + ViTri.FILE_LEFT, yy + ViTri.RANK_TOP);
            mGameLogic.clickSquare(sq);
            return true;
        }
        return true;
    }

    @Override
    public void postRepaint() {
        postInvalidate();
    }

    @Override
    public void drawPiece(int pc, int xx, int yy) {
        if (mCanvas != null) {
            float x = xx * mCellWidth;
            float y = yy * mCellWidth;
            pc -= 8;
            if (pc > 6) {
                pc--;
            }
            mPieceDstRectF.set(x, y, x + mCellWidth, y + mCellWidth);
            mCanvas.drawBitmap(mPiecesBitmap[pc], null, mPieceDstRectF, null);
        }
    }

    @Override
    public void drawSelected(int xx, int yy) {
        if (mCanvas != null) {
            float x = xx * mCellWidth;
            float y = yy * mCellWidth;
            mPieceDstRectF.set(x, y, x + mCellWidth, y + mCellWidth);
            mCanvas.drawBitmap(mPiecesBitmap[14], null, mPieceDstRectF, null);
        }
    }
}
