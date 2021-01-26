package de.fraunhofer.isst.configmanager.util;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * This class can be used as a helper class to outsource program code here.
 */
public class CalenderUtil {

    /**
     * This method creates an XMLGreorgianCalender for for time specifications as created or modified.
     *
     * @return XMLGregorianCalender
     */
    public static XMLGregorianCalendar getGregorianNow() {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException e) {
        }
        return null;
    }

}