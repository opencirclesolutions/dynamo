package com.ocs.dynamo.ui;

/**
 * Interface for components that HAVE to be build AFTER class instantiation
 * by calling the build method. This is to support bean like creation of
 * components without the need to build them directly from the constructor.
 * 
 * @author Patrick Deenen (patrick.deenen@opencirclesolutions.nl)
 */
public interface Buildable {

    /**
     * Call this method when the UI component has to be created.
     * Preferable this method is only called once in a user vaadin
     * session, e.g. data may be refreshed and UI components may be
     * enabled/disabled or displayed/hidden during a session opposed to
     * building and instantiating all UI components for every interaction.
     */
    void build();
}
