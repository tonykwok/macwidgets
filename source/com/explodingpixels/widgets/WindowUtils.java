package com.explodingpixels.widgets;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class WindowUtils {

    public static final String FRAME_ACTIVE_PROPERTY = "Frame.active";

    public static WindowFocusListener createAndInstallRepaintWindowFocusListener(Window window) {

        // create a WindowFocusListener that repaints the window on focus
        // changes.
        WindowFocusListener windowFocusListener = new WindowFocusListener() {
            public void windowGainedFocus(WindowEvent e) {
                e.getWindow().repaint();
            }

            public void windowLostFocus(WindowEvent e) {
                e.getWindow().repaint();
            }
        };

        window.addWindowFocusListener(windowFocusListener);

        return windowFocusListener;
    }

    public static boolean isParentWindowFocused(Component component) {
        Window window = SwingUtilities.getWindowAncestor(component);
        return window != null && window.isFocused();
    }

    // TODO fix this method - doesn't work across platforms.
    public static void installWindowFocusListener(
            WindowFocusListener focusListener, JComponent component) {
        // TODO add null argument checks.
        component.addPropertyChangeListener(FRAME_ACTIVE_PROPERTY,
                createFrameFocusPropertyChangeListener(focusListener, component));
    }

    /**
     * Installs a listener on the given {@link JComponent}'s parent {@link Window} that repaints
     * the given component when the parent window's focused state changes. If the given component
     * does not have a parent at the time this method is called, then an ancestor listener will be
     * installed that installs a window listener when the components parent changes.
     *
     * @param component the {@code JComponent} to add the repaint focus listener to.
     */
    public static void installJComponentRepainterOnWindowFocusChanged(JComponent component) {
        // TODO check to see if the component already has an ancestor.
        component.addAncestorListener(createAncestorListener(component,
                createWindowListener(component)));
    }

    private static AncestorListener createAncestorListener(
            final JComponent component, final WindowListener windowListener) {
        return new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) {
                Window window = SwingUtilities.getWindowAncestor(component);
                if (window != null) {
                    window.removeWindowListener(windowListener);
                    window.addWindowListener(windowListener);
                }
            }

            public void ancestorRemoved(AncestorEvent event) {
                // no implementation.
            }

            public void ancestorMoved(AncestorEvent event) {
                // no implementation.
            }
        };
    }

    private static WindowListener createWindowListener(final JComponent component) {
        return new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                component.repaint();
            }

            public void windowDeactivated(WindowEvent e) {
                component.repaint();
            }
        };
    }

    private static PropertyChangeListener createFrameFocusPropertyChangeListener(
            final WindowFocusListener focusListener, final JComponent component) {
        return new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                Window window = SwingUtilities.getWindowAncestor(component);
                // use the client property that initiated this this
                // property change event, as the actual window's
                // isFocused method may not return the correct value
                // because the window is in transition.
                boolean hasFocus = (Boolean) component.getClientProperty(FRAME_ACTIVE_PROPERTY);
                if (hasFocus) {
                    focusListener.windowGainedFocus(
                            new WindowEvent(window, WindowEvent.WINDOW_GAINED_FOCUS));
                } else {
                    focusListener.windowLostFocus(
                            new WindowEvent(window, WindowEvent.WINDOW_LOST_FOCUS));
                }
            }
        };
    }

}