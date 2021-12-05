package com.highresults.robotconnection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class Joystick extends View {

    /* renamed from: X */
    public Integer x = 100;

    /* renamed from: X0 */
    public Integer x0 = 100;

    /* renamed from: Y */
    public Integer y = 100;

    /* renamed from: Y0 */
    public Integer y0 = 100;

    public boolean cleanCanvasFlag = true;
    private final Paint mPaint = new Paint();
    private final Paint startPaint = new Paint();

    public Joystick(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    private static final Rect rectangle = new Rect();
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
            rectangle.bottom = this.y0 + 200;
            rectangle.top = this.y0 - 200;
            rectangle.left = this.x0 - 200;
            rectangle.right = this.x0 + 200;
            canvas.drawRect(rectangle, this.startPaint);
            canvas.drawLine((float) this.x, 0.0f, (float) this.x, (float) getHeight(), this.startPaint);
            canvas.drawLine(0.0f, (float) this.y, (float) getWidth(), (float) this.y, this.startPaint);
            canvas.drawCircle((float) this.x, (float) this.y, 50.0f, this.mPaint);
        }
    }

}
