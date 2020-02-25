package life.qbic.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.JAXBException;

import life.qbic.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import life.qbic.dbase.Database;
import life.qbic.model.PackageGroup;
import life.qbic.model.packageBean;
import life.qbic.portal.portlet.QBiCPortletUI;
import life.qbic.portal.utils.ConfigurationManager;
import life.qbic.portal.utils.ConfigurationManagerFactory;
import life.qbic.portal.utils.PropertiesBasedConfigurationManager;

public class PackageBatchReader {

  private static final Logger logger = LogManager.getLogger(PackageBatchReader.class);
  private List<String> mandatory;
  private List<String> optional;

  private String error;
  private List<String> tsvByRows;
  private List<packageBean> packages;

  public PackageBatchReader() {
    Set<String> mandatory = new HashSet<String>(Arrays.asList("package_name", "package_facility",
        "package_description", "package_group", "price_internal", "unit_type"));// , "added_by"));
    //
    this.mandatory = new ArrayList<String>(mandatory);
    Set<String> optional = new HashSet<String>(Arrays.asList());
    this.optional = new ArrayList<String>(optional);
  }

  public static void main(String[] args) throws JAXBException {
    String user = "andreas";
    try {
      Properties properties = new Properties();

      PackageBatchReader p = new PackageBatchReader();
      // TODO Remove this, after batch upload is supported in qOffer
      if (p.readPackageFile(new File(Constants.QOFFER_PACKAGES_LIST_PATH))) {

        try (final InputStream inputStream = ConfigurationManagerFactory.class.getClassLoader()
            .getResourceAsStream(QBiCPortletUI.DEVELOPER_PROPERTIES_FILE_PATH)) {
          if (inputStream == null) {
            logger.warn(
                "Your local configuration file was not found in the classpath. This might not be a problem. Perhaps you forgot to add {} in the classpath?",
                QBiCPortletUI.DEVELOPER_PROPERTIES_FILE_PATH);
          } else {
            properties.load(inputStream);
          }
        } catch (IOException e) {
          throw new RuntimeException("Could not load local configuration file "
              + QBiCPortletUI.DEVELOPER_PROPERTIES_FILE_PATH, e);
        }
        final String MSQL_HOST = "mysql.host";
        final String MSQL_DB = "mysql.db";
        final String MSQL_USER = "mysql.user";
        final String MSQL_PORT = "mysql.port";
        final String MSQL_PASS = "mysql.pass";

        String msqlHost = properties.getProperty(MSQL_HOST);
        String msqlDB = properties.getProperty(MSQL_DB);
        String msqlUser = properties.getProperty(MSQL_USER);
        String msqlPort = properties.getProperty(MSQL_PORT);
        String msqlPass = properties.getProperty(MSQL_PASS);

        Database db = new Database(msqlUser, msqlPass, msqlHost, msqlPort, msqlDB);
        for (packageBean pack : p.getPackages()) {
          db.addNewPackage(pack, user);
        }
      } else {
        System.out.println(p.getError());
      }
    } catch (

    IOException e) {
      e.printStackTrace();
    }
  }

  public List<packageBean> getPackages() {
    return packages;
  }

  public static final String UTF8_BOM = "\uFEFF";

  private static String removeUTF8BOM(String s) {
    if (s.startsWith(UTF8_BOM)) {
      s = s.substring(1);
    }
    return s;
  }

  /**
   * Reads in a TSV file containing samples that should be registered. Returns a List of
   * TSVSampleBeans containing all the necessary information to register each sample with its meta
   * information to openBIS, given that the types and parents exist.
   * 
   * @param file
   * @return ArrayList of TSVSampleBeans
   * @throws IOException
   * @throws JAXBException
   */
  public boolean readPackageFile(File file) throws IOException {
    packages = new ArrayList<packageBean>();
    tsvByRows = new ArrayList<String>();
    BufferedReader reader = new BufferedReader(new FileReader(file));
    ArrayList<String[]> data = new ArrayList<String[]>();
    String next;
    int i = 0;
    // isPilot = false;
    while ((next = reader.readLine()) != null) {
      i++;
      next = removeUTF8BOM(next);
      tsvByRows.add(next);
      String[] nextLine = next.split("\t", -1);// this is needed for trailing tabs
      if (data.isEmpty() || nextLine.length == data.get(0).length) {
        data.add(nextLine);
      } else {
        error = "Wrong number of columns in row " + i;
        reader.close();
        return false;
      }
    }
    reader.close();

    if (data.isEmpty()) {
      error = "File is empty.";
      return false;
    }
    String[] header = data.get(0);
    data.remove(0);

    Map<String, Integer> headerMapping = new HashMap<String, Integer>();

    ArrayList<String> found = new ArrayList<String>(Arrays.asList(header));
    for (int j = 0; j < found.size(); j++)
      found.set(j, found.get(j).toLowerCase());

    for (String col : mandatory) {
      if (!found.contains(col)) {
        error = "Mandatory column " + col + " not found.";
        return false;
      }
    }
    for (i = 0; i < header.length; i++) {
      String name = header[i].toLowerCase();
      int position = mandatory.indexOf(name);
      if (position > -1) {
        headerMapping.put(name, i);
      } else {
        position = optional.indexOf(name);
        if (position > -1) {
          headerMapping.put(name, i);
        }
      }

    }
    // create person objects
    int rowID = 0;
    for (String[] row : data) {
      rowID++;
      for (String col : mandatory) {
        if (row[headerMapping.get(col)].isEmpty()) {
          error = col + " is a mandatory field, but it is not set for row " + rowID + "!";
          return false;
        }
      }

      String name = row[headerMapping.get("package_name")];
      String facility = row[headerMapping.get("package_facility")];
      String desc = row[headerMapping.get("package_description")];
      String group = row[headerMapping.get("package_group")];

      if (!PackageGroup.contains(group.replace(" ", "_"))) {
        error = group + " is not a known package group! Known groups are: " + PackageGroup.values();
        return false;
      }

      double price = Double.parseDouble(row[headerMapping.get("price_internal")]);
      String unit = row[headerMapping.get("unit_type")];
      // String added_by = row[headerMapping.get("added_by")];

      // TODO
      packageBean p = new packageBean(name, facility, desc, group, price, unit);
      // new packageBean(title, first, last, mail);
      // if (headerMapping.get("optional") != null)
      // p.setPhone(row[headerMapping.get("phone")]);
      packages.add(p);
    }
    return true;
  }

  public String getError() {
    if (error != null)
      logger.error(error);
    else
      logger.info("Parsing of experimental design successful.");
    return error;
  }

  public List<String> getTSVByRows() {
    return tsvByRows;
  }

}
