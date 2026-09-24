package info.staticfree.android.robotfindskitten;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.res.AssetManager;

/**
 * Generate items which are not kitten. Loads from either a JSON file or a flat text .nki file.
 *
 * @author <a href="mailto:steve@staticfree.info">Steve Pomeroy</a>
 *
 */
public class NKIFactory {
    private final Random rand = new Random();

    private final ArrayList<String> mNki = new ArrayList<String>();

    private final Context mContext;

    private int mCounter = 0;

    public NKIFactory(Context context) {
        mContext = context;
    }

    /**
     * Reads through the android {@code assets/} folder and loads all the files which end in ".nki"
     * using {@link #loadNkiFile(InputStream)}.
     * 
     * @throws IOException
     */
    public void loadNkiFromAssets() throws IOException {
        final AssetManager assets = mContext.getResources().getAssets();
        for (final String asset : assets.list("")) {
            if (asset.endsWith(".nki")) {
                final InputStream is = assets.open(asset);
                loadNkiFile(is);
                is.close();

            }
        }
        postAddMessages();
    }

    /**
     * Run this after adding all the messages.
     */
    private void postAddMessages() {

        Collections.shuffle(mNki, rand);
    }

    /**
     * @return an item which is not kitten
     */
    public String getNki() {

        final int size = mNki.size();

        if (size == 0) {
            return mContext.getString(R.string.nki_no_nkis);
        } else {
            final String nki = mNki.get(mCounter);

            mCounter = (mCounter + 1) % mNki.size();

            return nki;
        }
    }

    /**
     * An NKI file is plain UTF-8 text, where each item is on its own line.
     *
     * @param nkiFile
     * @throws IOException
     */
    public void loadNkiFile(InputStream nkiFile) throws IOException {

        for (final BufferedReader isReader = new BufferedReader(new InputStreamReader(nkiFile,
                "utf-8"), 16000); isReader.ready();) {
            mNki.add(isReader.readLine());
        }
    }

    /**
     * Pull in all the messages from a JSON file.
     *
     * Eventually, one could have a JSON-serving URL for more!
     *
     * @throws IOException
     * @throws JSONException
     */
    public void loadMessagesJson(InputStream jsonFile) throws IOException, JSONException {

        final StringBuilder jsonString = new StringBuilder();

        for (final BufferedReader isReader = new BufferedReader(new InputStreamReader(jsonFile),
                16000); isReader.ready();) {
            jsonString.append(isReader.readLine());
        }

        final JSONArray msgJson = new JSONArray(jsonString.toString());

        for (int i = 0; i < msgJson.length(); i++) {
            mNki.add(msgJson.getString(i));
        }
    }
}
