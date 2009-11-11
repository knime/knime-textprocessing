package org.knime.ext.textprocessing.nodes.misc.stringmatcher;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.TermValue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 
 * @author adae, University of Konstanz
 */
public class StringMatcherNodeModel extends NodeModel {

    /** Config key of the first set column. */
    protected static final String CFG_COL1 = "col1";

    /** Config key of the second set column. */
    protected static final String CFG_COL2 = "col2";

    /** Config key for sort in memory. */
    protected static final String CFG_SORT_IN_MEMORY = "workInMemory";

    /**
     * The configuration key to en and disable the distance column.
     */
    protected static final String CFG_SHOW_DISTANCE = "Show minimal distance";

    /**
     * The configuration key for the cost of one deletion.
     */
    protected static final String CFG_WD = "Cost for deletion";

    /**
     * The configuration key for the cost of one insert.
     */
    protected static final String CFG_WI = "Cost for insertion";

    /**
     * The configuration key for the cost of one change.
     */
    protected static final String CFG_WC = "Cost for changing";

    /**
     * The configuration key for the cost of one switch.
     */
    protected static final String CFG_WS = "Cost for switching";

    /**
     * The configuration key for the maximal number of words to print.
     */
    protected static final String CFG_NUMBER =
            "Maximal number of related words";

    private final SettingsModelString m_col1;

    private final SettingsModelString m_col2;

    private final SettingsModelBoolean m_sortInMemory;

    private final SettingsModelBoolean m_showdist;

    private final SettingsModelInteger m_wd;

    private final SettingsModelInteger m_wi;

    private final SettingsModelInteger m_wc;

    private final SettingsModelInteger m_ws;

    private final SettingsModelInteger m_numberofrelatedwords;

    /**
     * 
     */
    public StringMatcherNodeModel() {
        super(2, 1);

        m_col1 = new SettingsModelString(CFG_COL1, null);
        m_col2 = new SettingsModelString(CFG_COL2, null);
        m_sortInMemory = new SettingsModelBoolean(CFG_SORT_IN_MEMORY, true);
        m_showdist = new SettingsModelBoolean(CFG_SHOW_DISTANCE, true);

        m_wd = new SettingsModelInteger(CFG_WD, 1);
        m_wi = new SettingsModelInteger(CFG_WI, 1);
        m_wc = new SettingsModelInteger(CFG_WC, 1);
        m_ws = new SettingsModelInteger(CFG_WS, 1);

        m_numberofrelatedwords = new SettingsModelInteger(CFG_NUMBER, 3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {

        if (inSpecs.length < 2) {
            throw new IllegalArgumentException("Two input tables expected");
        }
        final String col1 = m_col1.getStringValue();
        if (col1 == null) {
            throw new InvalidSettingsException("No column 1 defined");
        }
        final String col2 = m_col2.getStringValue();
        if (col2 == null) {
            throw new InvalidSettingsException("No column 2 defined");
        }
        if (2 * m_ws.getIntValue() < m_wi.getIntValue() + m_wd.getIntValue()) {
            throw new InvalidSettingsException("2*weight(switch) must be"
                    + " >= weight(insert)+weight(delete)");
        }
        return new DataTableSpec[]{createSpec()};
    }

    /**
     * Creates the DataTablespec. Using the given configuration.
     * 
     * @return the new data table spec.
     * 
     */
    protected DataTableSpec createSpec() {
        int numberofrelatedwords = m_numberofrelatedwords.getIntValue();

        int addcol = 0;
        if (m_showdist.getBooleanValue()) {
            addcol = 1;
        }

        String[] names = new String[numberofrelatedwords + 1 + addcol];
        DataType[] types = new DataType[numberofrelatedwords + 1 + addcol];

        names[0] = "Origin";
        types[0] = StringCell.TYPE;

        if (addcol == 1) {
            names[1] = "Distance";
            types[1] = IntCell.TYPE;
        }
        // all columns will be filled with Strings
        for (int i = 1; i <= numberofrelatedwords; i++) {
            names[i + addcol] = "Related" + i;
            types[i + addcol] = StringCell.TYPE;
        }

        return new DataTableSpec(DataTableSpec.createColumnSpecs(names, types));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        // needed for the progress calculation.
        int count = 0;
        int max = inData[0].getRowCount();

        int numberofrelatedwords = m_numberofrelatedwords.getIntValue();

        if (m_sortInMemory.getBooleanValue()) {
            exec.setProgress(0, "Presorting Datatable");
        }

        if (inData.length < 2) {
            throw new IllegalArgumentException("Two input tables expected");
        }
        DataTable searchdata = inData[0]; // searching table

        LevenDamerau ld =
                new LevenDamerau(inData[1], inData[1].getDataTableSpec()
                        .findColumnIndex(m_col2.getStringValue()),
                        m_sortInMemory.getBooleanValue(), exec);
        ld.setweight(m_wd.getIntValue(), m_wi.getIntValue(),
                m_wc.getIntValue(), m_ws.getIntValue());
        ArrayList<char[]> words; // found words
        int searchcol =
                inData[0].getDataTableSpec().findColumnIndex(
                        m_col1.getStringValue());

        DataCell[] related;

        /**
         * Initialize the buffer with one colum for the origin word and as
         * wished for the related words
         */

        BufferedDataContainer buf = exec.createDataContainer(createSpec());

        int startwords = 0; // if the min dist is not shown, the first word
        // will be written
        if (m_showdist.getBooleanValue()) {
            related = new DataCell[numberofrelatedwords + 2];
            startwords = 1;
        } else {
            related = new DataCell[numberofrelatedwords + 1];
        }

        for (DataRow row : searchdata) {

            exec.checkCanceled();
            if (row.getCell(searchcol).getType().isCompatible(TermValue.class)) 
            {
                related[0] = new StringCell(
                        ((TermValue)row.getCell(searchcol)).getTermValue()
                        .getText());
            } else {
                related[0] = row.getCell(searchcol);
            }
            if (!row.getCell(searchcol).isMissing()) {
                /**
                 * the field related representates the new row in the output the
                 * first output column is the origin word the second one the
                 * minimal found distance
                 */
               
                double prog = (double)(count++) / (double)max;
                exec.setProgress(prog, "Searching related words for '"
                        + related[0] + "'");

                /**
                 * words includes all words from bibdata, which have the minimal
                 * distance
                 */
                words = ld.getNearestWord(((StringValue)related[0])
                        .getStringValue().toCharArray());

                if (m_showdist.getBooleanValue()) {
                    related[1] 
                         = new IntCell(ld.getlastdistance());
                }

            } else {
                words = new ArrayList<char[]>();
            }

            for (int i = 1; i <= numberofrelatedwords; i++) {
                if (words.size() > i - 1) {
                    related[i + startwords]
                             = new StringCell(String.valueOf(words.get(i - 1)));

                } else {
                    related[i + startwords] = DataType.getMissingCell();
                }
            }
            /** add the new row to the table */
            buf.addRowToTable(new DefaultRow(row.getKey(), related));

        }

        buf.close();
        return new BufferedDataTable[]{buf.getTable()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to load

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_col1.loadSettingsFrom(settings);
        m_col2.loadSettingsFrom(settings);
        m_sortInMemory.loadSettingsFrom(settings);
        m_showdist.loadSettingsFrom(settings);
        m_wd.loadSettingsFrom(settings);
        m_wi.loadSettingsFrom(settings);
        m_wc.loadSettingsFrom(settings);
        m_ws.loadSettingsFrom(settings);

        m_numberofrelatedwords.loadSettingsFrom(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // nothing to reset

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to save

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {

        m_col1.saveSettingsTo(settings);
        m_col2.saveSettingsTo(settings);
        m_sortInMemory.saveSettingsTo(settings);
        m_showdist.saveSettingsTo(settings);

        m_wd.saveSettingsTo(settings);
        m_wi.saveSettingsTo(settings);
        m_wc.saveSettingsTo(settings);
        m_ws.saveSettingsTo(settings);

        m_numberofrelatedwords.saveSettingsTo(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_col1.validateSettings(settings);
        m_col2.validateSettings(settings);
    }

}
