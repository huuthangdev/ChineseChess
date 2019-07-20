package com.example.chinesechess.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.blankj.utilcode.util.SnackbarUtils;
import com.example.chinesechess.Control.AI;
import com.example.chinesechess.Control.GameCallBack;
import com.example.chinesechess.Control.ThietLap;
import com.example.chinesechess.Model.BanCo;
import com.example.chinesechess.Model.ViTri;
import com.example.chinesechess.R;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings("ALL")
public class CoTuongActivity extends AppCompatActivity
        implements GameCallBack {

    @BindView(R.id.game_board)
    BanCo mGameBoard;
    @BindView(R.id.game_progress)
    ProgressBar mGameProgress;
    private SoundPool mSoundPool;
    private LinkedList<Integer> mSoundList;
    private AI mGameLogic;
    private SharedPreferences mPreference;
    private boolean mSoundEnable;
    private int mHandicapIndex;
    private boolean mComputerFlip;
    private int mPieceStyle;
    private int mAILevel;

    public CoTuongActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_co_tuong);
        ButterKnife.bind(this);
        mPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loadDefaultConfig();
        initSoundPool();
        initGameLogic();
        loadBookAndStartGame();
        StartGame();
        Toolbar toolbar = findViewById(R.id.toolbarcotuong);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    private void loadBookAndStartGame() {
        new Thread() {
            @Override
            public void run() {
                try {
                    InputStream is = getAssets().open(ThietLap.DAT_ASSETS_PATH);
                    ViTri.loadBook(is);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void StartGame() {
        new Thread() {
            @Override
            public void run() {
                try {
                    mGameLogic.restart(mComputerFlip, mHandicapIndex);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cotuong_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected( @NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_restart:
                mGameLogic.restart(mComputerFlip, mHandicapIndex);
                showMessage(getString(R.string.new_game_started));
                break;
            case R.id.main_menu_retract:
                mGameLogic.retract();
                break;
            case R.id.main_menu_settings:
                startActivity(new Intent(this, CoTuongSettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDefaultConfig();
        mGameLogic.setLevel(mAILevel);
        mGameBoard.setPieceTheme(mPieceStyle);
        mGameBoard.invalidate();
    }

    @Override
    protected void onDestroy() {
        if (mSoundPool != null) {
            mSoundPool.release();
        }
        mPreference.edit().putString(ThietLap.PREF_LAST_FEN, mGameLogic.getCurrentFen()).apply();
        super.onDestroy();
    }

    private void loadDefaultConfig() {
        mSoundEnable = mPreference.getBoolean(getString(R.string.pref_sound_key), true);
        mComputerFlip = mPreference.getBoolean(getString(R.string.pref_who_first_key), false);
        mPieceStyle = Integer.parseInt(mPreference.getString(getString(R.string.pref_piece_style_key), "0"));
        mAILevel = Integer.parseInt(mPreference.getString(getString(R.string.pref_level_key), "0"));
    }

    private void initSoundPool() {
        mSoundList = new LinkedList<>();
        int poolSize = ThietLap.SOUND_RES_ARRAY.length;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = new SoundPool.Builder().setMaxStreams(poolSize).build();
        } else {
            mSoundPool = new SoundPool(poolSize, AudioManager.STREAM_MUSIC, 0);
        }
        for (int res : ThietLap.SOUND_RES_ARRAY) {
            mSoundList.add(mSoundPool.load(this, res, 1));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initGameLogic() {
        mGameLogic = mGameBoard.getGameLogic();
        mGameLogic.setCallback(this);
        mGameLogic.setLevel(mAILevel);
        mGameBoard.setPieceTheme(mPieceStyle);
    }

    @Override
    public void postPlaySound(final int soundIndex) {
        if (mSoundPool != null && mSoundEnable) {
            int soundId = mSoundList.get(soundIndex);
            mSoundPool.play(soundId, 1, 1, 0, 0, 1);
        }
    }

    @Override
    public void postShowMessage(final String message) {
        runOnUiThread(() -> showMessage(message));
    }

    private void showMessage(String message) {
        SnackbarUtils.with(mGameBoard).setDuration(SnackbarUtils.LENGTH_LONG)
                .setMessage(message).show();
    }

    @Override
    public void postShowMessage(int messageId) {
        postShowMessage(getString(messageId));
    }

    @Override
    public void postStartThink() {
        runOnUiThread(() -> mGameProgress.setVisibility(View.VISIBLE));
    }

    @Override
    public void postEndThink() {
        runOnUiThread(() -> mGameProgress.setVisibility(View.GONE));
    }
}

