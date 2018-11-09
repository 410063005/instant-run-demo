package com.sunmoonblog.appclient;

import com.android.utils.ILogger;

class DummyLogger implements ILogger {
    @Override
    public void error(Throwable t, String msgFormat, Object... args) {

    }

    @Override
    public void warning(String msgFormat, Object... args) {

    }

    @Override
    public void info(String msgFormat, Object... args) {

    }

    @Override
    public void verbose(String msgFormat, Object... args) {

    }
}
