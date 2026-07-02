package com.aocc.majorproject.display;

import android.app.Presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.aocc.framework.Image;
import com.aocc.framework.implementation.AndroidImage;

/** Full-screen content on a secondary display. */
public class SecondaryDisplayPresentation extends Presentation {

    private ImageView imageView;
    private TextView overlayText;
    private PendingContent pendingContent;

    public SecondaryDisplayPresentation(Context context, Display display) {
        super(context, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout root = new FrameLayout(getContext());
        root.setBackgroundColor(Color.BLACK);

        imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(imageView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        overlayText = new TextView(getContext());
        overlayText.setTextColor(Color.WHITE);
        overlayText.setTextSize(32f);
        overlayText.setGravity(Gravity.CENTER);
        overlayText.setBackgroundColor(Color.argb(160, 0, 0, 0));
        overlayText.setVisibility(TextView.GONE);
        root.addView(overlayText, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        setContentView(root);
        applyPendingContent();
    }

    public void setMode(SecondaryDisplayMode mode, Image background, String overlayLabel) {
        pendingContent = new PendingContent(mode, background, overlayLabel);
        applyPendingContent();
    }

    private void applyPendingContent() {
        if (imageView == null || pendingContent == null) {
            return;
        }

        Bitmap bitmap = toBitmap(pendingContent.background);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            imageView.setBackgroundColor(Color.BLACK);
        } else {
            imageView.setImageDrawable(null);
            imageView.setBackgroundColor(Color.BLACK);
        }

        String overlayLabel = pendingContent.overlayLabel;
        if (overlayLabel != null && !overlayLabel.isEmpty()) {
            overlayText.setText(overlayLabel);
            overlayText.setVisibility(TextView.VISIBLE);
        } else {
            overlayText.setVisibility(TextView.GONE);
        }
    }

    private static Bitmap toBitmap(Image image) {
        if (image instanceof AndroidImage androidImage) {
            return androidImage.getBitmap();
        }
        return null;
    }

    private static final class PendingContent {
        final SecondaryDisplayMode mode;
        final Image background;
        final String overlayLabel;

        PendingContent(SecondaryDisplayMode mode, Image background, String overlayLabel) {
            this.mode = mode;
            this.background = background;
            this.overlayLabel = overlayLabel;
        }
    }
}
