/*
 * Android SDK for Piwik
 *
 * @link https://github.com/piwik/piwik-android-sdk
 * @license https://github.com/piwik/piwik-sdk-android/blob/master/LICENSE BSD-3 Clause
 */

package org.piwik.sdk;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.piwik.sdk.tools.Logy;

import java.net.URL;
import java.util.Iterator;
import java.util.List;


public class TrackerBulkURLWrapper {
    private static final String LOGGER_TAG = Piwik.LOGGER_PREFIX + "TrackerBulkURLWrapper";
    private static final int EVENTS_PER_PAGE = 20;
    private int mCurrentPage = 0;
    private final int mPages;
    private final URL mApiUrl;
    private final String mAuthtoken;
    private final List<String> mEvents;

    public TrackerBulkURLWrapper(final URL apiUrl, final List<String> events, final String authToken) {
        mApiUrl = apiUrl;
        mAuthtoken = authToken;
        mPages = (int) Math.ceil(events.size() * 1.0 / EVENTS_PER_PAGE);
        mEvents = events;
    }

    protected static int getEventsPerPage(){
        return EVENTS_PER_PAGE;
    }

    /**
     * page iterator
     *
     * @return iterator
     */
    public Iterator<Page> iterator() {
        return new Iterator<Page>() {
            @Override
            public boolean hasNext() {
                return mCurrentPage < mPages;
            }

            @Override
            public Page next() {
                if (hasNext()) {
                    return new Page(mCurrentPage++);
                }
                return null;
            }

            @Override
            public void remove() {
            }
        };
    }

    public URL getApiUrl() {
        return mApiUrl;
    }

    /**
     * {
     * "requests": ["?idsite=1&url=http://example.org&action_name=Test bulk log Pageview&rec=1",
     * "?idsite=1&url=http://example.net/test.htm&action_name=Another bul k page view&rec=1"],
     * "token_auth": "33dc3f2536d3025974cccb4b4d2d98f4"
     * }
     *
     * @return json object
     */
    public JSONObject getEvents(Page page) {
        if (page == null || page.isEmpty()) {
            return null;
        }

        List<String> pageElements = mEvents.subList(page.fromIndex, page.toIndex);

        if(pageElements.size() == 0){
            Logy.w(LOGGER_TAG, "Empty page");
            return null;
        }

        JSONObject params = new JSONObject();
        try {
            params.put("requests", new JSONArray(pageElements));

            if (mAuthtoken != null) {
                params.put(QueryParams.AUTHENTICATION_TOKEN.toString(), mAuthtoken);
            }
        } catch (JSONException e) {
            Logy.w(LOGGER_TAG, "Cannot create json object", e);
            Logy.i(LOGGER_TAG, TextUtils.join(", ", pageElements));
            return null;
        }
        return params;
    }

    /**
     * @param page Page object
     * @return tracked url. For example
     *  "http://domain.com/piwik.php?idsite=1&url=http://a.org&action_name=Test bulk log Pageview&rec=1"
     */
    public String getEventUrl(Page page) {
        if (page == null || page.isEmpty()) {
            return null;
        }

        return getApiUrl().toString() + mEvents.get(page.fromIndex);
    }

    public final class Page {

        protected final int fromIndex, toIndex;

        protected Page(int pageNumber) {
            if (!(pageNumber >= 0 || pageNumber < mPages)) {
                fromIndex = toIndex = -1;
                return;
            }
            fromIndex = pageNumber * EVENTS_PER_PAGE;
            toIndex = Math.min(fromIndex + EVENTS_PER_PAGE, mEvents.size());
        }

        public int elementsCount() {
            return toIndex - fromIndex;
        }

        public boolean isEmpty() {
            return fromIndex == -1 || elementsCount() == 0;
        }
    }

}
