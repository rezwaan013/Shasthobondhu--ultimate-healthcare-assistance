package com.shasthobondhu.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.shasthobondhu.app.data.model.AppNotification;
import com.shasthobondhu.app.data.remote.FirebaseService;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NotificationRepository {
    private final FirebaseAuth auth;
    private final RealtimeDatabaseRepository realtimeRepository;

    @Inject
    public NotificationRepository(FirebaseService firebaseService,
                                  RealtimeDatabaseRepository realtimeRepository) {
        auth = firebaseService.getAuth();
        this.realtimeRepository = realtimeRepository;
    }

    public LiveData<List<AppNotification>> observeMyNotifications() {
        if (auth.getCurrentUser() == null) {
            return new MutableLiveData<>(Collections.emptyList());
        }
        return realtimeRepository.observeNotifications(auth.getCurrentUser().getUid());
    }

    public void markRead(String notificationId,
                         RealtimeDatabaseRepository.CompletionCallback callback) {
        if (auth.getCurrentUser() == null) {
            if (callback != null) {
                callback.onComplete(false,
                        RealtimeDatabaseRepository.Failure.UNAUTHENTICATED);
            }
            return;
        }
        realtimeRepository.markNotificationRead(auth.getCurrentUser().getUid(),
                notificationId, callback);
    }
}
