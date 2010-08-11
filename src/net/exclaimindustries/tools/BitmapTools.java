/**
 * BitmapTools.java
 * Copyright (C)2010 Nicholas Killewald
 * 
 * This file is distributed under the terms of the BSD license.
 * The source package should have a LICENCE file at the toplevel.
 */
package net.exclaimindustries.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * BitmapTools are, as you probably guessed, tools for Bitmap manipulation.
 * Static tools, too.
 * 
 * @author Nicholas Killewald
 */
public class BitmapTools {
    /**
     * Creates a new Bitmap that's a scaled version of the given Bitmap, but
     * with the aspect ratio preserved.  Note that this will only scale down; if
     * the image is already smaller than the given dimensions, this will return
     * the same bitmap that was given to it.
     *  
     * @param bitmap Bitmap to scale
     * @param maxWidth max width of new Bitmap, in pixels
     * @param maxHeight max height of new Bitmap, in pixels
     * @return a new, scaled Bitmap, or the old bitmap if no scaling took place, or null if it failed entirely
     */
    public static Bitmap createRatioPreservedDownscaledBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        if(bitmap == null) return null;

        if(bitmap.getHeight() > maxHeight || bitmap.getWidth() > maxWidth) {
            // So, we determine how we're going to scale this, mostly
            // because there's no method in Bitmap to maintain aspect
            // ratio for us.
            double scaledByWidthRatio = ((float)maxWidth) / bitmap.getWidth();
            double scaledByHeightRatio = ((float)maxHeight) / bitmap.getHeight();

            int newWidth = bitmap.getWidth();
            int newHeight = bitmap.getHeight();

            if (bitmap.getHeight() * scaledByWidthRatio <= maxHeight) {
                // Scale it by making the width the max, as scaling the
                // height by the same amount makes it less than or equal
                // to the max height.
                newWidth = maxWidth;
                newHeight = (int)(bitmap.getHeight() * scaledByWidthRatio);
            } else {
                // Otherwise, go by making the height its own max.
                newWidth = (int)(bitmap.getWidth() * scaledByHeightRatio);
                newHeight = maxHeight;
            }

            // Now, do the scaling!  The caller must take care of GCing the
            // original Bitmap.
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        } else {
            // If it's too small already, just return what came in.
            return bitmap;
        }
    }

    /**
     * Creates a new Bitmap that's a downscaled, ratio-preserved version of
     * a file on disk.  I'll admit there's probably a shorter name I could have
     * used, but none came to mind.  The major difference between this and the
     * Bitmap-oriented one is that it will attempt a rough downsampling before
     * it loads the original into memory, which should save tons of RAM and
     * avoid unsightly OutOfMemoryErrors.
     *
     * @param filename location of bitmap to open
     * @param maxWidth max width of new Bitmap, in pixels
     * @param maxHeight max height of new Bitmap, in pixels
     * @return a new, appropriately scaled Bitmap, or null if it failed entirely
     */
    public static Bitmap createRatioPreservedDownscaledBitmapFromFile(String filename, int maxWidth, int maxHeight) {
        // First up, open the Bitmap ONLY for its size, if we can.
        BitmapFactory.Options opts = new BitmapFactory.Options;
        opts.inJustDecodeBounds = true;
        // This will always return null thanks to inJustDecodeBounds.
        BitmapFactory.decodeFile(filename, opts);

        // If the height or width are -1 in opts, we failed.
        if(opts.outHeight < 0 || opts.outWidth < 0)
            return null;

        // Now, determine the best power-of-two to downsample by.
        int tempWidth = opts.outWidth;
        int tempHeight = opts.outHeight;
        int sampleFactor = 1;
        while(true) {
            if(tempWidth / 2 < maxWidth || tempHeight / 2 < maxHeight)
                break;
            tempWidth /= 2;
            tempHeight /= 2;
            sampleFactor *= 2;
        }

        // Good!  Now, let's pop it open and scale it the rest of the way.
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = sampleFactor;
        return createRatioPreservedDownscaledBitmap(BitmapFactory.decodeFile(filename, opts), maxWidth, maxHeight);
    }
}
