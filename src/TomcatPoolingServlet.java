import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

@WebServlet(name = "TomcatPoolingServlet", urlPatterns = "/")
public class TomcatPoolingServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(TomcatPoolingServlet.class.getName());
    
    // Create a dataSource which registered in web.xml
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public String getServletInfo() {
        return "Servlet connects to MySQL database and displays result of a SELECT";
    }

    // Use HTTP GET
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/html"); // Response mime type
        logger.info("Handling GET request.");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // the following line is to get a connection from a data source configured as a connection pool
        try (out; Connection conn = dataSource.getConnection()) {

            // the following commented lines are direct connections without pooling, which is the old way
            // Class.forName("org.gjt.mm.mysql.Driver");
            // Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            // try (Connection conn = DriverManager.getConnection(loginUrl, loginUser, loginPasswd)) {


            out.println("<HTML><HEAD><TITLE>MovieDBExample</TITLE></HEAD>");
            out.println("<BODY><H1>MovieDBExample (with some changes)</H1>");


            if (conn == null) {
                
                logger.info("Failed to connect to database.");
                
                out.println("conn is null.");
            } else {
                
                logger.info("Connected to database.");
                
                // Declare our statement
                Statement statement = conn.createStatement();
                String query = "SELECT * from stars limit 10";

                logger.info("Statement created.");

                // Perform the query
                ResultSet rs = statement.executeQuery(query);

                logger.info("Statement executed.");

                out.println("<TABLE border>");

                // Iterate through each row of rs
                while (rs.next()) {

                    logger.info("Handling row.");
                    
                    String m_id = rs.getString("id");
                    String m_LN = rs.getString("name");
                    String m_dob = rs.getString("birthYear");
                    out.println("<tr>" + "<td>" + m_id + "</td>" + "<td>" + m_LN + "</td>" + "<td>" + m_dob + "</td>"
                            + "</tr>");
                }

                out.println("</TABLE>");

                rs.close();
                statement.close();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            logger.info("ERROR: " + exception.getMessage());
            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
    }

}
