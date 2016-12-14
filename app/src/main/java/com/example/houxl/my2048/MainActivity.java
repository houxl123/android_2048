package com.example.houxl.my2048;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int _width = 0;
    private int _height = 0;
    private TextView _scoreView = null;
    private TextView _bestView = null;
    private GridLayout _gridLaout = null;
    private PointF _point = new PointF(0, 0);
    private boolean _bSlide = true;
    private int _rowNum = 4;
    List<SlideSturt>  _slideSturts = new ArrayList<>();
    private PieceView _viewListUp[] = new PieceView[10*10];
    private PieceView _viewListDown[] = new PieceView[10*10];
    private PieceView _viewListLeft[] = new PieceView[10*10];
    private PieceView _viewListRight[] = new PieceView[10*10];
    private static int _clickCount = 0;
    private static String[] _strList = new String[]{"来自花疼的爷爷","真的来自花疼的爷爷","说了你还不信是吧,来自花疼的爷爷"};
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        getSceenSize();
        initLayout();
        initData();
        initEvent();
    }

    protected void initLayout() {
        _gridLaout = (GridLayout) findViewById(R.id.PieceGridLayout);
        _gridLaout.removeAllViews();
        _gridLaout.setColumnCount(_rowNum);
        _gridLaout.setRowCount(_rowNum);

        for (int i = 0; i < _rowNum*_rowNum; i++) {
            PieceView view = new PieceView(this);
            view.setLayoutParams(new LinearLayout.LayoutParams((_width - 80) / _rowNum, (_width - 80) / _rowNum));
            _gridLaout.addView(view);
            _viewListLeft[i] = view;
            _viewListRight[(i / _rowNum) * _rowNum + _rowNum-1 - i % _rowNum] = view;
            _viewListUp[(i % _rowNum) * _rowNum + i / _rowNum] = view;
            _viewListDown[(i % _rowNum) * _rowNum + _rowNum -1 - i / _rowNum] = view;
        }
    }

    protected void initData() {
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        _bestView = (TextView)findViewById(R.id.best);
        _bestView.setText(getBestScore());
        _scoreView = (TextView)findViewById(R.id.score);
        _viewListDown[(int)(Math.random()* _rowNum*_rowNum)].setValue(2);
    }

    private void initEvent(){
        Button button = (Button) findViewById(R.id.newgame);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("onClick", "new game");
                for (int i = 0; i <  _rowNum*_rowNum; i++) {
                    _viewListLeft[i].setValue(0);
                }

                _viewListDown[(int) (Math.random() * _rowNum*_rowNum)].setValue(2);
                _scoreView.setText("0");
            }
        });

        button = (Button) findViewById(R.id.icon_w);
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if (_rowNum > 4){
                    --_rowNum;
                    initLayout();
                    Button newGame = (Button) findViewById(R.id.newgame);
                    newGame.callOnClick();
                }
            }
        });

        button = (Button) findViewById(R.id.icon_x);
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if (_rowNum < 7){
                    ++_rowNum;
                    initLayout();
                    Button newGame = (Button) findViewById(R.id.newgame);
                    newGame.callOnClick();
                }
            }
        });

        button = (Button) findViewById(R.id.icon_q);
        button.setOnClickListener(new selfClick());

        LinearLayout  mainLayout = (LinearLayout)findViewById(R.id.mainLayout);
        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!_bSlide) return true;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    _point.set(event.getX(), event.getY());
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    double temX = event.getX() - _point.x;
                    double temY = event.getY() - _point.y;
                    if ((temX > -5 && temX < 5) && (temY > -5 && temY < 5)) return false;

                    if (Math.abs(temX) > Math.abs(temY)) {
                        if (temX > 0) {
                            Log.d("onTouch", "右");
                            Slide(_viewListRight);
                        } else {
                            Log.d("onTouch", "左");
                            Slide(_viewListLeft);
                        }
                    } else {
                        if (temY > 0) {
                            Log.d("onTouch", "下");
                            Slide(_viewListDown);
                        } else {
                            Log.d("onTouch", "上");
                            Slide(_viewListUp);
                        }
                    }
                }

                return true;
            }
        });
    }

    public void getSceenSize() {
        WindowManager wm = this.getWindowManager();

        _width = wm.getDefaultDisplay().getWidth();
        _height = wm.getDefaultDisplay().getHeight();
    }

    protected void Slide(PieceView[] viewList) {
        ///*final*/ List<SlideSturt>  slideSturts = new ArrayList<>();
        _slideSturts.clear();
        for (int i = 0; i < _rowNum; i++) {
            int values[] = new int[_rowNum];
            for (int z=0; z<_rowNum; z++) {
                values[z] = viewList[i * _rowNum + z].getValue();
            };
            for (int j = 0; j < _rowNum-1; j++) {
                if (values[j] == 0){
                    for (int k = j+1; k<_rowNum; k++){
                        if (values[k] != 0){
                            _slideSturts.add(new SlideSturt(viewList[i*_rowNum+k], viewList[i*_rowNum+j], values[k], values[k], this));
                            values[j] = values[k];
                            values[k] = 0;
                            j--;
                            break;
                        }
                    }
                }else{
                    for (int k = j+1; k<_rowNum; k++){
                        if (values[k] == values[j]){
                            _slideSturts.add(new SlideSturt(viewList[i*_rowNum+k], viewList[i*_rowNum+j], values[k], 2*values[k],this));
                            values[j] = 2*values[j];
                            values[k] = 0;
                        }else if (values[k] != 0)break;
                    }
                }
            }
        }

        if (_slideSturts.size() == 0)return;

        ObjectAnimator animator = ObjectAnimator.ofInt(this, "abc", 0, 100);
        animator.setDuration(200);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int cVal = (int) animation.getAnimatedValue();
                for (int i = 0; i < _slideSturts.size(); i++) {
                    SlideSturt tmp = _slideSturts.get(i);
                    float startX = tmp.startView.getX();
                    float startY = tmp.startView.getY();
                    float endX = tmp.endView.getX();
                    float endY = tmp.endView.getY();
                    tmp.tmpView.setX(startX+(endX-startX)*cVal/100);
                    tmp.tmpView.setY(startY + (endY - startY) * cVal/100);
                }
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d("AnimatorListener", "onAnimationStart");
                for (int i = 0; i < _slideSturts.size(); i++) {
                    SlideSturt tmp = _slideSturts.get(i);
                    tmp.startView.setValue(0);
                    _bSlide = false;
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                Log.e("AnimatorListener", "onAnimationRepeat");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.e("AnimatorListener", "onAnimationEnd");
                for (int i = 0; i < _slideSturts.size(); i++) {
                    SlideSturt tmp = _slideSturts.get(i);
                    tmp.endView.setValue(tmp.endValue);
                    tmp.tmpView.setVisibility(View.INVISIBLE);
                    _bSlide = true;
                    if (tmp.startValue*2 == tmp.endValue){
                         int   score = Integer.parseInt(_scoreView.getText().toString().trim());
                        score += tmp.startValue;
                        _scoreView.setText("" + score);
                        int   best = Integer.parseInt(_bestView.getText().toString());
                        if (best<score) {
                            _bestView.setText(""+score);
                            saveBestScore(""+score);
                        }
                    }
                }

                List<PieceView>  zeroView = new ArrayList<>();
                for(int i=0; i< _rowNum*_rowNum; i++){
                    if (_viewListUp[i].getValue() == 0) {
                        zeroView.add(_viewListUp[i]);
                    }
                }
                zeroView.get((int)(Math.random()*zeroView.size())).setValue(2);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.e("AnimatorListener", "onAnimationCancel");
            }
        });

        animator.start();
    }

    private void saveBestScore(String score){
        try {
            String strPath = getApplicationContext().getFilesDir().getAbsolutePath()+"/bestScore";
            File file = new File(strPath);
            if (!file.exists()) {
//                File parent = file.getParentFile();
//                if (!parent.exists()) {
//                    Log.d("getBestScore", "bbb  " + score);
//                    parent.mkdirs();
//                }
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(strPath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeUTF(score);
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getBestScore(){

        try {
            String strPath = getApplicationContext().getFilesDir().getAbsolutePath()+"/bestScore";
            File file = new File(strPath);
            if (!file.exists()) {
                return "0";
            }

            FileInputStream fis = new FileInputStream(strPath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            String str = ois.readUTF();
            Log.d("getBestScore" , str.trim());
            ois.close();
            fis.close();
            return str.trim();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "0";
    }

    class SlideSturt {
        public PieceView tmpView = null;
        public PieceView startView = null;
        public PieceView endView = null;
        public int startValue = 0;
        public int endValue = 0;

        SlideSturt() {
        }

        SlideSturt(PieceView view1, PieceView view2, int value1, int value2, Context context) {
            startView = view1;
            endView = view2;
            startValue = value1;
            endValue = value2;

            RelativeLayout pieceRelative = (RelativeLayout) findViewById(R.id.PieceRelativeLayout);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)_gridLaout.getLayoutParams();
            pieceRelative.setLayoutParams(params);

            tmpView = new PieceView(context);
            tmpView.setLayoutParams(new RelativeLayout.LayoutParams(startView.getWidth(), startView.getHeight()));
            tmpView.setX(startView.getX());
            tmpView.setY(startView.getY());
            tmpView.setValue(startValue);
            pieceRelative.addView(tmpView);
        }
    }

    class selfClick implements View.OnClickListener {
        public void onClick(View v) {
            String strText = _strList[(_clickCount++)%3];
            Toast toast = Toast.makeText(MainActivity.this, strText, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.show();
        }
    }
}
