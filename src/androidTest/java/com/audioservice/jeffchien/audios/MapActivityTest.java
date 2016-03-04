package com.audioservice.jeffchien.audios;

import android.test.ActivityInstrumentationTestCase2;

import com.audioservice.jeffchien.audios.map.MainMapActivity;

public class MapActivityTest extends ActivityInstrumentationTestCase2<MainMapActivity> {

    public MapActivityTest(){
        super(MainMapActivity.class);
    }

    private MainMapActivity mActivity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }

    public void testActivity(){
        assertNotNull("Activity is not null", mActivity);
    }
}
