package org.skills.data.managers;

public class Cosmetic {
    private final CosmeticCategory category;
    private final String name;
    private final String displayname;
    private final String color;

    public Cosmetic(CosmeticCategory category, String name, String displayname, String color) {
        this.category = category;
        this.name = name;
        this.displayname = displayname;
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public String getDisplayname() {
        return displayname;
    }

    public String getName() {
        return name;
    }

    public CosmeticCategory getCategory() {
        return category;
    }
}
