package com.yagi2.eorzeawatchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


public class EorzeaWatchFace extends CanvasWatchFaceService {
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<EorzeaWatchFace.Engine> mWeakReference;

        public EngineHandler(EorzeaWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            EorzeaWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            ResultCallback<DataItemBuffer> {
        final Handler mUpdateTimeHandler = new EngineHandler(this);
        boolean mRegisteredTimeZoneReceiver = false;

        Paint mBackgroundPaint;
        Paint mTextPaint;
        Paint mLTPaint;
        Paint mETPaint;

        boolean mAmbient;
        Calendar mCalendar;
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };
        float mXOffset;
        float mYOffset;

        Bitmap mBackGroundFrameIshgaldColor;
        Bitmap mBackgroundFrameIshgaldGray;
        Bitmap mBackGroundFrameGridanaColor;
        Bitmap mBackGroundFrameGridanaGray;
        Bitmap mBackGroundFrameLimsaColor;
        Bitmap mBackGroundFrameLimsaGray;
        Bitmap mBackGroundFrameUldahColor;
        Bitmap mBackGroundFrameUldahGray;

        GoogleApiClient mGoogleApiClient;

        boolean mLowBitAmbient;

        String BACKGROUND_TAG = "";

        boolean isRound;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            mGoogleApiClient = new GoogleApiClient.Builder(EorzeaWatchFace.this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();

            setWatchFaceStyle(new WatchFaceStyle.Builder(EorzeaWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());
            Resources resources = EorzeaWatchFace.this.getResources();
            initialie(resources);

        }

        private void initialie(Resources resources) {
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(resources.getColor(R.color.background));

            mTextPaint = new Paint();
            mTextPaint = createTextPaint(resources.getColor(R.color.digital_text));

            mLTPaint = new Paint();
            mLTPaint = createTextPaint(resources.getColor(R.color.digital_lt));

            mETPaint = new Paint();
            mETPaint = createTextPaint(resources.getColor(R.color.digital_et));

            mCalendar = Calendar.getInstance();

            mBackGroundFrameIshgaldColor = ((BitmapDrawable) getDrawable(R.drawable.frame_ishguld)).getBitmap();
            mBackgroundFrameIshgaldGray  = ((BitmapDrawable) getDrawable(R.drawable.frame_ishguld_grayscale)).getBitmap();
            mBackGroundFrameGridanaColor = ((BitmapDrawable) getDrawable(R.drawable.frame_gridania)).getBitmap();
            mBackGroundFrameGridanaGray  = ((BitmapDrawable) getDrawable(R.drawable.frame_gridania_grayscale)).getBitmap();
            mBackGroundFrameLimsaColor   = ((BitmapDrawable) getDrawable(R.drawable.frame_limsa)).getBitmap();
            mBackGroundFrameLimsaGray    = ((BitmapDrawable) getDrawable(R.drawable.frame_limsa_grayscale)).getBitmap();
            mBackGroundFrameUldahColor   = ((BitmapDrawable) getDrawable(R.drawable.frame_uldah)).getBitmap();
            mBackGroundFrameUldahGray    = ((BitmapDrawable) getDrawable(R.drawable.frame_uldahgrayscale)).getBitmap();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();


                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
            else {
                unregisterReceiver();
            }

            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            EorzeaWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            EorzeaWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            Resources resources = EorzeaWatchFace.this.getResources();
            isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);

            mTextPaint.setTextSize(textSize / 2);
            mLTPaint.setTextSize(textSize);
            mETPaint.setTextSize(textSize);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mTextPaint.setAntiAlias(!inAmbientMode);
                    mLTPaint.setAntiAlias(!inAmbientMode);
                    mETPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            Bitmap image;
            Bitmap backgroundColor = null;
            Bitmap backgroundGray = null;
            boolean frameFlag = true;

            canvas.drawColor(0, PorterDuff.Mode.CLEAR);

            switch (BACKGROUND_TAG) {
                case "ishgald" :
                    backgroundColor = mBackGroundFrameIshgaldColor;
                    backgroundGray  = mBackgroundFrameIshgaldGray;
                    break;
                case "gridania" :
                    backgroundColor = mBackGroundFrameGridanaColor;
                    backgroundGray  = mBackGroundFrameGridanaGray;
                    break;
                case "limsa" :
                    backgroundColor = mBackGroundFrameLimsaColor;
                    backgroundGray  = mBackGroundFrameLimsaGray;
                    break;
                case "uldah" :
                    backgroundColor = mBackGroundFrameUldahColor;
                    backgroundGray  = mBackGroundFrameUldahGray;
                    break;
                case "none" :
                    frameFlag = false;
                    break;
                default :
                    backgroundColor = mBackGroundFrameIshgaldColor;
                    backgroundGray  = mBackgroundFrameIshgaldGray;
                    break;
            }

            canvas.drawColor(Color.BLACK);

            if (frameFlag) {
                if (isInAmbientMode()) {
                    float scale = (float) bounds.height() / (float) backgroundGray.getHeight();
                    image = Bitmap.createScaledBitmap(backgroundGray, (int)(backgroundGray.getWidth() * scale), (int)(backgroundGray.getHeight() * scale), false);
                }
                else {
                    float scale = (float) bounds.height() / (float) backgroundColor.getHeight();
                    image = Bitmap.createScaledBitmap(backgroundColor, (int) (backgroundColor.getWidth() * scale), (int) (backgroundColor.getHeight() * scale), false);
                }

                canvas.drawBitmap(image, 0, 0, null);
            }

            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);

            String LTtext = mAmbient
                    ? String.format("%02d:%02d", mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE))
                    : String.format("%02d:%02d:%02d", mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), mCalendar.get(Calendar.SECOND));
            ConvertLTtoET.EorzeaTime nowET = ConvertLTtoET.getEorzeaNowTime(now);
            String ETtext = String.format("%02d:%02d", nowET.hour, nowET.min);

            if (!isRound) {
                // 角型
                canvas.drawText("Local Time", mXOffset + 10, mTextPaint.getTextSize() + 25, mTextPaint);
                canvas.drawText(LTtext, mXOffset + 10, mTextPaint.getTextSize() + 25 + (mYOffset * 2f - mYOffset * 1.5f), mLTPaint);
                canvas.drawText("Eorzea Time", mXOffset + 10, mYOffset * 1.5f - 25, mTextPaint);
                canvas.drawText(ETtext, mXOffset + 10, mYOffset * 2 - 25, mETPaint);
            }
            else {
                // 丸型
                canvas.drawText("Local Time", mXOffset + 10, mTextPaint.getTextSize() + 50, mTextPaint);
                canvas.drawText(LTtext, mXOffset + 10, mTextPaint.getTextSize() + 50 + (mYOffset * 2f - mYOffset * 1.5f), mLTPaint);
                canvas.drawText("Eorzea Time", mXOffset + 10, mTextPaint.getTextSize() + 50 + (mYOffset * 2f - mYOffset * 1.5f) + 80, mTextPaint);
                canvas.drawText(ETtext, mXOffset + 10, mTextPaint.getTextSize() + 50 + (mYOffset * 2f - mYOffset * 1.5f) + 80 + (mYOffset * 2f - mYOffset * 1.5f), mETPaint);
            }
        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Log.d("Android Wear ", "connected");

            Wearable.DataApi.addListener(mGoogleApiClient, mDataListener);
            Wearable.DataApi.getDataItems(mGoogleApiClient).setResultCallback(this);
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.d("Android Wear", "Connection failed");
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.d("Android Wear", "Connection failed");
        }

        @Override
        public void onResult(@NonNull DataItemBuffer dataItems) {
            for(DataItem item : dataItems) {
                final Uri uri = item.getUri();
                final String path = uri != null ? uri.getPath() : null;

                if ("/config".equals(path)) {
                    final DataMapItem map = DataMapItem.fromDataItem(item);
                    BACKGROUND_TAG = map.getDataMap().getString("TAG");

                    invalidate();
                }
            }
        }

        DataApi.DataListener mDataListener = new DataApi.DataListener() {
            @Override
            public void onDataChanged(DataEventBuffer dataEventBuffer) {
                final List<DataEvent> events = FreezableUtils.freezeIterable(dataEventBuffer);
                for(DataEvent event : events) {
                    final Uri uri = event.getDataItem().getUri();
                    final String path = uri != null ? uri.getPath() : null;

                    if ("/config".equals(path)) {
                        final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                        BACKGROUND_TAG = map.getString("TAG");

                        invalidate();
                    }
                }
            }
        };
    }
}
