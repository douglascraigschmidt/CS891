package edu.vandy.recommender.common.autoconfigure;

/**
 * This class represents a set of common properties for
 * an application component.
 */
public class CommonProperties {
    /**
     * A boolean property that indicates whether the
     * component is enabled or disabled.
     */
    private boolean enabled = false;

    /**
     * Checks whether the component is enabled.
     *
     * @return True if the component is enabled,
     * false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the enabled status of the component.
     *
     * @param enabled A boolean value representing the
     *                enabled status of the component,
     *                which is set to true for enabled,
     *                false for disabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
