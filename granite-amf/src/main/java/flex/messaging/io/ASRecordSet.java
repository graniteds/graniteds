/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
/**
 * www.openamf.org
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package flex.messaging.io;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.granite.logging.Logger;
import org.granite.util.Introspector;
import org.granite.util.PropertyDescriptor;


/**
 * @author Jason Calabrese <jasonc@missionvi.com>
 * @version $Revision: 1.29 $, $Date: 2006/03/25 22:17:44 $
 */
public class ASRecordSet extends ASObject {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(ASRecordSet.class);

    public static final String SERVICE_NAME = "OpenAMFPageableRecordSet";

    private static final String SI = "serverInfo";
    private static final String SI_ID = "id";
    private static final String SI_TOTAL_COUNT = "totalCount";
    private static final String SI_INITIAL_DATA = "initialData";
    //private static final String SI_ROWS = "rows";
    private static final String SI_CURSOR = "cursor";
    private static final String SI_SERVICE_NAME = "serviceName";
    private static final String SI_COLUMN_NAMES = "columnNames";
    private static final String SI_VERSION = "version";

    private static int count = 0;

    private Map<String, Object> serverInfo;
    private List<List<Object>> rows;
    private int initialRowCount;

    public ASRecordSet() {
        super("RecordSet");
        serverInfo = new HashMap<String, Object>();
        put(SI, serverInfo);

        synchronized (ASRecordSet.class)
        {
            count++;
            setId("RS" + count);
        }

        setInitialData(new ArrayList<Object>());
        setServiceName(SERVICE_NAME);
        setCursor(1);
        setVersion(1);
        rows = new ArrayList<List<Object>>();
        initialRowCount = 0;
    }

    public String getId() {
        return (String) serverInfo.get(SI_ID);
    }
    public void setId(String id) {
        serverInfo.put(SI_ID, id);
    }

    public int getTotalCount() {
        Object value = serverInfo.get(SI_TOTAL_COUNT);
        if (value != null)
            return ((Integer) value).intValue();
        return 0;
    }
    public void setTotalCount(int totalCount) {
        serverInfo.put(SI_TOTAL_COUNT, Integer.valueOf(totalCount));
    }

    public List<?> getInitialData() {
        return (List<?>)serverInfo.get(SI_INITIAL_DATA);
    }
    public void setInitialData(List<?> initialData) {
        serverInfo.put(SI_INITIAL_DATA, initialData);
    }

    public Map<String, Object> getRecords(int from, int count) {

        List<List<Object>> page = rows.subList(from - 1, from - 1 + count);

        Map<String, Object> records = new HashMap<String, Object>();
        records.put("Page", page);
        records.put("Cursor", Integer.valueOf(from + 1));

        return records;

    }

    public int getCursor() {
        Object value = serverInfo.get(SI_CURSOR);
        if (value != null)
            return ((Integer) value).intValue();
        return 0;
    }
    public void setCursor(int cursor) {
        serverInfo.put(SI_CURSOR, Integer.valueOf(cursor));
    }

    public String getServiceName() {
        return (String) serverInfo.get(SI_SERVICE_NAME);
    }
    public void setServiceName(String serviceName) {
        serverInfo.put(SI_SERVICE_NAME, serviceName);
    }

    public String[] getColumnNames() {
        return (String[]) serverInfo.get(SI_COLUMN_NAMES);
    }
    public void setColumnNames(String[] columnNames) {
        serverInfo.put(SI_COLUMN_NAMES, columnNames);
    }

    public double getVersion() {
        Object value = serverInfo.get(SI_VERSION);
        if (value != null)
            return ((Double) value).doubleValue();
        return 0;
    }
    public void setVersion(double version) {
        serverInfo.put(SI_VERSION, new Double(version));
    }

    public List<List<Object>> rows() {
        return rows;
    }

    public void populate(ResultSet rs) throws IOException {

        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            String[] columnNames = new String[columnCount];

            int rowIndex = 0;
            List<List<Object>> initialData = new ArrayList<List<Object>>();
            while (rs.next()) {
                rowIndex++;
                List<Object> row = new ArrayList<Object>();
                for (int column = 0; column < columnCount; column++) {
                    if (rowIndex == 1) {
                        columnNames[column] = rsmd.getColumnName(column + 1);
                    }
                    row.add(rs.getObject(column + 1));
                }
                if (rowIndex == 1) {
                    setColumnNames(columnNames);
                }
                rows.add(row);
                if (rowIndex <= initialRowCount) {
                    initialData.add(row);
                }
            }
            setTotalCount(rowIndex);
            setInitialData(initialData);
            setColumnNames(columnNames);
        } catch (SQLException e) {
            throw new IOException(e.getMessage());
        }

    }

    /**
     * @param columnNames
     * @param rows ArrayList containing a ArrayList for each row
     */
    public void populate(String[] columnNames, List<List<Object>> rows) {
        this.rows = rows;

        List<List<Object>> initialData =
            rows.subList(
                0,
                (initialRowCount > rows.size()
                    ? rows.size()
                    : initialRowCount)); // NOTE: sublist semantics are [fromIndex, toIndex]
        setInitialData(initialData);
        setTotalCount(rows.size());
        setColumnNames(columnNames);
    }

    /**
     * @param list List of JavaBeans, all beans should be of the same type
     * @param ignoreProperties properties that should not be added to the RecordSet
     */
    public void populate(List<?> list, String[] ignoreProperties)
        throws
            IllegalArgumentException,
            IllegalAccessException,
            InvocationTargetException {

        List<String> names = new ArrayList<String> ();
        Object firstBean = list.get(0);
        
        PropertyDescriptor[] properties = Introspector.getPropertyDescriptors(firstBean.getClass());
        if (properties == null)
        	properties = new PropertyDescriptor[0];
        
        for (int i = 0; i < properties.length; i++) {
            PropertyDescriptor descriptor = properties[i];
            if (!ignoreProperty(descriptor, ignoreProperties))
                names.add(descriptor.getName());
        }
        String[] columnNames = new String[names.size()];
        columnNames = names.toArray(columnNames);
        setColumnNames(columnNames);

        int rowIndex = 0;
        List<List<Object>> initialData = new ArrayList<List<Object>>();
        Iterator<?> iterator = list.iterator();
        while (iterator.hasNext()) {
            rowIndex++;
            Object bean = iterator.next();
            List<Object> row = new ArrayList<Object>();
            for (int i = 0; i < properties.length; i++) {
                PropertyDescriptor descriptor = properties[i];
                if (!ignoreProperty(descriptor, ignoreProperties)) {
                    Object value = null;
                    Method readMethod = descriptor.getReadMethod();
                    if (readMethod != null) {
                        value = readMethod.invoke(bean, new Object[0]);
                    }
                    row.add(value);
                }
            }
            rows.add(row);
            if (rowIndex <= initialRowCount) {
                initialData.add(row);
            }
        }
        setInitialData(initialData);
        setTotalCount(rows.size());
        log.debug("%s", this);
    }

    private boolean ignoreProperty(
        PropertyDescriptor descriptor,
        String[] ignoreProperties) {

        boolean ignore = false;
        if (descriptor.getName().equals("class")) {
            ignore = true;
        } else {
            for (int i = 0; i < ignoreProperties.length; i++) {
                String ignoreProp = ignoreProperties[i];
                if (ignoreProp.equals(descriptor.getName())) {
                    log.debug("Ignoring %s", descriptor.getName());
                    ignore = true;
                    break;
                }
            }
        }
        return ignore;
    }

    @Override
    public String toString() {

        StringBuffer info = new StringBuffer();
        addInfo(info, SI_ID, getId());
        addInfo(info, SI_TOTAL_COUNT, getTotalCount());
        addInfo(info, SI_CURSOR, getCursor());
        addInfo(info, SI_SERVICE_NAME, getServiceName());
        addInfo(info, SI_VERSION, getVersion());
        StringBuffer names = new StringBuffer();
        String[] columnNames = getColumnNames();
        if (columnNames != null) {
            for (int i = 0; i < columnNames.length; i++) {
                String name = columnNames[i];
                if (i > 0) {
                    names.append(", ");
                }
                names.append(name);
            }
        }
        addInfo(info, SI_COLUMN_NAMES, names);
        addInfo(info, SI_INITIAL_DATA, getInitialData().toString());
        return info.toString();
    }

    private void addInfo(StringBuffer info, String name, int value) {
        addInfo(info, name, new Integer(value));
    }

    private void addInfo(StringBuffer info, String name, double value) {
        addInfo(info, name, new Double(value));
    }

    private void addInfo(StringBuffer info, String name, Object value) {
        info.append(name);
        info.append(" = ");
        info.append(value);
        info.append('\n');
    }
}
