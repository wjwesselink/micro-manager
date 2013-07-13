
package org.micromanager.utils;

import java.awt.AWTEvent;
import java.awt.Button;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.AbstractButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JList;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

/**
 *
 * @author Arthur
 */
public class UIMonitor {

   private static AWTEventListener clickListener_ = null;

   private static String getComponentText(Component component) {
      if (component instanceof AbstractButton) {
         return ((AbstractButton) component).getText();
      } else if (component instanceof Button) {
         return ((Button) component).getLabel();
      } else {
         return "";
      }
   }

   private static String getComponentName(Component component) {
      String text = component.getName();
      if (text == null || text.isEmpty()) {
         text = getComponentText(component);
      }
      return text;
   }

   private static String getClickAction(Component component) {
      if (component instanceof JToggleButton) {
         return "toggled " + (((JToggleButton) component).isSelected() ? "on" : "off");
      } else if (component instanceof JCheckBoxMenuItem) {
         return "toggled " + (((JCheckBoxMenuItem) component).isSelected() ? "on" : "off");
      } else if (component instanceof JList) {
          return null;
      } else if (component instanceof JSlider) {
         return "set to \"" + ((JSlider) component).getValue() + "\"";
      } else if (component instanceof JTabbedPane) {
         JTabbedPane tabbedPane = (JTabbedPane) component;
         return "set to \"" + tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()) + "\"";
      } else {
         return "clicked";
      }
   }

   private static String getReleaseAction(Component component) {
      if (component instanceof JList) {
         final JList list = (JList) component;
         return "set to " + "\"" + list.getSelectedValue().toString() + "\"";
      } else {
         return null;
      }
   }
   

   private static void handleAWTEvent(AWTEvent event) {
      final int eventID = event.getID();
      String identifier = null;
      String action = null;
      // System.out.println(event.toString()+"\n");
      if (0 != (eventID & (ActionEvent.ACTION_EVENT_MASK | 0))) {
         Object source = event.getSource();

         if (source instanceof Component) {
            Component component = (Component) source;
            if (component.isEnabled()) {
               String text = getComponentName(component);
               if (!text.isEmpty()) {
                  text = "\"" + text + "\" ";
               }
               identifier = "\n" + source.getClass().getSimpleName() + " " + text;
               if (eventID == MouseEvent.MOUSE_CLICKED) {
                  action = getClickAction(component);
               }
               if (eventID == MouseEvent.MOUSE_RELEASED) {
                  action = getReleaseAction(component);
               }
            }
            if (identifier != null && action != null) {
               String message = identifier + action + " in " + ((Window) SwingUtilities.getAncestorOfClass(Window.class, (Component) source)).getClass().getSimpleName() + ".";
               ReportingUtils.logMessage(message);
            }
         }
      }
   }

   private static void enable() {
      if (clickListener_ != null) {
         return;
      }
      clickListener_ = new AWTEventListener() {
         @Override
         public void eventDispatched(AWTEvent event) {
            handleAWTEvent(event);
         }
      };
      Toolkit.getDefaultToolkit().addAWTEventListener(clickListener_, AWTEvent.MOUSE_EVENT_MASK);
   }

   private static void disable() {
      Toolkit.getDefaultToolkit().removeAWTEventListener(clickListener_);
      clickListener_ = null;
   }

   public static void enable(final boolean on) {
      SwingUtilities.invokeLater(
              new Runnable() {
                 public void run() {
                    if (on) {
                       enable();
                    } else {
                       disable();
                    }
                 }
              });
   }
}
