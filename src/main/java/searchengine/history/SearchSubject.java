package searchengine.history;

import java.util.ArrayList;
import java.util.List;

public class SearchSubject {

    private final List<SearchObserver> observers = new ArrayList<>();

    public void addObserver(SearchObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(SearchObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(SearchEvent event) {
        for (SearchObserver observer : observers) {
            observer.onSearch(event);
        }
    }
}