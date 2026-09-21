package libraryCatalog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;

// Definitions of user-made categories (Coins, Comics, ...) and the database work to create and remove them.
// Only the definitions live in custom_categories / custom_fields; each category's items get their own table.
public class CustomCategories {

    public enum FieldType { TEXT, NUMBER, YESNO }

    public static class Field {
        public final String name;        // what the user typed, shown in the UI
        public final String columnName;  // generated, restricted to a-z 0-9 _ so it is safe in SQL
        public final FieldType type;

        Field(String name, String columnName, FieldType type) {
            this.name = name;
            this.columnName = columnName;
            this.type = type;
        }
    }

    public static class Category {
        public final int id;
        public final String name;
        public final String tableName;   // generated, safe in SQL
        public final String icon;        // one of the file names in ICONS
        public final List<Field> fields = new ArrayList<>();

        Category(int id, String name, String tableName, String icon) {
            this.id = id;
            this.name = name;
            this.tableName = tableName;
            this.icon = icon;
        }
    }

    // Icons a user can pick for a category: label shown in the dialog -> image file in this package.
    public static final Map<String, String> ICONS = new LinkedHashMap<>();
    static {
        ICONS.put("Star", "rating.png");
        ICONS.put("Tag", "label.png");
        ICONS.put("Books", "books.png");
        ICONS.put("Film", "movies.png");
        ICONS.put("Music", "music.png");
        ICONS.put("Art", "art.png");
        ICONS.put("Album", "album.png");
        ICONS.put("Frame", "framed.png");
        ICONS.put("Palette", "medium.png");
        ICONS.put("Shopping bag", "purchased.png");
        ICONS.put("Disc", "format.png");
        ICONS.put("Person", "actors.png");
        ICONS.put("Calendar", "year.png");
        ICONS.put("Ruler", "dimensions.png");
        ICONS.put("Pin", "location.png");
        ICONS.put("Megaphone", "director.png");
        ICONS.put("Clock", "runtime.png");
        ICONS.put("Signature", "signed.png");
    }

    // Names the built-in categories and standard fields already use (compared in upper case).
    public static final Set<String> RESERVED_CATEGORY_NAMES = Set.of("BOOKS", "MOVIES", "MUSIC", "ART", "MEDIATYPE");
    public static final Set<String> RESERVED_FIELD_NAMES = Set.of("TITLE", "ON HAND", "LOCATION", "CONDITION", "VALUE");

    public static final int MAX_FIELDS = 20;

    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[a-z0-9_]{1,64}");

    public static String iconFileOrDefault(String file) {
        return ICONS.containsValue(file) ? file : "rating.png";
    }

    public static String fieldIconFile(FieldType type) {
        switch (type) {
            case NUMBER: return "fieldnumber.png";
            case YESNO: return "fieldcheck.png";
            default: return "fieldtext.png";
        }
    }

    private static String slug(String text) {
        String s = text.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        if (s.isEmpty()) {
            s = "x";
        }
        return s.length() > 40 ? s.substring(0, 40) : s;
    }

    private static String sqlType(FieldType type) {
        switch (type) {
            case NUMBER: return "DECIMAL(14,4)";
            case YESNO: return "BOOLEAN";
            default: return "VARCHAR(255)";
        }
    }

    private static boolean tableExists(Connection conn, String table) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = DATABASE() AND table_name = ?")) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Reads every saved category. Returns an empty list if none has ever been created.
    public static List<Category> loadAll() throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        List<Category> result = new ArrayList<>();
        if (!tableExists(conn, "custom_categories") || !tableExists(conn, "custom_fields")) {
            return result;
        }

        Map<Integer, Category> byId = new LinkedHashMap<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name, table_name, icon FROM custom_categories ORDER BY id")) {
            while (rs.next()) {
                String table = rs.getString("table_name");
                if (table == null || !SAFE_IDENTIFIER.matcher(table).matches()) {
                    continue;
                }
                byId.put(rs.getInt("id"), new Category(rs.getInt("id"), rs.getString("name"), table,
                        iconFileOrDefault(rs.getString("icon"))));
            }
        }
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT category_id, field_name, column_name, field_type " +
                     "FROM custom_fields ORDER BY category_id, sort_order")) {
            while (rs.next()) {
                Category category = byId.get(rs.getInt("category_id"));
                String column = rs.getString("column_name");
                if (category == null || column == null || !SAFE_IDENTIFIER.matcher(column).matches()) {
                    continue;
                }
                FieldType type;
                try {
                    type = FieldType.valueOf(rs.getString("field_type"));
                } catch (IllegalArgumentException e) {
                    type = FieldType.TEXT;
                }
                category.fields.add(new Field(rs.getString("field_name"), column, type));
            }
        }
        result.addAll(byId.values());
        return result;
    }

    // Saves the category's definition and creates the table that will hold its items.
    public static Category create(String name, String icon, List<String> fieldNames, List<FieldType> fieldTypes)
            throws Exception {
        Connection conn = DatabaseConnection.getConnection();

        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS custom_categories (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(60) NOT NULL UNIQUE, " +
                    "table_name VARCHAR(80) NOT NULL, " +
                    "icon VARCHAR(40) NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS custom_fields (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "category_id INT NOT NULL, " +
                    "field_name VARCHAR(60) NOT NULL, " +
                    "column_name VARCHAR(60) NOT NULL, " +
                    "field_type VARCHAR(10) NOT NULL, " +
                    "sort_order INT NOT NULL, " +
                    "FOREIGN KEY (category_id) REFERENCES custom_categories(id) ON DELETE CASCADE)");
        }

        String base = "custom_" + slug(name);
        String table = base;
        int suffix = 2;
        while (tableExists(conn, table)) {
            table = base + "_" + suffix++;
        }

        List<Field> fields = new ArrayList<>();
        Set<String> usedColumns = new HashSet<>();
        for (int i = 0; i < fieldNames.size(); i++) {
            String column = "f_" + slug(fieldNames.get(i));
            String unique = column;
            int n = 2;
            while (!usedColumns.add(unique)) {
                unique = column + "_" + n++;
            }
            fields.add(new Field(fieldNames.get(i), unique, fieldTypes.get(i)));
        }

        StringBuilder ddl = new StringBuilder("CREATE TABLE `" + table + "` (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(150) NOT NULL");
        for (Field f : fields) {
            ddl.append(", `").append(f.columnName).append("` ").append(sqlType(f.type));
        }
        ddl.append(", on_hand INT, location VARCHAR(50), current_condition VARCHAR(50), " +
                "selection_value DECIMAL(10,2), INDEX idx_title (title))");
        try (Statement st = conn.createStatement()) {
            st.execute(ddl.toString());
        }

        int categoryId = -1;
        try {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO custom_categories (name, table_name, icon) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, table);
                ps.setString(3, icon);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    categoryId = keys.getInt(1);
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO custom_fields (category_id, field_name, column_name, field_type, sort_order) " +
                            "VALUES (?, ?, ?, ?, ?)")) {
                for (int i = 0; i < fields.size(); i++) {
                    ps.setInt(1, categoryId);
                    ps.setString(2, fields.get(i).name);
                    ps.setString(3, fields.get(i).columnName);
                    ps.setString(4, fields.get(i).type.name());
                    ps.setInt(5, i);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (Exception e) {
            // Don't leave a half-made category behind.
            try (Statement st = conn.createStatement()) {
                st.execute("DROP TABLE IF EXISTS `" + table + "`");
            } catch (Exception ignored) {
            }
            if (categoryId != -1) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM custom_categories WHERE id = ?")) {
                    ps.setInt(1, categoryId);
                    ps.executeUpdate();
                } catch (Exception ignored) {
                }
            }
            throw e;
        }

        Category category = new Category(categoryId, name, table, iconFileOrDefault(icon));
        category.fields.addAll(fields);
        return category;
    }

    // Permanently removes the category's definition AND every item stored in it.
    public static void delete(Category category) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS `" + category.tableName + "`");
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM custom_categories WHERE id = ?")) {
            ps.setInt(1, category.id);
            ps.executeUpdate();
        }
    }
}
