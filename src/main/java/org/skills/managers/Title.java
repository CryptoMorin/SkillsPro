package org.skills.managers;

public class Title {
    public final String title;
    public final String subtitle;
    public final int fadeIn;
    public final int stay;
    public final int fadeOut;

    public Title(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }
}
