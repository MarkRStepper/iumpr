/**
 * Copyright 2017 Equipment & Tool Institute
 */
package net.soliddesign.iumpr.ui.status;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renders the background color of a cell based upon the color from another
 * column
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class StatusRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 5323779374353482225L;

    private final int colorColumn;

    /**
     * Constructor
     *
     * @param colorColumn
     *            the column that contains the color for the background
     */
    public StatusRenderer(int colorColumn) {
        super();
        this.colorColumn = colorColumn;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        int modelRow = table.convertRowIndexToModel(row);
        Color color = (Color) table.getModel().getValueAt(modelRow, colorColumn);
        setBackground(isSelected ? table.getSelectionBackground() : (color == null ? table.getBackground() : color));

        return this;
    }
}