package com.shasthobondhu.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.shasthobondhu.app.data.model.AppNotification;
import com.shasthobondhu.app.data.repository.NotificationRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NotificationViewModel extends ViewModel {
    private final NotificationRepository repository;
    private final LiveData<List<AppNotification>> notifications;

    @Inject
    public NotificationViewModel(NotificationRepository repository) {
        this.repository = repository;
        notifications = repository.observeMyNotifications();
    }

    public LiveData<List<AppNotification>> getNotifications() {
        return notifications;
    }

    public void markRead(AppNotification notification) {
        if (notification != null && !notification.read) {
            repository.markRead(notification.id, null);
        }
    }
}
