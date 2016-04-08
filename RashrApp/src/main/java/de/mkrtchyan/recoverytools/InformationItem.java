package de.mkrtchyan.recoverytools;


/**
 * A dummy item representing a piece of content.
 */
public class InformationItem {
    public final String id;
    public final String content;

    public InformationItem(String id, String content) {
        this.id = id;
        this.content = content;
    }

    @Override
    public String toString() {
        return content;
    }
}