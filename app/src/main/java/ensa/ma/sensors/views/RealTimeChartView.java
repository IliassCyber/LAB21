package ensa.ma.sensors.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RealTimeChartView extends View {

    private final List<Float> dataPoints = new ArrayList<>();
    private static final int CAPACITY = 100;

    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint graphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public RealTimeChartView(Context context) {
        super(context);
        init();
    }

    public RealTimeChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        borderPaint.setColor(Color.GRAY);
        borderPaint.setStrokeWidth(2);
        borderPaint.setStyle(Paint.Style.STROKE);

        graphPaint.setColor(Color.BLUE);
        graphPaint.setStrokeWidth(4);
        graphPaint.setStyle(Paint.Style.STROKE);

        labelPaint.setColor(Color.BLACK);
        labelPaint.setTextSize(32);
    }

    public void appendValue(float val) {
        if (dataPoints.size() >= CAPACITY) {
            dataPoints.remove(0);
        }
        dataPoints.add(val);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float padding = 50f;

        // Draw basic axes
        canvas.drawRect(padding, padding, w - padding, h - padding, borderPaint);

        if (dataPoints.size() < 2) {
            canvas.drawText("Collecting data...", w / 3, h / 2, labelPaint);
            return;
        }

        float vMin = dataPoints.get(0);
        float vMax = dataPoints.get(0);
        for (float p : dataPoints) {
            if (p < vMin) vMin = p;
            if (p > vMax) vMax = p;
        }

        if (vMax == vMin) vMax += 1.0f;

        Path plotPath = new Path();
        float xStep = (w - 2 * padding) / (CAPACITY - 1);
        float yScale = (h - 2 * padding) / (vMax - vMin);

        for (int i = 0; i < dataPoints.size(); i++) {
            float curX = padding + i * xStep;
            float curY = h - padding - (dataPoints.get(i) - vMin) * yScale;

            if (i == 0) {
                plotPath.moveTo(curX, curY);
            } else {
                plotPath.lineTo(curX, curY);
            }
        }

        canvas.drawPath(plotPath, graphPaint);
        canvas.drawText("Range: [" + vMin + ", " + vMax + "]", padding, padding - 10, labelPaint);
    }
}
