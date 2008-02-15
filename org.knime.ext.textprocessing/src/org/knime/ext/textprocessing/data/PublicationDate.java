/* 
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * -------------------------------------------------------------------
 * 
 * History
 *   15.12.2006 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Encapsulates a publication date of a 
 * {@link org.knime.ext.textprocessing.data.Document} with the ability to handle
 * missing values. For example, an instance of <code>PublicationDate</code> 
 * can be created without explicitly setting the day the month or even the year.
 * If only the publication year of a publication is known, 
 * an instance of  <code>PublicationDate</code> can be created nevertheless. 
 * If the complete date is unknown a zero date can be created.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class PublicationDate implements Serializable, 
Comparable<PublicationDate> {

    private int m_year = 0;

    private int m_month = 0;

    private int m_day = 0;

    /**
     * Creates a new instance of <code>PublicationDate</code> with given year,
     * month and day. These parameters have to be valid, otherwise a
     * <code>ParseException</code> will be thrown.
     * @param year the dates year
     * @param month the dates month
     * @param day the dates day of month
     * @throws ParseException If given date parameters are not valid.
     */
    public PublicationDate(final int year, final int month, final int day)
            throws ParseException {
        String dateStr = year + "-" + month + "-" + day;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd");
        Date date = df.parse(dateStr);

        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        
        m_year = cal.get(Calendar.YEAR);
        m_month = cal.get(Calendar.MONTH);
        m_day = cal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Creates a new instance of <code>PublicationDate</code> with given year.
     * and month. If given year is greater than the current year, or given
     * month is not valid a <code>IllegalArgumentException</code> will be 
     * thrown. The day of month is set to zero.
     * @param year the dates year.
     * @param month the dates month.
     */
    public PublicationDate(final int year, final int month) {
        Calendar cal = new GregorianCalendar();
        if (year > cal.get(Calendar.YEAR)) {
            throw new IllegalArgumentException("Year " + year 
                    + " is not valid !");
        }
        if (month > 12 || month < 1) {
            throw new IllegalArgumentException("Month " + month 
                    + " is not valid !");
        }
        m_year = cal.get(Calendar.YEAR);
        m_month = cal.get(Calendar.MONTH);
    }
    
    /**
     * Creates a new instance of <code>PublicationDate</code> with given year.
     * If given year is greater than the current year, a 
     * <code>IllegalArgumentException</code> will be thrown. 
     * The month and day of month is set to zero.
     * @param year the dates year.
     */
    public PublicationDate(final int year) {
        Calendar cal = new GregorianCalendar();
        if (year > cal.get(Calendar.YEAR) || year <= 0) {
            throw new IllegalArgumentException("Year " + year 
                    + " is not valid !");
        }
        m_year = cal.get(Calendar.YEAR);
    }
    
    /**
     * Creates a new zeroed instance of <code>PublicationDate</code>.
     */
    public PublicationDate() { }
    
    

    /**
     * Returns the String representation of a date, which is
     * <i>yyyy-mm-dd</i> by default. Missing values will be ignored, a
     * zero or empty date is represented by the String "0000-00-00". 
     * 
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String str;
        
        if (m_month > 0 && m_day > 0) {
            str = m_year + "-" + m_month + "-" + m_day;
        } else if (m_month > 0 && m_day < 1) {
            str = m_year + "-" + m_month;
        } else if (m_year > 0 && m_month < 1) {
            str = Integer.toString(m_year);
        } else {
            str = "0000-00-00";
        }
        
        return str;
    }
    
    /**
     * Returns the dates serialization String, which is similar to the String
     * returned by {@link PublicationDate#toString()} except that missing 
     * values, like day or month, two zeros "00" will be replaced by zeros.  
     * @return The dates serialization String.
     */
    public String serializationString() {
        String str;
        
        if (m_month > 0 && m_day > 0) {
            str = m_year + "-" + m_month + "-" + m_day;
        } else if (m_month > 0 && m_day < 1) {
            str = m_year + "-" + m_month + "-00";
        } else if (m_year > 0 && m_month < 1) {
            str = Integer.toString(m_year) + "-00-00";
        } else {
            str = "0000-00-00";
        }
        
        return str;        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof PublicationDate)) {
            return false;
        }
        PublicationDate d = (PublicationDate)o;
        
        if (d.getYear() != m_year) {
            return false;
        }
        if (d.getMonth() != m_month) {
            return false;
        }
        if (d.getDay() != m_day) {
            return false;
        }
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int prime = 119;
        int hashCode = 1;
        
        hashCode *= prime + new Integer(m_year).hashCode();
        hashCode *= prime + new Integer(m_month).hashCode();
        hashCode *= prime + new Integer(m_day).hashCode();

        return hashCode;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final PublicationDate o) throws ClassCastException {
        int yd = m_year - o.getYear();
        int md = m_month - o.getMonth();
        int dd = m_day - o.getDay();

        if (yd != 0) {
            return yd;
        } else if (md != 0) {
            return md;
        }
        return dd;
    }
    
    
    /**
     * @return the day
     */
    public int getDay() {
        return m_day;
    }

    /**
     * @return the month
     */
    public int getMonth() {
        return m_month;
    }

    /**
     * @return the year
     */
    public int getYear() {
        return m_year;
    }
}
