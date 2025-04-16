package ui;

import common.provider.UIProvider;
import lombok.Getter;
import ui.dashboard.DashboardTab;
import ui.settings.SettingsTab;

import javax.swing.*;
import java.awt.*;


public class UIMain extends JTabbedPane {

    private final UIProvider provider;

    @Getter
    private final DashboardTab dashboardTab = new DashboardTab();



    @Getter
    private final SettingsTab settingsTab = new SettingsTab();

    public UIMain(UIProvider provider) {
        super();
        this.provider = provider;

        this.addTab("History", dashboardTab);
        this.addTab("Setting", settingsTab);

        // 主题适配
        applyTheme(this);
    }

    private void applyTheme(Component component) {
        provider.applyThemeToComponent(component);
        if (component instanceof Container) {
            Container container = (Container) component;
            synchronized (container.getTreeLock()) {
                Component[] components = container.getComponents();
                for (Component comp : components) {
                    applyTheme(comp);
                }
            }
        }
    }
}
