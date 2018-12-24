package ua.kiev.prog.case2;

import ua.kiev.prog.shared.Id;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDAO<K, T> {
    private final Connection conn;
    private final String table;

    public AbstractDAO(Connection conn, String table) {
        this.conn = conn;
        this.table = table;
    }

    public void add(T t) {
        try {
            Field[] fields = t.getClass().getDeclaredFields();

            StringBuilder names = new StringBuilder();
            StringBuilder values = new StringBuilder();

            for (Field f : fields) {
                f.setAccessible(true);

                names.append(f.getName()).append(',');
                values.append('"').append(f.get(t)).append("\",");
            }
            names.deleteCharAt(names.length() - 1); // last ','
            values.deleteCharAt(values.length() - 1); // last ','

            String sql = "INSERT INTO " + table + "(" + names.toString() +
                    ") VALUES(" + values.toString() + ")";

            try (Statement st = conn.createStatement()) {
                st.execute(sql.toString());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void delete(T t) {
        try {
            Field[] fields = t.getClass().getDeclaredFields();
            Field id = null;

            for (Field f : fields) {
                if (f.isAnnotationPresent(Id.class)) {
                    id = f;
                    id.setAccessible(true);
                    break;
                }
            }
            if (id == null)
                throw new RuntimeException("No Id field");

            String sql = "DELETE FROM " + table + " WHERE " + id.getName() +
                    " = \"" + id.get(t) + "\"";

            try (Statement st = conn.createStatement()) {
                st.execute(sql.toString());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<T> getAll(Class<T> cls) {
        List<T> res = new ArrayList<>();

        try {
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("SELECT * FROM " + table)) {
                    ResultSetMetaData md = rs.getMetaData();

                    while (rs.next()) {
                        T client = (T) cls.newInstance();

                        for (int i = 1; i <= md.getColumnCount(); i++) {
                            String columnName = md.getColumnName(i);

                            Field field = cls.getDeclaredField(columnName);
                            field.setAccessible(true);

                            field.set(client, rs.getObject(columnName));
                        }

                        res.add(client);
                    }
                }
            }

            return res;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public T search(int id, Class<T> cls) {
        T t = null;
        try {
            t = (T) cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        Field[] fields = cls.getDeclaredFields();
        Field fld = null;
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                fld = field;
            }
        }
        if (fld == null) {
            throw new RuntimeException("No id field found");
        } else
            try {
                try (Statement st = conn.createStatement()) {
                    try (ResultSet rs = st.executeQuery("SELECT * FROM " + table
                            + " WHERE " + fld.getName() + "=" + id)) {
                        ResultSetMetaData rsmd = rs.getMetaData();
                        if (rs.next()) {
                            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                                Field field = cls.getDeclaredField(rsmd.getColumnName(i));
                                field.setAccessible(true);
                                field.set(t, rs.getObject(rsmd.getColumnName(i)));
                            }
                            return t;
                        } else {
                            return null;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        return t;
    }

    public void update(T t){
        try {
            Class<?> cls = t.getClass();
            Field[] fields = t.getClass().getDeclaredFields();
            Field fld = null;
            String str = "";
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Id.class)) fld = field;
                str += (field.getName() + "= \"" + field.get(t) + "\",");
            }
            str = str.substring(0, str.length() - 1);

            if (fld == null) {
                System.out.println("No records for update by " + fld.getName() + "=" + fld.get(t) + " found");
            } else {
                String sql = "UPDATE " + table + " SET " + str +
                        " WHERE " + fld.getName() + "=" + fld.get(t);
                System.out.println(sql);
                try (Statement st = conn.createStatement()) {
                    st.execute(sql.toString());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

