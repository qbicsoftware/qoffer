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
import com.vaadin.server.VaadinService;
import life.qbic.portal.utils.ConfigurationManager;
import life.qbic.portal.utils.ConfigurationManagerFactory;
import life.qbic.portal.utils.LiferayConfigurationManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import static life.qbic.portal.utils.PortalUtils.isLiferayPortlet;

public class DBManager {

  private static String hostname;
  private static String port;
  private static String sql_database = "facs_facility";
  private static String username;
  private static String password;

  private static String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
  private static String propertyFilePath = basepath + "/WEB-INF/resourceFiles/Credentials.properties";

  private static ConfigurationManager conf = ConfigurationManagerFactory.getInstance();

  public DBManager() {
  }

  /**
   * sets the credentials either by parsing a local .properties file or by parsing the configuration file on the server
   */
  public static void setCredentials() {
    if (isLiferayPortlet()) {
      username = conf.getMysqlUser();
      password = conf.getMysqlPass();
      hostname = conf.getMsqlHost();
      port = conf.getMysqlPort();
    } else {
      parseCredentials(propertyFilePath);
    }
  }

  /**
   * parses the local .properties file
   * @param propertyFilePath: filepath of the property file
   */
  private static void parseCredentials(String propertyFilePath) {
    Properties prop = new Properties();
    InputStream input = null;

    try {

      input = new FileInputStream(propertyFilePath);

      // load a properties file
      prop.load(input);

      username = prop.getProperty(LiferayConfigurationManager.MSQL_USER);
      password = prop.getProperty(LiferayConfigurationManager.MSQL_PASS);
      hostname = prop.getProperty(LiferayConfigurationManager.MSQL_HOST);
      port = prop.getProperty(LiferayConfigurationManager.MSQL_PORT);

    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static Database getDatabaseInstance() {
    String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + sql_database;
    Database.Instance.init(username, password, jdbcUrl);
    return Database.Instance;
  }

  public static JDBCConnectionPool getDatabaseInstanceAlternative() throws SQLException {
    return new SimpleJDBCConnectionPool("com.mysql.jdbc.Driver",
            "jdbc:mysql://" + hostname + ":" + port + "/" + sql_database, username, password,
            2, 5);
  }
}
