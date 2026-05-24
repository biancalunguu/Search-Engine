package searchengine.widgets;

import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;

public class WidgetFactory {

    private final List<SearchWidget> availableWidgets;

    public WidgetFactory() {
        this.availableWidgets = List.of(
                new AnalyzeLogsWidget(),
                new GalleryWidget()
        );
    }

    public List<Node> createWidgets(SearchContext context) {
        List<Node> activeWidgets = new ArrayList<>();

        for (SearchWidget widget : availableWidgets) {
            if (widget.supports(context)) {
                activeWidgets.add(widget.createNode(context));
            }
        }

        return activeWidgets;
    }
}