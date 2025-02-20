package com.smartbear.ready.plugin.postman.utils;

import com.eviware.soapui.impl.wsdl.actions.project.SimpleDialog;
import com.eviware.soapui.support.CommonJScrollPane;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.swing.JTableFactory;
import net.sf.json.JSONObject;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class VaultVariableResolver {

    private static final String DIALOG_TITLE = "Resolve vault variables";
    private static final String DIALOG_DESCRIPTION = "You have vault variables in your collection. Please provide values for them.";
    private static final String UNRESOLVED_DIALOG_TITLE = "Warning - Unresolved vault variables";
    private static final String UNRESOLVED_DIALOG_QUESTION = "There are vault variables with no value, continue?";

    private JDialog dialog;
    private VaultVariablesTableModel vaultVariablesTableModel;
    private JTable table;

    public Map<String, String> resolve(JSONObject jsonCollection) {
        Map<String, String> variablesToResolve = getVaultVariablesFromCollection(jsonCollection);
        if (variablesToResolve.isEmpty()) {
            return new HashMap<>();
        }

        vaultVariablesTableModel = new VaultVariablesTableModel(variablesToResolve);
        if (dialog == null) {
            buildDialog();
        } else {
            table.setModel(vaultVariablesTableModel);
        }
        UISupport.centerDialog(dialog);
        dialog.setVisible(true);
        return vaultVariablesTableModel.getVaultVariables();
    }

    private Map<String, String> getVaultVariablesFromCollection(JSONObject jsonCollection) {
        Set<String> vaultVariableNames = PostmanCollectionUtils.extractVaultVariables(jsonCollection);
        return vaultVariableNames
                .stream()
                .collect(Collectors.toMap(key -> key, value -> ""));
    }

    private void buildDialog() {
        dialog = new SimpleDialog(DIALOG_TITLE, DIALOG_DESCRIPTION, "", true) {

            @Override
            protected Component buildContent() {
                JPanel panel = new JPanel(new BorderLayout());
                table = JTableFactory.getInstance().makeJTable(vaultVariablesTableModel);
                table.setRowHeight(table.getRowHeight() + 2);
                panel.add(new CommonJScrollPane(table), BorderLayout.CENTER);
                panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                return panel;
            }

            @Override
            protected boolean handleOk() {
                if (table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }
                return allVariablesResolved();
            }

            @Override
            protected boolean handleCancel() {
                dialog.dispose();
                return true;
            }
        };

        dialog.setModal(true);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                if (allVariablesResolved()) {
                    dialog.dispose();
                }
            }
        });
    }

    private boolean allVariablesResolved() {
        if (vaultVariablesTableModel.getVaultVariables().values().stream().anyMatch(StringUtils::isNullOrEmpty)) {
            return UISupport.confirm(UNRESOLVED_DIALOG_QUESTION, UNRESOLVED_DIALOG_TITLE);
        }
        return true;
    }

    private static class VaultVariablesTableModel extends AbstractTableModel {

        private final Map<String, String> vaultVariables;

        public VaultVariablesTableModel(Map<String, String> vaultVariables) {
            this.vaultVariables = vaultVariables;
        }

        public Map<String, String> getVaultVariables() {
            return vaultVariables;
        }

        public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            return vaultVariables.size();
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> "Vault variable";
                case 1 -> "Value";
                default -> super.getColumnName(column);
            };
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            String currentKey = new ArrayList<>(vaultVariables.keySet()).get(rowIndex);
            return switch (columnIndex) {
                case 0 -> currentKey;
                case 1 -> vaultVariables.get(currentKey);
                default -> null;
            };
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 1) {
                String currentKey = new ArrayList<>(vaultVariables.keySet()).get(rowIndex);
                vaultVariables.put(currentKey, aValue.toString());
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }
}
