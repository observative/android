package org.syncloud.apps.sam;

public class App {

    public String id;
    public String name;
    public boolean required;
    public String type;

    public Type appType() {
        try {
            return Type.valueOf(type);
        } catch (Exception ignored) {
            return Type.unknown;
        }
    }

    public static enum Type {unknown, system, admin, user}
}