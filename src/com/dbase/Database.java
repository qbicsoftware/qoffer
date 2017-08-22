package com.dbase;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import com.model.packageBean;

public enum Database {

  Instance;
  private String password;
  private String user;
  private String host;
  Connection conn = null;

  public void init(String user, String password, String host) {
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
      // System.out.println("Database: " + d.toString());
    }
    if (!existsDriver) {
      // Register JDBC driver
      // According http://docs.oracle.com/javase/6/docs/api/java/sql/DriverManager.html
      // this should not be needed anymore. But without it I get the following error:
      // java.sql.SQLException: No suitable driver found for
      // jdbc:mysql://localhost:3306/facs_facility
      // Does not work for serlvets, just for portlets :(
      try {
        Class.forName(mysqlDriverName);
      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    this.setPassword(password);
    this.setUser(user);
    this.setHost(host);
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password the password to set
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * @return the user
   */
  public String getUser() {
    return user;
  }

  /**
   * @param user the user to set
   */
  public void setUser(String user) {
    this.user = user;
  }

  /**
   * @return the host
   */
  public String getHost() {
    return host;
  }

  /**
   * @param host the host to set
   */
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * 
   * Undoes all changes made in the current transaction. Does not undo, if conn IS in auto commit
   * mode
   * 
   * @param conn
   * @param closeConnection
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
      if (conn != null && closeConnection) {
        logout(conn);
      }
      // TODO log everything
      e.printStackTrace();
    }
  }

  /**
   * logs into database with the parameters given in {@link Database.init}
   * 
   * @return Connection, otherwise null if connecting to the database fails
   */
  private Connection login() {
    try {
      return DriverManager.getConnection(host, user, password);

    } catch (SQLException e) {
      // TODO log login failure
      e.printStackTrace();
    }
    return null;
  }

  /**
   * trys to close the given connection and release it
   * 
   * From java documentation: It is strongly recommended that an application explicitly commits or
   * rolls back an active transaction prior to calling the close method. If the close method is
   * called and there is an active transaction, the results are implementation-defined.
   * 
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
    conn = null;
  }

  public List<packageBean> getPackages() {
    String sql = "SELECT * FROM packages";
    List<packageBean> pbean = new ArrayList<packageBean>();
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
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return pbean;
  }


  public ArrayList<String> getPackageGroups() {
    ArrayList<String> list = new ArrayList<String>();
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

  public ArrayList<String> getUsernames() {
    ArrayList<String> list = new ArrayList<String>();
    String sql = "SELECT user_name FROM user";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        list.add(rs.getString("user_name"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    // System.out.println(list);
    return list;
  }

  public ArrayList<String> getProjects() {
    ArrayList<String> list = new ArrayList<String>();
    String sql = "SELECT openbis_project_identifier FROM projects";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        // list.add(rs.getString("openbis_project_identifier"));
        list.add(rs.getString("openbis_project_identifier").replaceAll("/.*?/", ""));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    // System.out.println(list);
    return list;
  }

  public ArrayList<String> getPackageNames() {
    ArrayList<String> list = new ArrayList<String>();
    String sql = "SELECT package_name FROM packages ORDER BY package_name";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        list.add(rs.getString("package_name"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    // System.out.println(list);
    return list;
  }

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
      // System.out.println(rs.next() + " getString " + rs.getString(1));
      // System.out.println("PackageName: " + package_name + " Desc: " + package_description);
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return userEmail;
  }

  public String getPackDescriptionFromPackName(String package_name) {
    String package_description = "N/A";
    String sql = "SELECT package_description FROM packages WHERE package_name = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, package_name);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        package_description = rs.getString(1);
      // System.out.println(rs.next() + " getString " + rs.getString(1));
      // System.out.println("PackageName: " + package_name + " Desc: " + package_description);
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return package_description;
  }

  public int getPackIDFromPackName(String package_name) {
    int package_id = 0;
    String sql = "SELECT package_id FROM packages WHERE package_name = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, package_name);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        package_id = rs.getInt(1);
      // System.out.println(rs.next() + " getString " + rs.getString(1));
      // System.out.println("PackageName: " + package_name + " Desc: " + package_description);
      // nothing will be in the database, until you commit it!
      // conn.commit();
      // System.out.println("getPackIDFromPackName: " + statement);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return package_id;
  }

  public boolean internalOfferCheck(String offer_id) {
    boolean internal = true;
    String sql = "SELECT internal FROM offers WHERE offer_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, offer_id);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        internal = rs.getBoolean(1);
      // System.out.println(rs.next() + " getString " + rs.getString(1));
      // System.out.println("PackageName: " + package_name + " Desc: " + package_description);
      // nothing will be in the database, until you commit it!
      // conn.commit();
      // System.out.println("getPackIDFromPackName: " + statement);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return internal;
  }

  public boolean updateDiscount(String discount, String offer_id, float percentage) {
    boolean success = false;
    float updatedPrice = 0;

    String sqlS = "SELECT offer_price FROM offers WHERE offer_id = ? ";
    try (Connection conn = login(); PreparedStatement statementS = conn.prepareStatement(sqlS)) {
      statementS.setString(1, offer_id);
      ResultSet rs = statementS.executeQuery();
      if (rs.next())
        updatedPrice = rs.getFloat(1);
      updatedPrice = updatedPrice * ((100 - percentage) / 100);
      // System.out.println("percentage: " + percentage + " updatedPrice: " + updatedPrice);
      // System.out.println(rs.next() + " getString " + rs.getString(1));
      // System.out.println("PackageName: " + package_name + " Desc: " + package_description);
      // nothing will be in the database, until you commit it!
      // conn.commit();
      // System.out.println("getOfferDiscount: " + statementS);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    String sql = "UPDATE offers SET discount = ?, offer_total = ? WHERE offer_id = ? ";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {

      String updatedPriceFormatted = String.format("%.02f", updatedPrice);
      updatedPriceFormatted = updatedPriceFormatted.replaceAll(",", ".");
      statement.setString(1, discount);
      statement.setString(2, updatedPriceFormatted);
      statement.setString(3, offer_id);
      int result = statement.executeUpdate();
      success = (result > 0);
      // System.out.println(rs.next() + " getString " + rs.getString(1));
      // System.out.println("PackageName: " + package_name + " Desc: " + package_description);
      // nothing will be in the database, until you commit it!
      // conn.commit();
      // System.out.println("getOfferDiscount: " + statement);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return success;
  }

  public boolean updateQuantityQuery(String package_count, String offer_id, String package_id,
      boolean internal) {

    boolean success = false;
    float updatedPackageAddOnPrice = 0;
    float offerPrice = 0;
    float offerTotalPrice = 0;
    float discountPercentage = 0;

    if (internal) {
      String sqlS = "SELECT package_price FROM packages WHERE package_id = ? ";
      try (Connection conn = login(); PreparedStatement statementS = conn.prepareStatement(sqlS)) {
        statementS.setString(1, package_id);
        ResultSet rs = statementS.executeQuery();
        if (rs.next())
          updatedPackageAddOnPrice = rs.getFloat(1);
        updatedPackageAddOnPrice = updatedPackageAddOnPrice * Integer.parseInt(package_count);

      } catch (SQLException e) {
        e.printStackTrace();
      }
    } else {
      String sqlS = "SELECT package_price_external FROM packages WHERE package_id = ? ";
      try (Connection conn = login(); PreparedStatement statementS = conn.prepareStatement(sqlS)) {
        statementS.setString(1, package_id);
        ResultSet rs = statementS.executeQuery();
        if (rs.next())
          updatedPackageAddOnPrice = rs.getFloat(1);
        updatedPackageAddOnPrice = updatedPackageAddOnPrice * Integer.parseInt(package_count);

      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    String sql =
        "UPDATE offers_packages SET package_count = ?, package_addon_price = ? WHERE offer_id = ? AND package_id = ? ";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, package_count);
      statement.setFloat(2, updatedPackageAddOnPrice);
      statement.setString(3, offer_id);
      statement.setString(4, package_id);
      int result = statement.executeUpdate();
      success = (result > 0);
      // System.out.println(rs.next() + " getString " + rs.getString(1));
      // System.out.println("PackageName: " + package_name + " Desc: " + package_description);
      // nothing will be in the database, until you commit it!
      // conn.commit();
      // System.out.println("getOfferDiscount: " + statement);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    String sqlL = "SELECT SUM(package_addon_price) FROM offers_packages WHERE offer_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statementL = conn.prepareStatement(sqlL)) {
      statementL.setString(1, offer_id);
      ResultSet rs = statementL.executeQuery();
      if (rs.next())
        offerPrice = rs.getFloat(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // System.out.println("offerPrice: " + offerPrice);

    String sqlF = "UPDATE offers SET offer_price = ? WHERE offer_id = ?";
    try (Connection conn = login(); PreparedStatement statementF = conn.prepareStatement(sqlF)) {
      String offerPriceFormatted = String.format("%.02f", offerPrice);
      offerPriceFormatted = offerPriceFormatted.replaceAll(",", ".");
      statementF.setString(1, offerPriceFormatted);
      statementF.setString(2, offer_id);
      int result = statementF.executeUpdate();
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    String sqlD = "SELECT discount FROM offers WHERE offer_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statementD = conn.prepareStatement(sqlD)) {
      statementD.setString(1, offer_id);
      ResultSet rs = statementD.executeQuery();
      if (rs.next())
        discountPercentage = rs.getInt(1);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    offerTotalPrice = offerTotalPrice + offerPrice * ((100 - discountPercentage) / 100);
    // System.out.println(" 1 >>> offerTotalPrice: " + offerTotalPrice + " offerPrice: " +
    // offerPrice
    // + " discountPercentage: " + discountPercentage);

    String sqlT = "UPDATE offers SET offer_total = ? WHERE offer_id = ?";
    try (Connection conn = login(); PreparedStatement statementT = conn.prepareStatement(sqlT)) {
      String offerTotalPriceFormatted = String.format("%.02f", offerTotalPrice);
      offerTotalPriceFormatted = offerTotalPriceFormatted.replaceAll(",", ".");
      statementT.setString(1, offerTotalPriceFormatted);
      statementT.setString(2, offer_id);
      // System.out.println(" 2 >>> offerTotalPriceFormatted: " + offerTotalPriceFormatted
      // + " offer_id: " + offer_id);
      int result = statementT.executeUpdate();
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return success;
  }

  public boolean updateQuantityDiscountQuery(String package_discount, String offer_id,
      String package_id) {
    boolean success = false;
    String sql =
        "UPDATE offers_packages SET package_discount = ? WHERE offer_id = ? AND package_id = ? ";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, package_discount);
      statement.setString(2, offer_id);
      statement.setString(3, package_id);
      int result = statement.executeUpdate();
      success = (result > 0);
      // System.out.println(rs.next() + " getString " + rs.getString(1));
      // System.out.println("PackageName: " + package_name + " Desc: " + package_description);
      // nothing will be in the database, until you commit it!
      // conn.commit();
      // System.out.println("getOfferDiscount: " + statement);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return success;
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
      // System.out.println(rs.next() + " getString " + rs.getString(1));
      // System.out.println("PackageName: " + package_name + " Desc: " + package_description);
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return success;
  }

  public String getOfferDiscount(String offer_id) {
    String discount = "0%";
    String sql = "SELECT discount FROM offers WHERE offer_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, offer_id);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        discount = rs.getString(1);
      // System.out.println(rs.next() + " getString " + rs.getString(1));
      // System.out.println("PackageName: " + package_name + " Desc: " + package_description);
      // nothing will be in the database, until you commit it!
      // conn.commit();
      // System.out.println("getOfferDiscount: " + statement);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return discount;
  }

  public String getPackageCount(String offer_id, String package_id) {

    String count = null;
    String sql = "SELECT package_count FROM offers_packages WHERE offer_id = ? AND package_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, offer_id);
      statement.setString(2, package_id);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        count = rs.getString(1);
      // System.out.println(rs.next() + " getString " + rs.getString(1));
      // System.out.println("PackageName: " + package_name + " Desc: " + package_description);
      // nothing will be in the database, until you commit it!
      // conn.commit();
      // System.out.println("getOfferStatus: " + statement);
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
      // System.out.println(rs.next() + " getString " + rs.getString(1));
      // System.out.println("PackageName: " + package_name + " Desc: " + package_description);
      // nothing will be in the database, until you commit it!
      // conn.commit();
      // System.out.println("getOfferStatus: " + statement);
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
      // System.out.println(rs.next() + " getString " + rs.getString(1));
      // System.out.println("PackageName: " + package_name + " Desc: " + package_description);
      // nothing will be in the database, until you commit it!
      // conn.commit();
      // System.out.println("getOfferStatus: " + statement);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return status;
  }

  public float getPriceInfoFromPackageName(String package_name, boolean externalSelected) {
    float package_price = 0;
    String sql;
    if (externalSelected) {
      sql = "SELECT package_price FROM packages WHERE package_name = ?";
    } else {
      sql = "SELECT package_price_external FROM packages WHERE package_name = ?";
    }
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, package_name);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        package_price = rs.getFloat(1);
      // System.out.println(rs.next() + " getString " + rs.getString(1));
      // System.out.println("PackageName: " + package_name + " Desc: " + package_description);
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return package_price;
  }

  public void addNewPack(String name) {
    String sql = "INSERT INTO packages (package_name) VALUES(?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, name);
      statement.execute();
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

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
      // System.out.println(rs.next() + " getString " + rs.getString(1));
      // System.out.println("PackageName: " + package_name + " Desc: " + package_description);
      // nothing will be in the database, until you commit it!
      // conn.commit();

    } catch (SQLException e) {
      e.printStackTrace();
    }
    // System.out.println("Statement: " + sql + " Here: " + short_title);
    return short_title;
  }

  public String getLongDescFromProjectRef(String openbis_project_identifier) {
    String long_description = "N/A";
    String sql = "SELECT long_description FROM projects WHERE openbis_project_identifier LIKE ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, "%" + openbis_project_identifier + "%");
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        long_description = rs.getString(1);
      // System.out.println(rs.next() + " getString " + rs.getString(1));
      // System.out.println("PackageName: " + package_name + " Desc: " + package_description);
      // nothing will be in the database, until you commit it!
      // conn.commit();

    } catch (SQLException e) {
      e.printStackTrace();
    }
    // System.out.println("Statement: " + sql + " Here: " + long_description);
    return long_description;
  }

  public String getPIFromProjectRef(String openbis_project_identifier) {
    String pi_title = "", pi_name = "", pi_surname = "", pi_fullname = "";
    String sql =
        "SELECT DISTINCT persons.title, persons.first_name, persons.family_name FROM projects INNER JOIN projects_persons ON projects.`id` = projects_persons.`project_id` INNER JOIN persons ON persons.`id` = projects_persons.`person_id` WHERE projects_persons.`project_role` = 'PI' AND `projects`.`openbis_project_identifier` LIKE ?";
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
      // System.out.println(rs.next() + " getString " + rs.getString(1));
      // System.out.println("PackageName: " + package_name + " Desc: " + package_description);
      // nothing will be in the database, until you commit it!
      // conn.commit();

      pi_fullname = pi_title + " " + pi_name + " " + pi_surname;

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return pi_fullname;
  }


  public int registerNewOffer(String offer_number, String offer_project_reference,
      String offer_facility, String offer_name, String offer_description, float offer_price,
      Date offer_date, String added_by, boolean internal) {

    java.sql.Timestamp sql_offer_date = new java.sql.Timestamp(offer_date.getTime());

    int count = 0;
    int offer_id = 0;

    String sqlCheck = "SELECT COUNT(*) FROM offers WHERE offer_project_reference = ?";

    try (Connection conn = login();
        PreparedStatement statementCheck =
            conn.prepareStatement(sqlCheck, Statement.RETURN_GENERATED_KEYS)) {
      statementCheck.setString(1, offer_project_reference);
      ResultSet resultCheck = statementCheck.executeQuery();
      // System.out.println("Exists: " + statementCheck);
      while (resultCheck.next()) {
        count = resultCheck.getInt(1);
      }
      System.out
          .println("ResultCheck " + resultCheck + " SqlCheck " + sqlCheck + " Count " + count);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    if (count == 0) {
      String sql =
          "INSERT INTO offers (offer_number, offer_project_reference, offer_facility, offer_name, offer_description, offer_price, offer_total, offer_date, added_by, offer_status, discount, internal) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
      // String sql =
      // "INSERT INTO offers (offer_number, offer_project_reference, offer_facility, offer_name, offer_description, offer_price, offer_total, offer_date, added_by, offer_status, discount) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

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
        // nothing will be in the database, until you commit it!
        // conn.commit();
      } catch (SQLException e) {
        e.printStackTrace();
      }

      String sql2 = "SELECT offer_id FROM offers WHERE offer_project_reference = ?";
      try (Connection conn = login();
          PreparedStatement statement2 =
              conn.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS)) {

        statement2.setString(1, offer_project_reference);
        ResultSet rs = statement2.executeQuery();
        if (rs.next())
          offer_id = rs.getInt(1);
        // nothing will be in the database, until you commit it!
        // conn.commit();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      // System.out.println("INSERT: " + offer_id);
      return offer_id;


    }

    else {
      // System.out.println("NO INSERT: " + offer_id);
      return offer_id;
    }

  }

  public boolean insertOrUpdateOffersPackages(int offer_id, int package_id, float package_unit_price) {
    int count = 0;
    boolean success = false;

    String sqlCheck = "SELECT COUNT(*) FROM offers_packages WHERE offer_id = ? AND package_id = ?";

    try (Connection connCheck = login();
        PreparedStatement statementCheck =
            connCheck.prepareStatement(sqlCheck, Statement.RETURN_GENERATED_KEYS)) {
      statementCheck.setInt(1, offer_id);
      statementCheck.setInt(2, package_id);
      ResultSet resultCheck = statementCheck.executeQuery();
      // System.out.println("Exists: " + statementCheck);
      while (resultCheck.next()) {
        count = resultCheck.getInt(1);
      }
      // System.out.println("resultCheck: " + count);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    if (count > 0) {
      String sqlUpdate =
          "UPDATE offers_packages SET package_id = ?, package_addon_price = ? WHERE offer_id = ?";
      try (Connection connUpdate = login();
          PreparedStatement statementUpdate = connUpdate.prepareStatement(sqlUpdate)) {
        statementUpdate.setInt(1, package_id);
        statementUpdate.setFloat(2, package_unit_price);
        statementUpdate.setInt(3, offer_id);
        int result = statementUpdate.executeUpdate();
        // System.out.println("Update: " + statementUpdate);
        success = (result > 0);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } else {
      String sqlInsert =
          "INSERT INTO offers_packages (offer_id, package_id, package_addon_price,package_count,package_discount) VALUES (?,?,?,?,?)";
      try (Connection connInsert = login();
          PreparedStatement statementInsert = connInsert.prepareStatement(sqlInsert)) {
        statementInsert.setInt(1, offer_id);
        statementInsert.setInt(2, package_id);
        statementInsert.setFloat(3, package_unit_price);
        statementInsert.setInt(4, 1);
        statementInsert.setString(5, "0%");
        int result = statementInsert.executeUpdate();
        // System.out.println("Insert: " + statementInsert);
        success = (result > 0);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    return success;
  }

}
