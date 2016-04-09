package com.leuradu.android.bikeapp.repository;

import com.leuradu.android.bikeapp.model.Event;
import com.leuradu.android.bikeapp.model.Favorite;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radu on 09.04.2016.
 */
public class LocalRepository {

    private List<Favorite> mFavorites;
    private List<Event> mEvents;

    public LocalRepository() {
        mFavorites = new ArrayList<>();
        mEvents = new ArrayList<>();
    }

    public List<Favorite> getFavorites() {
        return mFavorites;
    }

    public void setFavorites(List<Favorite> favorites) {
        mFavorites = favorites;
    }

    public void addFavorite(Favorite f) {
        mFavorites.add(f);
    }

    public void removeFavorite(String id) {
        for (Favorite f : mFavorites) {
            if (f.getBackendId().equals(id)) {
                mFavorites.remove(f);
                return;
            }
        }
    }

    public void clearFavorites() {
        mFavorites = new ArrayList<>();
    }

    public List<Event> getEvents() {
        return mEvents;
    }

    public void setEvents(List<Event> events) {
        mEvents = events;
    }

    public void addEvent(Event e) {
        mEvents.add(e);
    }

    public void removeEvent(String id) {
        for (Event e : mEvents) {
            if (e.getBackendId().equals(id)) {
                mEvents.remove(e);
                return;
            }
        }
    }

    public void clearEvents() {
        mEvents = new ArrayList<>();
    }
}
