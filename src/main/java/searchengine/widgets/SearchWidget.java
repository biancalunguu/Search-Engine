package searchengine.widgets;

import javafx.scene.Node;

public interface SearchWidget {

    boolean supports(SearchContext context);

    Node createNode(SearchContext context);
}