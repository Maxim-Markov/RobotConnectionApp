package com.highresults.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class Joystick extends View {

    /* renamed from: X */
    public Integer f32X = 100;

    /* renamed from: X0 */
    public Integer f33X0 = 100;

    /* renamed from: Y */
    public Integer f34Y = 100;

    /* renamed from: Y0 */
    public Integer f35Y0 = 100;
    public boolean cleanCanvasFlag = true;
    private Paint mPaint = new Paint();
    private Paint startPaint = new Paint();

    public Joystick(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mPaint.setStyle(Paint.Style.FILL);
        this.mPaint.setColor(-1);
        canvas.drawPaint(this.mPaint);
        this.startPaint.setStyle(Paint.Style.STROKE);
        this.startPaint.setColor(Color.rgb(35, 121, 102));
        this.startPaint.setStrokeWidth(3.0f);
        this.startPaint.setAntiAlias(false);
        if (!this.cleanCanvasFlag) {
            this.mPaint.setAntiAlias(true);
            this.mPaint.setColor(Color.rgb(35, 121, 102));
            Rect rectangle = new Rect();
            rectangle.bottom = this.f35Y0.intValue() + 200;
            rectangle.top = this.f35Y0.intValue() - 200;
            rectangle.left = this.f33X0.intValue() - 200;
            rectangle.right = this.f33X0.intValue() + 200;
            canvas.drawRect(rectangle, this.startPaint);
            canvas.drawLine((float) this.f32X.intValue(), 0.0f, (float) this.f32X.intValue(), (float) getHeight(), this.startPaint);
            canvas.drawLine(0.0f, (float) this.f34Y.intValue(), (float) getWidth(), (float) this.f34Y.intValue(), this.startPaint);
            canvas.drawCircle((float) this.f32X.intValue(), (float) this.f34Y.intValue(), 50.0f, this.mPaint);
        }
    }

    public void setClearCanvas() {
        this.cleanCanvasFlag = true;
    }
}
