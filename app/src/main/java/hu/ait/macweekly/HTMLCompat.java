package hu.ait.macweekly;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;

/**
 * Code from https://stackoverflow.com/questions/37904739/html-fromhtml-deprecated-in-android-n
 */

public class HTMLCompat {
    private static String LOG_TAG = "HTMLCOMPAT";

    private static HTMLCompat htmlCompat = null;
    private static Context context = null;

    private HTMLCompat(){}

    public static HTMLCompat getInstance(Context c) {
        if(htmlCompat == null) {
            htmlCompat = new HTMLCompat();
            context = c;
        }
        return htmlCompat;
    }

    @SuppressWarnings("deprecation")
    public Spanned fromHtml(String source) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    @SuppressWarnings("deprecation")
    public Spanned fromImageHtml(String source, TextView tv, Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY, new URLImageParser(tv, context), new ImageTagParser());
        } else {
            return Html.fromHtml(source, new URLImageParser(tv, context), new ImageTagParser());
        }
    }

    private class ImageTagParser implements Html.TagHandler {
        @Override
        public void handleTag(boolean b, String s, Editable editable, XMLReader xmlReader) {
            HashSet<String> figureTags = new HashSet<>();
            String[] fTags = {"figcaption", "figure"};
            for(String tag : fTags) figureTags.add(tag);
            if(!b && s.equals("img")) {
                //Adds nice spacing after an image is found in html body
                editable.append("\n\n");
            }
//            else if(figureTags.contains(s)) { // TODO: 2/12/18 Get css figure tags 
//                editable.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.white)), 0, Math.min(editable.length(), 30), 1);
//            }
        }
    }

    private class URLImageParser implements Html.ImageGetter {
        Context c;
        TextView container;
        int width = -1;
        int height = -1;
        int maxWidth = -1;

        /***
         * Construct the URLImageParser which will execute AsyncTask and refresh the container
         * @param t
         * @param c
         */
        URLImageParser(TextView t, Context c) {
            this.c = c;
            this.container = t;
        }

        public Drawable getDrawable(String source) {
            URLDrawable urlDrawable = new URLDrawable();

            // get the actual source
            ImageGetterAsyncTask asyncTask =
                    new ImageGetterAsyncTask(urlDrawable);

            asyncTask.execute(source);

            // return reference to URLDrawable where I will change with actual image from
            // the src tag
            return urlDrawable;
        }

        public class ImageGetterAsyncTask extends AsyncTask<String, Void, BitmapDrawable>  {
            URLDrawable urlDrawable;

            public ImageGetterAsyncTask(URLDrawable d) {
                this.urlDrawable = d;
            }

            @Override
            protected BitmapDrawable doInBackground(String... params) {
                String source = params[0];
                return fetchDrawable(source, container);
            }

            //From here: https://stackoverflow.com/questions/7870312/android-imagegetter-images-overlapping-text
            @Override
            protected void onPostExecute(BitmapDrawable result) {
                // set the correct bound according to the result from HTTP call
                if(height == -1 || width == -1) Log.e(LOG_TAG, "Error: Width or height not set on image");
                urlDrawable.setBounds(0, 0, width, height);

                // change the reference of the current drawable to the result
                // from the HTTP call
                urlDrawable.drawable = result;

                // redraw the image by invalidating the container
                URLImageParser.this.container.invalidate();

                // For ICS
                URLImageParser.this.container.setHeight(URLImageParser.this.container.getHeight()+ height);

                // Pre ICS
                URLImageParser.this.container.setEllipsize(null);
            }

            /***
             * Get the Drawable from URL
             * @param urlString
             * @return
             */
            public BitmapDrawable fetchDrawable(String urlString, View container) {
                try {
                    InputStream is = fetch(urlString);

                    Bitmap baseBitmap = null;
                    try {
                        baseBitmap = BitmapFactory.decodeStream(is);
                    } catch(Exception e) {
                        Log.e(LOG_TAG, "Issue creating bitmap from input stream");
                    }

                    BitmapDrawable drawable = new BitmapDrawable(context.getResources(), baseBitmap);
                    //Help for this: https://stackoverflow.com/questions/10055883/how-can-i-shrink-a-drawable-to-match-the-width-a-textview

                    maxWidth = container.getMeasuredWidth();
                    //Calculate image dimens with a constant size (WIP)
//                    width = (int) Math.min(((float)drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight()) * 800, maxWidth);
//                    height = (int) (originalHeight / originalWidth * width);

                    height = (int) (((float) maxWidth / drawable.getIntrinsicWidth()) * (float) drawable.getIntrinsicHeight());
                    width = maxWidth;
                    drawable.setBounds(0, 0, width, height);

                    return drawable;
                } catch (Exception e) {
                    return null;
                }
            }

            private InputStream fetch(String urlString) throws IOException {
                return (InputStream) new URL(urlString).getContent();
            }
        }
    }

}

//            public BitmapDrawable getCentered(BitmapDrawable drawable, int maxWidth, int width, int height) {
//                Bitmap base = Bitmap.createBitmap(maxWidth, height, Bitmap.Config.ARGB_8888);
//                Canvas canvas = new Canvas(base);
////                canvas.drawARGB(0,255,0,0);
////                return new BitmapDrawable(context.getResources(), base);
//
////                Bitmap base2 = Bitmap.createBitmap(maxWidth/2, height/2, Bitmap.Config.ARGB_8888);
//
////                int color = Color.argb(255,0,100,100);
////                Paint p = new Paint();
////                p.setColor(color);
//
////                canvas.drawBitmap(base2, 0, 0, p);//(maxWidth - width)/2
//
////                Rect source = new Rect(0, 0, maxWidth, height);
////                canvas.drawBitmap(drawable, null, source, null);
////                canvas.drawBitmap(new BitmapDrawable(context.getResources(), drawable), null, source, null);
//
//                BitmapDrawable res = new BitmapDrawable(context.getResources(), drawable.getBitmap());
//                return res;
////                return drawable;
//            }


