package ui.dashboard;

import common.pool.UIThreadPool;
import ui.common.CommonTable;
import ui.common.HttpRequestResponseEditor;

import javax.swing.table.TableModel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;


public class DashboardTable extends CommonTable<DashboardTableData> {

    private final DashboardTableModel dashboardTableModel;
    private final HttpRequestResponseEditor requestResponseEditor;
    private final AtomicInteger globalId = new AtomicInteger(0);

    public DashboardTableModel getDashboardTableModel() {
        return this.dashboardTableModel;
    }


    public DashboardTable(
            TableModel tableModel,
            HttpRequestResponseEditor requestResponseEditor
    ) {
        super(tableModel);
        this.dashboardTableModel = (DashboardTableModel) tableModel;
        this.requestResponseEditor = requestResponseEditor;
        this.addContextMenu();
        // 设置某些列的最大列宽
    }


    public Integer generateId() {
        return globalId.addAndGet(1);
    }

    @Override
    public void changeSelection(int row, int col, boolean toggle, boolean extend) {
        super.changeSelection(row, col, toggle, extend);

        row = this.getRowSorter().convertRowIndexToModel(row);
        DashboardTableData dashboardTableData = this.getTableData().get(row);
        requestResponseEditor.setData(
                dashboardTableData.getRequestResponse(),
                dashboardTableData.getEdit_requestResponse()//修改后的请求
        );
    }

    private void addContextMenu() {
        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }



    private void showPopupMenu(MouseEvent e) {
        ArrayList<DashboardTableData> dashboardTableDataList = new ArrayList<>();
        int[] selectedRows = this.getSelectedRows();
        for (int selectedRow : selectedRows) {
            dashboardTableDataList.add(this.tableData.get(
                    this.getRowSorter().convertRowIndexToModel(selectedRow)
            ));
        }

        new DashboardTablePopupMenu(
                dashboardTableDataList,
                this
        ).show(e.getComponent(), e.getX(), e.getY());
    }
}
