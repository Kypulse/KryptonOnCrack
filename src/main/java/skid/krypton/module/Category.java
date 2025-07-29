package skid.krypton.module;

import skid.krypton.utils.EncryptedString;

public enum Category {
    COMBAT(EncryptedString.of("Combat")),
    MISC(EncryptedString.of("Misc")),
    DONUT(EncryptedString.of("Diddy")),
    RENDER(EncryptedString.of("Render")),
    CLIENT(EncryptedString.of("Client"));

    public final CharSequence name;

    Category(final CharSequence name) {
        this.name = name;
    }
}
