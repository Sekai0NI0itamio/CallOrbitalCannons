package com.callorbitalcannons.item;

/**
 * Weapon modes carried by the special fishing rods. The mode is stored in
 * item data (never inferred from the display name), so renaming an ordinary
 * fishing rod cannot forge a weapon.
 */
public enum WeaponMode {
    NUKE("nuke", "Orbital Fishing Rod"),
    STAB("stab", "Stab Shot Fishing Rod");

    private final String id;
    private final String displayName;

    WeaponMode(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    /** Parses a stored mode id; unknown values yield null (not a weapon). */
    public static WeaponMode fromId(String id) {
        if (id == null) {
            return null;
        }
        for (WeaponMode mode : values()) {
            if (mode.id.equalsIgnoreCase(id)) {
                return mode;
            }
        }
        return null;
    }
}
