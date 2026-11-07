package com.shasthobondhu.app.di;

import com.shasthobondhu.app.data.repository.PresenceRepository;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/** Gives the Application process-safe access to the singleton presence coordinator. */
@EntryPoint
@InstallIn(SingletonComponent.class)
public interface PresenceEntryPoint {
    PresenceRepository presenceRepository();
}
