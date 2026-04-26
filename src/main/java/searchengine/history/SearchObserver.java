package searchengine.history;

public interface SearchObserver {

    void onSearch(SearchEvent event);
}