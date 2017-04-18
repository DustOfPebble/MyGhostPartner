package core.launcher.application;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class Resizer {

    static public Bitmap getScaledBitmap(int Width, int Height, Resources EmbeddedDatas, int Id) {

        BitmapFactory.Options DecodingOptions = new BitmapFactory.Options();
        DecodingOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(EmbeddedDatas, Id, DecodingOptions);

        int IntrinsicWidth = DecodingOptions.outWidth;
        int IntrinsicHeight = DecodingOptions.outHeight;

        float ScalingFactor = Math.min(Width / (float) IntrinsicWidth, Height / (float) IntrinsicHeight);
        int FittedWidth = (int) (IntrinsicWidth * ScalingFactor);
        int FittedHeight = (int) (IntrinsicHeight * ScalingFactor);

         // Calculate sub-sampling to minimize memory consumption
        int SubSamplingFactor = Math.min(IntrinsicWidth / Width, IntrinsicHeight / Height);

        DecodingOptions.inJustDecodeBounds = false;
        DecodingOptions.inSampleSize = SubSamplingFactor;

        Bitmap SubSampledBitmap = BitmapFactory.decodeResource(EmbeddedDatas, Id, DecodingOptions);

        return Bitmap.createScaledBitmap(SubSampledBitmap, FittedWidth, FittedHeight, true);
    }
}