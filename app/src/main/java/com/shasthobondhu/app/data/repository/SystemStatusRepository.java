package com.shasthobondhu.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.shasthobondhu.app.system.SystemEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

/** Process-wide stream of current device conditions relevant to emergency alerts. */
@Singleton
public class SystemStatusRepository {

    private final MutableLiveData<SystemEvent> status = new MutableLiveData<>();

    @Inject
    public SystemStatusRepository() {
    }

    public LiveData<SystemEvent> getStatus() {
        return status;
    }

    public void publish(SystemEvent event) {
        if (event != null) {
            status.setValue(event);
        }
    }
}
