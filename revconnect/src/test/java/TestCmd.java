import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCmd {
    private static final Logger log = LoggerFactory.getLogger(TestCmd.class);

    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@db.freesql.com:1521/23ai_34ui2";
        String user = "GOWTHAMREDDY20201_SCHEMA_AG8XG";
        String pass = "YH4UVL08413WGI80o2W7QWJIQ$YF9K";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String q = "SELECT p.post_id, p.content, p.is_published, p.is_promotional, p.media_url " +
                    "FROM posts p JOIN users u ON p.user_id = u.id WHERE u.username = 'babu'";
            try (PreparedStatement stmt = conn.prepareStatement(q);
                    ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    log.info("ID: {}, Content: '{}', Published: {}, Promo: {}, Media: '{}'",
                            rs.getLong("post_id"),
                            rs.getString("content"),
                            rs.getBoolean("is_published"),
                            rs.getBoolean("is_promotional"),
                            rs.getString("media_url"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
