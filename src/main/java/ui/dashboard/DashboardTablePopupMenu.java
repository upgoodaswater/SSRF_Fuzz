package ui.dashboard;

import burp.api.montoya.logging.Logging;
import checker.SSRFChecker;
import ui.dashboard.DashboardTableModel;
import javax.swing.*;
import java.util.List;
import common.logger.AutoSSRFLogger;

// DashboardTablePopupMenu.java
public class DashboardTablePopupMenu extends JPopupMenu {
    final AutoSSRFLogger logger = AutoSSRFLogger.INSTANCE;
    public DashboardTablePopupMenu(List<DashboardTableData> dashboardTableDataList, DashboardTable table) {
        // 删除选中行

        JMenuItem delete = new JMenuItem("删除选中行");
        delete.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                DashboardTableModel model = table.getDashboardTableModel();
                List<DashboardTableData> modelData = model.getDataList();

                // 方法一：直接触发全表刷新（简单但效率较低）
                modelData.removeAll(dashboardTableDataList);
                model.fireTableDataChanged(); // 正确调用
            });
        });
        this.add(delete);

        // 删除全部
        JMenuItem deleteAll = new JMenuItem("删除全部");
        deleteAll.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                DashboardTableModel model = table.getDashboardTableModel();
                List<DashboardTableData> modelData = model.getDataList();
                // 记录旧行数用于通知删除范围
                int oldSize = modelData.size();
                modelData.clear();

                // 正确通知行删除（当有数据时才触发）
                if (oldSize > 0) {
                    model.fireTableRowsDeleted(0, oldSize - 1);
                }
            });
        });
        this.add(deleteAll);
    }
}