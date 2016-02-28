package de.tremoneck.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class HueColorPicker extends View {

    private float cBrightness = 0.5F;
    private float cSaturation;
    /**
     * in Fucking degrees
     */
    private float cHue;

    private float pointer_X, pointer_Y;

    private Paint paintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paintBrightnessSlider = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paintBlack = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Bitmap innerCircle;
    private Context context;

    public HueColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        paintCircle.setStyle(Paint.Style.STROKE);
        paintBrightnessSlider.setStyle(Paint.Style.STROKE);
        paintBlack.setStyle(Paint.Style.STROKE);
        paintBlack.setStrokeWidth(2);
        paintBlack.setTextSize(20);
        setColor(0xFFFFFF);
    }

    public int getColor() {
        return Color.HSVToColor(255, new float[]{cHue, cSaturation, cBrightness});
    }

    public void setColor(int color) {
        color = color | 0xFF000000;
        float[] f = new float[3];
        Color.colorToHSV(color, f);
        cHue = f[0];
        cSaturation = f[1];
        cBrightness = f[2];
        paintCircle.setColor(color);
        pointer_Y = (float) Math.sin(Math.toRadians(cHue)) * cSaturation;
        pointer_X = (float) Math.cos(Math.toRadians(cHue)) * cSaturation;
        updateBrightnessShader();
    }

    /**
     * Hue and Satuartion Chooser
     * ID of the colorDisk in the middle
     */
    private final static int HSChooser = 0;
    /**
     * Brightnes Chooser
     */
    private final static int BChooser = 1;
    private final static int OffInterface = -1;

    private int interfaceID;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float unit = getWidth() / 16;
        float x = event.getX() - unit * 8;
        float y = event.getY() - unit * 8;
        float hypo = (float) Math.hypot(x, y);
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if(hypo <= unit * 5){
                interfaceID = HSChooser;
                return HSInteraction(unit, hypo, x ,y);
            } else if(6.25F * unit <= hypo && hypo <= unit * 7.75F && x < 0) {
                interfaceID = BChooser;
                cBrightness = (float) (Math.atan(y / x) / Math.PI) + 0.5F;
                invalidate();
                return true;
            }
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (interfaceID == HSChooser) {
                return HSInteraction(unit, hypo, x ,y);
            } else if (interfaceID == BChooser && x < 0) {
                cBrightness = (float) (Math.atan(y / x) / Math.PI) + 0.5F;
                invalidate();
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP){
            if (interfaceID == HSChooser) {
                HSInteraction(unit, hypo, x ,y);
            } else if (interfaceID == BChooser && x < 0) {
                cBrightness = (float) (Math.atan(y / x) / Math.PI) + 0.5F;
                invalidate();
            }
            interfaceID = OffInterface;
            return true;
        }
        return super.onTouchEvent(event);
    }

    private boolean HSInteraction(float unit, float hypo, float x, float y){
        float unit5 = unit * 5;
        if(hypo <= unit * 5) {
            pointer_X = x / unit5;
            pointer_Y = y / unit5;
        } else {
            float rescaleFactor = unit5 / hypo;
            pointer_X = x * rescaleFactor / unit5;
            pointer_Y = y * rescaleFactor / unit5;
        }
        cSaturation = Math.min(hypo / unit5, 1);
        cHue = (float) Math.toDegrees(Math.atan(y / x));
        if (x < 0) {
            cHue += 180;
        } else if (y < 0) {
            cHue += 360;
        }
        updateBrightnessShader();
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float unit = getWidth() / 16;
        float unit8 = unit * 8;
        paintCircle.setColor(Color.HSVToColor(new float[]{cHue, cSaturation, cBrightness})); //TODO Move elsewhere
        canvas.drawArc(unit, unit, unit * 15, unit * 15, 269.98F, 180.02F, false, paintCircle);
        canvas.drawArc(unit, unit, unit * 15, unit * 15, 89.98F, 180.02F, false, paintBrightnessSlider);
        canvas.drawBitmap(innerCircle, unit * 3, unit * 3, paintBlack);
        canvas.drawCircle(pointer_X * unit * 5 + unit8, pointer_Y * unit * 5 + unit8, unit * 0.2F, paintBlack);
        float sin = (float) (-Math.sin(cBrightness * Math.PI)) * unit;
        float cos = (float) (Math.cos(cBrightness * Math.PI)) * unit;
        canvas.drawLine(sin * 6.25F + unit8, cos * 6.25F + unit8, sin * 7.75F + unit8, cos * 7.75F + unit8, paintBlack);
        if (android.os.Debug.isDebuggerConnected() && BuildConfig.DEBUG) {
            canvas.drawText("B " + cBrightness, 0, 20, paintBlack);
            canvas.drawText("H " + cHue, 0, 40, paintBlack);
            canvas.drawText("S " + cSaturation, 0, 60, paintBlack);
            canvas.drawText("U " + unit, 0, 80, paintBlack);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredWidth = 1000;
        int desiredHeight = 1000;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        int size = Math.min(width, height);

        //MUST CALL THIS
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createCenter();
        invalidate();
        paintCircle.setStrokeWidth(w / 16F * 1.5F);
        paintBrightnessSlider.setStrokeWidth(w / 16 * 1.5F);
        updateBrightnessShader();
    }

    private void createCenter() {
        int height = getHeight() / 16;
        int width = getWidth() / 16;
        int size = Math.min(height, width);

        innerCircle = Bitmap.createBitmap(context.getResources().getDisplayMetrics(), size * 10, size * 10, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(innerCircle);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(size);
        paint.setStyle(Paint.Style.FILL);
        RadialGradient radial_gradient = new RadialGradient(size * 5, size * 5, size * 5, 0xFFFFFFFF,
                0x00FFFFFF, android.graphics.Shader.TileMode.CLAMP);

        int colors[] = new int[13];
        float hsv[] = new float[3];
        hsv[1] = 1;
        hsv[2] = 1;
        for (int i = 0; i < 12; i++) {
            hsv[0] = (360 / 12) * i;
            colors[i] = Color.HSVToColor(hsv);
        }
        colors[12] = colors[0];

        SweepGradient sweep_gradient = new SweepGradient(size * 5, size * 5, colors, null);

        ComposeShader shader = new ComposeShader(sweep_gradient, radial_gradient, PorterDuff.Mode.SRC_OVER);
        paint.setShader(shader);
        canvas.drawCircle(size * 5, size * 5, size * 5, paint);
    }

    private void updateBrightnessShader() {
        float size = getWidth() / 2;
        paintBrightnessSlider.setShader(new SweepGradient(size, size, new int[]{Color.HSVToColor(new float[]{cHue, cSaturation, 0}), Color.HSVToColor(new float[]{cHue, cSaturation, 1F})},
                new float[]{0.25F, 0.75F}));
    }

}
