/*******************************************************************************
 * QBiC Offer Generator provides an infrastructure for creating offers using QBiC portal and
 * infrastructure. Copyright (C) 2017 Aydın Can Polatkan, 2018 Benjamin Sailer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see http://www.gnu.org/licenses/.
 *******************************************************************************/
package life.qbic.dbase;

import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import life.qbic.model.packageBean;
import life.qbic.portal.utils.ConfigurationManager;
import life.qbic.portal.utils.ConfigurationManagerFactory;
import life.qbic.utils.qOfferManagerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Date;

import static life.qbic.portal.utils.PortalUtils.isLiferayPortlet;

public class Database {

  private static Database INSTANCE;

  private static final Logger LOG = LogManager.getLogger(Database.class);
  private final String hostname;
  private final String url; // was host
  private final String port;
  private final String sql_database;
  private final String username;
  private final String password;
  private static SimpleJDBCConnectionPool connectionPool;

  // private static final String basePath =
  // VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();

  private static ConfigurationManager conf = ConfigurationManagerFactory.getInstance();

  private Database(String user, String password, String host, String port, String sql_database) {
    username = user;
    this.password = password;
    this.hostname = host;
    this.port = port;
    this.sql_database = sql_database;
    this.url = "jdbc:mysql://" + host + ":" + port + "/" + sql_database;

    LOG.info("MySQL Database instance created");
  }


  // create the connection

  public static synchronized Database getInstance() {
    init();
    return INSTANCE;
  }

  // creates new connection pool each time called BUT static context..
  public static JDBCConnectionPool getDatabaseInstanceAlternative() throws SQLException {

    return new SimpleJDBCConnectionPool("com.mysql.jdbc.Driver",
        "jdbc:mysql://" + INSTANCE.hostname + ":" + INSTANCE.port + "/" + INSTANCE.sql_database,
        INSTANCE.username, INSTANCE.password, 2, 5);
  }


  private static void init() {
    LOG.info("Initializing MySQL Database");
    if (INSTANCE == null) {
      String user = "";
      String pw = "";
      String host = "";
      String port = "";
      String sql_database = "";

      // TODO local properties file path is now: src/main/resources/developer.properties
      // if (isLiferayPortlet()) {
      user = conf.getMysqlUser();
      pw = conf.getMysqlPass();
      host = conf.getMysqlHost();
      port = conf.getMysqlPort();
      sql_database = conf.getMysqlDB();

      // } else {
      // Properties prop = new Properties();
      // InputStream input = null;
      //
      // try {
      //
      // input = new FileInputStream(qOfferManagerUtils.PROPERTIES_FILE_PATH);
      //
      // // load a properties file
      // prop.load(input);
      //
      // user = prop.getProperty(LiferayConfigurationManager.MSQL_USER);
      // pw = prop.getProperty(LiferayConfigurationManager.MSQL_PASS);
      // host = prop.getProperty(LiferayConfigurationManager.MSQL_HOST);
      // port = prop.getProperty(LiferayConfigurationManager.MSQL_PORT);
      // sql_database = prop.getProperty(LiferayConfigurationManager.MSQL_DB);
      //
      // } catch (IOException ex) {
      // ex.printStackTrace();
      // } finally {
      // if (input != null) {
      // try {
      // input.close();
      // } catch (IOException e) {
      // e.printStackTrace();
      // }
      // }
      // }
      // }
      INSTANCE = new Database(user, pw, host, port, sql_database);

      // check if com.mysql.jdbc.Driver exists. If not try to add it
      String mysqlDriverName = "com.mysql.jdbc.Driver";
      Enumeration<Driver> tmp = DriverManager.getDrivers();
      boolean existsDriver = false;
      while (tmp.hasMoreElements()) {
        Driver d = tmp.nextElement();
        if (d.toString().equals(mysqlDriverName)) {
          existsDriver = true;
          break;
        }
      }
      if (!existsDriver) {
        // Register JDBC driver
        // According http://docs.oracle.com/javase/6/docs/api/java/sql/DriverManager.html
        // this should not be needed anymore. But without it I get the following error:
        // java.sql.SQLException: No suitable driver found for
        // jdbc:mysql://localhost:3306/facs_facility
        // Does not work for servlets, just for portlets :(
        try {
          Class.forName(mysqlDriverName);
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
      }

    }
  }


  /*
  
   */
  /**
   * @return the password
   *//*
     * 
     * public String getPassword() { return password; }
     * 
     */
  /**
   * @param password the password to set
   *//*
     * 
     * public void setPassword(String password) { this.password = password; }
     * 
     */
  /**
   * @return the user
   *//*
     * 
     * public String getUser() { return username; }
     * 
     */
  /**
   * @param user the user to set
   *//*
     * 
     * public void setUser(String user) { this.username = user; }
     * 
     */
  /**
   * @return the host
   *//*
     * 
     * public String getHost() { return host; }
     * 
     */
  /**
   * @param host the host to set
   *//*
     * 
     * public void setHost(String host) { this.host = host; }
     */

  /**
   * Undoes all changes made in the current transaction. Does not undo, if conn IS in auto commit
   * mode
   *
   * @param conn:
   * @param closeConnection:
   */
  @SuppressWarnings("unused")
  private void rollback(Connection conn, boolean closeConnection) {

    try {
      if (!conn.getAutoCommit()) {
        conn.rollback();
      }
      if (closeConnection) {
        logout(conn);
      }
    } catch (SQLException e) {
      // TODO log everything
      e.printStackTrace();
    }
  }

  /**
   * logs into database with the parameters given in init()
   *
   * @return Connection, otherwise null if connecting to the database fails
   * @throws SQLException if a database access error occurs or the url is {@code null}
   */
  private Connection login() throws SQLException {
    return DriverManager.getConnection(url, username, password);
  }

  /**
   * tries to close the given connection and release it
   * <p>
   * From java documentation: It is strongly recommended that an application explicitly commits or
   * rolls back an active transaction prior to calling the close method. If the close method is
   * called and there is an active transaction, the results are implementation-defined.
   *
   * @param conn
   */
  private void logout(Connection conn) {
    try {
      conn.close();
    } catch (SQLException e) {
      // TODO log logout failure
      e.printStackTrace();
    }
  }

  public List<packageBean> getPackages() {
    String sql = "SELECT * FROM packages";
    List<packageBean> pbean = new ArrayList<>();
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {

      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        packageBean p = new packageBean();
        p.setpackage_id(rs.getInt("package_id"));
        p.setpackage_name(rs.getString("package_name"));
        p.setpackage_facility(rs.getString("package_facility"));
        p.setpackage_description(rs.getString("package_description"));
        p.setpackage_group(rs.getString("package_group"));
        p.setpackage_price(rs.getInt("package_price"));
        p.setPackage_price_external(rs.getInt("package_price_external"));
        p.setPackage_unit_type(rs.getString("package_unit_type"));
        p.setpackage_date(rs.getTimestamp("package_date"));
        pbean.add(p);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return pbean;
  }

  public ArrayList<String> getPackageGroups() {
    ArrayList<String> list = new ArrayList<>();
    String sql = "SELECT DISTINCT package_group FROM packages";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        list.add(rs.getString("package_group"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return list;
  }

  /**
   * returns the package ids and names based on the package_group
   *
   * @param package_group: one of ["Project Management", "Bioinformatics", "Sequencing", ""]
   * @return ArrayList of strings where each string consists of: packageId + ": " + packageName
   */
  public ArrayList<String> getPackageIdsAndNames(String package_group) {

    ArrayList<String> list = new ArrayList<>();
    String sql =
        "SELECT package_id, package_name FROM packages WHERE package_group = ? ORDER BY package_name";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, package_group);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        String packageId = rs.getString("package_id");
        String packageName = rs.getString("package_name");
        list.add(packageId + ": " + packageName);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return list;
  }

  public String getPackageNameFromPackageId(String packageId) {
    String sql = "SELECT package_name FROM packages WHERE package_id = ?";

    String packageName = "Error: package not found";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, packageId);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        packageName = rs.getString(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return packageName;
  }

  public ArrayList<String> getPackageIdsAndNames() {

    ArrayList<String> list = new ArrayList<>();
    // String sql = "SELECT package_id, package_name FROM packages ORDER BY package_name";
    // as requested order Packages by their ID
    String sql = "SELECT package_id, package_name FROM packages ORDER BY package_id";

    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        String packageId = rs.getString("package_id");
        String packageName = rs.getString("package_name");
        list.add(packageId + ": " + packageName);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return list;
  }

  /**
   * Gets the organization id to which the person belongs to.
   *
   * @param person_id the id of the person.
   * @return The id of the organization to which the given person belongs to.
   */
  public int getOrganizationIdForPersonId(int person_id) {

    int organizationId = -1;
    String sql = "SELECT organization_id FROM persons_organizations WHERE person_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, person_id);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        organizationId = rs.getInt(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return organizationId;
  }

  /**
   * Gets the address for the given organization.
   *
   * @param organizationId the organization id.
   * @return The address of the organization.
   */
  public String[] getAddressForOrganizationId(int organizationId) {

    String[] address = new String[7];
    String sql =
        "SELECT group_acronym, institute, umbrella_organization, street, zip_code, city, country "
            + "FROM organizations WHERE id = ?";

    String group_acronym = "";
    String institute = "";
    String umbrella_organization = "";
    String street = "";
    String zip_code = "";
    String city = "";
    String country = "";

    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, organizationId);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        group_acronym = rs.getString(1);
        institute = rs.getString(2);
        umbrella_organization = rs.getString(3);
        street = rs.getString(4);
        zip_code = rs.getString(5);
        city = rs.getString(6);
        country = rs.getString(7);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    address[0] = group_acronym;
    address[1] = institute;
    address[2] = umbrella_organization;
    address[3] = street;
    address[4] = zip_code;
    address[5] = city;
    address[6] = country;

    return address;
  }

  /**
   * Gets the unique id of a person, given a name.
   *
   * @param title title.
   * @param firstName first name.
   * @param familyName family name.
   * @return the unique id.
   */
  public int getPersonIdForPersonName(String title, String firstName, String familyName) {

    int personId = -1;
    String sql;

    // if the title is null it has to be treated differently otherwise no match in the database
    if (title.equals("null") | title.equals("NULL")) {
      sql = "SELECT id FROM persons WHERE first_name = ? AND family_name = ?";
    } else {
      sql = "SELECT id FROM persons WHERE title = ? AND first_name = ? AND family_name = ?";
    }
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      if (!(title.equals("null") | title.equals("NULL"))) {
        statement.setString(1, title);
        statement.setString(2, firstName);
        statement.setString(3, familyName);
      } else {
        statement.setString(1, firstName);
        statement.setString(2, familyName);
      }
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        personId = rs.getInt(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return personId;
  }

  public String[] getAddressForPerson(String personFullName) {

    // TODO: deal with multiple first names
    String[] clientNameArray = personFullName.split(" ");
    String title = clientNameArray[0];
    String firstName = clientNameArray[1];
    String familyName = clientNameArray[2];
    int personId = getPersonIdForPersonName(title, firstName, familyName);

    // the person could not be found, so we return the notification message
    if (personId == -1)
      return new String[] {"There is no entry in the persons table for the person " + personFullName
          + ". The address "
          + "fields in the generated .docx file will thus be placeholders. Please consider creating the user before "
          + "creating the offer."};

    int organizationId = getOrganizationIdForPersonId(personId);

    // the organization could not be found, so we return the notification message
    if (organizationId == -1)
      return new String[] {
          "There is no entry in the persons_organizations table for the person with id "
              + Integer.toString(personId)
              + ". The address fields in the generated .docx file will thus be placeholders. "
              + "Please consider linking the user to his organization before creating the offer."};

    return getAddressForOrganizationId(organizationId);
  }

  /**
   * Gets the email for the person registered under the given username.
   *
   * @param username the username.
   * @return The email address.
   */
  public String getUserEmail(String username) {
    String userEmail = "oops! no email address is available in the database.";
    String sql = "SELECT email FROM persons WHERE username = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, username);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        userEmail = rs.getString(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userEmail;
  }

  public String getPackageDescriptionFromPackageId(int package_id) {
    String package_description = "N/A";
    String sql = "SELECT package_description FROM packages WHERE package_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, package_id);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        package_description = rs.getString(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return package_description;
  }

  public int getPackageIDFromPackageName(String package_name) {
    int package_id = 0;
    String sql = "SELECT package_id FROM packages WHERE package_name = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, package_name);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        package_id = rs.getInt(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return package_id;
  }

  public ArrayList<String> getOfferIdsForPackage(int package_id) {
    String sqlCheck = "SELECT offer_id FROM offers_packages WHERE package_id = ?";
    ArrayList<String> offerIds = new ArrayList<>();

    try (Connection connCheck = login();
        PreparedStatement statementCheck =
            connCheck.prepareStatement(sqlCheck, Statement.RETURN_GENERATED_KEYS)) {
      statementCheck.setInt(1, package_id);
      ResultSet resultCheck = statementCheck.executeQuery();
      // System.out.println("Exists: " + statementCheck);
      while (resultCheck.next()) {
        offerIds.add(resultCheck.getString(1));
      }
      // System.out.println("resultCheck: " + count);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return offerIds;
  }

  public void deleteOffer(int offerId) {

    String sql = "DELETE FROM offers_packages WHERE offer_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, offerId);
      int rs = statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }


    sql = "DELETE FROM offers WHERE offer_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, offerId);
      int rs = statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  public void deletePackage(int packageId) {

    String sql = "DELETE FROM packages WHERE package_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, packageId);
      int rs = statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  public void removePackageFromOffer(int packageId, int selectedOfferId) {
    String sql = "DELETE FROM offers_packages WHERE package_id = ? AND offer_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, packageId);
      statement.setInt(2, selectedOfferId);
      int rs = statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  public void updateTotalOfferPrice(String offer_id, BigDecimal offer_total) {
    String sql = "UPDATE offers SET offer_total = ? WHERE offer_id = ? ";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {

      // String updatedPriceFormatted = String.format("%.02f", offer_total);
      // updatedPriceFormatted = updatedPriceFormatted.replaceAll(",", ".");
      statement.setString(1, offer_total.toString());
      statement.setString(2, offer_id);
      int result = statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  private BigDecimal getPackagePrice(String package_id, String packagePriceType) {
    BigDecimal price = BigDecimal.ZERO;

    String sqlS;

    switch (packagePriceType) {
      case "internal":
        sqlS = "SELECT package_price_internal FROM packages WHERE package_id = ? ";
        break;
      case "external_academic":
        sqlS = "SELECT package_price_external_academic FROM packages WHERE package_id = ? ";
        break;
      case "external_commercial":
        sqlS = "SELECT package_price_external_commercial FROM packages WHERE package_id = ? ";
        break;
      default:
        sqlS = "SELECT package_price_internal FROM packages WHERE package_id = ? ";
        break;
    }
    try (Connection conn = login(); PreparedStatement statementS = conn.prepareStatement(sqlS)) {
      statementS.setString(1, package_id);
      ResultSet rs = statementS.executeQuery();
      if (rs.next()) {
        price = rs.getBigDecimal(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return price;
  }

  public void updatePackagePrice(String offer_id, String package_id, String packagePriceType,
      float packageDiscount) {

    BigDecimal updatedPackageAddOnPrice = getPackagePrice(package_id, packagePriceType);

    updatedPackageAddOnPrice = updatedPackageAddOnPrice.multiply(new BigDecimal(packageDiscount));

    // update the package_addon_price
    String sql = "UPDATE offers_packages SET package_addon_price = ?"
        + "WHERE offer_id = ? AND package_id = ? ";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setBigDecimal(1, updatedPackageAddOnPrice);
      statement.setString(2, offer_id);
      statement.setString(3, package_id);

      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public String getPackagePriceType(String offer_id, String package_id) {


    // update the package_addon_price
    String sql =
        "SELECT package_price_type FROM offers_packages WHERE offer_id = ? AND package_id = ? ";

    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, offer_id);
      statement.setString(2, package_id);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        return rs.getString(1);

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return "no valid price type found";
  }

  /**
   *
   * @param package_count
   * @param offer_id
   * @param package_id
   * @param packagePriceType
   * @param packageDiscount Be careful! This is the discount from the discount_per_sample_size.csv
   */
  public void updatePackageQuantityAndRecalculatePrice(String package_count, String offer_id,
      String package_id, String packagePriceType, float packageDiscount) {

    LOG.info("started to update");

    BigDecimal updatedPackageAddOnPrice = getPackagePrice(package_id, packagePriceType);

    updatedPackageAddOnPrice = updatedPackageAddOnPrice.multiply(
        new BigDecimal(Integer.parseInt(package_count)).multiply(new BigDecimal(packageDiscount)));
    LOG.info("calc addonpackage " + updatedPackageAddOnPrice);

    // format the package discount
    DecimalFormat df = new DecimalFormat("#");
    String packageDiscountFormatted = df.format((1 - packageDiscount) * 100) + "%";

    // update the package_count, the package_addon_price and the package_discount in the
    // offers_packages table
    updateOffersPackages(offer_id, package_id, package_count, updatedPackageAddOnPrice,
        packageDiscountFormatted);
    LOG.info("updated addOnPrice");


    // get the sum of all total package prices as offer price
    BigDecimal offerPrice = calculateOfferPrice(offer_id);

    // update the offer price in the offers database
    updateOfferPrice(offer_id, offerPrice);
    LOG.info("updated offerPrice");


    // get the offer discount
    BigDecimal offerDiscount = getOfferDiscount(offer_id);
    LOG.info("get offer discount " + offerDiscount);


    // apply the discount + recalculate the total offer price
    BigDecimal offerTotalPrice = offerPrice
        .multiply((new BigDecimal(100).subtract(offerDiscount)).divide(new BigDecimal(100)));
    LOG.info("calc offer total price " + offerTotalPrice);


    // update the total offer price
    updateTotalOfferPrice(offer_id, offerTotalPrice);
    LOG.info("update offerTotalPrice");
  }

  /**
   *
   * @param offer_id
   * @param package_id
   * @param packagePriceType
   */
  public void updatePriceAndRecalculateTotalPrices(String offer_id, String package_id,
      String packagePriceType) {

    int package_count = getPackageCount(offer_id, package_id);

    BigDecimal packageDiscount =
        new BigDecimal(getPackageDiscount(offer_id, package_id).replace("%", ""));
    packageDiscount = new BigDecimal(1).subtract((packageDiscount.divide(new BigDecimal(100))));

    BigDecimal updatedPackageAddOnPrice = getPackagePrice(package_id, packagePriceType);

    updatedPackageAddOnPrice =
        updatedPackageAddOnPrice.multiply(new BigDecimal(package_count).multiply(packageDiscount));

    // update the package_count, the package_addon_price and the package_discount in the
    // offers_packages table
    updateOffersPackages(offer_id, package_id, updatedPackageAddOnPrice);

    // get the sum of all total package prices as offer price
    BigDecimal offerPrice = calculateOfferPrice(offer_id);

    // update the offer price in the offers database
    updateOfferPrice(offer_id, offerPrice);

    // get the offer discount
    BigDecimal offerDiscount = getOfferDiscount(offer_id);

    // apply the discount + recalculate the total offer price
    BigDecimal offerTotalPrice = offerPrice
        .multiply((new BigDecimal(100).subtract(offerDiscount)).divide(new BigDecimal(100)));

    // update the total offer price
    updateTotalOfferPrice(offer_id, offerTotalPrice);

  }

  /**
   * Calculate the offer price by summing up the prices for all packages in the offer
   * 
   * @param offer_id
   * @return
   */
  private BigDecimal calculateOfferPrice(String offer_id) {
    BigDecimal offerPrice = BigDecimal.ZERO;

    LOG.info("summing up packages for offer price");

    String sqlL = "SELECT SUM(package_addon_price) FROM offers_packages WHERE offer_id = ?";
    try (Connection conn = login(); PreparedStatement statementL = conn.prepareStatement(sqlL)) {

      statementL.setInt(1, Integer.parseInt(offer_id));
      ResultSet rs = statementL.executeQuery();
      if (rs.next())
        offerPrice = rs.getBigDecimal(1);

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return offerPrice;
  }


  public void updateOffersPackages(String offer_id, String package_id, String package_count,
      BigDecimal updatedPackageAddOnPrice, String packageDiscountFormatted) {

    String sql =
        "UPDATE offers_packages SET package_count = ?, package_addon_price = ?, package_discount = ? "
            + "WHERE offer_id = ? AND package_id = ? ";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, package_count);
      statement.setBigDecimal(2, updatedPackageAddOnPrice);
      statement.setString(3, packageDiscountFormatted);
      statement.setInt(4, Integer.parseInt(offer_id));
      statement.setInt(5, Integer.parseInt(package_id));
      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void updateOffersPackages(String offer_id, String package_id,
      BigDecimal updatedPackageAddOnPrice) {

    String sql =
        "UPDATE offers_packages SET package_addon_price = ? WHERE offer_id = ? AND package_id = ? ";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setBigDecimal(1, updatedPackageAddOnPrice);
      statement.setInt(2, Integer.parseInt(offer_id));
      statement.setInt(3, Integer.parseInt(package_id));
      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void updateOfferPrice(String offer_id, BigDecimal offerPrice) {
    String sql = "UPDATE offers SET offer_price = ? WHERE offer_id = ?";

    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      // String offerPriceFormatted = String.format("%.02f", offerPrice);
      // offerPriceFormatted = offerPriceFormatted.replaceAll(",", ".");
      statement.setBigDecimal(1, offerPrice);
      statement.setInt(2, Integer.parseInt(offer_id));
      int done = statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public BigDecimal getOfferDiscount(String offer_id) {
    BigDecimal offerDiscount = BigDecimal.ZERO;

    String sql = "SELECT discount FROM offers WHERE offer_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, Integer.parseInt(offer_id));
      // one represents the first '?' within the PreparedStatement --> insert the variable offer_id
      // here
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        // offerDiscount = rs.getInt(1);
        LOG.info("Get String for discount " + rs.getNString(1));
      // column discount is VARCHAR() --> get String --> cut '%' --> convert to integer
      String discount = rs.getString(1);
      offerDiscount = new BigDecimal(Integer.parseInt(discount.split("%")[0]));

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return offerDiscount;
  }


  public void updatePackageGroupForPackage(String selectedPackageGroup, String packageId) {

    String sql = "UPDATE packages SET package_group = ? WHERE package_id = ? ";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, selectedPackageGroup);
      statement.setInt(2, Integer.parseInt(packageId));
      int result = statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }


  public boolean updateStatus(String offer_status, String offer_id) {
    boolean success = false;
    String sql = "UPDATE offers SET offer_status = ? WHERE offer_id = ? ";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, offer_status);
      statement.setString(2, offer_id);
      int result = statement.executeUpdate();
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return success;
  }

  public ArrayList<Integer> getAllOfferIDsForPackageID(String package_id) {
    ArrayList<Integer> offerIDs = new ArrayList<>();

    String sql = "SELECT offer_id FROM offers_packages WHERE package_id = ?";

    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, Integer.parseInt(package_id));

      ResultSet rs = statement.executeQuery();
      while (rs.next())
        offerIDs.add(rs.getInt(1));

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return offerIDs;
  }

  public int getPackageCount(String offer_id, String package_id) {

    int count = -1;
    String sql = "SELECT package_count FROM offers_packages WHERE offer_id = ? AND package_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, offer_id);
      statement.setString(2, package_id);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        count = rs.getInt(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return count;
  }

  public String getPackageDiscount(String offer_id, String package_id) {
    String count = null;
    String sql =
        "SELECT package_discount FROM offers_packages WHERE offer_id = ? AND package_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, offer_id);
      statement.setString(2, package_id);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        count = rs.getString(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return count;
  }

  public String getOfferStatus(String offer_id) {
    String status = "In Progress";
    String sql = "SELECT offer_status FROM offers WHERE offer_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, offer_id);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        status = rs.getString(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return status;
  }

  public String getPriceFromPackageId(int package_id, String packagePriceType) {
    // we use String for the price, since ResultSet.getFloat returns 0.0 if the value in the
    // database is null
    String package_price = "-1";

    BigDecimal price = getPackagePrice(Integer.toString(package_id), packagePriceType);

    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    // try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
    // statement.setInt(1, package_id);
    // ResultSet rs = statement.executeQuery();
    // if (rs.next())
    // package_price = rs.getString(1);
    // } catch (SQLException e) {
    // e.printStackTrace();
    // }

    if (!price.equals(BigDecimal.ZERO)) {
      package_price = price.toString();
    }

    return package_price;
  }

  //TODO
  public void addNewPackage(packageBean pack, String user) {
    java.sql.Date date = new java.sql.Date(System.currentTimeMillis());

    String sql =
        "INSERT INTO packages (package_name, package_facility, package_decription, package_grp, "
            + "package_price_internal, package_price_external_academic, package_price_external_commercial, "
            + "package_unit_type, packag_date, added_by) VALUES(?,?,?,?,?,?,?,?,?,?)";

    String pack_grp = pack.getpackage_group();
    BigDecimal base_price = new BigDecimal(pack.getpackage_price());
    BigDecimal ext_acad_price = computeExternalPrice(pack_grp, "external_academics", base_price);
    BigDecimal ext_comm_price = computeExternalPrice(pack_grp, "external_commercial", base_price);

    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, pack.getpackage_name());
      statement.setString(2, pack.getpackage_facility());
      statement.setString(3, pack.getpackage_description());
      statement.setString(4, pack_grp);
      statement.setBigDecimal(5, base_price);
      statement.setBigDecimal(6, ext_acad_price);
      statement.setBigDecimal(7, ext_comm_price);
      statement.setString(8, pack.getPackage_unit_type());
      statement.setDate(9, date);
      statement.setString(10, user);
      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  // TODO test
  private BigDecimal computeExternalPrice(String priceType, String packageGroup,
      BigDecimal internalPrice) {
    
//    Group   Internal    Academic    Commercial
//    Mass Spec/Sequencing    1.0 1.1 1.5
//    Bioinformatics Analysis 1.0 1.3 2.0
//    Project Management  1.0 1.5 2.0
    
    Set<String> group1 = new HashSet<>(Arrays.asList("Sequencing", "Mass Spectrometry"));
    Set<String> group2 = new HashSet<>(Arrays.asList("Bioinformatics Analysis"));
    Set<String> group3 = new HashSet<>(Arrays.asList("Project Management"));
    
    if (priceType.equals("external_academics")) {
      if (group1.contains(packageGroup)) {
        return internalPrice.multiply(new BigDecimal(1.1));
      }
      if (group2.contains(packageGroup)) {
        return internalPrice.multiply(new BigDecimal(1.3));
      }
      if (group3.contains(packageGroup)) {
        return internalPrice.multiply(new BigDecimal(1.5));
      }
    }
    if (priceType.equals("external_commercial")) {
      if (group1.contains(packageGroup)) {
        return internalPrice.multiply(new BigDecimal(1.5));
      }
      if (group2.contains(packageGroup)) {
        return internalPrice.multiply(new BigDecimal(2.0));
      }
      if (group3.contains(packageGroup)) {
        return internalPrice.multiply(new BigDecimal(2.0));
      }
    }
    return internalPrice;
  }

  public void addNewPackage(String name) {
    String sql = "INSERT INTO packages (package_name) VALUES(?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, name);
      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  /**
   * Gets the project's short title (short name).
   *
   * @param openbis_project_identifier the identifier of the project.
   * @return the project's short name.
   */
  public String getShortTitleFromProjectRef(String openbis_project_identifier) {
    String short_title = "N/A";
    String sql = "SELECT short_title FROM projects WHERE openbis_project_identifier LIKE ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, "%" + openbis_project_identifier + "%");
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        short_title = rs.getString(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return short_title;
  }

  /**
   * Retrieves the name of the person responsible for the given project.
   *
   * @param openbis_project_identifier the id of the project.
   * @return the name of the person responsible for the given project.
   */
  public String getPIFromProjectRef(final String openbis_project_identifier) {
    String pi_title = "", pi_name = "", pi_surname = "", pi_fullname = "";
    String sql =
        "SELECT DISTINCT persons.title, persons.first_name, persons.family_name FROM projects as projects INNER JOIN "
            + "projects_persons as projects_persons ON projects.`id` = projects_persons.`project_id` INNER JOIN persons ON persons.`id` "
            + "= projects_persons.`person_id` WHERE projects_persons.`project_role` = 'PI' AND "
            + "`projects`.`openbis_project_identifier` LIKE ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, "%" + openbis_project_identifier + "%");
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        pi_title = rs.getString(1);
        pi_name = rs.getString(2);
        pi_surname = rs.getString(3);
      }
      pi_fullname = pi_title + " " + pi_name + " " + pi_surname;
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return pi_fullname;
  }


  public int registerNewOffer(String offer_number, String offer_project_reference,
      String offer_facility, String offer_name, String offer_description, float offer_price,
      Date offer_date, String added_by, boolean internal) {

    Timestamp sql_offer_date = new Timestamp(offer_date.getTime());

    int offer_id = 0;

    String sql =
        "INSERT INTO offers (offer_number, offer_project_reference, offer_facility, offer_name, offer_description,"
            + " offer_price, offer_total, offer_date, added_by, offer_status, discount, internal) "
            + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, offer_number);
      statement.setString(2, offer_project_reference);
      statement.setString(3, offer_facility);
      statement.setString(4, offer_name);
      statement.setString(5, offer_description);
      statement.setFloat(6, offer_price);
      statement.setFloat(7, offer_price);
      statement.setTimestamp(8, sql_offer_date);
      statement.setString(9, added_by);
      statement.setString(10, "In Progress");
      statement.setString(11, "0%");
      statement.setBoolean(12, internal);
      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // query the database for the current offer to get the offer id (there is probably a better
    // way..)
    String sql2 =
        "SELECT offer_id FROM offers WHERE offer_project_reference = ? AND offer_price = ?";
    try (Connection conn = login();
        PreparedStatement statement2 =
            conn.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS)) {
      statement2.setString(1, offer_project_reference);
      statement2.setFloat(2, offer_price);
      ResultSet rs = statement2.executeQuery();
      if (rs.next())
        offer_id = rs.getInt(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // update the offer_number by appending the offer_id to it
    offer_number = offer_number.concat("_").concat(String.valueOf(offer_id));
    String sql3 = "UPDATE offers SET offer_number = ? WHERE offer_id = ?";
    try (Connection conn = login();
        PreparedStatement statement3 =
            conn.prepareStatement(sql3, Statement.RETURN_GENERATED_KEYS)) {
      statement3.setString(1, offer_number);
      statement3.setInt(2, offer_id);
      int rs = statement3.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return offer_id;
  }

  public int getFirstOfferIdForPackageId(int package_id) {
    String sqlCheck = "SELECT offer_id FROM offers_packages WHERE package_id = ?";
    int offerID = -1;
    try (Connection connCheck = login();
        PreparedStatement statementCheck =
            connCheck.prepareStatement(sqlCheck, Statement.RETURN_GENERATED_KEYS)) {
      statementCheck.setInt(1, package_id);
      ResultSet resultCheck = statementCheck.executeQuery();
      if (resultCheck.next()) {
        offerID = resultCheck.getInt(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return offerID;
  }

  public boolean isPackageSelectedForAnyOffer(int package_id) {
    String sqlCheck = "SELECT * FROM offers_packages WHERE package_id = ?";
    try (Connection connCheck = login();
        PreparedStatement statementCheck =
            connCheck.prepareStatement(sqlCheck, Statement.RETURN_GENERATED_KEYS)) {
      statementCheck.setInt(1, package_id);
      ResultSet resultCheck = statementCheck.executeQuery();
      if (resultCheck.next()) {
        return true;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return false;
  }

  public boolean checkForPackageInOffer(int offer_id, int package_id) {

    int count = 0;
    String sqlCheck = "SELECT COUNT(*) FROM offers_packages WHERE offer_id = ? AND package_id = ?";

    try (Connection connCheck = login();
        PreparedStatement statementCheck =
            connCheck.prepareStatement(sqlCheck, Statement.RETURN_GENERATED_KEYS)) {
      statementCheck.setInt(1, offer_id);
      statementCheck.setInt(2, package_id);
      ResultSet resultCheck = statementCheck.executeQuery();
      while (resultCheck.next()) {
        count = resultCheck.getInt(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return count > 0;
  }

  // TODO check if BigDecimal works here
  public boolean insertOrUpdateOffersPackages(int offer_id, int package_id,
      BigDecimal package_unit_price) {
    int count = 0;
    boolean success = false;

    // check if we need to update or insert the package
    String sqlCheck = "SELECT COUNT(*) FROM offers_packages WHERE offer_id = ? AND package_id = ?";
    try (Connection connCheck = login();
        PreparedStatement statementCheck =
            connCheck.prepareStatement(sqlCheck, Statement.RETURN_GENERATED_KEYS)) {
      statementCheck.setInt(1, offer_id);
      statementCheck.setInt(2, package_id);
      ResultSet resultCheck = statementCheck.executeQuery();
      while (resultCheck.next()) {
        count = resultCheck.getInt(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    if (count > 0) {
      String sqlUpdate =
          "UPDATE offers_packages SET package_id = ?, package_addon_price = ? WHERE offer_id = ?";
      try (Connection connUpdate = login();
          PreparedStatement statementUpdate = connUpdate.prepareStatement(sqlUpdate)) {
        statementUpdate.setInt(1, package_id);
        statementUpdate.setBigDecimal(2, package_unit_price);
        statementUpdate.setInt(3, offer_id);
        int result = statementUpdate.executeUpdate();
        // System.out.println("Update: " + statementUpdate);
        success = (result > 0);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } else {
      String sqlInsert =
          "INSERT INTO offers_packages (offer_id, package_id, package_addon_price, package_count, package_discount) "
              + "VALUES (?,?,?,?,?)";
      try (Connection connInsert = login();
          PreparedStatement statementInsert = connInsert.prepareStatement(sqlInsert)) {
        statementInsert.setInt(1, offer_id);
        statementInsert.setInt(2, package_id);
        statementInsert.setBigDecimal(3, package_unit_price);
        statementInsert.setInt(4, 1);
        statementInsert.setString(5, "0%");
        int result = statementInsert.executeUpdate();
        success = (result > 0);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return success;
  }

  /**
   * returns all package ids for the offer which have no package group associated to itself
   *
   * @param offer_id: id of the offer to check
   * @return ArrayList of strings holding the package ids
   */
  public ArrayList<String> getPackageIdsWithoutPackageGroup(String offer_id) {

    ArrayList<String> packageIdsArray = new ArrayList<>();

    // returns the package_id from the packages of the current offer where package_group is null
    String sql = "SELECT packages.`package_id` " + "FROM packages "
        + "INNER JOIN offers_packages ON packages.`package_id` = offers_packages.`package_id` "
        + "WHERE offers_packages.`offer_id` = " + offer_id
        + " AND packages.`package_group` IS NULL";

    try (Connection connCheck = login();
        PreparedStatement statementCheck =
            connCheck.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ResultSet resultCheck = statementCheck.executeQuery();
      while (resultCheck.next()) {
        packageIdsArray.add(resultCheck.getString(1));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return packageIdsArray;
  }

  public void updatePackagePriceTypeForPackage(String offerId, String packageId,
      String packagePriceType) {

    String sqlUpdate =
        "UPDATE offers_packages SET package_price_type = ? WHERE offer_id = ? AND package_id = ?";
    try (Connection connUpdate = login();
        PreparedStatement statementUpdate = connUpdate.prepareStatement(sqlUpdate)) {
      statementUpdate.setString(1, packagePriceType);
      statementUpdate.setString(2, offerId);
      statementUpdate.setString(3, packageId);
      int result = statementUpdate.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  /**
   * visit the tables offers, projects, projects_persons and persons to find the project manager for
   * the given offer if no project manager is defined "no person found" is returned if there are
   * multiple project manager the last project manager is returned
   *
   * @param offer_id
   * @return
   */
  public String getProjectManager(String offer_id) {

    String person = "no person found";
    String sql = "SELECT persons.first_name, persons.family_name, persons.email " + "FROM persons "
        + "INNER JOIN projects_persons " + "ON persons.id = projects_persons.person_id "
        + "INNER JOIN projects " + "ON projects_persons.project_id = projects.id "
        + "INNER JOIN offers "
        + "ON projects.openbis_project_identifier LIKE CONCAT('%',offers.offer_project_reference) "
        + "WHERE offers.offer_id = ? AND projects_persons.project_role = 'Manager'";


    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, offer_id);
      ResultSet rs = statement.executeQuery();

      // only if the result set contains rows alter the person string
      if (rs.isBeforeFirst()) {
        while (rs.next()) {

          person = rs.getString("first_name") + ",";
          person += rs.getString("family_name") + ",";
          person += rs.getString("email");

        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return person;
  }

}
