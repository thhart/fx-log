package org.hildan.fxlog.view.components;

import java.util.regex.Pattern;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import org.fxmisc.easybind.EasyBind;
import org.hildan.fxlog.coloring.Style;
import org.hildan.fxlog.search.Search;

// FIXME the size of this component goes crazy and makes it unusable
public class SearchableText extends TextFlow {

    private static final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    private final StringProperty text = new SimpleStringProperty();

    private final ObjectProperty<Style> normalStyle = new SimpleObjectProperty<>(Style.DEFAULT);

    private final ObjectProperty<Style> searchMatchStyle = new SimpleObjectProperty<>(Style.HIGHLIGHT_SEARCH);

    private final ObjectProperty<Style> selectedSearchMatchStyle = new SimpleObjectProperty<>(Style.HIGHLIGHT_SEARCH);

    private final ObjectProperty<Font> font = new SimpleObjectProperty<>(Font.getDefault());

    private final Search search;

    private final Text initialText;

    public SearchableText(Search search) {
        this.search = search;

        initialText = createNonMatchingText("");
        initialText.textProperty().bind(text);

        EasyBind.subscribe(normalStyle, style -> style.bindNodes(initialText));

        getChildren().add(initialText);

        EasyBind.subscribe(text, s -> refreshSearch());
        EasyBind.subscribe(search.textProperty(), s -> refreshSearch());
    }

    private Text createNonMatchingText(String str) {
        Text text = new Text(str);
        text.fontProperty().bind(font);

        EasyBind.subscribe(normalStyle, style -> style.bindNodes(text));

        return text;
    }

    private StackPane createMatchingTextPane(String str) {
        Text text = new Text(str);
        text.fontProperty().bind(font);

        StackPane pane = new StackPane();
        pane.getChildren().add(text);

        EasyBind.subscribe(searchMatchStyle, style -> style.bindNodes(text, pane));

        return pane;
    }

    private void refreshSearch() {
        String currentText = text.get();
        String searchText = search.getText();
        if (currentText == null || currentText.isEmpty() || searchText == null || searchText.isEmpty()) {
            getChildren().setAll(initialText);
            return;
        }
        getChildren().clear();
        // separate the parts of the text that match the search and the others
        String[] parts = splitButKeepDelimiter(currentText, Pattern.quote(searchText));
        for (String part : parts) {
            boolean matchesSearch = part.equals(searchText);
            // each part
            for (String word : splitButKeepDelimiter(part, "\\s")) {
                Node wordNode = matchesSearch ? createNonMatchingText(word) : createMatchingTextPane(word);
                getChildren().add(wordNode);
            }
        }
    }

    private static String[] splitButKeepDelimiter(String text, String delimiterRegex) {
        String splitRegex = String.format(WITH_DELIMITER, delimiterRegex);
        return text.split(splitRegex);
    }

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public Style getNormalStyle() {
        return normalStyle.get();
    }

    public Property<Style> normalStyleProperty() {
        return normalStyle;
    }

    public void setNormalStyle(Style normalStyle) {
        this.normalStyle.set(normalStyle);
    }

    public Style getSearchMatchStyle() {
        return searchMatchStyle.get();
    }

    public Property<Style> searchMatchStyleProperty() {
        return searchMatchStyle;
    }

    public void setSearchMatchStyle(Style searchMatchStyle) {
        this.searchMatchStyle.set(searchMatchStyle);
    }

    public Style getSelectedSearchMatchStyle() {
        return selectedSearchMatchStyle.get();
    }

    public Property<Style> selectedSearchMatchStyleProperty() {
        return selectedSearchMatchStyle;
    }

    public void setSelectedSearchMatchStyle(Style selectedSearchMatchStyle) {
        this.selectedSearchMatchStyle.set(selectedSearchMatchStyle);
    }

    public Font getFont() {
        return font.get();
    }

    public Property<Font> fontProperty() {
        return font;
    }

    public void setFont(Font font) {
        this.font.set(font);
    }
}
